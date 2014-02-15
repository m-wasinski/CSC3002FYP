package com.example.myapplication.notification_management;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.myapplication.R;
import com.example.myapplication.activities.activities.JourneyRequestDialogActivity;
import com.example.myapplication.activities.activities.ReceivedFriendRequestDialogActivity;
import com.example.myapplication.constants.IntentConstants;
import com.example.myapplication.constants.NotificationContentTypes;
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.domain_objects.Notification;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.experimental.FindNDriveManager;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WCFServiceTask;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

/**
 * Created by Michal on 14/02/14.
 */
public class NotificationProcessor
{
    public static void DisplayNotification(Context context, ArrayList<Notification> notifications)
    {
        for(Notification notification : notifications)
        {
            int type = notification.NotificationContentType;

            DeviceNotificationManager deviceNotificationManager = new DeviceNotificationManager();

            Log.i("NotificationProcessor", "Received the following notification type: " + type);

            switch(type)
            {
                case NotificationContentTypes.NOTIFICATION_FRIEND_REQUEST_RECEIVED:
                    deviceNotificationManager.showNotification(context, notification, ReceivedFriendRequestDialogActivity.class);
                    break;
                case NotificationContentTypes.NOTIFICATION_FRIEND_OFFERED_NEW_JOURNEY:
                    break;
                case NotificationContentTypes.NOTIFICATION_FRIEND_REQUEST_ACCEPTED:
                    break;
                case NotificationContentTypes.NOTIFICATION_FRIEND_REQUEST_DENIED:
                    break;
                case NotificationContentTypes.NOTIFICATION_JOURNEY_REQUEST_RECEIVED:
                    break;
                case NotificationContentTypes.NOTIFICATION_JOURNEY_REQUEST_ACCEPTED:
                    break;
                case NotificationContentTypes.NOTIFICATION_JOURNEY_REQUEST_DENIED:
                    break;
            }
        }
    }

    public static Intent getIntent(Context context, Notification notification)
    {
        Gson gson = new Gson();

        switch (notification.NotificationContentType)
        {
            case NotificationContentTypes.NOTIFICATION_FRIEND_REQUEST_RECEIVED:
                return new Intent(context, ReceivedFriendRequestDialogActivity.class)
                        .putExtra(IntentConstants.NOTIFICATION, gson.toJson(notification));
            case NotificationContentTypes.NOTIFICATION_JOURNEY_REQUEST_RECEIVED:
                return new Intent(context, JourneyRequestDialogActivity.class)
                        .putExtra(IntentConstants.NOTIFICATION, gson.toJson(notification));
        }

        return null;
    }

    public static void MarkDelivered(Context context, FindNDriveManager findNDriveManager, Notification notification, final WCFServiceCallback<Boolean, Void> listener)
    {
        if(notification.Delivered)
        {
            return;
        }

        new WCFServiceTask<Integer>(context, context.getResources().getString(R.string.MarkNotificationDeliveredURL),notification.NotificationId,
                new TypeToken<ServiceResponse<Boolean>>() {}.getType(),
                findNDriveManager.getAuthorisationHeaders(), new WCFServiceCallback<Boolean, Void>() {
            @Override
            public void onServiceCallCompleted(ServiceResponse<Boolean> serviceResponse, Void parameter) {
                if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
                {
                    listener.onServiceCallCompleted(serviceResponse, parameter);
                }
            }
        }).execute();
    }
}
