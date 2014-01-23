package com.example.myapplication.dtos;

import java.util.ArrayList;

/**
 * Created by Michal on 06/11/13.
 */
public class Journey {

    public int JourneyId;
    public int DriverId;
    public User Driver;
    public ArrayList<GeoAddress> GeoAddresses;
    public String Description;
    public String DateAndTimeOfDeparture;
    public double Fee;
    public int AvailableSeats;
    public ArrayList<User> Participants;
    public boolean SmokersAllowed;
    public boolean WomenOnly;
    public boolean PetsAllowed;
    public int VehicleType;
    public boolean Private;
    public boolean SearchByDate;
    public boolean SearchByTime;
    public boolean Free;
    public ArrayList<JourneyRequest> Requests;
    public int UnreadRequestsCount;
    public int CarShareStatus;
    public ArrayList<JourneyMessage> Messages;
    public String CreationDate;

    public Journey()
    {
    }
}
