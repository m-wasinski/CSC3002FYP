package com.example.myapplication.gcm;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.myapplication.R;
import com.example.myapplication.activities.activities.FriendListActivity;
import com.example.myapplication.constants.GcmConstants;
import com.example.myapplication.domain_objects.ChatMessage;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Created by Michal on 30/12/13.
 */
public class GcmIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
            if (GoogleCloudMessaging.
                    MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendNotification("Send error: " + intent.getExtras().getString("contentTitle"), intent.getExtras().getString("message"));
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification("Deleted messages on server: " +
                        intent.getExtras().getString("contentTitle"), intent.getExtras().getString("message"));
                // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                // This loop represents the service doing some work.
                for (int i=0; i<5; i++) {
                    Log.i("GCMIntentService", "Working... " + (i + 1)
                            + "/5 @ " + SystemClock.elapsedRealtime());
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                    }
                }
                Log.i("GCMIntentService", "Completed work @ " + SystemClock.elapsedRealtime());
                // Post notification of received message.
                //sendNotification("Received: " + extras.toString());

                int requestType = Integer.parseInt(intent.getExtras().getString("notificationType"));
                Intent broadcastIntent;
                switch (requestType)
                {
                    case GcmConstants.ACTION_REFRESH:
                        broadcastIntent = new Intent();
                        broadcastIntent.setAction(GcmConstants.PROPERTY_ACTION_REFRESH);
                        sendBroadcast(broadcastIntent);
                        sendNotification(intent.getExtras().getString("contentTitle"), intent.getExtras().getString("message"));
                        break;
                    case GcmConstants.ACTION_FORWARD_MESSAGE:
                        broadcastIntent = new Intent();

                        broadcastIntent.setAction(GcmConstants.PROPERTY_ACTION_REFRESH);

                        sendBroadcast(broadcastIntent);
                        ChatMessage chatMessage = new Gson().fromJson(intent.getExtras().getString("message"), new TypeToken<ChatMessage>() {}.getType());
                        sendNotification("New message from: " + chatMessage.SenderUserName, chatMessage.MessageBody);

                        Intent broadcastIntent2 = new Intent();
                        broadcastIntent2.setAction(GcmConstants.PROPERTY_FORWARD_MESSAGE);
                        broadcastIntent2.putExtra("message", intent.getExtras().getString("message"));
                        sendBroadcast(broadcastIntent2);
                        break;
                }

                Log.i("GCMIntentService", "Received: " + extras.toString());
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);

    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String title, String message) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, FriendListActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.logo)
                        .setContentTitle(title)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(message))
                        .setContentText(message);

        mBuilder.setContentIntent(contentIntent);


        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
