package com.example.myapplication.dtos;

import com.example.myapplication.domain_objects.User;

/**
 * DTO object used to create a new account within the system.
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
