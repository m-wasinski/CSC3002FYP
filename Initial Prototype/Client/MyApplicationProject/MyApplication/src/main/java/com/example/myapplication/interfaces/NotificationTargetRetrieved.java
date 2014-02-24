package com.example.myapplication.interfaces;

import android.content.Intent;

import com.example.myapplication.domain_objects.JourneyRequest;

/**
 * Created by Michal on 23/02/14.
 */
public interface NotificationTargetRetrieved {
    void onNotificationTargetRetrieved(Intent intent);
}
