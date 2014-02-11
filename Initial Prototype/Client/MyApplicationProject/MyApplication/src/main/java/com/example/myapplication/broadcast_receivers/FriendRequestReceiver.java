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
import com.example.myapplication.activities.activities.ReceivedFriendRequestDialogActivity;
import com.example.myapplication.activities.activities.SendFriendRequestDialogActivity;
import com.example.myapplication.constants.IntentConstants;

/**
 * Created by Michal on 10/02/14.
 */
public class FriendRequestReceiver extends BroadcastReceiver {

    private final String TAG = this.getClass().getSimpleName();

    public void onReceive(Context context, Intent intent) {
        NotificationManager mNotificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        Bundle extras = intent.getExtras();

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, ReceivedFriendRequestDialogActivity.class)
                        .putExtra(IntentConstants.FRIEND_REQUEST, extras.getString(IntentConstants.MESSAGE)), PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.logo)
                        .setContentTitle("New friend request.")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(extras.getString(IntentConstants.CONTENT_TITLE)))
                        .setContentText(intent.getStringExtra(extras.getString(IntentConstants.CONTENT_TITLE)));


        mBuilder.setContentIntent(contentIntent);
        Notification notification = mBuilder.build();
        notification.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;

        mNotificationManager.notify(0,notification);
    }
}
