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
import com.example.myapplication.activities.activities.InstantMessengerActivity;
import com.example.myapplication.constants.IntentConstants;
import com.example.myapplication.domain_objects.ChatMessage;
import com.example.myapplication.experimental.FindNDriveManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Created by Michal on 28/01/14.
 */
public class InstantMessengerReceiver extends BroadcastReceiver {

    public final String TAG = this.getClass().getSimpleName();

    public void onReceive(Context context, Intent intent) {

        FindNDriveManager findNDriveManager = ((FindNDriveManager)context.getApplicationContext());

        Bundle bundle = intent.getExtras();

        com.example.myapplication.domain_objects.Notification notification = new Gson().fromJson(bundle.getString(IntentConstants.NOTIFICATION),
                new TypeToken<com.example.myapplication.domain_objects.Notification>() {}.getType());

        int collapsibleKey = notification == null ? Integer.parseInt(bundle.getString("collapsibleKey")) : notification.CollapsibleKey;

        int notificationId = collapsibleKey == -1 ?  (int) System.currentTimeMillis() : collapsibleKey;

        NotificationManager mNotificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        ChatMessage chatMessage = new Gson().fromJson(bundle.getString(IntentConstants.PAYLOAD) ,new TypeToken<ChatMessage>() {}.getType());

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, InstantMessengerActivity.class)
                        .putExtras(bundle).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP), PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.logo)
                        .setContentTitle("Message from " + chatMessage.SenderUserName)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(chatMessage.MessageBody))
                        .setContentText(chatMessage.MessageBody);

        mBuilder.setContentIntent(contentIntent);
        Notification deviceNotification = mBuilder.build();
        deviceNotification.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;

        mNotificationManager.notify(notificationId, deviceNotification);

        if(findNDriveManager != null)
        {
            findNDriveManager.addNotificationId(notificationId);
        }
    }
}
