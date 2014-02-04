package com.example.myapplication.activities.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.activities.base.BaseMapActivity;
import com.example.myapplication.constants.IntentConstants;
import com.example.myapplication.domain_objects.GeoAddress;
import com.example.myapplication.domain_objects.Journey;
import com.example.myapplication.experimental.GMapV2Direction;
import com.example.myapplication.experimental.WaypointHolder;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.w3c.dom.Document;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

/**
 * Created by Michal on 22/01/14.
 */
public class OfferJourneyStepOneActivity extends BaseMapActivity {

    private LinearLayout throughPointsLayout;
    private LinearLayout contentLayout;
    private TextView viaTextView;
    private Button minimizeButton;
    private Button stepTwoButton;
    private Button addThroughPointButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer_journey_step_one);
        actionBar.hide();
        this.waypointHolders = new ArrayList<WaypointHolder>();

        //Initialise UI elements.
        this.stepTwoButton = (Button) findViewById(R.id.OfferJourneyStepOneStepTwoButton);
        this.contentLayout = (LinearLayout) findViewById(R.id.OfferJourneyStepOneContentLayout);
        this.minimizeButton = (Button) findViewById(R.id.OfferJourneyStepOneActivitySearchPaneHeaderButton);
        this.throughPointsLayout = (LinearLayout) findViewById(R.id.AddNewJourneyAdditionalDestinationLayout);
        this.viaTextView = (TextView) findViewById(R.id.ActivityOfferJourneyViaEditText);
        this.addThroughPointButton = (Button) findViewById(R.id.ActivityOfferJourneyThroughPointButton);
        this.departureAddressEditText = (EditText) findViewById(R.id.ActivityOfferJourneyDepartureEditText);
        this.destinationAddressEditText = (EditText) findViewById(R.id.ActivityOfferJourneyDestinationEditText);

        // Setting up event handlers.
        this.setupEventHandlers();

        try {
            // Loading map
            initialiseMap();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(googleMap != null)
            centerMapOnMyLocation();
    }

    private void initialiseMap() {

        if (googleMap == null) {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.OfferNewJourneyActivityStepOneMapFragment)).getMap();

            if (googleMap == null) {
                Toast.makeText(this,
                        "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    private void showWaypointOnMap(WaypointHolder waypointHolder, MarkerOptions markerOptions)
    {
        waypointHolder.removeMarker();

        if(markerOptions != null)
        {
            waypointHolder.googleMapMarker = googleMap.addMarker(new MarkerOptions()
                    .position(markerOptions.getPosition())
                    .title(markerOptions.getTitle())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

            waypointHolder.googleMapMarker.showInfoWindow();
        }
        animateCamera();
    }

    private void setupEventHandlers()
    {
        this.stepTwoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buildJourney();
            }
        });

        this.addThroughPointButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(waypointHolders.size() == 6)
                {
                    return;
                }

                final WaypointHolder waypointHolder = new WaypointHolder(throughPointsLayout, getApplicationContext(), waypointHolders);
                waypointHolder.initialise();
                viaTextView.setVisibility(View.VISIBLE);
                waypointHolder.addressEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View view, boolean b) {
                        if (!b) {
                            //MarkerOptions markerOptions = getAddress(waypointHolder.addressEditText.getText().toString());
                            //showWaypointOnMap(waypointHolder, markerOptions);
                        }
                    }
                });

                waypointHolder.addressEditText.setOnKeyListener(new View.OnKeyListener() {
                    @Override
                    public boolean onKey(View view, int i, KeyEvent keyEvent) {
                        /*if(i == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_UP){MarkerOptions markerOptions = getAddress(waypointHolder.addressEditText.getText().toString());
                            showWaypointOnMap(waypointHolder, markerOptions);
                            inputMethodManager.hideSoftInputFromWindow(waypointHolder.addressEditText.getWindowToken(), 0);
                        }*/
                        return false;
                    }
                });

                waypointHolder.closeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        waypointHolder.removeItself();
                        if(waypointHolders.size() == 0)
                        {
                            viaTextView.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });


        this.departureAddressEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if(i == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_UP){
                    //MarkerOptions markerOptions = getAddress(departureAddressEditText.getText().toString());
                    //showDeparturePoint(markerOptions, 0);
                    inputMethodManager.hideSoftInputFromWindow(departureAddressEditText.getWindowToken(), 0);
                }
                return false;
            }
        });

        this.departureAddressEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    //MarkerOptions markerOptions = getAddress(departureAddressEditText.getText().toString());
                    //showDeparturePoint(markerOptions, 0);
                }
            }
        });



        this.destinationAddressEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    //MarkerOptions markerOptions = getAddress(destinationAddressEditText.getText().toString());
                    //showDestinationPoint(markerOptions, 0);
                }
            }
        });

        this.destinationAddressEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if(i == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_UP){
                    //MarkerOptions markerOptions = getAddress(destinationAddressEditText.getText().toString());
                    //showDestinationPoint(markerOptions, 0);
                    inputMethodManager.hideSoftInputFromWindow(destinationAddressEditText.getWindowToken(), 0);
                }
                return false;
            }
        });

        this.minimizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                contentLayout.setVisibility(contentLayout.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                minimizeButton.setText(contentLayout.getVisibility() == View.VISIBLE ? "Minimize" : "Restore");
                Drawable image = getResources().getDrawable(contentLayout.getVisibility() == View.VISIBLE ? R.drawable.down : R.drawable.up);
                minimizeButton.setCompoundDrawablesWithIntrinsicBounds(null, null, image, null);
            }
        });
    }

    private void buildJourney()
    {
        if(this.departureMarker == null || this.destinationMarker == null)
        {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setCancelable(false);
            alertDialog.setMessage("You must specify departure and destination points.");
            alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alertDialog.show();
            return;
        }

        final Journey journey = new Journey();
        journey.GeoAddresses = new ArrayList<GeoAddress>();
        journey.GeoAddresses.add(new GeoAddress(departureMarker.getPosition().latitude, departureMarker.getPosition().longitude, departureMarker.getTitle(), 0));

        int counter = 1;
        for(WaypointHolder waypointHolder : this.waypointHolders)
        {
            if(waypointHolder.googleMapMarker != null)
            {
                journey.GeoAddresses.add(new GeoAddress(waypointHolder.googleMapMarker.getPosition().latitude,
                        waypointHolder.googleMapMarker.getPosition().longitude,
                        waypointHolder.googleMapMarker.getTitle(), counter));

                counter += 1;
            }
        }

        journey.GeoAddresses.add(new GeoAddress(destinationMarker.getPosition().latitude, destinationMarker.getPosition().longitude, destinationMarker.getTitle(), counter));
        Log.i("This journey has the following geoaddresses", "\n");
        for(GeoAddress geoAddress : journey.GeoAddresses)
        {
            Log.i("GeoAddress " + geoAddress.Order, " "+geoAddress.Latitude + " " + geoAddress.Longitude + " " + geoAddress.AddressLine);
        }

        new AsyncTask<GoogleMap, Journey, Void>(){

            private GMapV2Direction gMapV2Direction;
            private Document doc;
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);


                if(doc != null)
                {
                    ArrayList<LatLng> directionPoint = gMapV2Direction.getDirection(doc);

                    PolylineOptions polylineOptions = new PolylineOptions().width(10).color(Color.BLUE);
                    for(int i = 0 ; i < directionPoint.size() ; i++) {
                        polylineOptions.add(directionPoint.get(i));
                    }
                    Polyline polyline = googleMap.addPolyline(polylineOptions);

                }

                alrightImDone(journey);
            }

            @Override
            protected Void doInBackground(GoogleMap... googleMaps) {
                gMapV2Direction = new GMapV2Direction();
                doc = gMapV2Direction.getDocument(journey.GeoAddresses, GMapV2Direction.MODE_DRIVING);
                return null;
            }
        }.execute(googleMap);


    }

    private void alrightImDone(final Journey journey)
    {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setCancelable(false);
        alertDialog.setMessage("Grand?");
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                googleMap.snapshot(new GoogleMap.SnapshotReadyCallback() {
                    @Override
                    public void onSnapshotReady(Bitmap bitmap) {
                        float densityMultiplier = getApplicationContext().getResources().getDisplayMetrics().density;
                        int h = (int) getApplicationContext().getResources().getDisplayMetrics().heightPixels / 3;
                        int w = (int) (h * bitmap.getWidth() / ((double) bitmap.getHeight()));

                        bitmap = Bitmap.createScaledBitmap(bitmap, w, h, true);
                        ByteArrayOutputStream bs = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 50, bs);
                        Bundle bundle = new Bundle();
                        bundle.putString(IntentConstants.JOURNEY, gson.toJson(journey));
                        Intent intent = new Intent(getApplicationContext(), OfferJourneyStepTwoActivity.class);
                        intent.putExtras(bundle);
                        intent.putExtra(IntentConstants.MINIMAP, bs.toByteArray());
                        startActivity(intent);
                    }
                });
                dialog.dismiss();
            }
        });
        alertDialog.show();


    }
}
