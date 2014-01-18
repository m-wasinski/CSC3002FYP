package com.example.myapplication.dtos;

/**
 * Created by Michal on 17/01/14.
 */
public class LoadRangeDTO {
    public int Id;
    public int Index;
    public int Count;
    public Boolean LoadMoreData;
    public LoadRangeDTO(int id, int index, int count, Boolean loadMoreData)
    {
        this.Id = id;
        this.Index = index;
        this.Count = count;
        this.LoadMoreData = loadMoreData;
    }
}
