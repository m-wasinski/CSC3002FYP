package com.example.myapplication.activities.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
import com.example.myapplication.enums.MarkerType;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.dtos.JourneySearchDTO;
import com.example.myapplication.google_maps_utilities.GeocoderParams;
import com.example.myapplication.interfaces.OptionsDialogDismissListener;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.GeocoderTask;
import com.example.myapplication.network_tasks.WcfPostServiceTask;
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
public class SearchActivity extends BaseMapActivity implements WCFServiceCallback<ArrayList<Journey>, String>, OptionsDialogDismissListener, SearchMoreOptionsDialogFragment.sizeChangeListener {

    private Button searchResultsButton;
    private Button searchButton;
    private Button departureGPSButton;
    private Button destinationGPSButton;
    private Button moreOptionsButton;

    private ProgressBar progressBar;

    private RelativeLayout departureRelativeLayout;
    private RelativeLayout destinationRelativeLayout;

    private TextView departureTextView;
    private TextView destinationTextView;

    private ArrayList<Journey> searchResults;

    private JourneySearchDTO journeySearchDTO;

    private final int METERS_IN_MILE = 1600;
    private int numOfSearchResults;

    private SearchMoreOptionsDialogFragment searchMoreOptionsDialogFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        //initialise variables.
        numOfSearchResults = 0;
        searchResults = new ArrayList<Journey>();
        journeySearchDTO = new JourneySearchDTO();

        //Initialise UI elements.
        progressBar = (ProgressBar) findViewById(R.id.SearchActivityProgressBar);
        departureRelativeLayout = (RelativeLayout) findViewById(R.id.SearchActivityDepartureRelativeLayout);
        destinationRelativeLayout = (RelativeLayout) findViewById(R.id.SearchActivityDestinationRelativeLayout);
        departureTextView = (TextView) findViewById(R.id.SearchActivityDepartureTextView);
        destinationTextView = (TextView) findViewById(R.id.SearchActivityDestinationTextView);
        searchResultsButton = (Button) findViewById(R.id.SearchActivityResultsButton);
        departureGPSButton = (Button) findViewById(R.id.SearchActivityDepartureGpsButton);
        destinationGPSButton = (Button) findViewById(R.id.SearchActivityDestinationGpsButton);
        moreOptionsButton = (Button) findViewById(R.id.ActivitySearchMapMoreOptionsButton);
        // Connect all event handlers.
        setupEventHandlers();

        try {
            // Loading map
            initialiseMap();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
                } catch(NumberFormatException nfe)
                {
                    nfe.printStackTrace();
                }


                if(addressText != null && !addressText.isEmpty())
                {
                    addressDialogClosed(markerType, addressEditText.getText().toString(),perimeter);
                }

