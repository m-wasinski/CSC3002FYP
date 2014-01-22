package com.example.myapplication.gcm;

import android.app.ActivityManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import com.example.myapplication.constants.SessionConstants;
import com.example.myapplication.constants.SharedPreferencesConstants;
import com.example.myapplication.dtos.ChatMessage;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                // This loop represents the service doing some work.
                /*for (int i=0; i<5; i++) {
                    Log.i("GCMIntentService", "Working... " + (i + 1)
                            + "/5 @ " + SystemClock.elapsedRealtime());
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                    }
                }*/
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
                        break;
                    case GcmConstants.ACTION_FORWARD_MESSAGE: //Chat instant message.
                        ChatMessage chatMessage = new Gson().fromJson(intent.getExtras().getString("message"), new TypeToken<ChatMessage>() {}.getType());
                        instantMessageReceived(chatMessage);
                        break;
                }
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void instantMessageReceived(ChatMessage chatMessage)
    {
        SharedPreferences sharedPreferences = getSharedPreferences(SharedPreferencesConstants.GLOBAL_APP_DATA, Context.MODE_PRIVATE);
        String sessionId = sharedPreferences.getString(SessionConstants.SESSION_ID, "");
        NotificationManager mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        if(sessionId.isEmpty())
        {
            PendingIntent contentIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(),
                    new Intent(this, LoginActivity.class), 0);

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.logo)
                            .setContentTitle("You have a new message")
                            .setStyle(new NotificationCompat.BigTextStyle().bigText(chatMessage.MessageBody))
                            .setContentText("Please login to see it.");

            mBuilder.setContentIntent(contentIntent);
            Notification notification = mBuilder.build();
            notification.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;

            mNotificationManager.notify(0, notification);
        }


        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(GcmConstants.PROPERTY_ACTION_REFRESH);
        sendBroadcast(broadcastIntent);

        Intent broadcastIntent2 = new Intent();
        broadcastIntent2.setAction(GcmConstants.PROPERTY_FORWARD_MESSAGE);
        broadcastIntent2.putExtra("message", new Gson().toJson(chatMessage));
        sendBroadcast(broadcastIntent2);



        ActivityManager am =(ActivityManager)getApplicationContext().getSystemService(ACTIVITY_SERVICE);

        // get the info from the currently running task
        List< ActivityManager.RunningTaskInfo > taskInfo = am.getRunningTasks(1);

        Log.d("topActivity", "CURRENT Activity ::"
                + taskInfo.get(0).topActivity.getClassName());

        ComponentName componentInfo = taskInfo.get(0).topActivity;
        //--- To get currently active activity in foreground. We can use getShortClassName() & getClassName()
        String classname =componentInfo.getClassName();
        //---get package name of currently running application in foreground. We can use getPackageName()
        String packagename =componentInfo.getPackageName();

        if(!classname.equals(InstantMessengerActivity.class.getName()))
        {
            int notificationId =  (int) System.currentTimeMillis();
            Bundle extras = new Bundle();
            extras.putString("RecipientUsername", chatMessage.SenderUserName);
            extras.putInt("RecipientId", chatMessage.SenderId);
            PendingIntent contentIntent = PendingIntent.getActivity(this, notificationId,
                    new Intent(this, InstantMessengerActivity.class)
                            .putExtras(extras).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP), 0);

             NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.logo)
                            .setContentTitle("Message from " + chatMessage.SenderUserName)
                            .setStyle(new NotificationCompat.BigTextStyle().bigText(chatMessage.MessageBody))
                            .setContentText(chatMessage.MessageBody);

            mBuilder.setContentIntent(contentIntent);
            Notification notification = mBuilder.build();
            notification.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;

            mNotificationManager.notify(notificationId, notification);
        }

    }
}
