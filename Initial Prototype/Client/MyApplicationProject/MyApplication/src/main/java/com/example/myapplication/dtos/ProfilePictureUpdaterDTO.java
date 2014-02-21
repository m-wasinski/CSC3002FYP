package com.example.myapplication.dtos;

/**
 * Created by Michal on 20/02/14.
 */
public class ProfilePictureUpdaterDTO {
    private int UserId;
    private String Picture;

    public ProfilePictureUpdaterDTO(int userId, String picture) {
        UserId = userId;
        Picture = picture;
    }
}
