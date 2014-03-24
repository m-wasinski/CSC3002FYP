package com.example.myapplication.app_management;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

/**
 * An intent service whose responsibility is to send a broadcast to the below addresses.
 * This is to keep the connection between the device and the GCM
 * servers open or to restore it when the connection is broken.
 */
public class GcmHeartbeatService extends IntentService {

    public GcmHeartbeatService() {
        super("GcmHeartbeatService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.i(this.getClass().getSimpleName(), "Sending GCM heartbeat broadcast...");

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("com.google.android.intent.action.MCS_HEARTBEAT");

        Intent broadcastIntent2 = new Intent();
        broadcastIntent2.setAction("com.google.android.intent.action.GTALK_HEARTBEAT");

        sendBroadcast(broadcastIntent);
        sendBroadcast(broadcastIntent2);
    }
}
