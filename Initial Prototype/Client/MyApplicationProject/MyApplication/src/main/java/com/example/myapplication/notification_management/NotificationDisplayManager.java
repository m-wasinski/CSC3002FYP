package com.example.myapplication.notification_management;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.example.myapplication.R;
import com.example.myapplication.broadcast_receivers.NotificationDeleteReceiver;
import com.example.myapplication.constants.BroadcastTypes;
import com.example.myapplication.constants.IntentConstants;
import com.example.myapplication.domain_objects.Notification;
import com.example.myapplication.experimental.FindNDriveManager;
import com.google.gson.Gson;

/**
 * Created by Michal on 15/02/14.
 */
public class NotificationDisplayManager {

    public NotificationDisplayManager()
    {
    }

    public void showNotification(FindNDriveManager findNDriveManager, Context context, Notification notification, Class c)
    {
        if(context == null)
        {
            return;
        }

        int notificationId = notification.CollapsibleKey == -1 ? (int) System.currentTimeMillis() : notification.CollapsibleKey;

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.logo)
                        .setContentTitle(notification.NotificationTitle)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(notification.NotificationMessage))
                        .setContentText(notification.NotificationMessage)
                        .setDeleteIntent(PendingIntent.getBroadcast(
                                context, 0, new Intent(context, NotificationDeleteReceiver.class).putExtra(IntentConstants.NOTIFICATION, new Gson().toJson(notification)).setAction(BroadcastTypes.BROADCAST_NOTIFICATION_DELETED), PendingIntent.FLAG_CANCEL_CURRENT));

        PendingIntent pendingIntent = null;

        if(c != null)
        {
             pendingIntent = PendingIntent.getActivity(context, notificationId,
                    new Intent(context, c)
                            .putExtra(IntentConstants.NOTIFICATION, new Gson().toJson(notification)), PendingIntent.FLAG_CANCEL_CURRENT);
        }

        if(pendingIntent != null)
        {
            builder.setContentIntent(pendingIntent);
        }
        //PendingIntent deleteIntent = PendingIntent.getBroadcast(
        //        context.getApplicationContext(), 0, new Intent(context.getApplicationContext(), NotificationDeleteReceiver.class).putExtra(IntentConstants.NOTIFICATION, new Gson().toJson(notification)), 0);
        //builder.setDeleteIntent(deleteIntent);
        android.app.Notification appNotification = builder.build();


        appNotification.flags = android.app.Notification.DEFAULT_LIGHTS | android.app.Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(notificationId, appNotification);
        findNDriveManager.addNotificationId(notificationId);
    }

}
