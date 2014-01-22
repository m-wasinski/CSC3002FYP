package com.example.myapplication.activities.activities;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.activities.base.BaseMapActivity;
import com.example.myapplication.activities.fragments.JourneyDetailsFragment;
import com.example.myapplication.adapters.SearchResultsAdapter;
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.dtos.Journey;
import com.example.myapplication.dtos.GeoAddress;
import com.example.myapplication.dtos.ServiceResponse;
import com.example.myapplication.experimental.GMapV2Direction;
import com.example.myapplication.interfaces.FragmentClosed;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WCFServiceTask;
import com.example.myapplication.R;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.reflect.TypeToken;

import org.w3c.dom.Document;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by Michal on 08/01/14.
 */

public class SearchActivity extends BaseMapActivity implements WCFServiceCallback<ArrayList<Journey>, String>{

    private SeekBar departureRadiusAddressSeekBar;
    private SeekBar destinationRadiusAddressSeekBar;
    private double departureRadiusValue;
    private double destinationRadiusValue;
    private DecimalFormat decimalFormat;
    private TextView toggleSearchResultsTextView;
    private TextView departureRadiusTextView;
    private TextView destinationRadiusTextView;
    private LinearLayout searchPaneHeaderLayout;
    private LinearLayout searchPaneOptions;
    private RelativeLayout resultsRelativeLayout;
    private RelativeLayout journeyDetailsRelativeLayout;
    private RelativeLayout parentSearchRelativeLayout;
    private Button searchButton;
    private ListView searchResultsListView;
    private JourneyDetailsFragment journeyDetailsFragment;
    private int numOfSearchResults;
    private ImageView arrowImageView;
    private ProgressBar progressBar;
    private final int METERS_IN_MILE = 1600;
    private final int PROGRESS_BAR_UNITS = 160;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity);
        actionBar.hide();
        progressBar = (ProgressBar) findViewById(R.id.SearchActivityProgressBar);
        numOfSearchResults = 0;
        journeyDetailsRelativeLayout = (RelativeLayout) findViewById(R.id.ActivitySearchMapJourneyDetailsRelativeLayout);
        parentSearchRelativeLayout = (RelativeLayout) findViewById(R.id.ActivitySearchMapParentSearchRelativeLayout);
        searchResultsListView = (ListView) findViewById(R.id.ActivitySearchMapResultsListView);
        resultsRelativeLayout = (RelativeLayout) findViewById(R.id.ActivitySearchMapResultsRelativeLayout);

        arrowImageView = (ImageView) findViewById(R.id.ActivitySearchMapArrowImageView);

        searchPaneHeaderLayout = (LinearLayout) findViewById(R.id.ActivitySearchMapSearchHeaderLayout);
        searchPaneHeaderLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                toggleSearchPaneVisibility();
                return false;
            }
        });
        toggleSearchResultsTextView = (TextView) findViewById(R.id.ActivitySearchMapToggleResultsTextView);
        toggleSearchResultsTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                toggleSearchResultsVisibility();
                return false;
            }
        });

        searchButton = (Button) findViewById(R.id.ActivitySearchMapSearchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                search();
            }
        });

        decimalFormat = new DecimalFormat("0.00");
        searchPaneOptions = (LinearLayout)findViewById(R.id.ActivitySearchPaneOptionsLinearLayout);

        departureRadiusTextView = (TextView) findViewById(R.id.ActivitySearchMapDepartureRadiusTextView);

        destinationRadiusTextView = (TextView) findViewById(R.id.ActivitySearchMapDestinationRadiusTextView);

        departureRadiusAddressSeekBar = (SeekBar) findViewById(R.id.ActivitySearchMapDepartureRadiusSeekBar);
        departureRadiusAddressSeekBar.setEnabled(false);
        departureRadiusAddressSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(departureMarker != null && departureRadius != null)
                {
                    departureRadiusValue = departureRadiusAddressSeekBar.getProgress()*PROGRESS_BAR_UNITS;
                    departureRadius.setRadius(departureRadiusValue);
                    departureRadiusTextView.setText("R: "+decimalFormat.format(departureRadiusValue/METERS_IN_MILE)+" miles");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {


            }
        });
        destinationRadiusAddressSeekBar = (SeekBar) findViewById(R.id.ActivitySearchMapDestinationRadiusSeekBar);
        destinationRadiusAddressSeekBar.setEnabled(false);
        destinationRadiusAddressSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(destinationMarker != null && destinationRadius != null)
                {
                    destinationRadiusValue = destinationRadiusAddressSeekBar.getProgress()*PROGRESS_BAR_UNITS;
                    destinationRadius.setRadius(destinationRadiusValue);
                    destinationRadiusTextView.setText("R: " + decimalFormat.format(destinationRadiusValue / METERS_IN_MILE) + " miles");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {


            }
        });


        destinationAddressEditText = (EditText) findViewById(R.id.MapActivityDestinationAddressTextView);
        destinationAddressEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    findNewLocation(destinationAddressEditText.getText().toString(), ModifiedMarker.Destination, destinationAddressEditText.getText().toString().isEmpty());
                    destinationRadiusAddressSeekBar.setEnabled(destinationMarker != null);
                }
            }
        });

        destinationAddressEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if(i == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_UP){
                    findNewLocation(destinationAddressEditText.getText().toString(), ModifiedMarker.Destination, destinationAddressEditText.getText().toString().isEmpty());
                    inputMethodManager.hideSoftInputFromWindow(destinationAddressEditText.getWindowToken(), 0);
                    destinationRadiusAddressSeekBar.setEnabled(destinationMarker != null);
                }
                return false;
            }
        });

        departureAddressEditText = (EditText) findViewById(R.id.MapActivityDepartureAddressTextView);
        departureAddressEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if(i == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_UP){
                    findNewLocation(departureAddressEditText.getText().toString(), ModifiedMarker.Departure, departureAddressEditText.getText().toString().isEmpty());
                    inputMethodManager.hideSoftInputFromWindow(departureAddressEditText.getWindowToken(), 0);
                    departureRadiusAddressSeekBar.setEnabled(departureMarker != null);
                }
                return false;
            }
        });

        departureAddressEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    findNewLocation(departureAddressEditText.getText().toString(), ModifiedMarker.Departure, departureAddressEditText.getText().toString().isEmpty());
                    inputMethodManager.hideSoftInputFromWindow(departureAddressEditText.getWindowToken(), 0);
                    departureRadiusAddressSeekBar.setEnabled(departureMarker != null);
                }
            }
        });

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
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.FragmentSearchMap)).getMap();

            if (googleMap == null) {
                Toast.makeText(this,
                        "Sorry! unable to create maps", Toast.LENGTH_SHORT)
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
        googleMap.clear();
        //TextView departureCityTextView = (TextView)  getView().findViewById(R.id.FragmentSearchDepartureAndDestinationTextView);
        //TextView destinationCityTextView = (TextView) view.findViewById(R.id.SearchDestinationCityTextView);
        //searchPaneOptions.setVisibility(searchPaneOptions.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        /*CheckBox smokers = (CheckBox)  view.findViewById(R.id.SmokersCheckbox);
        CheckBox womenOnly = (CheckBox)  view.findViewById(R.id.SearchWomenOnlyCheckbox);
        CheckBox free = (CheckBox) view.findViewById(R.id.SearchFreeCheckbox);
        CheckBox petsAllowed = (CheckBox) view.findViewById(R.id.SearchPetsCheckBox);*/

        Journey journey = new Journey();

        journey.DepartureAddress = new GeoAddress(0, 0, "Belfast");
        journey.DestinationAddress = new GeoAddress(0, 0, "Dublin");
        //journey.SmokersAllowed = smokers.isChecked();
        //journey.WomenOnly = womenOnly.isChecked();
        //journey.PetsAllowed = petsAllowed.isChecked();
        //journey.Free = free.isChecked();
        //journey.DateAndTimeOfDeparture = DateTimeHelper.convertToWCFDate(myCalendar.getTime());
        //journey.SearchByDate = dateTextView.getText().toString().length() != 0;
        //journey.SearchByTime = timeTextView.getText().toString().length() != 0;

        new WCFServiceTask<Journey>(getResources().getString(R.string.SearchForJourneysURL),
                journey, new TypeToken<ServiceResponse<ArrayList<Journey>>>() {}.getType(), appData.getAuthorisationHeaders(), this).execute();
    }

    @Override
    public void onServiceCallCompleted(final ServiceResponse<ArrayList<Journey>> serviceResponse, String parameter) {
        super.checkIfAuthorised(serviceResponse.ServiceResponseCode);
        progressBar.setVisibility(View.GONE);
        if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
        {
            numOfSearchResults = serviceResponse.Result.size();
            if(serviceResponse.Result.size() > 0)
            {
                toggleSearchResultsTextView.setText("Hide search results (" + numOfSearchResults +")");
                searchResultsListView.setVisibility(View.VISIBLE);
                parentSearchRelativeLayout.setVisibility(View.GONE);
            }

            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            RelativeLayout.LayoutParams layout_description = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, metrics.heightPixels);
            resultsRelativeLayout.setLayoutParams(layout_description);

            SearchResultsAdapter adapter = new SearchResultsAdapter(this, R.layout.fragment_search_results_listview_row, serviceResponse.Result);
            searchResultsListView.setAdapter(adapter);

            searchResultsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {

                    searchResultsListView.setVisibility(View.GONE);
                    if(journeyDetailsFragment != null)
                    {
                        getSupportFragmentManager().beginTransaction().remove(journeyDetailsFragment).commit();
                    }

                    journeyDetailsFragment = new JourneyDetailsFragment();

                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.add(journeyDetailsRelativeLayout.getId(), journeyDetailsFragment);
                    fragmentTransaction.commit();
                    Bundle bundle = new Bundle();
                    bundle.putString("CurrentCarShare", gson.toJson(serviceResponse.Result.get(i)));
                    journeyDetailsFragment.setArguments(bundle);
                    journeyDetailsFragment.setOnCloseListener(new FragmentClosed() {
                        @Override
                        public void onFragmentClosed() {
                            getSupportFragmentManager().beginTransaction().remove(journeyDetailsFragment).commit();
                            parentSearchRelativeLayout.setVisibility(View.VISIBLE);
                            arrowImageView.setImageResource(R.drawable.down);
                        }
                    });
                    journeyDetailsRelativeLayout.setVisibility(View.VISIBLE);
                    parentSearchRelativeLayout.setVisibility(View.GONE);

                    toggleSearchResultsTextView.setText("Show search results ("+ numOfSearchResults +")");
                    drawRouteOnMap(serviceResponse.Result.get(i));
                }
            });

            LatLngBounds.Builder builder = new LatLngBounds.Builder();

            for(Journey journey : serviceResponse.Result)
            {
                googleMap.addMarker(new MarkerOptions().position(new LatLng(journey.DepartureAddress.Latitude, journey.DepartureAddress.Longitude)).title(journey.DepartureAddress.AddressLine)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

                googleMap.addMarker(new MarkerOptions().position(new LatLng(journey.DestinationAddress.Latitude, journey.DestinationAddress.Longitude)).title(journey.DestinationAddress.AddressLine)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));


                builder.include(new LatLng(journey.DepartureAddress.Latitude, journey.DepartureAddress.Longitude));
                builder.include(new LatLng(journey.DestinationAddress.Latitude, journey.DestinationAddress.Longitude));

            }

            int padding = 100;
            LatLngBounds bounds = builder.build();
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            googleMap.animateCamera(cameraUpdate);
        }
    }

    private void drawRouteOnMap(final Journey journey)
    {
        googleMap.clear();

        googleMap.addMarker(new MarkerOptions().position(new LatLng(journey.DepartureAddress.Latitude, journey.DepartureAddress.Longitude)).title(journey.DepartureAddress.AddressLine)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

        googleMap.addMarker(new MarkerOptions().position(new LatLng(journey.DestinationAddress.Latitude, journey.DestinationAddress.Longitude)).title(journey.DestinationAddress.AddressLine)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));


        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(new LatLng(journey.DepartureAddress.Latitude, journey.DepartureAddress.Longitude));
        builder.include(new LatLng(journey.DestinationAddress.Latitude, journey.DestinationAddress.Longitude));

        int padding = 150;
        LatLngBounds bounds = builder.build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);


        new AsyncTask<GoogleMap, Journey, Void>(){

            private GMapV2Direction gMapV2Direction;
            private Document doc;

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                if(doc != null)
                {
                    ArrayList<LatLng> directionPoint = gMapV2Direction.getDirection(doc);

                    PolylineOptions rectLine = new PolylineOptions().width(10).color(Color.BLUE);
                    for(int i = 0 ; i < directionPoint.size() ; i++) {
                        rectLine.add(directionPoint.get(i));
                    }
                    googleMap.addPolyline(rectLine);
                }
            }

            @Override
            protected Void doInBackground(GoogleMap... googleMaps) {
                gMapV2Direction = new GMapV2Direction();
                doc = gMapV2Direction.getDocument(new LatLng(journey.DepartureAddress.Latitude, journey.DepartureAddress.Longitude),
                        new LatLng(journey.DestinationAddress.Latitude, journey.DestinationAddress.Longitude), GMapV2Direction.MODE_DRIVING);
                return null;
            }
        }.execute(googleMap);

        googleMap.animateCamera(cameraUpdate);
    }

    private void toggleSearchResultsVisibility()
    {
        searchResultsListView.setVisibility(searchResultsListView.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        toggleSearchResultsTextView.setText(searchResultsListView.getVisibility() == View.VISIBLE ? "Hide search results (" + numOfSearchResults +")" : "Show search results ("+ numOfSearchResults +")");
        //searchPaneOptions.setVisibility(searchResultsListView.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        parentSearchRelativeLayout.setVisibility(searchResultsListView.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        if(journeyDetailsFragment != null)
        {
            getSupportFragmentManager().beginTransaction().remove(journeyDetailsFragment).commit();
        }
    }

    private void toggleSearchPaneVisibility()
    {
        searchPaneOptions.setVisibility(searchPaneOptions.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        arrowImageView.setImageResource(searchPaneOptions.getVisibility() == View.VISIBLE ? R.drawable.down : R.drawable.up);
    }
}