                addressDialog.dismiss();
            }
        });

        addressDialog.show();
    }

    private void addressDialogClosed(MarkerType markerType, String address, double perimeter)
    {
        new GeocoderTask(this, this, markerType, perimeter).execute(new GeocoderParams(address, null));
    }

    private void addressEntered(MarkerType markerType, MarkerOptions markerOptions, double perimeter)
    {
        if(markerType == MarkerType.Departure)
        {
            showDeparturePoint(markerOptions, perimeter);
            departureTextView.setText(markerOptions.getTitle());
        }
        else if(markerType == MarkerType.Destination)
        {
            showDestinationPoint(markerOptions, perimeter);
            destinationTextView.setText(markerOptions.getTitle());
        }
    }

    private void setupEventHandlers()
    {
        departureRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddressDialog(MarkerType.Departure, departureMarker, departureRadius);
            }
        });

        destinationRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddressDialog(MarkerType.Destination, destinationMarker, destinationRadius);
            }
        });

        departureGPSButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getCurrentAddress(MarkerType.Departure, locationClient.getLastLocation(), 2);
            }
        });

        destinationGPSButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getCurrentAddress(MarkerType.Destination, locationClient.getLastLocation(), 2);
            }
        });

        searchButton = (Button) findViewById(R.id.ActivitySearchMapSearchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search();
            }
        });

        searchResultsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSearchResultsDialog();
            }
        });

        moreOptionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showExtraOptions();
            }
        });
    }

    private void showExtraOptions()
    {
        searchMoreOptionsDialogFragment = new SearchMoreOptionsDialogFragment(SearchActivity.this, journeySearchDTO, this, this);
        searchMoreOptionsDialogFragment.show(getFragmentManager(), "");
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {

        if(searchMoreOptionsDialogFragment != null)
        {
            updateSizeOfOptionsDialog();
        }

        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    protected void onPause() {

        super.onPause();
    }

    private void initialiseMap() {

        if (googleMap == null) {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.FragmentSearchMap)).getMap();

            if (googleMap == null) {
                Toast.makeText(this,
                        "Unable to initialise Google Maps, please check your network connection.", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        initialiseMap();
    }

    private void search() {

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

        journeySearchDTO.setGeoAddresses(new ArrayList<GeoAddress>(Arrays.asList(
                new GeoAddress(departureMarker.getPosition().latitude, departureMarker.getPosition().longitude, departureMarker.getTitle(), 0),
                new GeoAddress(destinationMarker.getPosition().latitude, destinationMarker.getPosition().longitude, destinationMarker.getTitle(), 1))));

        journeySearchDTO.setDepartureRadius(departureRadius.getRadius() / METERS_IN_MILE);
        journeySearchDTO.setDestinationRadius(destinationRadius.getRadius() / METERS_IN_MILE);

        // All good, call the webservice to begin the search.
        new WcfPostServiceTask<JourneySearchDTO>(this, getResources().getString(R.string.SearchForJourneysURL),
                journeySearchDTO, new TypeToken<ServiceResponse<ArrayList<Journey>>>() {}.getType(), appManager.getAuthorisationHeaders(), this).execute();
    }

    @Override
    public void onServiceCallCompleted(final ServiceResponse<ArrayList<Journey>> serviceResponse, String parameter) {
        progressBar.setVisibility(View.GONE);

        if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
        {
            numOfSearchResults = serviceResponse.Result.size();

            searchResultsButton.setVisibility(View.VISIBLE);
            searchResultsButton.setEnabled(numOfSearchResults > 0);
            searchResultsButton.setText(numOfSearchResults == 0 ? "No journeys found" : "Hide search results ("+numOfSearchResults+")");

            if(serviceResponse.Result.size() > 0)
            {
                searchResults = serviceResponse.Result;
                showSearchResultsDialog();
            }
        }
    }

    private void showSearchResultsDialog()
    {
        Dialog searchResultsDialog = new Dialog(this, R.style.Theme_CustomDialog);
        searchResultsDialog.setCanceledOnTouchOutside(true);
        searchResultsDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                searchResultsButton.setText("Show search results ("+ numOfSearchResults +")");
            }
        });
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

    private void showJourneyDetails(Journey journey)
    {
        Bundle bundle = new Bundle();
        bundle.putString(IntentConstants.JOURNEY, gson.toJson(journey));
        startActivity(new Intent(this, SearchResultsJourneyDetailsActivity.class).putExtras(bundle));
    }

    @Override
    public void onGeoCoderFinished(MarkerOptions address, MarkerType markerType, double perimeter)
    {
        super.onGeoCoderFinished(address, markerType, perimeter);
        if(address != null)
        {
            addressEntered(markerType, address, perimeter);
        }
        else
        {
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

    @Override
    public void OnOptionsDialogDismiss(JourneySearchDTO journeySearchDTO) {
        this.journeySearchDTO = journeySearchDTO;

        if(searchMoreOptionsDialogFragment != null)
        {
            searchMoreOptionsDialogFragment.dismiss();
            searchMoreOptionsDialogFragment = null;
        }

    }

    @Override
    public void sizeChanged() {
        if(searchMoreOptionsDialogFragment != null)
        {
            updateSizeOfOptionsDialog();
        }
    }

    private int convertDipToPixels(float dips)
    {
        return (int) (dips * getResources().getDisplayMetrics().density + 0.5f);
    }


    private void updateSizeOfOptionsDialog()
    {
        if(searchMoreOptionsDialogFragment != null)
        {
            int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(ViewGroup.LayoutParams.MATCH_PARENT, View.MeasureSpec.UNSPECIFIED);
            int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(ViewGroup.LayoutParams.WRAP_CONTENT, View.MeasureSpec.UNSPECIFIED);
            TableLayout tableLayout = (TableLayout) searchMoreOptionsDialogFragment.getDialog().getWindow(). findViewById(R.id.SearchMoreOptionsFragmentDialogParentLayout);
            tableLayout.measure(widthMeasureSpec, heightMeasureSpec);
            searchMoreOptionsDialogFragment.getDialog().getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, (tableLayout.getMeasuredHeight()+(8*convertDipToPixels(2))));
        }
    }
}
