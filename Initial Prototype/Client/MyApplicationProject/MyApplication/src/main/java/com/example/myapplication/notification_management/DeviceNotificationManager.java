package com.example.myapplication.notification_management;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.example.myapplication.R;
import com.example.myapplication.activities.activities.ReceivedFriendRequestDialogActivity;
import com.example.myapplication.constants.IntentConstants;
import com.example.myapplication.domain_objects.Notification;
import com.google.gson.Gson;

/**
 * Created by Michal on 15/02/14.
 */
public class DeviceNotificationManager {

    public DeviceNotificationManager()
    {
    }

    public void showNotification(Context context, Notification notification, Class c)
    {
        if(context == null)
        {
            return;
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.logo)
                        .setContentTitle(notification.NotificationTitle)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(notification.NotificationMessage))
                        .setContentText(notification.NotificationMessage);

        PendingIntent pendingIntent = null;

        if(c != null)
        {
             pendingIntent = PendingIntent.getActivity(context, 0,
                    new Intent(context, c)
                            .putExtra(IntentConstants.NOTIFICATION, new Gson().toJson(notification)), PendingIntent.FLAG_CANCEL_CURRENT);
        }

        if(pendingIntent != null)
        {
            builder.setContentIntent(pendingIntent);
        }

        android.app.Notification appNotification = builder.build();
        appNotification.flags = android.app.Notification.DEFAULT_LIGHTS | android.app.Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(0, appNotification);
    }

}
