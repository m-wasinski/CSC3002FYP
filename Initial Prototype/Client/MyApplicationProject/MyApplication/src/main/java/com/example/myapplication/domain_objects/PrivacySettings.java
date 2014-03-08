package com.example.myapplication.domain_objects;

/**
 * Created by Michal on 06/03/14.
 */
public class PrivacySettings
{
    private int EmailPrivacyLevel;
    private int GenderPrivacyLevel;
    private int DateOfBirthPrivacyLevel;
    private int PhoneNumberPrivacyLevel;
    private int RatingPrivacyLevel;
    private int JourneysPrivacyLevel;

    public int getJourneysPrivacyLevel() {
        return JourneysPrivacyLevel;
    }

    public int getRatingPrivacyLevel() {
        return RatingPrivacyLevel;
    }

    public int getPhoneNumberPrivacyLevel() {
        return PhoneNumberPrivacyLevel;
    }

    public int getDateOfBirthPrivacyLevel() {
        return DateOfBirthPrivacyLevel;
    }

    public int getGenderPrivacyLevel() {
        return GenderPrivacyLevel;
    }

    public int getEmailPrivacyLevel() {
        return EmailPrivacyLevel;
    }

    public PrivacySettings(int emailPrivacyLevel, int genderPrivacyLevel, int dateOfBirthPrivacyLevel, int phoneNumberPrivacyLevel, int ratingPrivacyLevel, int journeysPrivacyLevel) {
        EmailPrivacyLevel = emailPrivacyLevel;
        GenderPrivacyLevel = genderPrivacyLevel;
        DateOfBirthPrivacyLevel = dateOfBirthPrivacyLevel;
        PhoneNumberPrivacyLevel = phoneNumberPrivacyLevel;
        RatingPrivacyLevel = ratingPrivacyLevel;
        JourneysPrivacyLevel = journeysPrivacyLevel;
    }
}
