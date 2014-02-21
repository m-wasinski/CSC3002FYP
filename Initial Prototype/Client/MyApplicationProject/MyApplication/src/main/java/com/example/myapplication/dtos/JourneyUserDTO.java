package com.example.myapplication.dtos;

/**
 * Created by Michal on 16/02/14.
 */
public class JourneyUserDTO {
    private int JourneyId;
    private int UserId;

    public JourneyUserDTO(int journeyId, int userId) {
        this.JourneyId = journeyId;
        this.UserId = userId;
    }
}
