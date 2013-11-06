package com.example.myapplication;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Michal on 05/11/13.
 */
public class User {

    public String GetFirstName() {return FirstName;}

    public String GetDateOfBirth() {return DateOfBirth;}

    public String GetLastName() {return LastName;}


    private String Email;

    private String FirstName;

    private String LastName;

    private int Gender;

    private String DateOfBirth;

    private Date DoB;


    public User(String email, String first, String last, int s, long dob)
    {
        Email = email;
        FirstName = first;
        LastName = last;
        Gender = s;
        //long date = dob.getTime();
        //DateOfBirth = dob;
        DateOfBirth = "/Date("+dob+")/";
    }
}
