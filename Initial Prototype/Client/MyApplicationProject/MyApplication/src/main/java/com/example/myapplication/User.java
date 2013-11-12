package com.example.myapplication;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Michal on 05/11/13.
 */
public class User {

    public int GetId() {return Id;}

    public String GetUserName() {return UserName;}

    public String GetEmail() {return EmailAddress; }

    public String GetFirstName() {return FirstName;}

    public String GetLastName() {return LastName;}

    public String GetDateOfBirth() {return DateOfBirth;}

    private int Id;

    private String UserName;

    private String EmailAddress;

    private String FirstName;

    private String LastName;

    private int Gender;

    private String DateOfBirth;

    private Date DoB;

    public User(){}

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
