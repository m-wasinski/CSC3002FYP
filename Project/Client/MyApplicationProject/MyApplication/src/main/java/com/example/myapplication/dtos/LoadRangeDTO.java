package com.example.myapplication.dtos;

/**
 * DTO object used in the process of loading items from the web service in increments.
 */
public class LoadRangeDTO {

    private int Id;
    private int Take;
    private int Skip;

    public LoadRangeDTO(int id, int skip, int take)
    {
        this.Id = id;
        this.Skip = skip;
        this.Take = take;
    }
}
