package com.example.myapplication.dtos;

/**
 * DTO object used to retrieve a journey chat conversation history for a specific journey.
 */
public class JourneyMessageRetrieverDTO {
    public int JourneyId;
    public int UserId;
    LoadRangeDTO LoadRangeDTO;


    public JourneyMessageRetrieverDTO(int journeyId, int userId, LoadRangeDTO loadRangeDTO) {
        this.JourneyId = journeyId;
        this.UserId = userId;
        this.LoadRangeDTO = loadRangeDTO;
    }
}
