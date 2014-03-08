package com.example.myapplication.dtos;

/**
 * Created by Michal on 08/03/14.
 */
public class FriendDeletionDTO
{
    private int UserId;
    private int FriendId;

    public FriendDeletionDTO(int userId, int friendId) {
        UserId = userId;
        FriendId = friendId;
    }
}
