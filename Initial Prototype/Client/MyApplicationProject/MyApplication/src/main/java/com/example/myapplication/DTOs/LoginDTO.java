package com.example.myapplication.dtos;

/**
 * Created by Michal on 11/11/13.
 */
public class LoginDTO {
    private String UserName;
    private String Password;
    private String GCMRegistrationID;

    public LoginDTO(String userName, String password, String GCMRegistrationID) {
        this.UserName = userName;
        this.GCMRegistrationID = GCMRegistrationID;
        this.Password = password;
    }
}
