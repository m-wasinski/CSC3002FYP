package com.example.myapplication.activities.base;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;

import com.example.myapplication.R;
import com.example.myapplication.activities.activities.HomeActivity;
import com.example.myapplication.app_management.AppManager;
import com.example.myapplication.domain_objects.GeoAddress;
import com.example.myapplication.domain_objects.Journey;
import com.example.myapplication.enums.MarkerType;
import com.example.myapplication.google_maps_utilities.GMapV2Direction;
import com.example.myapplication.google_maps_utilities.GeocoderParams;
import com.example.myapplication.interfaces.GeoCoderFinishedCallBack;
import com.example.myapplication.interfaces.OnDrivingDirectionsRetrrievedListener;
import com.example.myapplication.network_tasks.GeocoderTask;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
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
 * Serves as a base activity for all activities implementing Google Maps.
 **/
public class BaseMapActivity extends FragmentActivity implements GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener, GeoCoderFinishedCallBack
{
    private GoogleMap googleMap;
    private Geocoder geocoder;
    private LocationListener locationListener;
    private Marker departureMarker;
    private Marker destinationMarker;
    private GMapV2Direction gMapV2Direction;
    private Location myLocation;
    private LocationManager  locationManager;
    private Circle departureRadius;
    private Circle destinationRadius;
    private AppManager appManager;
    private Gson gson;
    private ActionBar actionBar;
    private InputMethodManager inputMethodManager;
    private ArrayList<WaypointHolder> wayPoints;
    private LocationClient locationClient;

    public void setWayPoints(ArrayList<WaypointHolder> wayPoints) {
        this.wayPoints = wayPoints;
    }

    public LocationClient getLocationClient() {
        return locationClient;
    }

    public ArrayList<WaypointHolder> getWayPoints() {
        return wayPoints;
    }

    public InputMethodManager getInputMethodManager() {
        return inputMethodManager;
    }

    public ActionBar getActionBar() {
        return actionBar;
    }

    public Gson getGson() {
        return gson;
    }

    public AppManager getAppManager() {
        return appManager;
    }

    public Circle getDestinationRadius() {
        return destinationRadius;
    }

    public Circle getDepartureRadius() {
        return departureRadius;
    }

    public LocationManager getLocationManager() {
        return locationManager;
    }

    public Location getMyLocation() {
        return myLocation;
    }

    public GMapV2Direction getgMapV2Direction() {
        return gMapV2Direction;
    }

    public Marker getDestinationMarker() {
        return destinationMarker;
    }

    public Marker getDepartureMarker() {
        return departureMarker;
    }

    public Geocoder getGeocoder() {
        return geocoder;
    }

    public GoogleMap getGoogleMap() {
        return googleMap;
    }

    public void setGoogleMap(GoogleMap googleMap) {
        this.googleMap = googleMap;
    }

    public LocationListener getLocationListener() {
        return locationListener;
    }

