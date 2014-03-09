package com.example.myapplication.domain_objects;

import java.util.ArrayList;

/**
 * Represents a Journey Chat Room message object.
 */
public class JourneyMessage {

    private int JourneyMessageId;
    private int JourneyId;
    private int SenderId;
    private String SenderUsername;

    public String getMessageBody() {
        return MessageBody;
    }

    public ArrayList<User> getSeenBy() {
        return SeenBy;
    }

    public String getSentOnDate() {
        return SentOnDate;
    }

    public String getSenderUsername() {
        return SenderUsername;
    }

    public int getJourneyId() {
        return JourneyId;
    }

    public int getJourneyMessageId() {
        return JourneyMessageId;
    }

    public int getSenderId() {
        return SenderId;
    }

    private String MessageBody;
    private String SentOnDate;
    private ArrayList<User> SeenBy;

    public JourneyMessage(int journeyId, String senderUsername, int senderId, String messageBody, String sentOnDate) {
        this.JourneyId = journeyId;
        this.SenderId = senderId;
        this.SenderUsername = senderUsername;
        this.MessageBody = messageBody;
        this.SentOnDate = sentOnDate;
    }
}
