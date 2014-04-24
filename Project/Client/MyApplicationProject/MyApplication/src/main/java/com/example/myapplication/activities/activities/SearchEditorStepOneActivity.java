package com.example.myapplication.activities.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.activities.base.BaseMapActivity;
import com.example.myapplication.adapters.SearchResultsAdapter;
import com.example.myapplication.constants.IntentConstants;
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.domain_objects.GeoAddress;
import com.example.myapplication.domain_objects.Journey;
import com.example.myapplication.domain_objects.JourneyTemplate;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.enums.MarkerType;
import com.example.myapplication.google_maps_utilities.GeocoderParams;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.GeocoderTask;
import com.example.myapplication.network_tasks.WcfPostServiceTask;
import com.example.myapplication.factories.DialogFactory;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * This activity provides the user with the functionality to search for journeys in the database.
 * Users can specify the start and end locations of their desired journeys as well as various other advanced options such as Date, Time etc.
 * For the advanced options, please refer to the SearchEditorStepTwoActivity class.
 **/
public class SearchEditorStepOneActivity extends BaseMapActivity implements WCFServiceCallback<ArrayList<Journey>, Void>, View.OnClickListener {

    private Button searchButton;

    private ProgressBar progressBar;

    private RelativeLayout destinationRelativeLayout;

    private TextView departureTextView;
    private TextView destinationTextView;

    private JourneyTemplate journeyTemplate;

    private final int METERS_IN_MILE = 1600;

