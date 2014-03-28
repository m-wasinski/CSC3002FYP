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
 * Receiver triggered upon the arrival of a new message in one of the journey chat roons.
 * This is the default receiver for the app's built in chat room functionality.
 * Receiving a broadcast here means that the relevant chat activity is not
 * currently open as it would have cancelled the broadcast which as a result would not reach this receiver.
 * Here, we display the notification to alert the user of a new message.
 */
public class JourneyChatMessageReceiver extends BroadcastReceiver {

    public final String TAG = this.getClass().getSimpleName();

    /**
     * Called when a new broadcast is received.
     * @param context - Context passed in from GcmIntentService.
     * @param intent - Intent containing the message.
     */
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
