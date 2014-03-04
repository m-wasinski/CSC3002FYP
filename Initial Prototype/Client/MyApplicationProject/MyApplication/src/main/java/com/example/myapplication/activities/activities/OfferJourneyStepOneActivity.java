package com.example.myapplication.activities.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.activities.base.BaseMapActivity;
import com.example.myapplication.constants.IntentConstants;
import com.example.myapplication.domain_objects.GeoAddress;
import com.example.myapplication.domain_objects.Journey;
import com.example.myapplication.enums.MarkerType;
import com.example.myapplication.google_maps_utilities.GeocoderParams;
import com.example.myapplication.interfaces.OnDrivingDirectionsRetrrievedListener;
import com.example.myapplication.network_tasks.GeocoderTask;
import com.example.myapplication.utilities.DateTimeHelper;
import com.example.myapplication.utilities.DialogCreator;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Michal on 22/01/14.
 */
public class OfferJourneyStepOneActivity extends BaseMapActivity implements OnDrivingDirectionsRetrrievedListener {

    private RelativeLayout departureRelativeLayout;
    private RelativeLayout destinationRelativeLayout;
    private RelativeLayout waypointRelativeLayout;

    private Button departureGPSButton;
    private Button destinationGPSButton;
    private Button stepTwoButton;

    private TextView departureTextView;
    private TextView destinationTextView;
    private TextView viaTextView;

    private int mode;

    private Journey journey;

