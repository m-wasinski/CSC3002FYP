package com.example.myapplication.broadcast_receivers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.myapplication.R;
import com.example.myapplication.activities.activities.JourneyChatActivity;
import com.example.myapplication.constants.IntentConstants;
import com.example.myapplication.domain_objects.JourneyMessage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Created by Michal on 12/02/14.
 */
public class JourneyChatMessageReceiver extends BroadcastReceiver {

    public final String TAG = this.getClass().getSimpleName();

    public void onReceive(Context context, Intent intent) {
        NotificationManager mNotificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        Log.i(TAG, intent.getStringExtra(IntentConstants.PAYLOAD));

        JourneyMessage journeyMessage = new Gson().fromJson(intent.getStringExtra(IntentConstants.PAYLOAD), new TypeToken<JourneyMessage>() {}.getType());

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, JourneyChatActivity.class)
                        .putExtra(IntentConstants.JOURNEY, journeyMessage.JourneyId)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP), PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.logo)
                        .setContentTitle("New journey journey_chat message")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText("Click here to see it."))
                        .setContentText("Click here to see it.");

        mBuilder.setContentIntent(contentIntent);
        Notification notification = mBuilder.build();
        notification.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;

        mNotificationManager.notify(0, notification);
    }
}
