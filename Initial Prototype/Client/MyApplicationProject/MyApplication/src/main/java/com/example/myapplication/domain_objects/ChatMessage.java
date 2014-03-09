package com.example.myapplication.domain_objects;

/**
 * Represents an instant chat message object.
 */
public class ChatMessage {

    private int ChatMessageId;
    private int SenderId;

    public int getChatMessageId() {
        return ChatMessageId;
    }

    public int getSenderId() {
        return SenderId;
    }

    public String getMessageBody() {
        return MessageBody;
    }

    public int getRecipientId() {
        return RecipientId;
    }

    public Boolean getRead() {
        return Read;
    }

    public String getRecipientUserName() {
        return RecipientUserName;
    }

    public String getSenderUserName() {
        return SenderUserName;
    }

    public String getSentOnDate() {
        return SentOnDate;
    }

    private int RecipientId;
    private String MessageBody;
    private String SentOnDate;
    private Boolean Read;
    private String RecipientUserName;
    private String SenderUserName;

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
