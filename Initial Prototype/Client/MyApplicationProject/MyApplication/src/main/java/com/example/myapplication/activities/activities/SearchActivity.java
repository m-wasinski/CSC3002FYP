package com.example.myapplication.activities.activities;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
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
import com.example.myapplication.constants.IntentConstants;
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.domain_objects.Journey;
import com.example.myapplication.domain_objects.GeoAddress;
import com.example.myapplication.dtos.JourneySearchDTO;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.experimental.GMapV2Direction;
import com.example.myapplication.interfaces.FragmentClosed;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WCFServiceTask;
import com.example.myapplication.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.reflect.TypeToken;

import org.w3c.dom.Document;
import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Michal on 08/01/14.
 */

public class SearchActivity extends BaseMapActivity implements WCFServiceCallback<ArrayList<Journey>, String>, GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener{

    private SeekBar departureRadiusAddressSeekBar;
    private SeekBar destinationRadiusAddressSeekBar;

    private double departureRadiusValue;
    private double destinationRadiusValue;

    private DecimalFormat decimalFormat;

    private TextView departureRadiusTextView;
    private TextView destinationRadiusTextView;

    private LinearLayout searchPaneOptions;

    private RelativeLayout resultsRelativeLayout;
    private RelativeLayout journeyDetailsRelativeLayout;
    private RelativeLayout parentSearchRelativeLayout;

    private ListView searchResultsListView;

    private JourneyDetailsFragment journeyDetailsFragment;

    private int numOfSearchResults;

    private Button searchPaneHeaderButton;
    private Button searchResultsButton;
    private Button searchButton;
    private Button departureGPSButton;
    private Button destinationGPSButton;

    private ProgressBar progressBar;

    private RelativeLayout departureRelativeLayout;
    private RelativeLayout destinationRelativeLayout;

    private TextView departureTextView;
    private TextView destinationTextView;

