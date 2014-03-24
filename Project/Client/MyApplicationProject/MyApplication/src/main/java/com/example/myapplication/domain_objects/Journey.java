package com.example.myapplication.domain_objects;

import java.util.ArrayList;

/**
 * Created by Michal on 06/11/13.
 */
public class Journey {

    private int JourneyId;
    public int getJourneyId()
    {
        return this.JourneyId;
    }

    private User Driver;
    private ArrayList<GeoAddress> GeoAddresses;

    public void setDriver(User driver) {
        Driver = driver;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public void setGeoAddresses(ArrayList<GeoAddress> geoAddresses) {
        GeoAddresses = geoAddresses;
    }

    public void setDateAndTimeOfDeparture(String dateAndTimeOfDeparture) {
        DateAndTimeOfDeparture = dateAndTimeOfDeparture;
    }

    public void setFee(double fee) {
        Fee = fee;
    }

    public void setAvailableSeats(int availableSeats) {
        AvailableSeats = availableSeats;
    }

    public void setSmokersAllowed(boolean smokersAllowed) {
        Smokers = smokersAllowed;
    }

    public void setVehicleType(int vehicleType) {
        VehicleType = vehicleType;
    }

    public void setPetsAllowed(boolean petsAllowed) {
        Pets = petsAllowed;
    }

    public void setPrivate(boolean aPrivate) {
        Private = aPrivate;
    }

    public void setPreferredPaymentMethod(String preferredPaymentMethod) {
        PreferredPaymentMethod = preferredPaymentMethod;
    }

    private String Description;
    private String DateAndTimeOfDeparture;
    private double Fee;

    public int getUnreadMessagesCount() {
        return UnreadMessagesCount;
    }

    public String getPreferredPaymentMethod() {
        return PreferredPaymentMethod;
    }

    public int getJourneyStatus() {
        return JourneyStatus;
    }

    public String getCreationDate() {
        return CreationDate;
    }

    public boolean isPrivate() {
        return Private;
    }

    public int getUnreadRequestsCount() {
        return UnreadRequestsCount;
    }

    public int getVehicleType() {
        return VehicleType;
    }

    public boolean arePetsAllowed() {
        return Pets;
    }

    public boolean areSmokersAllowed() {
        return Smokers;
    }

    public int getAvailableSeats() {
        return AvailableSeats;
    }

    public double getFee() {
        return Fee;
    }

    public String getDateAndTimeOfDeparture() {
        return DateAndTimeOfDeparture;
    }

    public String getDescription() {
        return Description;
    }

    public ArrayList<GeoAddress> getGeoAddresses() {
        return GeoAddresses;
    }

    public User getDriver() {
        return Driver;
    }

    private int AvailableSeats;
    private ArrayList<User> Participants;
    private boolean Smokers;
    private boolean Pets;
    private int VehicleType;
    private boolean Private;
    private int UnreadRequestsCount;
    private int JourneyStatus;
    private String CreationDate;
    private String PreferredPaymentMethod;
    private int UnreadMessagesCount;

    public Journey()
    {
    }
}
