package com.example.myapplication.domain_objects;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by Michal on 07/01/14.
 */
public class JourneyMessage {

    public int JourneyMessageId;
    public int JourneyId;
    public int SenderId;
    public String SenderUsername;
    public String MessageBody;
    public String SentOnDate;
    public ArrayList<User> SeenBy;

    public JourneyMessage(int journeyId, String senderUsername, int senderId, String messageBody, String sentOnDate) {
        this.JourneyId = journeyId;
        this.SenderId = senderId;
        this.SenderUsername = senderUsername;
        this.MessageBody = messageBody;
        this.SentOnDate = sentOnDate;
    }
}
