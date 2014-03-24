package com.example.myapplication.domain_objects;

/**
 * Created by Michal on 09/01/14.
 */
public class GeoAddress
{
    private double Latitude;
    private double Longitude;
    private int Order;
    private int JourneyId;

    public String getAddressLine() {
        return AddressLine;
    }

    public int getJourneyId() {
        return JourneyId;
    }

    public int getOrder() {
        return Order;
    }

    public double getLatitude() {
        return Latitude;
    }

    public double getLongitude() {
        return Longitude;
    }

    private String AddressLine;

    public GeoAddress(double Latitude, double Longitude, String AddressLine, int order)
    {
        this.Latitude = Latitude;
        this.Longitude = Longitude;
        this.AddressLine = AddressLine;
        this.Order = order;
    }
}
