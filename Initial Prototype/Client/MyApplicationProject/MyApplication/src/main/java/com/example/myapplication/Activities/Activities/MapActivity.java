package com.example.myapplication.Activities.Activities;

import android.content.Context;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.myapplication.Activities.Base.BaseActivity;
import com.example.myapplication.Experimental.GMapV2Direction;
import com.example.myapplication.R;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Michal on 07/01/14.
 */
public class MapActivity extends BaseActivity
{
    // Google Map
    private GoogleMap googleMap;
    private EditText departureAddressEditText;
    private EditText destinationAddressEditText;

    private Geocoder geocoder;
    private LocationListener locationListener;

    private Marker departureMarker;
    private Marker destinationMarker;
    private int marker_count;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);


        geocoder = new Geocoder(getApplication());
        marker_count = 0;
        try {
            // Loading map
            initilizeMap();

        } catch (Exception e) {
            e.printStackTrace();
        }

        departureAddressEditText = (EditText) findViewById(R.id.MapActivityDepartureAddressTextView);
        departureAddressEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    findNewLocation(departureAddressEditText.getText().toString(), ModifiedMarker.Departure, departureAddressEditText.getText().toString().isEmpty());
                }

            }
        });

        destinationAddressEditText = (EditText) findViewById(R.id.MapActivityDestinationAddressTextView);
        destinationAddressEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    findNewLocation(destinationAddressEditText.getText().toString(), ModifiedMarker.Destination, destinationAddressEditText.getText().toString().isEmpty());
                }
            }
        });

        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                if (location != null) {
                    centerMapOnMyLocation();
                }
            }

            public void onProviderDisabled(String provider) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }
        };

        googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                //googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(54.4690711, -6.3393998), 14));
                centerMapOnMyLocation();

                GMapV2Direction gMapV2Direction = new GMapV2Direction();

                //Document doc = gMapV2Direction.getDocument(new LatLng(54.4690711,-6.3393998), new LatLng(54.5814623,-5.9422357), GMapV2Direction.MODE_DRIVING);

                //ArrayList<LatLng> directionPoint = gMapV2Direction.getDirection(doc);
                //final PolylineOptions rectLine = new PolylineOptions().width(3).color(Color.BLUE);

                /*for (int i = 0; i < directionPoint.size(); i++) {
                    Log.e("LOCATION: ", ""+directionPoint.get(i).longitude);
                    rectLine.add(directionPoint.get(i));
                }*/

                //googleMap.addPolyline(rectLine);
            }
        });


    }

    private void findNewLocation(String address, ModifiedMarker modifiedMarker, Boolean isEmpty)
    {
        if(Geocoder.isPresent()){
            try {
                MarkerOptions markerOptions = null;

                if(!isEmpty)
                {
                    List<Address> addresses= geocoder.getFromLocationName(address, 1); // get the found Address Objects

                    if(addresses.size() > 0)
                    {
                        LatLng latLng = new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude());
                        markerOptions = new MarkerOptions().position(latLng).title(address);
                    }

                }

                showOnMap(markerOptions, modifiedMarker);
            } catch (IOException e) {
                // handle the exception
            }

        }
    }

    private void showOnMap(MarkerOptions marker, ModifiedMarker modifiedMarker)
    {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        if(modifiedMarker == ModifiedMarker.Departure)
        {
            if(this.departureMarker != null)
            {
                this.departureMarker.remove();
                this.departureMarker = null;
                if(marker_count > 0)
                    marker_count -= 1;
            }

            if(marker != null)
            {
                this.departureMarker = googleMap.addMarker(new MarkerOptions().position(marker.getPosition()));
                if(marker_count < 2)
                    marker_count += 1;
            }
        }
        else
        {
            if(this.destinationMarker != null)
            {
               this.destinationMarker.remove();
               this.destinationMarker = null;
               if(marker_count > 0)
                    marker_count -= 1;
            }

            if(marker != null)
            {
                this.destinationMarker = googleMap.addMarker(new MarkerOptions().position(marker.getPosition()));
                if(marker_count < 2)
                    marker_count += 1;
            }
        }

        if(this.departureMarker != null)
        {
            builder.include(departureMarker.getPosition());

        }

        if(this.destinationMarker != null)
        {
            builder.include(destinationMarker.getPosition());

        }

        if(marker_count == 0)
        {
            centerMapOnMyLocation();
            return;
        }

        CameraUpdate cameraUpdate;

        if(marker_count == 1)
        {
            Marker marker1 = null;
            marker1 = (this.departureMarker == null) ? this.destinationMarker : this.departureMarker;

            cameraUpdate = CameraUpdateFactory.newLatLngZoom(marker1.getPosition(), 14);
        }
        else
        {
            int padding = 40;
            LatLngBounds bounds = builder.build();
            cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        }

        googleMap.animateCamera(cameraUpdate);
    }
    /**
     * function to load map. If map is not created it will create it for you
     * */
    private void initilizeMap() {

        if (googleMap == null) {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(
                    R.id.MapFragment)).getMap();

            if (googleMap == null) {
                Toast.makeText(getApplicationContext(),
                        "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                        .show();
            }
        }

    }

    private void centerMapOnMyLocation() {

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location myLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        Log.i("CURRENT LOCATION1: ", locationManager.getBestProvider(new Criteria(), false).toString());
        if (myLocation != null) {
            LatLng latLng = new LatLng(myLocation.getLatitude(),
                    myLocation.getLongitude());
            Log.i("CURRENT LOCATION2: ", myLocation.getLatitude() + " " + myLocation.getLongitude());
            //googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,14));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));
        }
        else
        {
            locationManager.requestLocationUpdates(locationManager.getBestProvider(new Criteria(), false), 1000, 10, locationListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initilizeMap();
    }

    enum ModifiedMarker{
        Departure,
        Destination
    }
}
