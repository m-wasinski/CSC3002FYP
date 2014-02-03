package com.example.myapplication.domain_objects;

/**
 * Created by Michal on 09/01/14.
 */
public class GeoAddress {
    public double Latitude;
    public double Longitude;
    public int Order;
    public String AddressLine;

    public GeoAddress(double Latitude, double Longitude, String AddressLine, int order)
    {
        this.Latitude = Latitude;
        this.Longitude = Longitude;
        this.AddressLine = AddressLine;
        this.Order = order;
    }
}
