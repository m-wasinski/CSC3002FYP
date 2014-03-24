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
import com.example.myapplication.factories.DialogFactory;
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
 * First step in the journey creation/editing process consists of specifying start and end locations for the journey as well as any optional waypoints in between.
 * This activity aims at providing all the necessary functionality for the user to specify the above locations with the help of Google Map and Geocoder.
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

    private final String TAG = "JourneyEditorStepOne";

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

        // If the current mode is set to editing, we must draw all of the locations stored in the journey object on the map.
        if(mode == IntentConstants.JOURNEY_CREATOR_MODE_EDITING)
        {
            destinationRelativeLayout.setVisibility(View.VISIBLE);
            stepTwoButton.setVisibility(View.VISIBLE);

            Location startingLocation = new Location("");
            startingLocation.setLatitude(journey.getGeoAddresses().get(0).getLatitude());
            startingLocation.setLongitude(journey.getGeoAddresses().get(0).getLongitude());

            // Draw the start and destination points on the map.
            new GeocoderTask(this, this, MarkerType.Departure, 0).execute(new GeocoderParams(journey.getGeoAddresses().get(0).getAddressLine(), null));
            new GeocoderTask(this, this, MarkerType.Destination, 0).execute(new GeocoderParams(journey.getGeoAddresses().get(journey.getGeoAddresses().size()-1).getAddressLine(), null));

            // Draw any optional waypoints on the map.
            if(journey.getGeoAddresses().size() > 2)
            {
                viaTextView.setText("");
                for(int i = 1; i < journey.getGeoAddresses().size()-1; i++)
                {
                    new GeocoderTask(this, this, MarkerType.Waypoint, i).execute(new GeocoderParams(journey.getGeoAddresses().get(i).getAddressLine(), null));
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
                DialogFactory.getHelpDialog(this,
                        mode == IntentConstants.JOURNEY_CREATOR_MODE_EDITING ? "Making changes to your journey" : "Offering new journey",
                        mode == IntentConstants.JOURNEY_CREATOR_MODE_EDITING ? getResources().getString(R.string.EditingJourneyStepOneHelp) :
                                getResources().getString(R.string.OfferingJourneyStepOneHelp));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Initialises Google Map present in this activity.
     */
    private void initialiseMap() {

        if (googleMap == null) {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.OfferJourneyStepOneActivityMap)).getMap();

            if (googleMap == null) {
                Toast.makeText(this, "Sorry! unable to create maps", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Responsible for placing new waypoint on the map.
     *
     * @param waypointHolder - Acts as a holder for the new address and the marker to be placed on the map.
     * @param markerOptions - Contains the address to be placed on the map.
     */
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

    /***
     * Sets up the event handlers for all UI elements present in this activity.
     */
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
                progressBar.setVisibility(View.VISIBLE);
                getCurrentAddress(MarkerType.Departure, locationClient.getLastLocation(), 0);
            }
        });

        destinationGPSButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
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

    /***
     * Shows a dialog which allows the user to enter up to 6 different waypoints for their new journey.
     */
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
            if(waypointHolder.geoAddress.getOrder() == 1 && waypointHolder.googleMapMarker != null)
            {
                firstWayPointEditText.setText(waypointHolder.googleMapMarker.getTitle());
            }

            if(waypointHolder.geoAddress.getOrder() == 2 && waypointHolder.googleMapMarker != null)
            {
                secondWayPointEditText.setText(waypointHolder.googleMapMarker.getTitle());
            }

            if(waypointHolder.geoAddress.getOrder() == 3 && waypointHolder.googleMapMarker != null)
            {
                thirdWayPointEditText.setText(waypointHolder.googleMapMarker.getTitle());
            }

            if(waypointHolder.geoAddress.getOrder() == 4 && waypointHolder.googleMapMarker != null)
            {
                fourthWayPointEditText.setText(waypointHolder.googleMapMarker.getTitle());
            }

            if(waypointHolder.geoAddress.getOrder() == 5 && waypointHolder.googleMapMarker != null)
            {
                fifthWayPointEditText.setText(waypointHolder.googleMapMarker.getTitle());
            }

            if(waypointHolder.geoAddress.getOrder() == 6 && waypointHolder.googleMapMarker != null)
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

    /**
     * Called after user has entered an address into the address dialog.
     * This starts a new Geocoder task to retrieve latitude and longitude points for this address.
     *
     * @param markerType - Departure/Destination/Waypoint?
     * @param address - String entered by the user.
     * @param order - used to specify the order of the address to be retrieved. For example, Departure = 0, Waypoint #1 = 1, Waypoint #2 = 2, Destination  = 3
     */
    private void addressDialogClosed(MarkerType markerType, String address, double order)
    {
        new GeocoderTask(this, this, markerType, order).execute(new GeocoderParams(address, null));
    }

    /**
     * Called after used clicks on the next button/
     * This function is responsible for building the journey object,
     * taking a snapshot of the map and loading the step two activity of the journey editor.
     */
    private void buildJourney()
    {
        // Check if the required minimum number of points on the map have been specified.
        if(departureMarker == null || destinationMarker == null)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("You must specify departure and destination points before proceeding.")
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

        // Add all GeoAddresses specified by the user. Start at the departure address.
        journey.setGeoAddresses(new ArrayList<GeoAddress>());
        journey.getGeoAddresses().add(new GeoAddress(departureMarker.getPosition().latitude, departureMarker.getPosition().longitude, departureMarker.getTitle(), 0));

        // Go through any optional waypoints.
        for(WaypointHolder waypointHolder : wayPoints)
        {
            if(waypointHolder.googleMapMarker != null)
            {
                journey.getGeoAddresses().add(waypointHolder.geoAddress);
            }
        }

        // And finally, add the destination address.
        journey.getGeoAddresses().add(new GeoAddress(destinationMarker.getPosition().latitude, destinationMarker.getPosition().longitude, destinationMarker.getTitle(), wayPoints.size()+1));
        Log.i(TAG, "This journey has the following geoaddresses \n");
        for(GeoAddress geoAddress : journey.getGeoAddresses())
        {
            Log.i("GeoAddress " + geoAddress.getOrder(), " "+geoAddress.getLatitude() + " " + geoAddress.getLongitude() + " " + geoAddress.getAddressLine());
        }

        // If the current mode is set to editing, we must preserve all the other properties that were specified in this journey.
        // Else if the current mode is set to creating, we initialise the new journey object with default values.
        journey.setAvailableSeats(mode == IntentConstants.JOURNEY_CREATOR_MODE_EDITING ? journey.getAvailableSeats() : 1);
        journey.setPetsAllowed(mode == IntentConstants.JOURNEY_CREATOR_MODE_EDITING && journey.arePetsAllowed());
        journey.setSmokersAllowed(mode == IntentConstants.JOURNEY_CREATOR_MODE_EDITING && journey.areSmokersAllowed());
        journey.setPrivate(mode == IntentConstants.JOURNEY_CREATOR_MODE_EDITING && journey.isPrivate());
        journey.setVehicleType(mode == IntentConstants.JOURNEY_CREATOR_MODE_EDITING ? journey.getVehicleType() : -1);
        journey.setFee(mode == IntentConstants.JOURNEY_CREATOR_MODE_EDITING ? journey.getFee() : -1);
        journey.setPreferredPaymentMethod(mode == IntentConstants.JOURNEY_CREATOR_MODE_EDITING ? journey.getPreferredPaymentMethod() : null);
        journey.setDescription(mode == IntentConstants.JOURNEY_CREATOR_MODE_EDITING ? journey.getDescription() : null);
        journey.setDateAndTimeOfDeparture(mode == IntentConstants.JOURNEY_CREATOR_MODE_EDITING ?
                journey.getDateAndTimeOfDeparture() : DateTimeHelper.convertToWCFDate(Calendar.getInstance().getTime()));
        journey.setDriver(appManager.getUser());

        // Take a snapshot of the current map view.
        googleMap.snapshot(new GoogleMap.SnapshotReadyCallback() {
            @Override
            public void onSnapshotReady(Bitmap bitmap) {
                int h = OfferJourneyStepOneActivity.this.getResources().getDisplayMetrics().heightPixels / 3;
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

    /**
     * Called after Geocoder completes the address translation process and returns an address.
     *
     * @param address - MarkerOptions to be placed on the Google Map containing latitude and longitude values of our address as well as String address line.
     * @param markerType - Indicates the marker type which has been processed. Departure, Waypoint, Destination.
     * @param radius - In case of search, represents the radius in miles which users want to take into consideration when searching.
     */
    @Override
    public void onGeoCoderFinished(MarkerOptions address, MarkerType markerType, Double radius)
    {
        super.onGeoCoderFinished(address, markerType, radius);
        progressBar.setVisibility(View.GONE);
        // Address found, check the type of location the user was looking for.
        if(address != null)
        {
            if(markerType == MarkerType.Departure)
            {
                showDeparturePoint(address, radius);
                departureTextView.setText(address.getTitle());
            }
            else if(markerType == MarkerType.Destination)
            {
                showDestinationPoint(address, radius);
                destinationTextView.setText(address.getTitle());
            }else
            {
                WaypointHolder waypointHolder = new WaypointHolder();
                waypointHolder.geoAddress = new GeoAddress(address.getPosition().latitude, address.getPosition().longitude, address.getTitle(), radius.intValue());
                wayPoints.add(waypointHolder);
                showWaypointOnMap(waypointHolder, address);
                viaTextView.setText(viaTextView.getText().toString() + address.getTitle() + ", ");
            }

            destinationRelativeLayout.setVisibility(departureMarker != null ? View.VISIBLE : View.GONE);
            stepTwoButton.setVisibility(departureMarker != null && destinationMarker != null ? View.VISIBLE : View.GONE);

            drawDrivingDirectionsOnMap();
        }
        else // Address was not found
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Could not retrieve location.")
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

    /**
     * If the required points have been plotted on the map, it's time to draw the driving route by calling Google Web API.
     * Directions are automatically drawn on the map the is passed in to the Async Task.
     */
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

    /**
     * Called after driving directions have been successfully drawn on the map.
     */
    @Override
    public void onDrivingDirectionsRetrieved() {
        progressBar.setVisibility(View.GONE);
    }
}
