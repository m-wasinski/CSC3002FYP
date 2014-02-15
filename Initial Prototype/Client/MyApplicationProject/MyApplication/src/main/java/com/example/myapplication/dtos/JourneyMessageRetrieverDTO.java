package com.example.myapplication.dtos;

/**
 * Created by Michal on 12/02/14.
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
