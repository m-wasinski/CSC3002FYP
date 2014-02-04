package com.example.myapplication.interfaces;

import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by Michal on 04/02/14.
 */
public interface GeoCoderFinishedCallBack {
    void onGeoCoderFinished(MarkerOptions address, Boolean isDeparture, double perimeter);
}
