package com.example.myapplication.domain_objects;

/**
 * Created by Michal on 23/02/14.
 */
public class ProfilePicture
{
    private int ProfilePictureId;

    public String getProfilePictureData() {
        return ProfilePictureData;
    }

    public int getProfilePictureId() {
        return ProfilePictureId;
    }

    private String ProfilePictureData;

    public ProfilePicture(int profilePictureId, String profilePictureData) {
        ProfilePictureId = profilePictureId;
        ProfilePictureData = profilePictureData;
    }
}