    private Polyline polyline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialise local variables.
        inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        appManager = ((AppManager)getApplication());
        gson = new Gson();
        actionBar = getActionBar();
        if(actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        gMapV2Direction = new GMapV2Direction();
        locationClient = new LocationClient(this, this, this);
        geocoder = new Geocoder(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.other_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_home:
                startActivity(new Intent(this, HomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
                break;
            case R.id.logout_menu_option:
                appManager.logout(true, true);
                break;
            case android.R.id.home:
                finish();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Responsible for showing the departure point on the map as well as
     * drawing the radius around the desired location.
     * We must keep reference to the departure radius and departure marker objects to allow
     * their removal if the user changes the departure point from one location to another.
     **/
    protected void showDeparturePoint(MarkerOptions markerOptions, double perimeter)
    {
        if(departureRadius != null)
        {
            departureRadius.remove();
        }

        if(departureMarker != null)
        {
            departureMarker.remove();
            departureMarker = null;
        }

        if(markerOptions != null)
        {
            departureMarker = googleMap.addMarker(markerOptions);

            departureMarker.showInfoWindow();
            departureRadius = googleMap.addCircle(new CircleOptions()
                    .center(markerOptions.getPosition())
                    .radius(perimeter * 1600)
                    .strokeColor(Color.rgb(15, 94, 135))
                    .fillColor(Color.argb(50, 42, 124, 157)));
        }

        animateCamera();
    }

    /**
     * Responsible for showing the destination point on the map as well as
     * drawing the radius around the desired location.
     * We must keep reference to the destination radius and destination marker objects to allow
     * their removal if the user changes the departure point from one location to another.
     **/
    protected void showDestinationPoint(MarkerOptions markerOptions, double perimeter)
    {
        if(destinationRadius != null)
        {
            destinationRadius.remove();
        }

        if(destinationMarker != null)
        {
            destinationMarker.remove();
            destinationMarker = null;
        }

        if(markerOptions != null)
        {
            destinationMarker = googleMap.addMarker(markerOptions);

            destinationMarker.showInfoWindow();
            destinationRadius = googleMap.addCircle(new CircleOptions()
                    .center(markerOptions.getPosition())
                    .radius(perimeter * 1600)
                    .strokeColor(Color.rgb(15, 94, 135))
                    .fillColor(Color.argb(50, 42, 124, 157)));
        }

        animateCamera();
    }

    /**
     * Responsible for animating the camera and taking all the points currently present on the map into consideration.
     * The LatLngBounds.Builder calculates the optimal zoom and position to accommodate all the markers present on the map.
     * Google Map then performs the animation with the help of the LatLngBounds.Builder object.
     **/
    protected void animateCamera()
    {
        int marker_count = 0;
        Marker marker = null;
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        if(departureMarker != null)
        {
            builder.include(departureMarker.getPosition());
            marker = departureMarker;
            marker_count += 1;
        }

        if(destinationMarker != null)
        {
            builder.include(destinationMarker.getPosition());
            marker = destinationMarker;
            marker_count += 1;
        }

        if(wayPoints != null)
        {
            for(WaypointHolder waypointHolder : wayPoints)
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

        if(marker_count == 1 || ( marker_count == 2 && departureMarker != null && destinationMarker != null && departureMarker.getTitle().equals(destinationMarker.getTitle())))
        {
            if(marker != null)
            {
                cameraUpdate = CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 12);
            }
        }
        else
        {
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);

            LatLngBounds bounds = builder.build();
            cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, metrics.widthPixels, (int)(metrics.heightPixels*0.75f), 0);
        }

        if(cameraUpdate != null)
        {
            googleMap.animateCamera(cameraUpdate);
        }
    }

    /**
     * If location services are available, we want to center the map on our most recently acquired location.
     **/
    protected void centerMapOnMyLocation() {
        if(locationClient.getLastLocation() != null)
        {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(locationClient.getLastLocation().getLatitude(), locationClient.getLastLocation().getLongitude()), 14));
        }
    }

    /**
     *
     **/
    protected void drawDrivingDirectionsOnMap(final GoogleMap map, final ArrayList<GeoAddress> geoAddresses, final OnDrivingDirectionsRetrrievedListener listener)
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
                    DisplayMetrics metrics = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(metrics);

                    map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, metrics.widthPixels, (int)(metrics.heightPixels*0.5f), 0));
                    listener.onDrivingDirectionsRetrieved();
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

    protected void getCurrentAddress(MarkerType markerType, Location location, double perimeter)
    {
        new GeocoderTask(this, this, markerType, perimeter).execute(new GeocoderParams(null, location));
    }

    @Override
    public void onConnected(Bundle bundle) {
        centerMapOnMyLocation();
    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onGeoCoderFinished(MarkerOptions address, MarkerType markerType, Double perimeter) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        locationClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        locationClient.disconnect();
    }

    protected class WaypointHolder {

        public Marker googleMapMarker;
        public GeoAddress geoAddress;

        public WaypointHolder() {
        }

        public void removeMarker()
        {
            if(googleMapMarker != null)
            {
                googleMapMarker.remove();
                googleMapMarker = null;
            }
        }
    }
}
