package com.example.myapplication.experimental;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.example.myapplication.R;
import com.example.myapplication.domain_objects.GeoAddress;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;

/**
 * Created by Michal on 22/01/14.
 */
public class WaypointHolder {

    public Marker googleMapMarker;
    public GeoAddress geoAddress;

    public WaypointHolder() {
    }

    public void removeMarker()
    {
        if(this.googleMapMarker != null)
        {
            this.googleMapMarker.remove();
            this.googleMapMarker = null;
        }
    }
}
