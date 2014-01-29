package com.example.myapplication.activities.base;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
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
import com.example.myapplication.experimental.FindNDriveManager;
import com.example.myapplication.experimental.GMapV2Direction;
import com.example.myapplication.experimental.WaypointHolder;
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
import java.util.ArrayList;
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
    protected GMapV2Direction gMapV2Direction;
    protected Location myLocation;
    protected LocationManager  locationManager;
    protected Circle departureRadius;
    protected Circle destinationRadius;
    protected FindNDriveManager findNDriveManager;
    protected Gson gson;
    protected ActionBar actionBar;
    protected EditText departureAddressEditText;
    protected EditText destinationAddressEditText;
    protected InputMethodManager inputMethodManager;
    protected ArrayList<WaypointHolder> waypointHolders;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inputMethodManager = (InputMethodManager) this.getSystemService(INPUT_METHOD_SERVICE);
        findNDriveManager = ((FindNDriveManager)getApplication());
        gson = new Gson();
        actionBar = getActionBar();
        gMapV2Direction = new GMapV2Direction();
        geocoder = new Geocoder(this);
    }

    protected MarkerOptions getAddress(String address)
    {
        MarkerOptions markerOptions = null;

        if(Geocoder.isPresent()){
            try {
                if(!address.isEmpty())
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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return markerOptions;
    }

    protected void showDeparturePoint(MarkerOptions markerOptions)
    {
        if(this.departureRadius != null)
        {
            this.departureRadius.remove();
        }

        if(this.departureMarker != null)
        {
            this.departureMarker.remove();
            this.departureMarker = null;
        }

        if(markerOptions != null)
        {
            this.departureMarker = googleMap.addMarker(new MarkerOptions()
                    .position(markerOptions.getPosition())
                    .title(markerOptions.getTitle())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

            this.departureMarker.showInfoWindow();
            this.departureRadius = googleMap.addCircle(new CircleOptions()
                    .center(markerOptions.getPosition())
                    .radius(0)
                    .strokeColor(Color.rgb(15, 94, 135))
                    .fillColor(Color.argb(50, 42, 124, 157)));
        }

        animateCamera();
    }

    protected void showDestinationPoint(MarkerOptions markerOptions)
    {
        if(this.destinationRadius != null)
        {
            this.destinationRadius.remove();
        }

        if(this.destinationMarker != null)
        {
            this.destinationMarker.remove();
            this.destinationMarker = null;
        }

        if(markerOptions != null)
        {
            this.destinationMarker = googleMap.addMarker(new MarkerOptions()
                    .position(markerOptions.getPosition())
                    .title(markerOptions.getTitle())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

            this.destinationMarker.showInfoWindow();
            this.destinationRadius = googleMap.addCircle(new CircleOptions()
                    .center(markerOptions.getPosition())
                    .radius(0)
                    .strokeColor(Color.rgb(15, 94, 135))
                    .fillColor(Color.argb(50, 42, 124, 157)));
        }

        animateCamera();
    }

    protected void animateCamera()
    {
        int marker_count = 0;
        Marker marker = null;
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        if(this.departureMarker != null)
        {
            builder.include(departureMarker.getPosition());
            marker = this.departureMarker;
            marker_count += 1;
        }

        if(this.destinationMarker != null)
        {
            builder.include(destinationMarker.getPosition());
            marker = this.destinationMarker;
            marker_count += 1;
        }

        if(this.waypointHolders != null)
        {
            for(WaypointHolder waypointHolder : this.waypointHolders)
            {
                if(waypointHolder.googleMapMarker != null)
                {
                    builder.include(waypointHolder.googleMapMarker.getPosition());
                    marker = waypointHolder.googleMapMarker;
                    marker_count += 1;
                }
            }
        }

        if(marker_count == 0)
        {
            centerMapOnMyLocation();
            return;
        }

        CameraUpdate cameraUpdate = null;

        if(marker_count == 1)
        {
            if(marker != null)
            {
                cameraUpdate = CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 14);
            }
        }
        else
        {
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);

            LatLngBounds bounds = builder.build();
            cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 0);
        }

        if(cameraUpdate != null)
        {
            googleMap.animateCamera(cameraUpdate);
        }
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
}
