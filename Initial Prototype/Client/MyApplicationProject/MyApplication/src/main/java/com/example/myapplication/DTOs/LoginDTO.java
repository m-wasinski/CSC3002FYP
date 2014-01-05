package com.example.myapplication.DTOs;

/**
 * Created by Michal on 11/11/13.
 */
public class LoginDTO {
    private String UserName;
    private String Password;
    private String GCMRegistrationID;

    public LoginDTO(String userName, String password, String registrationId)
    {
        UserName = userName;
        Password = password;
        GCMRegistrationID = registrationId;
    }
}
