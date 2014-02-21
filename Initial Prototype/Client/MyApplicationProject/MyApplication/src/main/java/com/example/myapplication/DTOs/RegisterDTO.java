package com.example.myapplication.dtos;

import com.example.myapplication.domain_objects.User;

/**
 * Created by Michal on 05/11/13.
 */
public class RegisterDTO {
    private User User;
    private String Password;
    private String ConfirmedPassword;
    public RegisterDTO(String password, String confirmedPassword, User user) {
        ConfirmedPassword = confirmedPassword;
        Password = password;
        User = user;
    }
}
