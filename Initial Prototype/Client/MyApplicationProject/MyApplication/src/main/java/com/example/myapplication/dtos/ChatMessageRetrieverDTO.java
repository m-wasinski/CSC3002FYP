package com.example.myapplication.dtos;

/**
 * Created by Michal on 18/01/14.
 */
public class ChatMessageRetrieverDTO {
    public int SenderId;
    public int RecipientId;

    public ChatMessageRetrieverDTO(int senderId, int recipientId)
    {
        this.SenderId = senderId;
        this.RecipientId = recipientId;
    }
}
