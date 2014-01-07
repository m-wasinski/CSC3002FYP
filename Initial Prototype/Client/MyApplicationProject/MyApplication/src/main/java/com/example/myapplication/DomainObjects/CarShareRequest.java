package com.example.myapplication.DomainObjects;

import java.util.Date;

/**
 * Created by Michal on 01/01/14.
 */
public class CarShareRequest {
    public int CarShareRequestId;
    public Boolean AddToTravelBuddies;
    public int UserId;
    public User User;
    public int CarShareId;
    public CarShare CarShare;
    public String Message;
    public boolean Read;
    public int Decision;
    public String SentOnDate;
    public String DecidedOnDate;
    public Date SentOn;
    public Date DecidedOn;
}
