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
 * Created by Michal on 28/01/14.
 */
public class InstantMessengerReceiver extends BroadcastReceiver {

    public final String TAG = "Instant Messenger Receiver";

    public void onReceive(final Context context, Intent intent) {


        final AppManager appManager = ((AppManager)context.getApplicationContext());
        final Bundle bundle = intent.getExtras();
        int pictureId = Integer.parseInt(bundle.getString("pictureId"));
        Log.i(TAG, String.valueOf(pictureId));
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
