package com.example.myapplication.dtos;

/**
 * DTO object used to retrieve another user's information from the web service.
 * We need the id's of both, the target and the requesting user to determine the relationship between them to protect the target user's privacy.
 */
public class UserRetrieverDTO {
    private int RetrievingUserId;
    private int TargetUserId;

    public UserRetrieverDTO(int retrievingUserId, int targetUserId) {
        RetrievingUserId = retrievingUserId;
        TargetUserId = targetUserId;
    }
}
