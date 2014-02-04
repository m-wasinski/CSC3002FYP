package com.example.myapplication.experimental;

import android.location.Location;

/**
 * Created by Michal on 04/02/14.
 */
public class GeocoderParams {

    public String getAddress() {
        return this.address;
    }

    public Location getLocation() {
        return this.location;
    }

    private String address;
    private Location location;

    public GeocoderParams(String address, Location location)
    {
        this.address = address;
        this.location = location;
    }
}
