package com.example.myapplication.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.myapplication.R;
import com.example.myapplication.constants.BroadcastTypes;
import com.example.myapplication.constants.GcmNotificationTypes;
import com.example.myapplication.constants.IntentConstants;
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.domain_objects.Notification;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.experimental.FindNDriveManager;
import com.example.myapplication.notification_management.NotificationProcessor;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WCFServiceTask;
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

        FindNDriveManager findNDriveManager = ((FindNDriveManager)getApplication());

        if(findNDriveManager.hasAppBeenKilled())
        {
            findNDriveManager.logout(false, false);
            //TODO tell user they have a new home_activity_notification and ask them to log in.
            return;
        }

        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

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
                switch (requestType)
                {
                    case GcmNotificationTypes.NOTIFICATION_TICKLE:
                        sendBroadcast(refreshBroadcastIntent);
                        this.retrieveDeviceNotifications(findNDriveManager);
                        break;
                    case GcmNotificationTypes.CHAT_MESSAGE: //Chat instant message.
                        sendBroadcast(refreshBroadcastIntent);
                        instantMessageReceived(intent.getExtras().getString(IntentConstants.PAYLOAD));
                        break;
                    case GcmNotificationTypes.JOURNEY_CHAT_MESSAGE:
                        sendBroadcast(refreshBroadcastIntent);
                        journeyChatMessageReceived(intent.getExtras().getString(IntentConstants.PAYLOAD));
                        break;
                }
            }
        }

        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    /*
    * Notifies the Instant Message Broadcast receiver about a new message.
    */
    private void instantMessageReceived(String message)
    {
        Intent orderedBroadcastIntent = new Intent(BroadcastTypes.BROADCAST_INSTANT_MESSENGER);
        sendOrderedBroadcast(orderedBroadcastIntent.putExtra(IntentConstants.PAYLOAD, message), null);
    }

    /*
    * Notifies the Journey Request Receiver about a new journey request.
    */
    private void journeyRequestReceived(String header, String message)
    {
        Bundle extras = new Bundle();
        extras.putString(IntentConstants.PAYLOAD, message);
        extras.putString(IntentConstants.CONTENT_TITLE, header);

        Intent broadcastIntent = new Intent(BroadcastTypes.BROADCAST_JOURNEY_REQUEST);
        sendBroadcast(broadcastIntent.putExtras(extras), null);
    }

    private void journeyRequestReplyReceived(String header, String message)
    {
        Bundle extras = new Bundle();
        extras.putString(IntentConstants.PAYLOAD, message);
        extras.putString(IntentConstants.CONTENT_TITLE, header);

        Intent broadcastIntent = new Intent(BroadcastTypes.BROADCAST_JOURNEY_REQUEST_REPLY);
        sendBroadcast(broadcastIntent.putExtras(extras), null);
    }

    private void journeyChatMessageReceived(String message)
    {
        Intent orderedBroadcastIntent = new Intent(BroadcastTypes.BROADCAST_JOURNEY_MESSAGE);
        sendOrderedBroadcast(orderedBroadcastIntent.putExtra(IntentConstants.PAYLOAD, message), null);
    }

    private void retrieveDeviceNotifications(FindNDriveManager findNDriveManager)
    {
        new WCFServiceTask<Integer>(this, getResources().getString(R.string.GetDeviceNotificationsURL), findNDriveManager.getUser().UserId,
                new TypeToken<ServiceResponse<ArrayList<Notification>>>() {}.getType(),
                findNDriveManager.getAuthorisationHeaders(), new WCFServiceCallback<ArrayList<Notification>, Void>() {
            @Override
            public void onServiceCallCompleted(ServiceResponse<ArrayList<Notification>> serviceResponse, Void parameter) {
                if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
                {
                    deviceNotificationsRetrieved(serviceResponse.Result);
                }
            }
        }).execute();
    }

    private void deviceNotificationsRetrieved(ArrayList<Notification> notifications)
    {
        Log.d(TAG, "Retrieved: "+notifications.size() + " new notifications.");
        NotificationProcessor.DisplayNotification(this, notifications);
    }
}
