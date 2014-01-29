package com.example.myapplication.gcm;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.myapplication.R;
import com.example.myapplication.activities.activities.InstantMessengerActivity;
import com.example.myapplication.activities.activities.LoginActivity;
import com.example.myapplication.constants.GcmConstants;
import com.example.myapplication.constants.IntentConstants;
import com.example.myapplication.constants.SessionConstants;
import com.example.myapplication.constants.SharedPreferencesConstants;
import com.example.myapplication.dtos.ChatMessage;
import com.example.myapplication.dtos.ServiceResponse;
import com.example.myapplication.experimental.FindNDriveManager;
import com.example.myapplication.experimental.InstantMessengerReceiver;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Michal on 30/12/13.
 */
public class GcmIntentService extends IntentService {

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        FindNDriveManager findNDriveManager = ((FindNDriveManager)getApplication());

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
               // sendNotification("Send error: " + intent.getExtras().getString("contentTitle"), intent.getExtras().getString("message"));
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_DELETED.equals(messageType)) {
                //sendNotification("Deleted messages on server: " +
                //        intent.getExtras().getString("contentTitle"), intent.getExtras().getString("message"));
                // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType))
            {
                int requestType = Integer.parseInt(intent.getExtras().getString(IntentConstants.NOTIFICATION_TYPE));

                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction(GcmConstants.BROADCAST_ACTION_REFRESH);

                switch (requestType)
                {
                    case GcmConstants.NOTIFICATION_REFRESH:
                        sendBroadcast(broadcastIntent);
                        break;
                    case GcmConstants.NOTIFICATION_INSTANT_MESSENGER: //Chat instant message.
                        sendBroadcast(broadcastIntent);
                        instantMessageReceived(intent.getExtras().getString(IntentConstants.MESSAGE));
                        break;
                    case GcmConstants.NOTIFICATION_LOGOUT: //Force logout, user must have logged on somewhere using a different device.
                        findNDriveManager.logout(true, true);
                        break;
                }
            }
        }

        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void instantMessageReceived(String message)
    {
        Intent orderedBroadcastIntent = new Intent(GcmConstants.BROADCAST_INSTANT_MESSENGER);
        sendOrderedBroadcast(orderedBroadcastIntent.putExtra(IntentConstants.MESSAGE, message), null);
    }
}
