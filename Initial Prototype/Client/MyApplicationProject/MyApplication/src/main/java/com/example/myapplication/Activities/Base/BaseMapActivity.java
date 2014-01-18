package com.example.myapplication.activities.base;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Display;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.example.myapplication.activities.activities.LoginActivity;
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.experimental.AppData;
import com.example.myapplication.experimental.GMapV2Direction;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;

/**
 * Created by Michal on 07/01/14.
 */
public class BaseMapActivity extends FragmentActivity
{
    // Google Map
    protected GoogleMap googleMap;
    protected Geocoder geocoder;
    protected LocationListener locationListener;
    protected Marker departureMarker;
    protected Marker destinationMarker;
    protected int marker_count;
    protected GMapV2Direction gMapV2Direction;
    protected Location myLocation;
    protected LocationManager  locationManager;
    protected Circle departureRadius;
    protected Circle destinationRadius;
    protected AppData appData;
    protected Gson gson;
    protected ActionBar actionBar;
    protected EditText departureAddressEditText;
    protected EditText destinationAddressEditText;
    protected InputMethodManager inputMethodManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inputMethodManager = (InputMethodManager) this.getSystemService(this.INPUT_METHOD_SERVICE);
        appData = ((AppData)getApplication());
        gson = new Gson();
        actionBar = getActionBar();
        gMapV2Direction = new GMapV2Direction();
        geocoder = new Geocoder(this);
        marker_count = 0;

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


    }

    protected void findNewLocation(String address, ModifiedMarker modifiedMarker, Boolean isEmpty)
    {
        if(Geocoder.isPresent()){
            try {
                MarkerOptions markerOptions = null;

                if(!isEmpty)
                {
                    List<Address> addresses= geocoder.getFromLocationName(address, 1); // get the found Address Objects

                    if(addresses.size() > 0)
                    {
                        String addressText = String.format(
                                "%s, %s, %s",
                                // If there's a street address, add it
                                addresses.get(0).getMaxAddressLineIndex() > 0 ?
                                        addresses.get(0).getAddressLine(0) : "",
                                // Locality is usually a city
                                addresses.get(0).getLocality(),
                                // The country of the address
                                addresses.get(0).getCountryName());

                        LatLng latLng = new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude());
                        markerOptions = new MarkerOptions().position(latLng).title(addressText).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));;
                    }

                }

                showOnMap(markerOptions, modifiedMarker);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    protected void showOnMap(MarkerOptions marker, ModifiedMarker modifiedMarker)
    {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        if(modifiedMarker == ModifiedMarker.Departure)
        {
            if(this.departureMarker != null)
            {
                if(departureRadius != null)
                    this.departureRadius.remove();

                this.departureMarker.remove();
                this.departureMarker = null;
                if(marker_count > 0)
                    marker_count -= 1;
            }

            if(marker != null)
            {
                this.departureMarker = googleMap.addMarker(new MarkerOptions().position(marker.getPosition()).title(marker.getTitle())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                this.departureMarker.showInfoWindow();
                this.departureRadius = googleMap.addCircle(new CircleOptions()
                        .center(departureMarker.getPosition())
                        .radius(0)
                        .strokeColor(Color.rgb(15, 94, 135))
                        .fillColor(Color.argb(50, 42, 124, 157)));
                if(marker_count < 2)
                    marker_count += 1;
            }
        }
        else
        {
            if(this.destinationMarker != null)
            {
               if(destinationRadius != null)
                    this.destinationRadius.remove();

               this.destinationMarker.remove();
               this.destinationMarker = null;
               if(marker_count > 0)
                    marker_count -= 1;
            }

            if(marker != null)
            {
                this.destinationMarker = googleMap.addMarker(new MarkerOptions().position(marker.getPosition()).title(marker.getTitle()));
                this.destinationMarker.showInfoWindow();
                this.destinationRadius = googleMap.addCircle(new CircleOptions()
                        .center(destinationMarker.getPosition())
                        .radius(0)
                        .strokeColor(Color.rgb(15, 94, 135))
                        .fillColor(Color.argb(50, 42, 124, 157)));
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
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);

            int padding = 100;
            LatLngBounds bounds = builder.build();
            cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 0);
        }

        googleMap.animateCamera(cameraUpdate);
    }

    protected void centerMapOnMyLocation() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        myLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (myLocation != null) {
            LatLng latLng = new LatLng(myLocation.getLatitude(),
                    myLocation.getLongitude());

            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));
        }
        else
        {
            locationManager.requestLocationUpdates(locationManager.getBestProvider(new Criteria(), false), 1000, 10, locationListener);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    protected void checkIfAuthorised(int serviceResponseCode) {

        if(serviceResponseCode == ServiceResponseCode.UNAUTHORISED)
        {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            finish();
            startActivity(intent);
            appData.setUser(null);
            Toast toast = Toast.makeText(this, "Your session has expired, you must log in again.", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    protected enum ModifiedMarker{
        Departure,
        Destination
    }
}
