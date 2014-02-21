package com.example.myapplication.dtos;

/**
 * Created by Michal on 15/02/14.
 */
public class NotificationMarkerDTO {
    private int NotificationId;
    private int UserId;

    public NotificationMarkerDTO(int userId, int notificationId) {
        this.UserId = userId;
        this.NotificationId = notificationId;
    }
}