    private ProgressBar progressBar;

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer_journey_step_one);

        Bundle bundle = getIntent().getExtras();
        mode = bundle.getInt(IntentConstants.JOURNEY_CREATOR_MODE);

        journey = mode == IntentConstants.JOURNEY_CREATOR_MODE_EDITING ?
                (Journey)gson.fromJson(bundle.getString(IntentConstants.JOURNEY), new TypeToken<Journey>() {}.getType()) : new Journey();

        // Initialise variables.
        wayPoints = new ArrayList<WaypointHolder>();

        //Initialise UI elements.
        stepTwoButton = (Button) findViewById(R.id.OfferJourneyStepOneActivityStepTwoButton);
        departureGPSButton = (Button) findViewById(R.id.OfferJourneyStepOneActivityDepartureGPSButton);
        destinationGPSButton = (Button) findViewById(R.id.OfferJourneyStepOneActivityDestinationGPSButton);
        departureRelativeLayout = (RelativeLayout) findViewById(R.id.OfferJourneyStepOneActivityDepartureRelativeLayout);
        destinationRelativeLayout = (RelativeLayout) findViewById(R.id.OfferJourneyStepOneActivityDestinationRelativeLayout);
        waypointRelativeLayout = (RelativeLayout) findViewById(R.id.OfferJourneyStepOneActivityViaRelativeLayout);
        departureTextView = (TextView) findViewById(R.id.OfferJourneyStepOneActivityDepartureTextView);
        destinationTextView = (TextView) findViewById(R.id.OfferJourneyStepOneActivityDestinationTextView);
        viaTextView = (TextView) findViewById(R.id.OfferJourneyStepOneActivityViaTextView);
        actionBar.setTitle(mode == IntentConstants.JOURNEY_CREATOR_MODE_EDITING ? "Editing journey, step 1" : "Offering journey, step 1");
        progressBar = (ProgressBar) findViewById(R.id.OfferJourneyStepOneActivityProgressBar);

        // Setting up event handlers.
        setupEventHandlers();

        if(mode == IntentConstants.JOURNEY_CREATOR_MODE_EDITING)
        {
            destinationRelativeLayout.setVisibility(View.VISIBLE);
            stepTwoButton.setVisibility(View.VISIBLE);

            Location startingLocation = new Location("");
            startingLocation.setLatitude(journey.getGeoAddresses().get(0).Latitude);
            startingLocation.setLongitude(journey.getGeoAddresses().get(0).Longitude);

            new GeocoderTask(this, this, MarkerType.Departure, 0).execute(new GeocoderParams(journey.getGeoAddresses().get(0).AddressLine, null));
            new GeocoderTask(this, this, MarkerType.Destination, 0).execute(new GeocoderParams(journey.getGeoAddresses().get(journey.getGeoAddresses().size()-1).AddressLine, null));

            if(journey.getGeoAddresses().size() > 2)
            {
                viaTextView.setText("");
                for(int i = 1; i < journey.getGeoAddresses().size()-1; i++)
                {
                    new GeocoderTask(this, this, MarkerType.Waypoint, i).execute(new GeocoderParams(journey.getGeoAddresses().get(i).AddressLine, null));
                }
            }
        }

        try {
            // Loading map
            initialiseMap();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.help:
                DialogCreator.showHelpDialog(this,
                        mode == IntentConstants.JOURNEY_CREATOR_MODE_EDITING ? "Making changes to your journey" : "Offering new journey",
                        mode == IntentConstants.JOURNEY_CREATOR_MODE_EDITING ? getResources().getString(R.string.EditingJourneyStepOneHelp) :
                                getResources().getString(R.string.OfferingJourneyStepOneHelp));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initialiseMap() {

        if (googleMap == null) {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.OfferJourneyStepOneActivityMap)).getMap();

            if (googleMap == null) {
                Toast.makeText(this,
                        "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    private void showWaypointOnMap(WaypointHolder waypointHolder, MarkerOptions markerOptions)
    {
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
        stepTwoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stepTwoButton.setEnabled(false);
                buildJourney();
            }
        });

        departureRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddressSelectionDialog(MarkerType.Departure, departureMarker);
            }
        });

        destinationRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddressSelectionDialog(MarkerType.Destination, destinationMarker);
            }
        });

        departureGPSButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getCurrentAddress(MarkerType.Departure, locationClient.getLastLocation(), 0);
            }
        });

        destinationGPSButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getCurrentAddress(MarkerType.Destination, locationClient.getLastLocation(), 0);
            }
        });

        waypointRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showWayPointAddressDialog();
            }
        });
    }

    private void showWayPointAddressDialog()
    {
        final Dialog wayPointDialog = new Dialog(this);
        wayPointDialog.setCanceledOnTouchOutside(true);
        wayPointDialog.setContentView(R.layout.dialog_waypoint_selector);
        wayPointDialog.setTitle("Journey waypoints");

        final EditText firstWayPointEditText  = (EditText) wayPointDialog.findViewById(R.id.AlertDialogWaypointSelectorFirstWaypointEditText);
        final EditText secondWayPointEditText  = (EditText) wayPointDialog.findViewById(R.id.AlertDialogWaypointSelectorSecondWaypointEditText);
        final EditText thirdWayPointEditText  = (EditText) wayPointDialog.findViewById(R.id.AlertDialogWaypointSelectorThirdWaypointEditText);
        final EditText fourthWayPointEditText  = (EditText) wayPointDialog.findViewById(R.id.AlertDialogWaypointSelectorFourthWaypointEditText);
        final EditText fifthWayPointEditText  = (EditText) wayPointDialog.findViewById(R.id.AlertDialogWaypointSelectorFifthWaypointEditText);
        final EditText sixthWayPointEditText  = (EditText) wayPointDialog.findViewById(R.id.AlertDialogWaypointSelectorSixthWaypointEditText);

        for(WaypointHolder waypointHolder : wayPoints)
        {
            if(waypointHolder.geoAddress.Order == 1 && waypointHolder.googleMapMarker != null)
            {
                firstWayPointEditText.setText(waypointHolder.googleMapMarker.getTitle());
            }

            if(waypointHolder.geoAddress.Order == 2 && waypointHolder.googleMapMarker != null)
            {
                secondWayPointEditText.setText(waypointHolder.googleMapMarker.getTitle());
            }

            if(waypointHolder.geoAddress.Order == 3 && waypointHolder.googleMapMarker != null)
            {
                thirdWayPointEditText.setText(waypointHolder.googleMapMarker.getTitle());
            }

            if(waypointHolder.geoAddress.Order == 4 && waypointHolder.googleMapMarker != null)
            {
                fourthWayPointEditText.setText(waypointHolder.googleMapMarker.getTitle());
            }

            if(waypointHolder.geoAddress.Order == 5 && waypointHolder.googleMapMarker != null)
            {
                fifthWayPointEditText.setText(waypointHolder.googleMapMarker.getTitle());
            }

            if(waypointHolder.geoAddress.Order == 6 && waypointHolder.googleMapMarker != null)
            {
                sixthWayPointEditText.setText(waypointHolder.googleMapMarker.getTitle());
            }
        }


        Button okButton = (Button) wayPointDialog.findViewById(R.id.AlertDialogWaypointSelectorOkButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                for(WaypointHolder waypointHolder : wayPoints)
                {
                    waypointHolder.removeMarker();
                }

                wayPoints.clear();
                viaTextView.setText("Add new waypoint (optional)");
                int wayPointCounter = 1;
                if(!firstWayPointEditText.getText().toString().isEmpty())
                {
                    addressDialogClosed(MarkerType.Waypoint, firstWayPointEditText.getText().toString(), wayPointCounter);
                    viaTextView.setText("");
                    wayPointCounter += 1;
                }

                if(!secondWayPointEditText.getText().toString().isEmpty())
                {
                    addressDialogClosed(MarkerType.Waypoint, secondWayPointEditText.getText().toString(), wayPointCounter);
                    viaTextView.setText("");
                    wayPointCounter += 1;
                }

                if(!thirdWayPointEditText.getText().toString().isEmpty())
                {
                    addressDialogClosed(MarkerType.Waypoint, thirdWayPointEditText.getText().toString(), wayPointCounter);
                    viaTextView.setText("");
                    wayPointCounter += 1;
                }

                if(!fourthWayPointEditText.getText().toString().isEmpty())
                {
                    addressDialogClosed(MarkerType.Waypoint, fourthWayPointEditText.getText().toString(), wayPointCounter);
                    viaTextView.setText("");
                    wayPointCounter += 1;
                }

                if(!fifthWayPointEditText.getText().toString().isEmpty())
                {
                    addressDialogClosed(MarkerType.Waypoint, fifthWayPointEditText.getText().toString(), wayPointCounter);
                    viaTextView.setText("");
                    wayPointCounter += 1;
                }

                if(!sixthWayPointEditText.getText().toString().isEmpty())
                {
                    addressDialogClosed(MarkerType.Waypoint, sixthWayPointEditText.getText().toString(), wayPointCounter);
                    viaTextView.setText("");
                }
                wayPointDialog.dismiss();
                drawDrivingDirectionsOnMap();
            }
        });

        wayPointDialog.show();
    }

    private void showAddressSelectionDialog(final MarkerType markerType, Marker marker)
    {
        final Dialog addressSelectorDialog = new Dialog(this);
        addressSelectorDialog.setCanceledOnTouchOutside(true);
        addressSelectorDialog.setContentView(R.layout.dialog_offer_journey_address_selector);
        addressSelectorDialog.setTitle(markerType == MarkerType.Departure ? "Enter departure address" : "Enter destination address");
        final  EditText addressEditText = (EditText) addressSelectorDialog.findViewById(R.id.AlertDialogOfferJourneyAddressSelectorAddressEditText);
        addressEditText.setText(marker == null ? "" : marker.getTitle());
        Button okButton = (Button) addressSelectorDialog.findViewById(R.id.AlertDialogOfferJourneyAddressSelectorOkButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String addressText = addressEditText.getText().toString();
                if(addressText != null && !addressText.isEmpty())
                {
                    addressDialogClosed(markerType, addressText, 0);
                    addressSelectorDialog.dismiss();

                }
            }
        });

        addressSelectorDialog.show();
    }

    private void addressDialogClosed(MarkerType markerType, String address, double order)
    {
        new GeocoderTask(this, this, markerType, order).execute(new GeocoderParams(address, null));
    }

    private void buildJourney()
    {
        if(departureMarker == null || destinationMarker == null)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("You must specify departure and destination points.")
                    .setCancelable(false)
                    .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });

            stepTwoButton.setEnabled(true);
            AlertDialog alert = builder.create();
            alert.show();
            return;
        }

        journey.setGeoAddresses(new ArrayList<GeoAddress>());
        journey.getGeoAddresses().add(new GeoAddress(departureMarker.getPosition().latitude, departureMarker.getPosition().longitude, departureMarker.getTitle(), 0));

        for(WaypointHolder waypointHolder : wayPoints)
        {
            if(waypointHolder.googleMapMarker != null)
            {
                journey.getGeoAddresses().add(waypointHolder.geoAddress);
            }
        }

        journey.getGeoAddresses().add(new GeoAddress(destinationMarker.getPosition().latitude, destinationMarker.getPosition().longitude, destinationMarker.getTitle(), wayPoints.size()+1));
        Log.i("This journey has the following geoaddresses", "\n");
        for(GeoAddress geoAddress : journey.getGeoAddresses())
        {
            Log.i("GeoAddress " + geoAddress.Order, " "+geoAddress.Latitude + " " + geoAddress.Longitude + " " + geoAddress.AddressLine);
        }

        journey.setAvailableSeats(mode == IntentConstants.JOURNEY_CREATOR_MODE_EDITING ? journey.getAvailableSeats() : 1);
        journey.setPetsAllowed(mode == IntentConstants.JOURNEY_CREATOR_MODE_EDITING && journey.isPetsAllowed());
        journey.setSmokersAllowed(mode == IntentConstants.JOURNEY_CREATOR_MODE_EDITING && journey.isSmokersAllowed());
        journey.setPrivate(mode == IntentConstants.JOURNEY_CREATOR_MODE_EDITING && journey.isPrivate());
        journey.setVehicleType(mode == IntentConstants.JOURNEY_CREATOR_MODE_EDITING ? journey.getVehicleType() : -1);
        journey.setFee(mode == IntentConstants.JOURNEY_CREATOR_MODE_EDITING ? journey.getFee() : -1);
        journey.setPreferredPaymentMethod(mode == IntentConstants.JOURNEY_CREATOR_MODE_EDITING ? journey.getPreferredPaymentMethod() : null);
        journey.setDescription(mode == IntentConstants.JOURNEY_CREATOR_MODE_EDITING ? journey.getDescription() : null);
        journey.setDateAndTimeOfDeparture(mode == IntentConstants.JOURNEY_CREATOR_MODE_EDITING ?
                journey.getDateAndTimeOfDeparture() : DateTimeHelper.convertToWCFDate(Calendar.getInstance().getTime()));
        journey.setDriver(appManager.getUser());

        proceedToStepTwo();
    }

    private void proceedToStepTwo()
    {
        googleMap.snapshot(new GoogleMap.SnapshotReadyCallback() {
            @Override
            public void onSnapshotReady(Bitmap bitmap) {
                int h = getApplicationContext().getResources().getDisplayMetrics().heightPixels / 3;
                int w = (int) (h * bitmap.getWidth() / ((double) bitmap.getHeight()));

                bitmap = Bitmap.createScaledBitmap(bitmap, w, h, true);
                ByteArrayOutputStream bs = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 50, bs);
                Bundle bundle = new Bundle();
                bundle.putString(IntentConstants.JOURNEY, gson.toJson(journey));
                bundle.putInt(IntentConstants.JOURNEY_CREATOR_MODE, mode);
                Intent intent = new Intent(getApplicationContext(), OfferJourneyStepTwoActivity.class);
                intent.putExtras(bundle);
                intent.putExtra(IntentConstants.MINIMAP, bs.toByteArray());
                startActivity(intent);
            }
        });
    }

    @Override
    public void onGeoCoderFinished(MarkerOptions address, MarkerType markerType, Double perimeter)
    {
        super.onGeoCoderFinished(address, markerType, perimeter);

        if(address != null)
        {
            if(markerType == MarkerType.Departure)
            {
                showDeparturePoint(address, perimeter);
                departureTextView.setText(address.getTitle());
            }
            else if(markerType == MarkerType.Destination)
            {
                showDestinationPoint(address, perimeter);
                destinationTextView.setText(address.getTitle());
            }else
            {
                WaypointHolder waypointHolder = new WaypointHolder();
                waypointHolder.geoAddress = new GeoAddress(address.getPosition().latitude, address.getPosition().longitude, address.getTitle(), perimeter.intValue());
                wayPoints.add(waypointHolder);
                showWaypointOnMap(waypointHolder, address);
                viaTextView.setText(viaTextView.getText().toString() + address.getTitle() + ", ");
            }

            destinationRelativeLayout.setVisibility(departureMarker != null ? View.VISIBLE : View.GONE);
            stepTwoButton.setVisibility(departureMarker != null && destinationMarker != null ? View.VISIBLE : View.GONE);

            drawDrivingDirectionsOnMap();
        }
        else
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Could not retrieve current location. Please enter it manually.")
                    .setCancelable(false)
                    .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });

            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    private void drawDrivingDirectionsOnMap()
    {
        if(departureMarker != null && destinationMarker != null)
        {
            ArrayList<GeoAddress> geoAddresses = new ArrayList<GeoAddress>();
            geoAddresses.add(new GeoAddress(departureMarker.getPosition().latitude, departureMarker.getPosition().longitude, departureMarker.getTitle(), 0));
            geoAddresses.add(new GeoAddress(destinationMarker.getPosition().latitude, destinationMarker.getPosition().longitude, destinationMarker.getTitle(), 1));
            if(wayPoints.size() > 0)
            {
                for(WaypointHolder waypointHolder : wayPoints)
                {
                    if(waypointHolder.geoAddress != null)
                    {
                        geoAddresses.add(waypointHolder.geoAddress);
                    }
                }
            }
            progressBar.setVisibility(View.VISIBLE);
            drawDrivingDirectionsOnMap(googleMap, geoAddresses, this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        stepTwoButton.setEnabled(true);
    }

    @Override
    public void onDrivingDirectionsRetrieved() {
        progressBar.setVisibility(View.GONE);
    }
}
