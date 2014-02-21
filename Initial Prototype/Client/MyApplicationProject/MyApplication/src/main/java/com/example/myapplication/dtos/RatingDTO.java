package com.example.myapplication.dtos;

/**
 * Created by Michal on 21/02/14.
 */
public class RatingDTO {
    private int UserId;
    private int FromUserId;
    private int Score;
    private String Feedback;

    public RatingDTO(int userId, int score, int fromUserId, String feedback) {
        UserId = userId;
        Score = score;
        FromUserId = fromUserId;
        Feedback = feedback;
    }
}
