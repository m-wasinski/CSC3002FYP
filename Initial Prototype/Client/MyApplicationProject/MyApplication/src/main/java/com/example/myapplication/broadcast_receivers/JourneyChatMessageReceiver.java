package com.example.myapplication.broadcast_receivers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.example.myapplication.R;
import com.example.myapplication.activities.activities.JourneyChatActivity;
import com.example.myapplication.app_management.AppManager;

/**
 * Created by Michal on 12/02/14.
 */
public class JourneyChatMessageReceiver extends BroadcastReceiver {

    public final String TAG = this.getClass().getSimpleName();

    public void onReceive(Context context, Intent intent) {

        AppManager appManager = ((AppManager)context.getApplicationContext());

        Bundle bundle = intent.getExtras();

        int notificationId = Integer.parseInt(bundle.getString("collapsibleKey"));

        NotificationManager mNotificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(context, notificationId,
                new Intent(context, JourneyChatActivity.class)
                        .putExtras(bundle)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP), PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.logo)
                        .setContentTitle("New message in journey chat room.")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText("Click here to see it."))
                        .setContentText("Click here to see it.");

        mBuilder.setContentIntent(contentIntent);
        Notification deviceNotification = mBuilder.build();
        deviceNotification.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;

        mNotificationManager.notify(1, deviceNotification);

        if(appManager != null)
        {
            appManager.addNotificationId(notificationId);
        }
    }
}
