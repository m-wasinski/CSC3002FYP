package com.example.myapplication.activities.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.activities.base.BaseMapActivity;
import com.example.myapplication.adapters.SearchResultsAdapter;
import com.example.myapplication.constants.IntentConstants;
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.domain_objects.GeoAddress;
import com.example.myapplication.domain_objects.Journey;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.dtos.JourneySearchDTO;
import com.example.myapplication.enums.MarkerType;
import com.example.myapplication.google_maps_utilities.GeocoderParams;
import com.example.myapplication.interfaces.OptionsDialogDismissListener;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.GeocoderTask;
import com.example.myapplication.network_tasks.WcfPostServiceTask;
import com.example.myapplication.utilities.DialogCreator;
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
 * For the advanced options, please refer to the SearchMoreOptionsActivity class.
 **/
public class SearchActivity extends BaseMapActivity implements WCFServiceCallback<ArrayList<Journey>, Void>,
        OptionsDialogDismissListener, SearchMoreOptionsDialogFragment.sizeChangeListener, View.OnClickListener {

    private Button searchButton;

    private ProgressBar progressBar;

    private RelativeLayout destinationRelativeLayout;

    private TextView departureTextView;
    private TextView destinationTextView;

    private JourneySearchDTO journeySearchDTO;

    private final int METERS_IN_MILE = 1600;

    private SearchMoreOptionsDialogFragment searchMoreOptionsDialogFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        //initialise variables.
        journeySearchDTO = new JourneySearchDTO();

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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.advanced_search:
                searchMoreOptionsDialogFragment = new SearchMoreOptionsDialogFragment(SearchActivity.this, journeySearchDTO, this, this);
                searchMoreOptionsDialogFragment.show(getFragmentManager(), "");
                break;
            case R.id.help:
                DialogCreator.showHelpDialog(this, "Finding journeys", getResources().getString(R.string.FindJourneysHelp));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
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

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {

        if(searchMoreOptionsDialogFragment != null)
        {
            updateSizeOfOptionsDialog();
        }

        super.onWindowFocusChanged(hasFocus);
    }

    /**
     * Initialises the map.
     **/
    private void initialiseMap()
    {
        if (googleMap == null)
        {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.FragmentSearchMap)).getMap();

            if (googleMap == null)
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
        if(departureMarker == null || destinationMarker == null)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("You must specify departure and destination points.")
                    .setCancelable(false)
                    .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                    }});

            AlertDialog alert = builder.create();
            alert.show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        searchButton.setEnabled(false);

        // Build the journeySearchDTO object and send it to the web service.
        journeySearchDTO.setGeoAddresses(new ArrayList<GeoAddress>(Arrays.asList(
                new GeoAddress(departureMarker.getPosition().latitude, departureMarker.getPosition().longitude, departureMarker.getTitle(), 0),
                new GeoAddress(destinationMarker.getPosition().latitude, destinationMarker.getPosition().longitude, destinationMarker.getTitle(), 1))));

        journeySearchDTO.setDepartureRadius(departureRadius.getRadius() / METERS_IN_MILE);
        journeySearchDTO.setDestinationRadius(destinationRadius.getRadius() / METERS_IN_MILE);
        journeySearchDTO.setUserId(appManager.getUser().getUserId());
        // All good, call the webservice to begin the search.
        new WcfPostServiceTask<JourneySearchDTO>(this, getResources().getString(R.string.SearchForJourneysURL),
                journeySearchDTO, new TypeToken<ServiceResponse<ArrayList<Journey>>>() {}.getType(), appManager.getAuthorisationHeaders(), this).execute();
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
                Toast.makeText(this, "No journeys found!", Toast.LENGTH_SHORT).show();
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
     * Once user clicks on on the the search results, they are transferred to the SearchResultsJourneyDetailsActivity.
     * @param journey
     */
    private void showJourneyDetails(Journey journey)
    {
        Bundle bundle = new Bundle();
        bundle.putString(IntentConstants.JOURNEY, gson.toJson(journey));
        startActivity(new Intent(this, SearchResultsJourneyDetailsActivity.class).putExtras(bundle));
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
    /**
     * Responsible for saving the journeySearchDTO object passed in
     * from the Advanced search window and dismissing the dialog.
     *
     * @param journeySearchDTO - the current journeySearchDTO used to search for journeys.
     **/
    @Override
    public void OnOptionsDialogDismiss(JourneySearchDTO journeySearchDTO) {
        this.journeySearchDTO = journeySearchDTO;

        if(searchMoreOptionsDialogFragment != null)
        {
            searchMoreOptionsDialogFragment.dismiss();
            searchMoreOptionsDialogFragment = null;
        }

    }

    /**
     * Callback used to indicate that size of the advanced search dialog fragment has changed
     * and that it must be re-measured.
     **/
    @Override
    public void sizeChanged() {
        if(searchMoreOptionsDialogFragment != null)
        {
            updateSizeOfOptionsDialog();
        }
    }

    /**
     * Simple formula to convert dip units (display-independent-pixels) to physical pixels.
     * This is used in calculating the size of the advanced search window and is part of the workaround described below.
     *
     * @param dips - number of dips to be converted to physical pixels.
     **/
    private int convertDipToPixels(float dips)
    {
        return (int) (dips * getResources().getDisplayMetrics().density + 0.5f);
    }

    /**
     * It's a know problem in Android that and instance of a Dialog Fragment window, which in this case is the advanced search window,
     * do not resize properly to wrap the content and ignore the layout height attribute specified in the xml layout file.
     * As a workaround to this issue to make sure the window resizes itself properly, I have created a callback which
     * manually measures and recalculates the size of the window and applies the new values as using the .setLayout() method.
     **/
    private void updateSizeOfOptionsDialog()
    {
        if(searchMoreOptionsDialogFragment != null)
        {
            // To get the correct height, we must first measure the height of the window.
            int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(ViewGroup.LayoutParams.MATCH_PARENT, View.MeasureSpec.UNSPECIFIED);
            int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(ViewGroup.LayoutParams.WRAP_CONTENT, View.MeasureSpec.UNSPECIFIED);

            TableLayout tableLayout = (TableLayout) searchMoreOptionsDialogFragment.getDialog().getWindow(). findViewById(R.id.SearchMoreOptionsFragmentDialogParentLayout);
            tableLayout.measure(widthMeasureSpec, heightMeasureSpec);

            // Apply the new measrued dimensions.
            searchMoreOptionsDialogFragment.getDialog().getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, (tableLayout.getMeasuredHeight()+(8*convertDipToPixels(2))));
        }
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
                getCurrentAddress(MarkerType.Destination, locationClient.getLastLocation(), 2);
                break;
            case R.id.SearchActivityDepartureGpsButton:
                progressBar.setVisibility(View.VISIBLE);
                getCurrentAddress(MarkerType.Departure, locationClient.getLastLocation(), 2);
                break;
            case R.id.SearchActivityDepartureRelativeLayout:
                showAddressDialog(MarkerType.Departure, departureMarker, departureRadius);
                break;
            case R.id.SearchActivityDestinationRelativeLayout:
                showAddressDialog(MarkerType.Destination, destinationMarker, destinationRadius);
                break;
        }
    }
}
