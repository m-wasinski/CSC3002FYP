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

    public int DriverId;
    public User Driver;
    public ArrayList<GeoAddress> GeoAddresses;
    public String Description;
    public String DateAndTimeOfDeparture;
    public double Fee;
    public int AvailableSeats;
    public ArrayList<User> Participants;
    public boolean SmokersAllowed;
    public boolean PetsAllowed;
    public int VehicleType;
    public boolean Private;
    public int UnreadRequestsCount;
    public int JourneyStatus;
    public String CreationDate;
    public int PaymentOption;
    public String PreferredPaymentMethod;
    public int UnreadMessagesCount;

    public Journey()
    {
    }
}
