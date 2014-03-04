package com.example.myapplication.dtos;

import com.example.myapplication.domain_objects.GeoAddress;

import java.util.ArrayList;

/**
 * Created by Michal on 30/01/14.
 */
public class JourneySearchDTO {

    private int UserId;

    public int getUserId() {
        return UserId;
    }

    public void setUserId(int userId) {
        UserId = userId;
    }

    private boolean SearchByDate;
    private boolean SearchByTime;
    private double Fee;
    private double DestinationRadius;

    private int Smokers;
    private int Pets;
    private int VehicleType;
    private ArrayList<GeoAddress> GeoAddresses;

    private int DateAllowance;
    private int TimeAllowance;

    private String DateAndTimeOfDeparture;
    private double DepartureRadius;
    private String DepartureTime;
    private String DepartureDate;

    public void setDepartureTime(String departureTime) {
        DepartureTime = departureTime;
    }

    public void setDepartureDate(String departureDate) {
        DepartureDate = departureDate;
    }

    public String getDepartureTime() {

        return DepartureTime;
    }

    public String getDepartureDate() {
        return DepartureDate;
    }

    public void setDepartureRadius(double departureRadius) {
        DepartureRadius = departureRadius;
    }

    public String getDateAndTimeOfDeparture() {
       return DateAndTimeOfDeparture;
    }

    public void setTimeAllowance(int timeAllowance) {
        TimeAllowance = timeAllowance;
    }

    public void setDateAllowance(int dateAllowance) {
        DateAllowance = dateAllowance;
    }

    public void setDestinationRadius(double destinationRadius) {
        DestinationRadius = destinationRadius;
    }

    public void setGeoAddresses(ArrayList<GeoAddress> geoAddresses) {
        GeoAddresses = geoAddresses;
    }

    public void setSearchByDate(boolean searchByDate) {
        SearchByDate = searchByDate;
    }

    public void setFee(double free) {
        Fee = free;
    }

    public double getFee() {
        return Fee;
    }
    public void setSearchByTime(boolean searchByTime) {
        SearchByTime = searchByTime;
    }

    public void setDateAndTimeOfDeparture(String dateAndTimeOfDeparture) {
        DateAndTimeOfDeparture = dateAndTimeOfDeparture;
    }

    public int getTimeAllowance() {
        return TimeAllowance;
    }

    public int getDateAllowance() {
        return DateAllowance;
    }

    public ArrayList<GeoAddress> getGeoAddresses() {
        return GeoAddresses;
    }

    public double getDestinationRadius() {
        return DestinationRadius;
    }

    public boolean isSearchByTime() {
        return SearchByTime;
    }

    public boolean isSearchByDate() {
        return SearchByDate;
    }

    public double isFree() {
        return Fee;
    }

    public double getDepartureRadius() {
        return DepartureRadius;
    }

    public int getPets() {
        return Pets;
    }

    public int getSmokers() {
        return Smokers;
    }

    public int getVehicleType() {
        return VehicleType;
    }

    public void setSmokers(int smokers) {
        Smokers = smokers;
    }

    public void setPets(int pets) {
        Pets = pets;
    }

    public void setVehicleType(int vehicleType) {
        VehicleType = vehicleType;
    }


}
