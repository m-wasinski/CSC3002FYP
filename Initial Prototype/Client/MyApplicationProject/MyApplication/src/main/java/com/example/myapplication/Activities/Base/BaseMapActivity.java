package com.example.myapplication.activities.base;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Display;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.example.myapplication.domain_objects.GeoAddress;
import com.example.myapplication.domain_objects.Journey;
import com.example.myapplication.domain_objects.MarkerType;
import com.example.myapplication.experimental.FindNDriveManager;
import com.example.myapplication.experimental.GMapV2Direction;
import com.example.myapplication.experimental.GeocoderParams;
import com.example.myapplication.experimental.WaypointHolder;
import com.example.myapplication.interfaces.GeoCoderFinishedCallBack;
import com.example.myapplication.network_tasks.GeocoderTask;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;

import org.w3c.dom.Document;

import java.util.ArrayList;

/**
 * Created by Michal on 07/01/14.
 */
public class BaseMapActivity extends FragmentActivity implements GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener, GeoCoderFinishedCallBack
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
    protected ArrayList<WaypointHolder> wayPoints;
    protected LocationClient locationClient;
    private Polyline polyline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialise variables.
        inputMethodManager = (InputMethodManager) this.getSystemService(INPUT_METHOD_SERVICE);
        findNDriveManager = ((FindNDriveManager)getApplication());
        gson = new Gson();
        actionBar = getActionBar();
        gMapV2Direction = new GMapV2Direction();
        this.locationClient = new LocationClient(this, this, this);
        geocoder = new Geocoder(this);
    }

    protected void showDeparturePoint(MarkerOptions markerOptions, double perimeter)
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
            this.departureMarker = googleMap.addMarker(markerOptions);

            this.departureMarker.showInfoWindow();
            this.departureRadius = googleMap.addCircle(new CircleOptions()
                    .center(markerOptions.getPosition())
                    .radius(perimeter * 1600)
                    .strokeColor(Color.rgb(15, 94, 135))
                    .fillColor(Color.argb(50, 42, 124, 157)));
        }

        animateCamera();
    }

    protected void showDestinationPoint(MarkerOptions markerOptions, double perimeter)
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
            this.destinationMarker = googleMap.addMarker(markerOptions);

            this.destinationMarker.showInfoWindow();
            this.destinationRadius = googleMap.addCircle(new CircleOptions()
                    .center(markerOptions.getPosition())
                    .radius(perimeter * 1600)
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

        if(this.wayPoints != null)
        {
            for(WaypointHolder waypointHolder : this.wayPoints)
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
                cameraUpdate = CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 12);
            }
        }
        else
        {
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);

            LatLngBounds bounds = builder.build();
            cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 150);
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

    protected void drawDrivingDirectionsOnMap(final GoogleMap map, final ArrayList<GeoAddress> geoAddresses)
    {

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
                    if(polyline != null)
                    {
                        polyline.remove();
                    }

                    polyline = map.addPolyline(rectLine);

                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    for(LatLng latLng : directionPoint)
                    {
                        builder.include(latLng);
                    }
                    LatLngBounds bounds = builder.build();
                    map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 40));
                }
            }

            @Override
            protected Void doInBackground(GoogleMap... googleMaps) {
                gMapV2Direction = new GMapV2Direction();
                doc = gMapV2Direction.getDocument(geoAddresses, GMapV2Direction.MODE_DRIVING);
                return null;
            }
        }.execute(map);
    }

    protected void getCurrentAddress(MarkerType markerType, Location location)
    {
        new GeocoderTask(this, this, markerType, 0).execute(new GeocoderParams(null, location));
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onGeoCoderFinished(MarkerOptions address, MarkerType markerType, double perimeter) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

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
}
