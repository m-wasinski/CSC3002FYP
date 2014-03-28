package com.example.myapplication.broadcast_receivers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.myapplication.R;
import com.example.myapplication.activities.activities.InstantMessengerActivity;
import com.example.myapplication.app_management.AppManager;
import com.example.myapplication.constants.IntentConstants;
import com.example.myapplication.domain_objects.ChatMessage;
import com.example.myapplication.interfaces.WCFImageRetrieved;
import com.example.myapplication.network_tasks.WcfPictureServiceTask;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Receiver triggered upon the arrival of a new chat message.
 * This is the default receiver for the app's built in instant messenger. Receiving a broadcast here means
 * that the relevant chat activity is not currently open as it would have cancelled the broadcast which as a result would not reach this receiver.
 * Here, we display the notification to alert the user of a new message.
 */
public class InstantMessengerReceiver extends BroadcastReceiver {

    public final String TAG = "Instant Messenger Receiver";

    /**
     * Called when a new broadcast is received.
     * @param context - Context passed in from GcmIntentService.
     * @param intent - Intent containing the message.
     */
    public void onReceive(final Context context, Intent intent) {

        final AppManager appManager = ((AppManager)context.getApplicationContext());
        final Bundle bundle = intent.getExtras();
        int pictureId = Integer.parseInt(bundle.getString("pictureId"));
        Log.i(TAG, String.valueOf(pictureId));

        //Retrieve the picture of the user who sent this message.
        if(pictureId != -1)
        {
            new WcfPictureServiceTask(appManager.getBitmapLruCache(), context.getResources().getString(R.string.GetProfilePictureURL),
                    pictureId, appManager.getAuthorisationHeaders(), new WCFImageRetrieved() {
                @Override
                public void onImageRetrieved(Bitmap bitmap) {
                      showNotification(context, bundle, appManager, bitmap);
                }
            }).execute();
        }else
        {
            showNotification(context, bundle, appManager, null);
        }


    }

    /***
     * Displays the notification in the top left corner of the device using Android's
     * native NotificationManager.
     * @param context - Context passed in from GcmIntentService.
     * @param bundle - Bundle containing the message.
     * @param appManager - Globally available appManager object.
     * @param bitmap - picture of the user who sent the message.
     */
    private void showNotification(Context context,Bundle bundle, AppManager appManager, Bitmap bitmap)
    {
        NotificationManager mNotificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        ChatMessage chatMessage = new Gson().fromJson(bundle.getString(IntentConstants.PAYLOAD) ,new TypeToken<ChatMessage>() {}.getType());
        int notificationId = Integer.parseInt(bundle.getString("collapsibleKey"));

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, InstantMessengerActivity.class)
                        .putExtras(bundle).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP), PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.logo)
                        .setLargeIcon(bitmap != null ? Bitmap.createScaledBitmap(bitmap, 128, 128, false) : BitmapFactory.decodeResource(context.getResources(), R.drawable.logo))
                        .setContentTitle("Message from " + chatMessage.getSenderUserName())
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(chatMessage.getMessageBody()))
                        .setContentText(chatMessage.getMessageBody());

        mBuilder.setContentIntent(contentIntent);
        Notification deviceNotification = mBuilder.build();
        deviceNotification.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;

        mNotificationManager.notify(notificationId, deviceNotification);

        if(appManager != null)
        {
            appManager.addNotificationId(notificationId);
        }
    }
}
