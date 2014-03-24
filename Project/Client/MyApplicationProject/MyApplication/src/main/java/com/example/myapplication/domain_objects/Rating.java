package com.example.myapplication.domain_objects;

/**
 * Created by Michal on 19/02/14.
 */
public class Rating {

    private int RatingId;
    private int UserId;
    private User TargetUser;
    private User FromUser;
    private String LeftOnDate;
    private int Score;
    private String Feedback;
    private int RatingContext;

    public int getRatingId() {
        return RatingId;
    }

    public int getUserId() {
        return UserId;
    }

    public User getTargetUser() {
        return TargetUser;
    }

    public User getFromUser() {
        return FromUser;
    }

    public int getScore() {
        return Score;
    }

    public String getLeftOnDate() {
        return LeftOnDate;
    }

    public String getFeedback() {
        return Feedback;
    }

    public int getRatingContext() {
        return RatingContext;
    }
}
