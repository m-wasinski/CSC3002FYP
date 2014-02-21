package com.example.myapplication.gcm;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.myapplication.R;
import com.example.myapplication.activities.activities.LoginActivity;
import com.example.myapplication.constants.BroadcastTypes;
import com.example.myapplication.constants.GcmNotificationTypes;
import com.example.myapplication.constants.IntentConstants;
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.domain_objects.Notification;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.experimental.FindNDriveManager;
import com.example.myapplication.network_tasks.WcfPostServiceTask;
import com.example.myapplication.notification_management.NotificationProcessor;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

/**
 * Created by Michal on 30/12/13.
 */
public class GcmIntentService extends IntentService {

    private final String TAG = this.getClass().getSimpleName();

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.d(TAG, "GCM Intent Service called.");

        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        FindNDriveManager findNDriveManager = ((FindNDriveManager)getApplication());

        if (!extras.isEmpty())
        {
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType))
            {
               // sendNotification("Send error: " + intent.getExtras().getString("contentTitle"), intent.getExtras().getString("message"));
            }
            else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType))
            {
                //sendNotification("Deleted messages on server: " +
                //        intent.getExtras().getString("contentTitle"), intent.getExtras().getString("message"));
                // If it's a regular GCM message, do some work.
            }
            else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType))
            {
                int requestType = Integer.parseInt(intent.getExtras().getString(IntentConstants.GCM_NOTIFICATION_TYPE));

                Intent refreshBroadcastIntent = new Intent();
                refreshBroadcastIntent.setAction(BroadcastTypes.BROADCAST_ACTION_REFRESH);
                Log.d(TAG, "GCM Notification Type: " + requestType);

                if(findNDriveManager.hasAppBeenKilled())
                {
                    displayAnonymousNotification(getApplicationContext(), findNDriveManager);
                    return;
                }

                switch (requestType)
                {
                    case GcmNotificationTypes.NOTIFICATION_TICKLE:
                        sendBroadcast(refreshBroadcastIntent);
                        this.retrieveDeviceNotifications(findNDriveManager);
                        break;
                    case GcmNotificationTypes.CHAT_MESSAGE: //Chat instant message.
                        sendBroadcast(refreshBroadcastIntent);
                        sendOrderedBroadcast(new Intent(BroadcastTypes.BROADCAST_INSTANT_MESSENGER).putExtras(intent.getExtras()), null);
                        break;
                    case GcmNotificationTypes.JOURNEY_CHAT_MESSAGE:
                        sendBroadcast(refreshBroadcastIntent);
                        sendOrderedBroadcast(new Intent(BroadcastTypes.BROADCAST_JOURNEY_MESSAGE).putExtras(intent.getExtras()), null);
                        break;
                }
            }
        }

        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void retrieveDeviceNotifications(final FindNDriveManager findNDriveManager)
    {
        new WcfPostServiceTask<Integer>(this, getResources().getString(R.string.GetDeviceNotificationsURL), findNDriveManager.getUser().getUserId(),
                new TypeToken<ServiceResponse<ArrayList<Notification>>>() {}.getType(),
                findNDriveManager.getAuthorisationHeaders(), new WCFServiceCallback<ArrayList<Notification>, Void>() {
            @Override
            public void onServiceCallCompleted(ServiceResponse<ArrayList<Notification>> serviceResponse, Void parameter) {
                if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
                {
                    deviceNotificationsRetrieved(serviceResponse.Result, findNDriveManager);
                }
            }
        }).execute();
    }

    private void deviceNotificationsRetrieved(ArrayList<Notification> notifications, FindNDriveManager findNDriveManager)
    {
        Log.d(TAG, "Retrieved: "+notifications.size() + " new notifications.");
        NotificationProcessor.DisplayNotification(this, findNDriveManager, notifications);
    }

    private void displayAnonymousNotification(Context context, FindNDriveManager findNDriveManager)
    {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.logo)
                        .setContentTitle("You have a new notification.")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText("Please log in to see it."))
                        .setContentText("Please log in to see it.");

        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

        builder.setContentIntent(PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT));
        android.app.Notification appNotification = builder.build();

        appNotification.flags = android.app.Notification.DEFAULT_LIGHTS | android.app.Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(0, appNotification);
        findNDriveManager.addNotificationId(0);
    }
}
