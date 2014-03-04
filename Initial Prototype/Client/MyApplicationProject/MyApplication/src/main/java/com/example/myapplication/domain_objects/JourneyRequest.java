package com.example.myapplication.domain_objects;

/**
 * Created by Michal on 01/01/14.
 */
public class JourneyRequest {
    private int JourneyRequestId;
    private User FromUser;
    private int JourneyId;
    private Journey Journey;

    private String Message;
    private boolean Read;
    private int Decision;
    private String SentOnDate;
    private String DecidedOnDate;

    public void setJourney(Journey journey) {
        Journey = journey;
    }

    public void setJourneyId(int journeyId) {
        JourneyId = journeyId;
    }

    public void setFromUser(User fromUser) {
        FromUser = fromUser;
    }

    public void setMessage(String message) {
        Message = message;
    }

    public void setDecision(int decision) {
        Decision = decision;
    }

    public int getJourneyRequestId() {
        return JourneyRequestId;
    }

    public String getDecidedOnDate() {
        return DecidedOnDate;
    }

    public boolean isRead() {
        return Read;
    }

    public String getMessage() {
        return Message;
    }

    public int getJourneyId() {
        return JourneyId;
    }

    public User getFromUser() {
        return FromUser;
    }

    public Journey getJourney() {
        return Journey;
    }

    public String getSentOnDate() {
        return SentOnDate;
    }

    public int getDecision() {
        return Decision;
    }


}
