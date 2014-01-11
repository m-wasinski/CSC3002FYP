package com.example.myapplication.Activities.Activities;

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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.Activities.Base.BaseMapActivity;
import com.example.myapplication.Activities.Fragments.JourneyDetailsFragment;
import com.example.myapplication.Adapters.SearchResultsAdapter;
import com.example.myapplication.Constants.ServiceResponseCode;
import com.example.myapplication.DomainObjects.CarShare;
import com.example.myapplication.DomainObjects.GeoAddress;
import com.example.myapplication.DomainObjects.ServiceResponse;
import com.example.myapplication.Experimental.GMapV2Direction;
import com.example.myapplication.Interfaces.FragmentClosed;
import com.example.myapplication.Interfaces.WCFServiceCallback;
import com.example.myapplication.NetworkTasks.WCFServiceTask;
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

public class ActivitySearch extends BaseMapActivity implements WCFServiceCallback<ArrayList<CarShare>, String>{

    private SeekBar departureRadiusAddressSeekBar;
    private SeekBar destinationRadiusAddressSeekBar;
    private double departureRadiusValue;
    private double destinationRadiusValue;
    private DecimalFormat decimalFormat;
    private ImageButton toggleSearchOptionsButton;
    private TextView toggleSearchResultsTextView;
    private TextView departureRadiusTextView;
    private TextView destinationRadiusTextView;
    private LinearLayout searchPane;
    private LinearLayout searchPaneOptions;
    private RelativeLayout resultsRelativeLayout;
    private RelativeLayout journeyDetailsRelativeLayout;
    private RelativeLayout parentSearchRelativeLayout;
    private Button searchButton;
    private ImageButton optionsButton;
    private ListView searchResultsListView;
    private JourneyDetailsFragment journeyDetailsFragment;
    private int numOfSearchResults;

