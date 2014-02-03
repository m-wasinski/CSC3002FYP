package com.example.myapplication.dtos;

import com.example.myapplication.domain_objects.Journey;

/**
 * Created by Michal on 30/01/14.
 */
public class JourneySearchDTO {
    public Journey Journey;
    public boolean SearchByDate;
    public boolean SearchByTime;
    public boolean Free;
    public double DepartureRadius;
    public double DestinationRadius;
}
