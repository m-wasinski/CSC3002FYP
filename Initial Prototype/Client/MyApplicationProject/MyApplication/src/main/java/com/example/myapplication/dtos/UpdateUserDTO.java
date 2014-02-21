package com.example.myapplication.dtos;

/**
 * Created by Michal on 19/02/14.
 */
public class UpdateUserDTO {

    private int UserId;
    private String EmailAddress;
    private int Gender;
    private String DateOfBirth;
    private String PhoneNumber;
    private String FirstName;
    private String LastName;

    public UpdateUserDTO(int userId, String firstName, String lastName, String emailAddress, int gender, String dateOfBirth, String phoneNumber) {
        EmailAddress = emailAddress;
        Gender = gender;
        DateOfBirth = dateOfBirth;
        PhoneNumber = phoneNumber;
        UserId = userId;
        FirstName = firstName;
        LastName = lastName;
    }
}
