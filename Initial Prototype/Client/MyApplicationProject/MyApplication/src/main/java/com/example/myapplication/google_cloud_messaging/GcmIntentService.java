package com.example.myapplication.google_cloud_messaging;

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
import com.example.myapplication.app_management.AppManager;
import com.example.myapplication.constants.BroadcastTypes;
import com.example.myapplication.constants.GcmNotificationTypes;
import com.example.myapplication.constants.IntentConstants;
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.domain_objects.Notification;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.network_tasks.WcfPostServiceTask;
import com.example.myapplication.notification_management.NotificationProcessor;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

/**
 * Service called by the GcmBroadcastReceiver, responsible for processing GCM notifications.
 */
public class GcmIntentService extends IntentService {

    private final String TAG = "GCM Intent Service.";

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

        AppManager appManager = ((AppManager)getApplication());

        if (!extras.isEmpty())
        {
            if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType))
            {
                int requestType = Integer.parseInt(intent.getExtras().getString(IntentConstants.GCM_NOTIFICATION_TYPE));

                Intent refreshBroadcastIntent = new Intent();
                refreshBroadcastIntent.setAction(BroadcastTypes.BROADCAST_ACTION_REFRESH);
                Log.d(TAG, "GCM Notification Type: " + requestType);

                if(appManager.hasAppBeenKilled())
                {
                    displayAnonymousNotification(getApplicationContext(), appManager);
                    return;
                }

                switch (requestType)
                {
                    case GcmNotificationTypes.NOTIFICATION_TICKLE:
                        sendBroadcast(refreshBroadcastIntent);
                        this.retrieveDeviceNotifications(appManager);
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

    private void retrieveDeviceNotifications(final AppManager appManager)
    {
        new WcfPostServiceTask<Integer>(this, getResources().getString(R.string.GetDeviceNotificationsURL), appManager.getUser().getUserId(),
                new TypeToken<ServiceResponse<ArrayList<Notification>>>() {}.getType(),
                appManager.getAuthorisationHeaders(), new WCFServiceCallback<ArrayList<Notification>, Void>() {
            @Override
            public void onServiceCallCompleted(ServiceResponse<ArrayList<Notification>> serviceResponse, Void parameter) {
                if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
                {
                    deviceNotificationsRetrieved(serviceResponse.Result, appManager);
                }
            }
        }).execute();
    }

    private void deviceNotificationsRetrieved(ArrayList<Notification> notifications, AppManager appManager)
    {
        Log.d(TAG, "Retrieved: "+notifications.size() + " new notifications.");
        NotificationProcessor notificationProcessor = new NotificationProcessor();

        for(Notification notification : notifications)
        {
            notificationProcessor.process(this, appManager, notification, null);
        }
    }

    /**
     * This is exactly the reason why task killers should not be used in an Android app.
     *
     * If used suddenly kills the app, it will never get the chance to log out and set current user's status to offline.
     * This will unfortunately not prevent notifications from arriving, despite all app data being already wiped from the device.
     * We can detect this and display an anonymous notification asking the user to log in.
     * @param context
     * @param appManager
     */
    private void displayAnonymousNotification(Context context, AppManager appManager)
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
        appManager.addNotificationId(0);
    }
}
