package com.example.myapplication;

/**
 * Created by Michal on 11/11/13.
 */
public class LoginDTO {
    private String UserName;
    private String Password;

    public LoginDTO(String userName, String password)
    {
        UserName = userName;
        Password = password;
    }
}
