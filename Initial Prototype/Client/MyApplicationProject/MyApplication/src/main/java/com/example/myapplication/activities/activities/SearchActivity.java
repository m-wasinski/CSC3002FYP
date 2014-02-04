package com.example.myapplication.activities.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.activities.base.BaseMapActivity;
import com.example.myapplication.activities.fragments.JourneyDetailsFragment;
import com.example.myapplication.adapters.SearchResultsAdapter;
import com.example.myapplication.constants.IntentConstants;
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.domain_objects.GeoAddress;
import com.example.myapplication.domain_objects.Journey;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.dtos.JourneySearchDTO;
import com.example.myapplication.experimental.GMapV2Direction;
import com.example.myapplication.experimental.GeocoderParams;
import com.example.myapplication.interfaces.GeoCoderFinishedCallBack;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.GeocoderTask;
import com.example.myapplication.network_tasks.WCFServiceTask;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.reflect.TypeToken;

import org.w3c.dom.Document;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Michal on 08/01/14.
 */

public class SearchActivity extends BaseMapActivity implements WCFServiceCallback<ArrayList<Journey>, String>, GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener, GeoCoderFinishedCallBack {

    private DecimalFormat decimalFormat;

    private Button searchResultsButton;
    private Button searchButton;
    private Button departureGPSButton;
    private Button destinationGPSButton;

    private ProgressBar progressBar;

    private RelativeLayout departureRelativeLayout;
    private RelativeLayout destinationRelativeLayout;

    private TextView departureTextView;
    private TextView destinationTextView;

    private LocationClient locationClient;

    private ArrayList<Journey> searchResults;

    private final int METERS_IN_MILE = 1600;
    private int numOfSearchResults;

    @Override
    protected void onStart() {
        super.onStart();
        this.locationClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.locationClient.disconnect();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_search);

        //initialise variables.
        this.actionBar.hide();
        this.numOfSearchResults = 0;
        this.decimalFormat = new DecimalFormat("0.00");
        this.locationClient = new LocationClient(this, this, this);
        this.searchResults = new ArrayList<Journey>();

        //Initialise UI elements.
        this.progressBar = (ProgressBar) this.findViewById(R.id.SearchActivityProgressBar);
        this.departureRelativeLayout = (RelativeLayout) this.findViewById(R.id.SearchActivityDepartureRelativeLayout);
        this.destinationRelativeLayout = (RelativeLayout) this.findViewById(R.id.SearchActivityDestinationRelativeLayout);
        this.departureTextView = (TextView) this.findViewById(R.id.SearchActivityDepartureTextView);
        this.destinationTextView = (TextView) this.findViewById(R.id.SearchActivityDestinationTextView);
        this.searchResultsButton = (Button) this.findViewById(R.id.SearchActivityResultsButton);
        this.departureGPSButton = (Button) this.findViewById(R.id.SearchActivityDepartureGpsButton);
        this.destinationGPSButton = (Button) this.findViewById(R.id.SearchActivityDestinationGpsButton);

        // Connect all event handlers.
        this.setupEventHandlers();

        try {
            // Loading map
            this.initialiseMap();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(googleMap != null)
            centerMapOnMyLocation();
    }

    private void showAddressDialog(final Boolean isDeparture, Marker marker, Circle radius)
    {

        // Show the address dialog.
        final Dialog addressDialog = new Dialog(this);

        addressDialog.setContentView(R.layout.address_dialog);
        addressDialog.setTitle(isDeparture ? "Enter departure point" : "Enter destination point");

        final EditText addressEditText = (EditText) addressDialog.findViewById(R.id.AddressDialogAddressEditText);
        addressEditText.setText(marker == null ? "" : marker.getTitle());

        final EditText perimeterEditText = (EditText) addressDialog.findViewById(R.id.AddressDialogPerimeterEditText);
        perimeterEditText.setText(radius == null ? "0.0" : ""+radius.getRadius()/METERS_IN_MILE);

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
                    addressDialogClosed(isDeparture, addressEditText.getText().toString(),perimeter);
                }

