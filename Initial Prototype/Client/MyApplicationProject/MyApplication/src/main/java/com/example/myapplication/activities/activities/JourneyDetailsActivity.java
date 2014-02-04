package com.example.myapplication.activities.activities;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.activities.base.BaseActivity;
import com.example.myapplication.constants.IntentConstants;
import com.example.myapplication.domain_objects.GeoAddress;
import com.example.myapplication.domain_objects.Journey;
import com.example.myapplication.experimental.GMapV2Direction;
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
import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by Michal on 04/02/14.
 */
public class JourneyDetailsActivity extends BaseActivity {

    private GoogleMap googleMap;

    private Journey journey;

    private MapFragment mapFragment;

    private TextView journeyIdTextView;
    private TextView journeyDriverTextView;
    private TextView journeyDateTextView;
    private TextView journeyTimeTextView;
    private TextView journeySeatsAvailableTextView;
    private TextView journeySmokersTextView;
    private TextView journeyPetsTextView;
    private TextView journeyFeeTextView;

    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alert_journey_details);

        // Initialise variables.
        Bundle extras = getIntent().getExtras();
        this.journey = gson.fromJson(extras.getString(IntentConstants.JOURNEY), new TypeToken<Journey>() {}.getType());
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int halfScreen = metrics.heightPixels/2;
        int thirdScreen = metrics.heightPixels/3;
        int fifthScreen = metrics.heightPixels/5;
        LinearLayout.LayoutParams layout_description = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, halfScreen);
        this.mapFragment = ((MapFragment)  getFragmentManager().findFragmentById(R.id.AlertJourneyDetailsMap));
        this.mapFragment.getView().setLayoutParams(layout_description);

        // Initialise UI elements.
        this.journeyIdTextView = (TextView) this.findViewById(R.id.JourneyDetailsActivityJourneyIdTextView);
        this.journeyDriverTextView = (TextView) this.findViewById(R.id.JourneyDetailsActivityJourneyDriverTextView);
        this.journeyDateTextView = (TextView) this.findViewById(R.id.JourneyDetailsActivityJourneyDateTextView);
        this.journeyTimeTextView = (TextView) this.findViewById(R.id.JourneyDetailsActivityJourneyTimeTextView);
        this.journeySeatsAvailableTextView = (TextView) this.findViewById(R.id.JourneyDetailsActivityJourneySeatsTextView);
        this.journeyPetsTextView = (TextView) this.findViewById(R.id.JourneyDetailsActivityJourneyPetsTextView);
        this.journeySmokersTextView = (TextView) this.findViewById(R.id.JourneyDetailsActivityJourneySmokersTextView);
        this.journeyFeeTextView = (TextView) this.findViewById(R.id.JourneyDetailsActivityJourneyFeeTextView);
        this.scrollView = (ScrollView) this.findViewById(R.id.JourneyDetailsActivityScrollView);

        try {
            // Loading map
            this.initialiseMap();
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                drawDrivingDirectionsOnMap();
            }
        });
    }

    private void initialiseMap() {

        if (this.googleMap == null && this.mapFragment != null) {
            this.googleMap = this.mapFragment.getMap();

            if (googleMap == null) {
                Toast.makeText(this,
                        "Unable to initialise Google Maps, please check your network connection.", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    private void drawDrivingDirectionsOnMap()
    {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        for(GeoAddress geoAddress : journey.GeoAddresses)
        {
            googleMap.addMarker(new MarkerOptions().position(new LatLng(geoAddress.Latitude, geoAddress.Longitude)).title(geoAddress.AddressLine)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

            builder.include(new LatLng(geoAddress.Latitude, geoAddress.Longitude));
            builder.include(new LatLng(geoAddress.Latitude, geoAddress.Longitude));
        }

        int padding = 40;
        LatLngBounds bounds = builder.build();
        final CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);


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
                    googleMap.animateCamera(cameraUpdate);
                }
            }

            @Override
            protected Void doInBackground(GoogleMap... googleMaps) {
                gMapV2Direction = new GMapV2Direction();
                doc = gMapV2Direction.getDocument(journey.GeoAddresses, GMapV2Direction.MODE_DRIVING);
                return null;
            }
        }.execute(googleMap);
    }
}
