package com.example.myapplication.dtos;

/**
 * DTO object used to log a user in.
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
