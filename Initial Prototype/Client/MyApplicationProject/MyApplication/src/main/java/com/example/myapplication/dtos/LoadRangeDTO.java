package com.example.myapplication.dtos;

/**
 * Created by Michal on 17/01/14.
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
