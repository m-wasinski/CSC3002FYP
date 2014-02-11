package com.example.myapplication.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

import com.example.myapplication.constants.GcmConstants;
import com.example.myapplication.constants.IntentConstants;
import com.example.myapplication.experimental.FindNDriveManager;
import com.google.android.gms.gcm.GoogleCloudMessaging;

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

        if(findNDriveManager.hasAppBeenKilled())
        {
            findNDriveManager.logout(false, false);
            //TODO tell user they have a new notification and ask them to log in.
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
                    case GcmConstants.NOTIFICATION_JOURNEY_REQUEST_RECEIVED:
                        sendBroadcast(broadcastIntent);
                        journeyRequestReceived(intent.getExtras().getString(IntentConstants.CONTENT_TITLE), intent.getExtras().getString(IntentConstants.MESSAGE));
                        break;
                    case GcmConstants.NOTIFICATION_FRIEND_REQUEST:
                        friendRequestReceived(intent.getExtras().getString(IntentConstants.CONTENT_TITLE), intent.getExtras().getString(IntentConstants.MESSAGE));
                        break;
                    case GcmConstants.NOTIFICATION_JOURNEY_REQUEST_ACCEPTED:
                        journeyRequestReplyReceived(intent.getExtras().getString(IntentConstants.CONTENT_TITLE), intent.getExtras().getString(IntentConstants.MESSAGE));
                        break;
                    case GcmConstants.NOTIFICATION_JOURNEY_REQUEST_DENIED:
                        journeyRequestReplyReceived(intent.getExtras().getString(IntentConstants.CONTENT_TITLE), intent.getExtras().getString(IntentConstants.MESSAGE));
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
        Intent orderedBroadcastIntent = new Intent(GcmConstants.BROADCAST_INSTANT_MESSENGER);
        sendOrderedBroadcast(orderedBroadcastIntent.putExtra(IntentConstants.MESSAGE, message), null);
    }

    /*
    * Notifies the Journey Request Receiver about a new journey request.
    */
    private void journeyRequestReceived(String header, String message)
    {
        Bundle extras = new Bundle();
        extras.putString(IntentConstants.MESSAGE, message);
        extras.putString(IntentConstants.CONTENT_TITLE, header);

        Intent broadcastIntent = new Intent(GcmConstants.BROADCAST_JOURNEY_REQUEST);
        sendBroadcast(broadcastIntent.putExtras(extras), null);
    }

    /*
    * Notifies the Journey Request Receiver about a new journey request.
    */
    private void friendRequestReceived(String header, String message)
    {
        Bundle extras = new Bundle();
        extras.putString(IntentConstants.MESSAGE, message);
        extras.putString(IntentConstants.CONTENT_TITLE, header);

        Intent broadcastIntent = new Intent(GcmConstants.BROADCAST_FRIEND_REQUEST);
        sendBroadcast(broadcastIntent.putExtras(extras), null);
    }

    private void journeyRequestReplyReceived(String header, String message)
    {
        Bundle extras = new Bundle();
        extras.putString(IntentConstants.MESSAGE, message);
        extras.putString(IntentConstants.CONTENT_TITLE, header);

        Intent broadcastIntent = new Intent(GcmConstants.BROADCAST_JOURNEY_REQUEST_REPLY);
        sendBroadcast(broadcastIntent.putExtras(extras), null);
    }
}