                addressDialog.dismiss();
            }
        });

        addressDialog.show();
    }

    private void addressDialogClosed(Boolean isDeparture, String address, double perimeter)
    {
        new GeocoderTask(this, this, isDeparture, perimeter).execute(new GeocoderParams(address, null));
    }

    private void addressEntered(Boolean isDeparture, MarkerOptions markerOptions, double perimeter)
    {
        if(isDeparture)
        {
            showDeparturePoint(markerOptions, perimeter);
            this.departureTextView.setText(markerOptions.getTitle());
        }
        else
        {
            showDestinationPoint(markerOptions, perimeter);
            this.destinationTextView.setText(markerOptions.getTitle());
        }
    }

    private void getCurrentAddress(Boolean isDeparture, Location location)
    {
        new GeocoderTask(this, this, isDeparture, 0).execute(new GeocoderParams(null, location));
    }

    private void setupEventHandlers()
    {
        this.departureRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddressDialog(true, departureMarker, departureRadius);
            }
        });

        this.destinationRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddressDialog(false, destinationMarker, destinationRadius);
            }
        });

        this.departureGPSButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getCurrentAddress(true, locationClient.getLastLocation());
            }
        });

        this.destinationGPSButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getCurrentAddress(false, locationClient.getLastLocation());
            }
        });

        this.searchButton = (Button) findViewById(R.id.ActivitySearchMapSearchButton);
        this.searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                search();
            }
        });

        this.searchResultsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSearchResultsListView();
            }
        });
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
        JourneySearchDTO journeySearchDTO = new JourneySearchDTO();
        journeySearchDTO.Journey = new Journey();
        journeySearchDTO.Journey.GeoAddresses = new ArrayList<GeoAddress>(Arrays.asList(
                new GeoAddress(departureMarker.getPosition().latitude, departureMarker.getPosition().longitude, departureMarker.getTitle(), 0),
                new GeoAddress(destinationMarker.getPosition().latitude, destinationMarker.getPosition().longitude, destinationMarker.getTitle(), 1)));
        journeySearchDTO.DepartureRadius = this.departureRadius.getRadius() / METERS_IN_MILE;
        journeySearchDTO.DestinationRadius = this.destinationRadius.getRadius() / METERS_IN_MILE;
        // Call the webservice to begin the search.
        new WCFServiceTask<JourneySearchDTO>(this, getResources().getString(R.string.SearchForJourneysURL),
                journeySearchDTO, new TypeToken<ServiceResponse<ArrayList<Journey>>>() {}.getType(), findNDriveManager.getAuthorisationHeaders(), this).execute();
    }

    @Override
    public void onServiceCallCompleted(final ServiceResponse<ArrayList<Journey>> serviceResponse, String parameter) {
        this.progressBar.setVisibility(View.GONE);

        if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
        {
            this.numOfSearchResults = serviceResponse.Result.size();

            this.searchResultsButton.setVisibility(View.VISIBLE);
            this.searchResultsButton.setEnabled(this.numOfSearchResults > 0);
            this.searchResultsButton.setText(this.numOfSearchResults == 0 ? "No journeys found" : "Hide search results ("+this.numOfSearchResults+")");

            if(serviceResponse.Result.size() > 0)
            {
                this.googleMap.clear();
                this.searchResults = serviceResponse.Result;
                this.showSearchResultsListView();

                /*SearchResultsAdapter adapter = new SearchResultsAdapter(this, R.layout.fragment_search_results_listview_row, serviceResponse.Result);
                this.searchResultsListView.setAdapter(adapter);

                DisplayMetrics metrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(metrics);
                RelativeLayout.LayoutParams layout_description = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, metrics.heightPixels -
                                                                                                 this.departureRelativeLayout.getHeight() - this.destinationRelativeLayout.getHeight() - this.searchButton.getHeight());
                resultsRelativeLayout.setLayoutParams(layout_description);

                showSearchResults();
                //hideSearch();

                this.searchResultsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {

                        unloadDetailsFragment();
                        hideSearchResults();

                        journeyDetailsFragment = new JourneyDetailsFragment();

                        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                        fragmentTransaction.add(journeyDetailsRelativeLayout.getId(), journeyDetailsFragment);
                        fragmentTransaction.commit();

                        Bundle bundle = new Bundle();
                        bundle.putString(IntentConstants.JOURNEY, gson.toJson(serviceResponse.Result.get(i)));

                        journeyDetailsFragment.setArguments(bundle);
                        journeyDetailsFragment.setOnCloseListener(new FragmentClosed() {
                            @Override
                            public void onFragmentClosed() {
                                hideSearchResults();
                                showSearch();
                                unloadDetailsFragment();
                            }
                        });
                        journeyDetailsRelativeLayout.setVisibility(View.VISIBLE);
                        drawRouteOnMap(serviceResponse.Result.get(i));
                    }
                });

                LatLngBounds.Builder builder = new LatLngBounds.Builder();

                for(Journey journey : serviceResponse.Result)
                {
                    for(GeoAddress geoAddress : journey.GeoAddresses)
                    {
                        googleMap.addMarker(new MarkerOptions().position(new LatLng(geoAddress.Latitude, geoAddress.Longitude)).title(geoAddress.AddressLine)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                        builder.include(new LatLng(geoAddress.Latitude, geoAddress.Longitude));
                        builder.include(new LatLng(geoAddress.Latitude, geoAddress.Longitude));
                    }
                }

                int padding = 100;
                LatLngBounds bounds = builder.build();
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                googleMap.animateCamera(cameraUpdate);
            }*/
        }
        }
    }

    private void showSearchResultsListView()
    {
        Dialog searchResultsDialog = new Dialog(this, R.style.Theme_CustomDialog);
        searchResultsDialog.setCanceledOnTouchOutside(true);
        searchResultsDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                searchResultsButton.setText("Show search results ("+ numOfSearchResults +")");
            }
        });
        searchResultsDialog.setContentView(R.layout.search_results_dialog);
        ListView resultsListView = (ListView) searchResultsDialog.findViewById(R.id.SearchResultsDialogResultsListView);
        SearchResultsAdapter adapter = new SearchResultsAdapter(this, R.layout.fragment_search_results_listview_row, this.searchResults);
        resultsListView.setAdapter(adapter);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int halfScreen = metrics.heightPixels/2;

        if(this.getTotalHeightOfListView(resultsListView) > halfScreen)
        {
            LinearLayout.LayoutParams layout_description = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, halfScreen);
            resultsListView.setLayoutParams(layout_description);
        }

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
        /*Dialog journeyDetailsDialog = new Dialog(this, R.style.Theme_CustomDialog);

        journeyDetailsDialog.setContentView(R.layout.alert_journey_details);
        journeyDetailsDialog.setCanceledOnTouchOutside(true);
        final MapFragment journeyMapFragment = ((MapFragment)  getFragmentManager().findFragmentById(R.id.AlertJourneyDetailsMap));
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int halfScreen = metrics.heightPixels/2;
        LinearLayout.LayoutParams layout_description = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, halfScreen);

        journeyMapFragment.getView().setLayoutParams(layout_description);
        GoogleMap journeyGoogleMap = journeyMapFragment.getMap();

        journeyDetailsDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (journeyMapFragment != null)
                    getFragmentManager().beginTransaction().remove(journeyMapFragment).commit();
            }
        });

        journeyDetailsDialog.show();
        drawRouteOnMap(journey, journeyGoogleMap);*/
        Bundle bundle = new Bundle();
        bundle.putString(IntentConstants.JOURNEY, gson.toJson(journey));
        Intent intent = new Intent(this, JourneyDetailsActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private int getTotalHeightOfListView(ListView listView) {

        ListAdapter LvAdapter = listView.getAdapter();
        int listviewElementsheight = 0;
        for (int i = 0; i < LvAdapter.getCount(); i++) {
            View mView = LvAdapter.getView(i, null, listView);
            mView.measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            listviewElementsheight += mView.getMeasuredHeight();
        }
        return listviewElementsheight;
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onGeoCoderFinished(MarkerOptions address, Boolean isDeparture, double perimeter)
    {
        if(address != null)
        {
            addressEntered(isDeparture, address, perimeter);
        }
        else
        {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setCancelable(false);
            alertDialog.setTitle("Error.");
            alertDialog.setMessage("Could not retrieve current location. Please enter address manually.");
            alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alertDialog.show();
        }
    }
}
