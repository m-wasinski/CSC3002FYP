package com.example.myapplication.dtos;

/**
 * Created by Michal on 13/02/14.
 */
public class JourneyMessageMarkerDTO {
    public int UserId;
    public int JourneyMessageId;

    public JourneyMessageMarkerDTO(int userId, int journeyMesssageId) {
        this.UserId = userId;
        this.JourneyMessageId = journeyMesssageId;
    }
}
