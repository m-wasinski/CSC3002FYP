package com.example.myapplication.activities.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.activities.base.BaseMapActivity;
import com.example.myapplication.experimental.WaypointHolder;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

/**
 * Created by Michal on 22/01/14.
 */
public class OfferJourneyActivity extends BaseMapActivity {

    private LinearLayout throughPointsLayout;
    private LinearLayout contentLayout;
    private LinearLayout headerLayout;

    private TextView viaTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer_journey);
        actionBar.hide();
        waypointHolders = new ArrayList<WaypointHolder>();
        throughPointsLayout = (LinearLayout) findViewById(R.id.AddNewJourneyAdditionalDestinationLayout);
        contentLayout = (LinearLayout) findViewById(R.id.OfferJourneyStepOneContentLayout);
        headerLayout = (LinearLayout) findViewById(R.id.OfferJourneyStepOneActivityMinimizeLayout);
        headerLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return false;
            }
        });
        viaTextView = (TextView) findViewById(R.id.ActivityOfferJourneyViaEditText);

        Button addThroughPointButton = (Button) findViewById(R.id.ActivityOfferJourneyThroughPointButton);
        addThroughPointButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(waypointHolders.size() == 6)
                {
                    return;
                }

                final WaypointHolder waypointHolder = new WaypointHolder(throughPointsLayout, getApplicationContext(), waypointHolders);
                waypointHolder.initialise();
                viaTextView.setVisibility(View.VISIBLE);
                waypointHolder.addressEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View view, boolean b) {
                        if (!b) {
                                MarkerOptions markerOptions = getAddress(waypointHolder.addressEditText.getText().toString());
                                showWaypointOnMap(waypointHolder, markerOptions);
                        }
                    }
                });

                waypointHolder.addressEditText.setOnKeyListener(new View.OnKeyListener() {
                    @Override
                    public boolean onKey(View view, int i, KeyEvent keyEvent) {
                        if(i == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_UP){MarkerOptions markerOptions = getAddress(waypointHolder.addressEditText.getText().toString());
                                showWaypointOnMap(waypointHolder, markerOptions);
                                inputMethodManager.hideSoftInputFromWindow(waypointHolder.addressEditText.getWindowToken(), 0);
                        }
                        return false;
                    }
                });

                waypointHolder.closeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        waypointHolder.removeItself();
                        if(waypointHolders.size() == 0)
                        {
                            viaTextView.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });

        departureAddressEditText = (EditText) findViewById(R.id.ActivityOfferJourneyDepartureEditText);
        departureAddressEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if(i == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_UP){
                    MarkerOptions markerOptions = getAddress(departureAddressEditText.getText().toString());
                    showDeparturePoint(markerOptions);
                    inputMethodManager.hideSoftInputFromWindow(departureAddressEditText.getWindowToken(), 0);
                }
                return false;
            }
        });

        departureAddressEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    MarkerOptions markerOptions = getAddress(departureAddressEditText.getText().toString());
                    showDeparturePoint(markerOptions);
                }
            }
        });


        destinationAddressEditText = (EditText) findViewById(R.id.ActivityOfferJourneyDestinationEditText);
        destinationAddressEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    MarkerOptions markerOptions = getAddress(destinationAddressEditText.getText().toString());
                    showDestinationPoint(markerOptions);
                }
            }
        });

        destinationAddressEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if(i == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_UP){
                    MarkerOptions markerOptions = getAddress(destinationAddressEditText.getText().toString());
                    showDestinationPoint(markerOptions);
                    inputMethodManager.hideSoftInputFromWindow(destinationAddressEditText.getWindowToken(), 0);
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
    }

    private void initialiseMap() {

        if (googleMap == null) {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.OfferNewJourneyMapFragment)).getMap();

            if (googleMap == null) {
                Toast.makeText(this,
                        "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    private void showWaypointOnMap(WaypointHolder waypointHolder, MarkerOptions markerOptions)
    {
        waypointHolder.removeMarker();

        if(markerOptions != null)
        {
            waypointHolder.googleMapMarker = googleMap.addMarker(new MarkerOptions()
                    .position(markerOptions.getPosition())
                    .title(markerOptions.getTitle())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

            waypointHolder.googleMapMarker.showInfoWindow();
        }
        animateCamera();
    }
}
