package com.example.myapplication.dtos;

/**
 * Created by Michal on 07/03/14.
 */
public class UserRetrieverDTO {
    private int RetrievingUserId;
    private int TargetUserId;

    public UserRetrieverDTO(int retrievingUserId, int targetUserId) {
        RetrievingUserId = retrievingUserId;
        TargetUserId = targetUserId;
    }
}
