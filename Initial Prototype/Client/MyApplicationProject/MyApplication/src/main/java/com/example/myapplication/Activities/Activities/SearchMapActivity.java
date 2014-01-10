package com.example.myapplication.Activities.Activities;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.w3c.dom.Document;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by Michal on 08/01/14.
 */
@SuppressWarnings("ConstantConditions")
public class SearchMapActivity extends BaseMapActivity implements WCFServiceCallback<ArrayList<CarShare>, String>{

    private EditText departureAddressEditText;
    private EditText destinationAddressEditText;

    private SeekBar departureRadiusAddressSeekBar;
    private SeekBar destinationRadiusAddressSeekBar;

    private double departureRadiusValue;
    private double destinationRadiusValue;

    private DecimalFormat decimalFormat;

    private TextView toggleSearchOptionsTextView;
    private TextView toggleSearchResultsTextView;
    private TextView departureRadiusTextView;
    private TextView destinationRadiusTextView;

    private InputMethodManager inputMethodManager;

    private LinearLayout searchPane;
    private LinearLayout searchPaneOptions;
    private LinearLayout optionsLinearLayout;
    private RelativeLayout resultsRelativeLayout;
    private RelativeLayout journeyDetailsRelativeLayout;
    private RelativeLayout parentSearchRelativeLayout;

    private Button searchButton;

    private ListView searchResultsListView;

    private JourneyDetailsFragment journeyDetailsFragment;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.activity_search_map, container, false);

        journeyDetailsRelativeLayout = (RelativeLayout) view.findViewById(R.id.ActivitySearchMapJourneyDetailsRelativeLayout);
        parentSearchRelativeLayout = (RelativeLayout) view.findViewById(R.id.ActivitySearchMapParentSearchRelativeLayout);
        searchResultsListView = (ListView) view.findViewById(R.id.ActivitySearchMapResultsListView);
        resultsRelativeLayout = (RelativeLayout) view.findViewById(R.id.ActivitySearchMapResultsRelativeLayout);

        toggleSearchResultsTextView = (TextView) view.findViewById(R.id.ActivitySearchMapToggleResultsTextView);
        toggleSearchResultsTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                searchResultsListView.setVisibility(searchResultsListView.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                toggleSearchResultsTextView.setText(searchResultsListView.getVisibility() == View.VISIBLE ? "Hide search results" : "Show search results");
                return false;
            }
        });

        searchButton = (Button) view.findViewById(R.id.ActivitySearchMapSearchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    search();
            }
        });

        inputMethodManager = (InputMethodManager)getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
        decimalFormat = new DecimalFormat("0.00");
        searchPane = (LinearLayout) view.findViewById(R.id.ActivitySearchPaneLinearLayout);
        searchPaneOptions = (LinearLayout) view.findViewById(R.id.ActivitySearchPaneOptionsLinearLayout);
        optionsLinearLayout = (LinearLayout) view.findViewById(R.id.ActivitySearchShowOptionsLinearLayout);

        toggleSearchOptionsTextView = (TextView) view.findViewById(R.id.ActivitySearchMapMinimizeRestoreTextView);
        toggleSearchOptionsTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                searchPaneOptions.setVisibility(searchPaneOptions.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                toggleSearchOptionsTextView.setText(searchPaneOptions.getVisibility() == View.VISIBLE ? "Minimize" : "Restore");
                return false;
            }
        });

        optionsLinearLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return false;
            }
        });
        departureRadiusTextView = (TextView) view.findViewById(R.id.ActivitySearchMapDepartureRadiusTextView);

        destinationRadiusTextView = (TextView) view.findViewById(R.id.ActivitySearchMapDestinationRadiusTextView);

        departureRadiusAddressSeekBar = (SeekBar) view.findViewById(R.id.ActivitySearchMapDepartureRadiusSeekBar);
        departureRadiusAddressSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(departureMarker != null && departureRadius != null)
                {
                    departureRadiusValue = departureRadiusAddressSeekBar.getProgress()*160;
                    departureRadius.setRadius(departureRadiusValue);
                    departureRadiusTextView.setText("R: "+decimalFormat.format(departureRadiusValue/1600)+" miles");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {


            }
        });
        destinationRadiusAddressSeekBar = (SeekBar) view.findViewById(R.id.ActivitySearchMapDestinationRadiusSeekBar);
        destinationRadiusAddressSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(destinationMarker != null && destinationRadius != null)
                {
                    destinationRadiusValue = destinationRadiusAddressSeekBar.getProgress()*160;
                    destinationRadius.setRadius(destinationRadiusValue);
                    destinationRadiusTextView.setText("R: " + decimalFormat.format(destinationRadiusValue / 1600) + " miles");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {


            }
        });

        departureAddressEditText = (EditText) view.findViewById(R.id.MapActivityDepartureAddressTextView);
        departureAddressEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    findNewLocation(departureAddressEditText.getText().toString(), ModifiedMarker.Departure, departureAddressEditText.getText().toString().isEmpty());
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

        destinationAddressEditText = (EditText) view.findViewById(R.id.MapActivityDestinationAddressTextView);
        destinationAddressEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    findNewLocation(destinationAddressEditText.getText().toString(), ModifiedMarker.Destination, destinationAddressEditText.getText().toString().isEmpty());
                }
            }
        });

        destinationAddressEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if(i == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_UP){
                    findNewLocation(destinationAddressEditText.getText().toString(), ModifiedMarker.Destination, destinationAddressEditText.getText().toString().isEmpty());
                    inputMethodManager.hideSoftInputFromWindow(departureAddressEditText.getWindowToken(), 0);
                }
                return false;
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

        return view;
    }

    private void initialiseMap() {

        if (googleMap == null) {
            googleMap = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.SearchMapFragment)).getMap();

            if (googleMap == null) {
                Toast.makeText(getActivity(),
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
        searchPaneOptions.setVisibility(searchPaneOptions.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        toggleSearchOptionsTextView.setText(searchPaneOptions.getVisibility() == View.VISIBLE ? "Minimize" : "Restore");
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
            if(serviceResponse.Result.size() > 0)
            {
                toggleSearchResultsTextView.setText("Hide search results");
            }

            RelativeLayout.LayoutParams layout_description = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, getView().getHeight()/2);

            resultsRelativeLayout.setLayoutParams(layout_description);

            SearchResultsAdapter adapter = new SearchResultsAdapter(getActivity(), R.layout.fragment_search_results_listview_row, serviceResponse.Result);
            searchResultsListView.setAdapter(adapter);

            searchResultsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {


                    if(journeyDetailsFragment != null)
                    {
                        getFragmentManager().beginTransaction().remove(journeyDetailsFragment).commit();
                    }

                    journeyDetailsFragment = new JourneyDetailsFragment();
                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    fragmentTransaction.add(journeyDetailsRelativeLayout.getId(), journeyDetailsFragment);
                    fragmentTransaction.commit();
                    Bundle bundle = new Bundle();
                    bundle.putString("CurrentCarShare", new Gson().toJson(serviceResponse.Result.get(i)));
                    journeyDetailsFragment.setArguments(bundle);
                    journeyDetailsFragment.setOnCloseListener(new FragmentClosed() {
                        @Override
                        public void onFragmentClosed() {
                            parentSearchRelativeLayout.setVisibility(View.VISIBLE);
                        }
                    });
                    journeyDetailsRelativeLayout.setVisibility(View.VISIBLE);
                    parentSearchRelativeLayout.setVisibility(View.GONE);
                    searchResultsListView.setVisibility(View.GONE);
                    toggleSearchResultsTextView.setText("Show search results");
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
}
