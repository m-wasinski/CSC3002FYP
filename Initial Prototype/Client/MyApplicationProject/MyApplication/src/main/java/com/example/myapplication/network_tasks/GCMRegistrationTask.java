package com.example.myapplication.network_tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.example.myapplication.constants.GcmConstants;
import com.example.myapplication.interfaces.GCMRegistrationCallback;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.util.Random;

/**
 * Created by Michal on 30/12/13.
 */
public class GCMRegistrationTask extends AsyncTask<TextView, String, Boolean> {

    private GoogleCloudMessaging gcm;
    private Context context;
    private GCMRegistrationCallback listener;
    private Random random;
    private String registrationId;

    public GCMRegistrationTask(GCMRegistrationCallback l, Context context)
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
                registrationId = gcm.register(GcmConstants.SENDER_ID);
                Log.e("ACQUIRED REGISTRATION ID", registrationId);
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
        listener.onGCMRegistrationCompleted(registrationId);
    }

}
