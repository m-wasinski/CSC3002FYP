package com.example.myapplication.domain_objects;

/**
 * Created by Michal on 18/01/14.
 */
public class ChatMessage {

    public int ChatMessageId;
    public int SenderId;
    public int RecipientId;
    public String MessageBody;
    public String SentOnDate;
    public Boolean Read;
    public String RecipientUserName;
    public String SenderUserName;

    public ChatMessage(int senderId, int recipientId, String messageBody, String sentOnDate, Boolean read, String recipientUserName, String senderUserName)
    {
        this.SenderId = senderId;
        this.RecipientId = recipientId;
        this.MessageBody = messageBody;
        this.SentOnDate = sentOnDate;
        this.Read = read;
        this.RecipientUserName = recipientUserName;
        this.SenderUserName = senderUserName;
    }
}
