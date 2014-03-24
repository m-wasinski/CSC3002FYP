package com.example.myapplication.dtos;

/**
 * Used to mark journey messages as read by the current user.
 */
public class JourneyMessageMarkerDTO {
    private int UserId;
    private int JourneyMessageId;

    public JourneyMessageMarkerDTO(int userId, int journeyMesssageId) {
        this.UserId = userId;
        this.JourneyMessageId = journeyMesssageId;
    }
}
