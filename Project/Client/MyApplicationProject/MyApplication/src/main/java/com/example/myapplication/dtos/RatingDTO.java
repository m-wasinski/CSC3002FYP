package com.example.myapplication.dtos;

/**
 * Used when a user submits a new rating for driver of one of the journeys they participated in.
 */
public class RatingDTO {
    private int TargetUserId;
    private int FromUserId;
    private int Score;
    private String Feedback;

    public RatingDTO(int targetUserId, int score, int fromUserId, String feedback) {
        TargetUserId = targetUserId;
        Score = score;
        FromUserId = fromUserId;
        Feedback = feedback;
    }
}
