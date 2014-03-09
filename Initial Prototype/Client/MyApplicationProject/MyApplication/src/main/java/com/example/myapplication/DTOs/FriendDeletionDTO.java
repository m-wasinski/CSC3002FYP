package com.example.myapplication.dtos;

/**
 * DTO object used when deleting a friend from user's list of friends.
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
