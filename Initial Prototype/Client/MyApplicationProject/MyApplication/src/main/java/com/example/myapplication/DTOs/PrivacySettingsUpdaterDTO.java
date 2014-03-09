package com.example.myapplication.dtos;

/**
 * DTO object used to update user's privacy settings.
 */
public class PrivacySettingsUpdaterDTO {

    private int UserId;
    private int EmailPrivacyLevel;
    private int GenderPrivacyLevel;
    private int DateOfBirthPrivacyLevel;
    private int PhoneNumberPrivacyLevel;
    private int RatingPrivacyLevel;
    private int JourneysPrivacyLevel;

    public PrivacySettingsUpdaterDTO(int userId, int journeysPrivacyLevel, int ratingPrivacyLevel, int phoneNumberPrivacyLevel, int dateOfBirthPrivacyLevel, int emailPrivacyLevel, int genderPrivacyLevel) {
        UserId = userId;
        JourneysPrivacyLevel = journeysPrivacyLevel;
        RatingPrivacyLevel = ratingPrivacyLevel;
        PhoneNumberPrivacyLevel = phoneNumberPrivacyLevel;
        DateOfBirthPrivacyLevel = dateOfBirthPrivacyLevel;
        EmailPrivacyLevel = emailPrivacyLevel;
        GenderPrivacyLevel = genderPrivacyLevel;
    }
}
