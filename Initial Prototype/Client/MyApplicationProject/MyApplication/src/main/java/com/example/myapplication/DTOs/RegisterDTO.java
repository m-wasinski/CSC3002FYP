package com.example.myapplication.DTOs;

import com.example.myapplication.DomainObjects.User;

/**
 * Created by Michal on 05/11/13.
 */
public class RegisterDTO {

    public com.example.myapplication.DomainObjects.User GetUser(){return User;}

    private User User;
    private String Password;
    private String ConfirmedPassword;

    public RegisterDTO(User u, String p, String cp)
    {
        User = u;
        Password = p;
        ConfirmedPassword = cp;
    }
}
