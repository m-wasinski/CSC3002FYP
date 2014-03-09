package com.example.myapplication.dtos;

/**
 * DTO object used for retrieving instant chat messages.
 */
public class ChatMessageRetrieverDTO {

    private int SenderId;
    private int RecipientId;
    private LoadRangeDTO LoadRangeDTO;

    public ChatMessageRetrieverDTO(int senderId, int recipientId, LoadRangeDTO loadRangeDTO)
    {
        this.SenderId = senderId;
        this.RecipientId = recipientId;
        this.LoadRangeDTO = loadRangeDTO;
    }
}
