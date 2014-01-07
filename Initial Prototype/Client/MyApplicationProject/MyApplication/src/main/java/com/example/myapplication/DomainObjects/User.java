package com.example.myapplication.DomainObjects;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Michal on 05/11/13.
 */
public class User {

    public int UserId;

    public String UserName;

    public String EmailAddress;

    public String FirstName;

    public String LastName;

    public int Gender;

    public String DateOfBirth;

    public Date DoB;

    public String GCMRegistrationID;

    public ArrayList<User> TravelBuddies;
    public User()
    {
    }
    public User(String userName, String email, String first, String last, long dob, int s)
    {
        UserName = userName;
        EmailAddress = email;
        FirstName = first;
        LastName = last;
        Gender = s;
        //long date = dob.getTime();
        //DateOfBirth = dob;
        DateOfBirth = "/Date("+dob+")/";
    }
}
