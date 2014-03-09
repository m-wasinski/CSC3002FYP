package com.example.myapplication.dtos;

/**
 * DTO object used to update user's profile picture.
 * When sending to the WCF web service, picture is stored as a base64 String object.
 */
public class ProfilePictureUpdaterDTO {
    private int UserId;
    private String Picture;

    public ProfilePictureUpdaterDTO(int userId, String picture) {
        UserId = userId;
        Picture = picture;
    }
}
