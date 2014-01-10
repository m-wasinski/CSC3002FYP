package com.example.myapplication.DomainObjects;

/**
 * Created by Michal on 09/01/14.
 */
public class GeoAddress {
    public double Latitude;
    public double Longitude;
    public String AddressLine;

    public GeoAddress(double Latitude, double Longitude, String AddressLine)
    {
        this.Latitude = Latitude;
        this.Longitude = Longitude;
        this.AddressLine = AddressLine;
    }
}
