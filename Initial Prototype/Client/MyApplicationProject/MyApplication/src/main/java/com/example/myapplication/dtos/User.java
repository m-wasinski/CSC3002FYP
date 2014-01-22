package com.example.myapplication.dtos;

import java.util.ArrayList;

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

    public String GCMRegistrationID;

    public ArrayList<User> Friends;

    public int Status;

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
        DateOfBirth = "/Date("+dob+")/";
    }
}