    private int mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_editor_step_one);

        Bundle bundle = getIntent().getExtras();
        mode = bundle.getInt(IntentConstants.SEARCH_MODE);

        journeyTemplate = mode == IntentConstants.SEARCH_MODE_NEW ? journeyTemplate = new JourneyTemplate() :
                (JourneyTemplate) getGson().fromJson(bundle.getString(IntentConstants.JOURNEY_TEMPLATE), new TypeToken<JourneyTemplate>(){}.getType());

        //Initialise UI elements.
        progressBar = (ProgressBar) findViewById(R.id.SearchActivityProgressBar);

        findViewById(R.id.SearchActivityDepartureRelativeLayout).setOnClickListener(this);
        destinationRelativeLayout = (RelativeLayout) findViewById(R.id.SearchActivityDestinationRelativeLayout);
        destinationRelativeLayout.setOnClickListener(this);

        // Departure and destination TextViews.
        departureTextView = (TextView) findViewById(R.id.SearchActivityDepartureTextView);
        destinationTextView = (TextView) findViewById(R.id.SearchActivityDestinationTextView);

        // GPS buttons.
        findViewById(R.id.SearchActivityDepartureGpsButton).setOnClickListener(this);
        findViewById(R.id.SearchActivityDestinationGpsButton).setOnClickListener(this);

        // The search button.
        searchButton = (Button) findViewById(R.id.ActivitySearchMapSearchButton);
        searchButton.setOnClickListener(this);

        try {
            // Loading map
            initialiseMap();
        } catch (Exception e) {
            e.printStackTrace();
        }

        switch(mode)
        {
            case IntentConstants.SEARCH_MODE_NEW:
                setTitle("New search");
                journeyTemplate.setPets(true);
                journeyTemplate.setSmokers(true);
                journeyTemplate.setVehicleType(-1);
                break;
            case IntentConstants.SEARCH_MODE_FROM_TEMPLATE:
                setTitle(journeyTemplate.getAlias());
                break;
            case IntentConstants.CREATING_NEW_TEMPLATE:
                setTitle("New template");
                break;
        }

        if(mode == IntentConstants.SEARCH_MODE_FROM_TEMPLATE || mode == IntentConstants.EDITING_TEMPLATE)
        {
            destinationRelativeLayout.setVisibility(View.VISIBLE);
            startGeocoderTask(MarkerType.Departure, journeyTemplate.getGeoAddresses().get(0).getAddressLine(), journeyTemplate.getDepartureRadius());
            startGeocoderTask(MarkerType.Destination, journeyTemplate.getGeoAddresses().get(1).getAddressLine(), journeyTemplate.getDestinationRadius());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.help:
                DialogFactory.getHelpDialog(this, "Finding journeys", getResources().getString(R.string.FindJourneysHelp));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /***
     * Shows the dialog window used by the user to enter an address.
     *
     * @param markerType - Departure, Destination, Waypoint.
     * @param marker - Marker which will be placed on the map.
     * @param radius - The perimeter in miles which will be represented by a circle drawn on the map.
     */
    private void showAddressDialog(final MarkerType markerType, Marker marker, Circle radius)
    {
        // Show the address dialog.
        final Dialog addressDialog = new Dialog(this);

        addressDialog.setContentView(R.layout.dialog_address_selector);
        addressDialog.setTitle(markerType == MarkerType.Departure ? "Enter departure point" : "Enter destination point");

        final EditText addressEditText = (EditText) addressDialog.findViewById(R.id.AddressDialogAddressEditText);
        addressEditText.setText(marker == null ? "" : marker.getTitle());

        final EditText perimeterEditText = (EditText) addressDialog.findViewById(R.id.AddressDialogPerimeterEditText);
        perimeterEditText.setText(radius == null ? "2.0" : ""+radius.getRadius()/METERS_IN_MILE);

        Button okButton = (Button) addressDialog.findViewById(R.id.AddressDialogOKButton);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String addressText = addressEditText.getText().toString();

                double perimeter = 0;

                try
                {
                    perimeter = Double.parseDouble(perimeterEditText.getText().toString());
                }
                catch(NumberFormatException nfe)
                {
                    nfe.printStackTrace();
                }


                if(!addressText.isEmpty())
                {
                    startGeocoderTask(markerType, addressEditText.getText().toString(), perimeter);
                }

                addressDialog.dismiss();
            }
        });

        addressDialog.show();
    }

    /**
     * Starts a new GeocoderTask to find the address entered by the user and retrieve all the necessary information such as latitude and longitude.
     *
     * @param markerType - Departure, Destination, Waypoint.
     * @param address - The address line ie. Belfast, Eglantine Avenue, BT96EU.
     * @param perimeter - The perimeter in miles which will be represented by a circle drawn on the map.
     **/
    private void startGeocoderTask(MarkerType markerType, String address, double perimeter)
    {
        progressBar.setVisibility(View.VISIBLE);
        new GeocoderTask(this, this, markerType, perimeter).execute(new GeocoderParams(address, null));
    }

    /**
     * Initialises the map.
     **/
    private void initialiseMap()
    {
        if (getGoogleMap() == null)
        {
            setGoogleMap(((MapFragment) getFragmentManager().findFragmentById(R.id.FragmentSearchMap)).getMap());

            if (getGoogleMap() == null)
            {
                Toast.makeText(this, "Unable to initialise Google Maps, please check your network connection.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Calls the web service and tells it to perform the search.
     * The JourneySearchDTO object is passed in to the service and its variables are analysed when performing the search.
     **/
    private void search() {

        // For the search to be performed, we must have both departure and destination points.
        // Search cannot commence if any of them is absent.
        if(getDepartureMarker() == null || getDestinationMarker() == null)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("You must specify departure and destination points to be able to proceed.")
                    .setCancelable(false)
                    .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                    }});

            AlertDialog alert = builder.create();
            alert.show();
            return;
        }

        // Build the journeyTemplate object and send it to the web service.
        journeyTemplate.setGeoAddresses(new ArrayList<GeoAddress>(Arrays.asList(
                new GeoAddress(getDepartureMarker().getPosition().latitude, getDepartureMarker().getPosition().longitude, getDepartureMarker().getTitle(), 0),
                new GeoAddress(getDestinationMarker().getPosition().latitude, getDestinationMarker().getPosition().longitude, getDestinationMarker().getTitle(), 1))));

        journeyTemplate.setDepartureRadius(getDepartureRadius().getRadius() / METERS_IN_MILE);
        journeyTemplate.setDestinationRadius(getDestinationRadius().getRadius() / METERS_IN_MILE);
        journeyTemplate.setUserId(getAppManager().getUser().getUserId());

        Bundle bundle = new Bundle();
        bundle.putInt(IntentConstants.SEARCH_MODE, mode);
        bundle.putString(IntentConstants.JOURNEY_TEMPLATE, getGson().toJson(journeyTemplate));

        startActivity(new Intent(this, SearchEditorStepTwoActivity.class).putExtras(bundle));
    }

    /**
     * Called by the WcfPostServiceTask after its doInBackground method finishes and search results are retrieved from the server.
     *
     * @param serviceResponse - Search result containing list of journeys.
     */
    @Override
    public void onServiceCallCompleted(final ServiceResponse<ArrayList<Journey>> serviceResponse, Void v) {
        progressBar.setVisibility(View.GONE);
        searchButton.setEnabled(true);

        if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
        {
            if(serviceResponse.Result.size() > 0)
            {
                showSearchResultsDialog(serviceResponse.Result);
            }
            else
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("No journeys matching your criteria were found. Would you like to create a new template and be notified when a journey like this becomes available?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                createNewTemplate();
                            }}).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();
            }
        }
    }

    /**
     * Displays the dialog with search results.
     **/
    private void showSearchResultsDialog(final ArrayList<Journey> searchResults)
    {
        Dialog searchResultsDialog = new Dialog(this, R.style.Theme_CustomDialog);
        searchResultsDialog.setCanceledOnTouchOutside(true);
        searchResultsDialog.setContentView(R.layout.dialog_search_results);
        ListView resultsListView = (ListView) searchResultsDialog.findViewById(R.id.SearchResultsDialogResultsListView);
        SearchResultsAdapter adapter = new SearchResultsAdapter(this, R.layout.listview_row_search_result, searchResults);
        resultsListView.setAdapter(adapter);

        resultsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                showJourneyDetails(searchResults.get(i));
            }
        });

        searchResultsDialog.show();
    }

    /**
     * Once user clicks on on the the search results, they are transferred to the SearchResultDetailsActivity.
     * @param journey
     */
    private void showJourneyDetails(Journey journey)
    {
        Bundle bundle = new Bundle();
        bundle.putString(IntentConstants.JOURNEY, getGson().toJson(journey));
        startActivity(new Intent(this, SearchResultDetailsActivity.class).putExtras(bundle));
    }

    /**
     * Called by the GeocoderTask after completing its doInBackground method.
     *
     * @param address  - MarkerOptions containing the required address.
     * @param markerType - Departure, Destination, Waypoint.
     * @param perimeter - Perimeter in miles supplied by the user.
     **/
    @Override
    public void onGeoCoderFinished(MarkerOptions address, MarkerType markerType, Double perimeter)
    {
        progressBar.setVisibility(View.GONE);
        super.onGeoCoderFinished(address, markerType, perimeter);

        if(address != null)
        {
            if(markerType == MarkerType.Departure)
            {
                showDeparturePoint(address, perimeter);
                departureTextView.setText(address.getTitle());
                destinationRelativeLayout.setVisibility(View.VISIBLE);
            }
            else if(markerType == MarkerType.Destination)
            {
                showDestinationPoint(address, perimeter);
                destinationTextView.setText(address.getTitle());
                searchButton.setVisibility(View.VISIBLE);
            }
        }
        else
        {
            // Address could not be retrieved.
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Could not retrieve address.")
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

    private void createNewTemplate()
    {
        progressBar.setVisibility(View.VISIBLE);
        new WcfPostServiceTask<JourneyTemplate>(this, getResources().getString(R.string.CreateNewJourneyTemplateURL),
                journeyTemplate, new TypeToken<ServiceResponse<Void>>() {}.getType(), getAppManager().getAuthorisationHeaders(), new WCFServiceCallback<Void, Void>() {
            @Override
            public void onServiceCallCompleted(ServiceResponse<Void> serviceResponse, Void parameter) {
                progressBar.setVisibility(View.GONE);
                if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
                {
                    Toast.makeText(getApplicationContext(), "New template was created successfully.", Toast.LENGTH_LONG).show();
                }
            }
        }).execute();
    }

    /**
     * Setting up the event handlers for all the UI elements present in this activity.
     *
     * @param view
     */
    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            case R.id.ActivitySearchMapSearchButton:
                search();
                break;
            case R.id.SearchActivityDestinationGpsButton:
                progressBar.setVisibility(View.VISIBLE);
                getCurrentAddress(MarkerType.Destination, getLocationClient().getLastLocation(), 2);
                break;
            case R.id.SearchActivityDepartureGpsButton:
                progressBar.setVisibility(View.VISIBLE);
                getCurrentAddress(MarkerType.Departure, getLocationClient().getLastLocation(), 2);
                break;
            case R.id.SearchActivityDepartureRelativeLayout:
                showAddressDialog(MarkerType.Departure, getDepartureMarker(), getDepartureRadius());
                break;
            case R.id.SearchActivityDestinationRelativeLayout:
                showAddressDialog(MarkerType.Destination, getDestinationMarker(), getDestinationRadius());
                break;
        }
    }
}
