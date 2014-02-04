package com.example.myapplication.activities.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import com.example.myapplication.R;
import com.example.myapplication.activities.base.BaseActivity;
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.domain_objects.User;
import com.example.myapplication.interfaces.GCMRegistrationCallback;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.GCMRegistrationTask;
import com.example.myapplication.network_tasks.WCFServiceTask;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.gson.reflect.TypeToken;
import java.util.UUID;

public class MainActivity extends BaseActivity implements WCFServiceCallback<User, String> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.initialisationStepOne();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
            return false;
        }
        return true;
    }

    /**
     * WCFServiceTask callback function invoked when a response from the WCF service is retrieved.
     * If auto-login was successful, transfer the current user directly to their home page,
     * otherwise display the login page and ask the user to log in manually.
     */
    @Override
    public void onServiceCallCompleted(ServiceResponse<User> serviceResponse, String param) {
        findNDriveManager.setUser(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS ? serviceResponse.Result : null);
        Intent intent = new Intent(this, serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS ? HomeActivity.class : LoginActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    /**
     * Performs first part of the initialisation process.
     * Detects whether Google Play services are available on this device.
     * Displays a warning message in case Google Play services are not supported.
     */
    private void initialisationStepOne()
    {
        // Check device for Play Services APK.
        if (!checkPlayServices()) {
            // If this check succeeds, proceed with normal processing.
            Log.i(TAG, "Google Play is not available on this device.");
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setCancelable(false);
            alertDialog.setTitle("Google Play is not available on this device.");
            alertDialog.setMessage("For optimal experience, please ensure that Google Play is installed and that your google account is set up on your device. You will not be able to receive notifications and instant messages in real time until Google Play is installed.");
            alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    findNDriveManager.setRegistrationId("0");
                    initialisationStepTwo();
                }
            });
            alertDialog.show();


        }
        else
        {
            if(findNDriveManager.getRegistrationId().isEmpty())
            {
                Log.i(TAG, "GCM Registration Id is empty, attempting to register device.");
                GCMRegistrationTask registerGCMTask = new GCMRegistrationTask(new GCMRegistrationCallback() {
                    @Override
                    public void onGCMRegistrationCompleted(String registrationId) {
                        Log.i(TAG, "GCM Registration completed, the new registration id is: " + registrationId);
                        findNDriveManager.setRegistrationId(registrationId);
                        initialisationStepTwo();
                    }
                }, getApplicationContext());
                registerGCMTask.execute();
            }
            else
            {
                Log.i(TAG, "Current GCM registration id: "+ findNDriveManager.getRegistrationId());
                initialisationStepTwo();
            }
        }
    }

    private void initialisationStepTwo()
    {
        // Generate new random UUID for the duration of this session.
        findNDriveManager.setUUID(UUID.randomUUID().toString());
        Log.i(TAG, "New UUID generated, " + findNDriveManager.getUUID());

        //If session exists, attempt auto-login.
        if (!findNDriveManager.getSessionId().isEmpty())
        {
            new WCFServiceTask<String>(this, getResources().getString(R.string.UserAutoLoginURL), "", new TypeToken<ServiceResponse<User>>() {}.getType(),
                    findNDriveManager.getAuthorisationHeaders(), this).execute();
        }
        else
        {
            //If auto-login fails, start login activity and ask user to log in manually.
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
