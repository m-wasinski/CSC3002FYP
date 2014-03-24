package com.example.myapplication.dtos;

/**
 * DTO object used to mark a notification as delivered.
 */
public class NotificationMarkerDTO {
    private int NotificationId;
    private int UserId;

    public NotificationMarkerDTO(int userId, int notificationId) {
        this.UserId = userId;
        this.NotificationId = notificationId;
    }
}
