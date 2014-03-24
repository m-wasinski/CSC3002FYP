package com.example.myapplication.network_tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.example.myapplication.interfaces.GCMUnregistrationCallback;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.util.Random;

/**
 * Async Task which unregisters the user's device from the Google Cloud Messaging (GCM) service.
 *
 * This is currently not used anywhere within the app since unregistering a device should rarely be done.
 * Unregistering does not take place immediately but is rather a process which takes up to several hours to complete.
 * This causes even an unregistered device to still receive notifications.
 **/
public class GCMUnregisterTask extends AsyncTask<TextView, String, Boolean> {

    private GoogleCloudMessaging gcm;
    private Context context;
    private GCMUnregistrationCallback listener;
    private Random random;

    public GCMUnregisterTask(GCMUnregistrationCallback l, Context context)
    {
        listener = l;
        this.context = context;
        random = new Random();
    }

    @Override
    protected Boolean doInBackground(TextView... textViews) {

        int BACKOFF_MILLI_SECONDS = 2000;
        long backoff = BACKOFF_MILLI_SECONDS + random.nextInt(1000);

        if (gcm == null) {
            gcm = GoogleCloudMessaging.getInstance(context);
        }

        int MAX_ATTEMPTS = 5;
        for (int i = 1; i <= MAX_ATTEMPTS; i++) {

            try {
                gcm.unregister();
                break;
            } catch (IOException ex) {

                // If there is an error, don't just keep trying to register.
                // Require the user to click a button again, or perform
                // exponential back-off.
                Log.e("ERROR, COULD NOT REGISTER WITH GCM.", ex.getMessage());
                // increase backoff exponentially
                backoff *= 2;

                try {

                    Log.d("", "Sleeping for " + backoff + " ms before retry");
                    Thread.sleep(backoff);

                } catch (InterruptedException e1) {
                    // Activity finished before we complete - exit.
                    Log.d("", "Thread interrupted: abort remaining retries!");
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        listener.onGCMUnregistrationCompleted();
    }

}
