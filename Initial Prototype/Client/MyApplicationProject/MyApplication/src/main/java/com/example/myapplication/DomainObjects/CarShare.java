package com.example.myapplication.DomainObjects;

import java.util.List;

/**
 * Created by Michal on 06/11/13.
 */
public class CarShare {

    public int CarShareId;
    public int UserId;
    public User Driver;
    public String DepartureCity;
    public String DestinationCity;
    public String Description;
    public String DateOfDeparture;
    public String TimeOfDeparture;
    public double Fee;
    public int AvailableSeats;
    public List<User> Participants;
    public boolean SmokersAllowed;
    public boolean WomenOnly;
    public int VehicleType;

    public CarShare()
    {

    }
}