    private final int METERS_IN_MILE = 1600;
    private final int PROGRESS_BAR_UNITS = 160;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_search);
        this.actionBar.hide();
        this.numOfSearchResults = 0;
        this.decimalFormat = new DecimalFormat("0.00");

        //Initialise UI elements.
        this.progressBar = (ProgressBar) this.findViewById(R.id.SearchActivityProgressBar);
        this.departureRelativeLayout = (RelativeLayout) this.findViewById(R.id.SearchActivityDepartureRelativeLayout);
        this.destinationRelativeLayout = (RelativeLayout) this.findViewById(R.id.SearchActivityDestinationRelativeLayout);
        this.departureTextView = (TextView) this.findViewById(R.id.SearchActivityDepartureTextView);
        this.destinationTextView = (TextView) this.findViewById(R.id.SearchActivityDestinationTextView);
        this.searchResultsButton = (Button) this.findViewById(R.id.SearchActivityResultsButton);
        this.departureGPSButton = (Button) this.findViewById(R.id.SearchActivityDepartureGpsButton);
        this.destinationGPSButton = (Button) this.findViewById(R.id.SearchActivityDestinationGpsButton);

        /*
        this.journeyDetailsRelativeLayout = (RelativeLayout) findViewById(R.id.ActivitySearchMapJourneyDetailsRelativeLayout);
        this.parentSearchRelativeLayout = (RelativeLayout) findViewById(R.id.ActivitySearchMapParentSearchRelativeLayout);
        this.searchResultsListView = (ListView) findViewById(R.id.ActivitySearchMapResultsListView);
        this.resultsRelativeLayout = (RelativeLayout) findViewById(R.id.ActivitySearchMapResultsRelativeLayout);
        this.searchPaneHeaderButton = (Button) findViewById(R.id.SearchActivitySearchPaneHeaderButton);

        this.searchPaneOptions = (LinearLayout)findViewById(R.id.ActivitySearchPaneOptionsLinearLayout);
        this.departureRadiusTextView = (TextView) findViewById(R.id.ActivitySearchMapDepartureRadiusTextView);
        this.destinationRadiusTextView = (TextView) findViewById(R.id.ActivitySearchMapDestinationRadiusTextView);
        this.departureRadiusAddressSeekBar = (SeekBar) findViewById(R.id.ActivitySearchMapDepartureRadiusSeekBar);
        this.departureRadiusAddressSeekBar.setEnabled(false);
        this.departureAddressEditText = (EditText) findViewById(R.id.MapActivityDepartureAddressTextView);
        this.destinationAddressEditText = (EditText) findViewById(R.id.MapActivityDestinationAddressTextView);
        this.destinationRadiusAddressSeekBar = (SeekBar) findViewById(R.id.ActivitySearchMapDestinationRadiusSeekBar);
        this.destinationRadiusAddressSeekBar.setEnabled(false);*/
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

    private void showAddressDialog(final Boolean isDeparture)
    {
        // custom addressDialog
        final Dialog addressDialog = new Dialog(this);

        addressDialog.setContentView(R.layout.address_dialog);
        addressDialog.setTitle(isDeparture ? "Enter departure point" : "Enter destination point");

        final EditText addressEditText = (EditText) addressDialog.findViewById(R.id.AddressDialogAddressEditText);
        final EditText perimeterEditText = (EditText) addressDialog.findViewById(R.id.AddressDialogPerimeterEditText);
        Button okButton = (Button) addressDialog.findViewById(R.id.AddressDialogOKButton);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String addressText = addressEditText.getText().toString();

                if(addressText != null && !addressText.isEmpty())
                {
                    MarkerOptions markerOptions = getAddress(addressEditText.getText().toString());
                    addressEntered(isDeparture, markerOptions, Integer.parseInt(perimeterEditText.getText().toString()));
                }

                addressDialog.dismiss();
            }
        });

        addressDialog.show();
    }

    private void addressEntered(Boolean isDeparture, MarkerOptions markerOptions, int perimeter)
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

    private void setupEventHandlers()
    {
        this.departureRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddressDialog(true);
            }
        });

        this.destinationRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddressDialog(false);
            }
        });

        this.departureGPSButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        this.destinationGPSButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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
        /*this.searchPaneHeaderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleSearchPaneVisibility();
            }
        });


        this.searchResultsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleSearchResultsVisibility();
            }
        });



        this.departureRadiusAddressSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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

        this.destinationRadiusAddressSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                destinationRadiusValue = destinationRadiusAddressSeekBar.getProgress()*PROGRESS_BAR_UNITS;
                destinationRadius.setRadius(destinationRadiusValue);
                destinationRadiusTextView.setText("R: " + decimalFormat.format(destinationRadiusValue / METERS_IN_MILE) + " miles");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {


            }
        });

        this.departureAddressEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if(i == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_UP){
                    MarkerOptions markerOptions = getAddress(departureAddressEditText.getText().toString());
                    showDeparturePoint(markerOptions);
                    departureRadiusAddressSeekBar.setEnabled(departureMarker != null);
                }
                return false;
            }
        });

        this.departureAddressEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    MarkerOptions markerOptions = getAddress(departureAddressEditText.getText().toString());
                    showDeparturePoint(markerOptions);
                    departureRadiusAddressSeekBar.setEnabled(departureMarker != null);
                }
            }
        });

        this.destinationAddressEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    MarkerOptions markerOptions = getAddress(destinationAddressEditText.getText().toString());
                    showDestinationPoint(markerOptions);
                    destinationRadiusAddressSeekBar.setEnabled(destinationMarker != null);
                }
            }
        });

        this.destinationAddressEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if(i == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_UP){
                    MarkerOptions markerOptions = getAddress(destinationAddressEditText.getText().toString());
                    showDestinationPoint(markerOptions);
                    destinationRadiusAddressSeekBar.setEnabled(destinationMarker != null);
                }
                return false;
            }
        });*/
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
        journeySearchDTO.DepartureRadius = departureRadiusValue / METERS_IN_MILE;
        journeySearchDTO.DestinationRadius = destinationRadiusValue / METERS_IN_MILE;
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

            if(serviceResponse.Result.size() > 0)
            {
                googleMap.clear();
                searchResultsButton.setText("Hide search results (" + numOfSearchResults +")");
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
                    bundle.putString(IntentConstants.JOURNEY, gson.toJson(serviceResponse.Result.get(i)));
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

                    searchResultsButton.setText("Show search results ("+ numOfSearchResults +")");
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

            if(serviceResponse.Result.size() > 0)
            {
                int padding = 100;
                LatLngBounds bounds = builder.build();
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                googleMap.animateCamera(cameraUpdate);
            }

        }
    }

    private void drawRouteOnMap(final Journey journey)
    {
        googleMap.clear();
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        for(GeoAddress geoAddress : journey.GeoAddresses)
        {
            googleMap.addMarker(new MarkerOptions().position(new LatLng(geoAddress.Latitude, geoAddress.Longitude)).title(geoAddress.AddressLine)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

            builder.include(new LatLng(geoAddress.Latitude, geoAddress.Longitude));
            builder.include(new LatLng(geoAddress.Latitude, geoAddress.Longitude));
        }

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
                doc = gMapV2Direction.getDocument(journey.GeoAddresses, GMapV2Direction.MODE_DRIVING);
                return null;
            }
        }.execute(googleMap);

        googleMap.animateCamera(cameraUpdate);
    }

    private void toggleSearchResultsVisibility()
    {
        searchResultsListView.setVisibility(searchResultsListView.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        searchResultsButton.setText(searchResultsListView.getVisibility() == View.VISIBLE ? "Hide search results (" + numOfSearchResults +")" : "Show search results ("+ numOfSearchResults +")");
        parentSearchRelativeLayout.setVisibility(searchResultsListView.getVisibility() == View.VISIBLE && searchResultsListView.getCount() > 0 ? View.GONE : View.VISIBLE);
        if(journeyDetailsFragment != null)
        {
            getSupportFragmentManager().beginTransaction().remove(journeyDetailsFragment).commit();
        }
    }

    private void toggleSearchPaneVisibility()
    {
        searchPaneOptions.setVisibility(searchPaneOptions.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        Drawable image = getResources().getDrawable(searchPaneOptions.getVisibility() == View.VISIBLE ? R.drawable.down : R.drawable.up);
        searchPaneHeaderButton.setCompoundDrawablesWithIntrinsicBounds(null, null, image, null);
        searchPaneHeaderButton.setText(searchPaneOptions.getVisibility() == View.VISIBLE ? "Minimize" : "Restore");
    }

    private void minimizeSearchPane()
    {
        searchPaneOptions.setVisibility(View.GONE);
        Drawable image = getResources().getDrawable(R.drawable.up);
        searchPaneHeaderButton.setCompoundDrawablesWithIntrinsicBounds(null, null, image, null);
        searchPaneHeaderButton.setText("Restore");
    }

    private void restoreSearchPane()
    {
        searchPaneOptions.setVisibility(View.VISIBLE);
        Drawable image = getResources().getDrawable(R.drawable.down);
        searchPaneHeaderButton.setCompoundDrawablesWithIntrinsicBounds(null, null, image, null);
        searchPaneHeaderButton.setText("Minimize");
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
}
