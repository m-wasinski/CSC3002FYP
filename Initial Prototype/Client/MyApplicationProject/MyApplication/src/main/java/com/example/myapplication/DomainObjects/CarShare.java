package com.example.myapplication.DomainObjects;

import com.example.myapplication.Experimental.WCFDateTimeHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

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

    public String DateOfDepartureAsString()
    {
        Calendar _calendar = new GregorianCalendar();
        _calendar.setTime(WCFDateTimeHelper.parseWCFDateTimeString(DateAndTimeOfDeparture));
        SimpleDateFormat _dateFormat  = new SimpleDateFormat("dd-MMMM-yyyy");
        return _dateFormat.format(_calendar.getTime());
    }

    public Date DateOfDepartureAsDate()
    {
        return WCFDateTimeHelper.parseWCFDateTimeString(DateAndTimeOfDeparture);
    }

    public String TimeOfDepartureAsString()
    {
        Calendar _calendar = new GregorianCalendar();
        _calendar.setTime(WCFDateTimeHelper.parseWCFDateTimeString(DateAndTimeOfDeparture));
        SimpleDateFormat _dateFormat  = new SimpleDateFormat("HH:mm");
        _dateFormat.setTimeZone( TimeZone.getTimeZone("GMT") );
        return _dateFormat.format(_calendar.getTime());

        //_calendar.setTimeInMillis(Long.parseLong(WCFDateTimeHelper.GetStringFromEpoch(DateAndTimeOfDeparture)));
        //Log.e("TIME:", ""+_calendar.get(Calendar.HOUR_OF_DAY));
        //return ""+_calendar.get(Calendar.HOUR_OF_DAY);
    }

    public CarShare()
    {
    }
}
