package com.example.myapplication.interfaces;

import com.example.myapplication.domain_objects.MarkerType;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by Michal on 04/02/14.
 */
public interface GeoCoderFinishedCallBack {
    void onGeoCoderFinished(MarkerOptions address, MarkerType markerType, double perimeter);
}
