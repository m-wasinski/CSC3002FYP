package com.example.myapplication;
import java.util.Date;

/**
 * Created by Michal on 05/11/13.
 */
public class User {

    private String Email;

    private String FirstName;

    private String LastName;

    private int Gender;

    public User(String email, String first, String last, int s)
    {
        Email = email;
        FirstName = first;
        LastName = last;
        Gender = s;
    }
}