    private final int METERS_IN_MILE = 1600;
    private final int PROGRESS_BAR_UNITS = 160;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        actionBar.hide();
        numOfSearchResults = 0;
        journeyDetailsRelativeLayout = (RelativeLayout) findViewById(R.id.ActivitySearchMapJourneyDetailsRelativeLayout);
        parentSearchRelativeLayout = (RelativeLayout) findViewById(R.id.ActivitySearchMapParentSearchRelativeLayout);
        searchResultsListView = (ListView) findViewById(R.id.ActivitySearchMapResultsListView);
        resultsRelativeLayout = (RelativeLayout) findViewById(R.id.ActivitySearchMapResultsRelativeLayout);

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
                search();
            }
        });

        optionsButton = (ImageButton) findViewById(R.id.ActivitySearchMapOptionsButton);
        optionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        decimalFormat = new DecimalFormat("0.00");
        searchPane = (LinearLayout) findViewById(R.id.ActivitySearchPaneLinearLayout);
        searchPaneOptions = (LinearLayout)findViewById(R.id.ActivitySearchPaneOptionsLinearLayout);

        toggleSearchOptionsButton = (ImageButton) findViewById(R.id.ActivitySearchMapMinimizeButton);
        toggleSearchOptionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleSearchPaneVisibility();
            }
        });

        departureRadiusTextView = (TextView) findViewById(R.id.ActivitySearchMapDepartureRadiusTextView);

        destinationRadiusTextView = (TextView) findViewById(R.id.ActivitySearchMapDestinationRadiusTextView);

        departureRadiusAddressSeekBar = (SeekBar) findViewById(R.id.ActivitySearchMapDepartureRadiusSeekBar);
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

        departureAddressEditText = (EditText) findViewById(R.id.MapActivityDepartureAddressTextView);
        destinationAddressEditText = (EditText) findViewById(R.id.MapActivityDestinationAddressTextView);
        destinationAddressEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    findNewLocation(destinationAddressEditText.getText().toString(), ModifiedMarker.Destination, destinationAddressEditText.getText().toString().isEmpty());
                }
            }
        });

        departureAddressEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if(i == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_UP){
                    findNewLocation(departureAddressEditText.getText().toString(), ModifiedMarker.Departure, departureAddressEditText.getText().toString().isEmpty());
                    inputMethodManager.hideSoftInputFromWindow(departureAddressEditText.getWindowToken(), 0);
                }
                return false;
            }
        });

        departureAddressEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    findNewLocation(departureAddressEditText.getText().toString(), ModifiedMarker.Departure, departureAddressEditText.getText().toString().isEmpty());
                    inputMethodManager.hideSoftInputFromWindow(departureAddressEditText.getWindowToken(), 0);
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

        CarShare carShare = new CarShare();

        carShare.DepartureAddress = new GeoAddress(0, 0, "Belfast");
        carShare.DestinationAddress = new GeoAddress(0, 0, "Dublin");
        //carShare.SmokersAllowed = smokers.isChecked();
        //carShare.WomenOnly = womenOnly.isChecked();
        //carShare.PetsAllowed = petsAllowed.isChecked();
        //carShare.Free = free.isChecked();
        //carShare.DateAndTimeOfDeparture = DateTimeHelper.convertToWCFDate(myCalendar.getTime());
        //carShare.SearchByDate = dateTextView.getText().toString().length() != 0;
        //carShare.SearchByTime = timeTextView.getText().toString().length() != 0;

        new WCFServiceTask<CarShare, ArrayList<CarShare>>("https://findndrive.no-ip.co.uk/Services/SearchService.svc/searchcarshare",
                carShare, new TypeToken<ServiceResponse<ArrayList<CarShare>>>() {}.getType(), appData.getAuthorisationHeaders(), null, this).execute();
    }

    @Override
    public void onServiceCallCompleted(final ServiceResponse<ArrayList<CarShare>> serviceResponse, String parameter) {
        super.checkIfAuthorised(serviceResponse.ServiceResponseCode);
        if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
        {
            numOfSearchResults = serviceResponse.Result.size();
            if(serviceResponse.Result.size() > 0)
            {
                toggleSearchResultsTextView.setText("Hide search results (" + numOfSearchResults +")");
                searchResultsListView.setVisibility(View.VISIBLE);
                searchPaneOptions.setVisibility(View.GONE);
            }

            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            RelativeLayout.LayoutParams layout_description = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,  metrics.heightPixels /2);
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
                        }
                    });
                    journeyDetailsRelativeLayout.setVisibility(View.VISIBLE);
                    parentSearchRelativeLayout.setVisibility(View.GONE);

                    toggleSearchResultsTextView.setText("Show search results ("+ numOfSearchResults +")");
                    drawRouteOnMap(serviceResponse.Result.get(i));
                }
            });

            LatLngBounds.Builder builder = new LatLngBounds.Builder();

            for(CarShare carShare : serviceResponse.Result)
            {
                googleMap.addMarker(new MarkerOptions().position(new LatLng(carShare.DepartureAddress.Latitude, carShare.DepartureAddress.Longitude)).title(carShare.DepartureAddress.AddressLine)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

                googleMap.addMarker(new MarkerOptions().position(new LatLng(carShare.DestinationAddress.Latitude, carShare.DestinationAddress.Longitude)).title(carShare.DestinationAddress.AddressLine)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));


                builder.include(new LatLng(carShare.DepartureAddress.Latitude, carShare.DepartureAddress.Longitude));
                builder.include(new LatLng(carShare.DestinationAddress.Latitude, carShare.DestinationAddress.Longitude));

            }

            int padding = 100;
            LatLngBounds bounds = builder.build();
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            googleMap.animateCamera(cameraUpdate);
        }
    }

    private void drawRouteOnMap(final CarShare carShare)
    {
        googleMap.clear();

        googleMap.addMarker(new MarkerOptions().position(new LatLng(carShare.DepartureAddress.Latitude, carShare.DepartureAddress.Longitude)).title(carShare.DepartureAddress.AddressLine)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

        googleMap.addMarker(new MarkerOptions().position(new LatLng(carShare.DestinationAddress.Latitude, carShare.DestinationAddress.Longitude)).title(carShare.DestinationAddress.AddressLine)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));


        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(new LatLng(carShare.DepartureAddress.Latitude, carShare.DepartureAddress.Longitude));
        builder.include(new LatLng(carShare.DestinationAddress.Latitude, carShare.DestinationAddress.Longitude));

        int padding = 150;
        LatLngBounds bounds = builder.build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);


        new AsyncTask<GoogleMap, CarShare, Void>(){

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
                doc = gMapV2Direction.getDocument(new LatLng(carShare.DepartureAddress.Latitude, carShare.DepartureAddress.Longitude),
                        new LatLng(carShare.DestinationAddress.Latitude, carShare.DestinationAddress.Longitude), GMapV2Direction.MODE_DRIVING);
                return null;
            }
        }.execute(googleMap);

        googleMap.animateCamera(cameraUpdate);
    }

    private void toggleSearchResultsVisibility()
    {
        searchResultsListView.setVisibility(searchResultsListView.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        toggleSearchResultsTextView.setText(searchResultsListView.getVisibility() == View.VISIBLE ? "Hide search results (" + numOfSearchResults +")" : "Show search results ("+ numOfSearchResults +")");
    }

    private void toggleSearchPaneVisibility()
    {
        searchPaneOptions.setVisibility(searchPaneOptions.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
    }
}
