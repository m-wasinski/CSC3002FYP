package com.example.myapplication.DomainObjects;

import java.util.ArrayList;

/**
 * Created by Michal on 06/11/13.
 */
public class CarShare {

    public int CarShareId;
    public int DriverId;
    public User Driver;
    public String DepartureCity;
    public String DestinationCity;
    public String Description;
    public String DateOfDeparture;
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
    public ArrayList<CarShareRequest> Requests;
    public int UnreadRequestsCount;
    public int CarShareStatus;
    public ArrayList<CarShareMessage> Messages;
    public CarShare()
    {
    }
}
