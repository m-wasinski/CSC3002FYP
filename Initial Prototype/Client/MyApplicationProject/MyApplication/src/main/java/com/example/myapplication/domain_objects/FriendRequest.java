package com.example.myapplication.domain_objects;

/**
 * Created by Michal on 07/02/14.
 */
public class FriendRequest {

    public int FriendRequestId;
    public int TargetUserId;
    public int RequestingUserId;
    public String Message;
    public int FriendRequestDecision;
    public Boolean Read;
    public String SentOnDate;
    public String DecidedOnDate;
}
