package com.example.myapplication.broadcast_receivers;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import com.example.myapplication.R;
import com.example.myapplication.constants.IntentConstants;

/**
 * Created by Michal on 10/02/14.
 */
public class JourneyRequestReplyReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager mNotificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        Bundle extras = intent.getExtras();

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.logo)
                        .setContentTitle("Request " + extras.getString(IntentConstants.CONTENT_TITLE))
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(extras.getString(IntentConstants.PAYLOAD)))
                        .setContentText(intent.getStringExtra(extras.getString(IntentConstants.PAYLOAD)));

        Notification notification = mBuilder.build();
        notification.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;

        mNotificationManager.notify(0,notification);
    }
}
