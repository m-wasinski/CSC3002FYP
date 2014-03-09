package com.example.myapplication.domain_objects;

/**
 * Created by Michal on 16/01/14.
 */
public class Notification {

    private int NotificationId;
    private int UserId;
    private int CollapsibleKey;
    private int NotificationType;
    private int NotificationContentType;
    private String NotificationTitle;
    private String NotificationMessage;
    private int ProfilePictureId;
    private String ReceivedOnDate;
    private Boolean Delivered;

    public void setDelivered(Boolean delivered) {
        Delivered = delivered;
    }

    private int TargetObjectId;

    public int getNotificationId() {
        return NotificationId;
    }

    public int getUserId() {
        return UserId;
    }

    public int getCollapsibleKey() {
        return CollapsibleKey;
    }

    public int getNotificationType() {
        return NotificationType;
    }

    public int getNotificationContentType() {
        return NotificationContentType;
    }

    public String getNotificationTitle() {
        return NotificationTitle;
    }

    public String getNotificationMessage() {
        return NotificationMessage;
    }

    public Boolean getDelivered() {
        return Delivered;
    }

    public int getTargetObjectId() {
        return TargetObjectId;
    }

    public String getReceivedOnDate() {
        return ReceivedOnDate;
    }

    public int getProfilePictureId() {
        return ProfilePictureId;
    }
}
