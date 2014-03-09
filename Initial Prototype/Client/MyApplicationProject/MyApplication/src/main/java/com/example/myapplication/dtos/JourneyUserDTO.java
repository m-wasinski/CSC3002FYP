package com.example.myapplication.dtos;

/**
 * DTO object used when a user withdraws from a journey or by the driver when they decide to cancel one of their journeys.
 */
public class JourneyUserDTO {
    private int JourneyId;
    private int UserId;

    public JourneyUserDTO(int journeyId, int userId) {
        this.JourneyId = journeyId;
        this.UserId = userId;
    }
}
