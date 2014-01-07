package com.example.myapplication.Activities.Activities;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import com.example.myapplication.Activities.Base.BaseActionBarActivity;
import com.example.myapplication.Constants.Constants;
import com.example.myapplication.Constants.ServiceResponseCode;
import com.example.myapplication.DomainObjects.ServiceResponse;
import com.example.myapplication.DomainObjects.User;
import com.example.myapplication.Helpers.ApplicationFileManager;
import com.example.myapplication.Interfaces.GCMRegistrationCallback;
import com.example.myapplication.Interfaces.WCFServiceCallback;
import com.example.myapplication.NetworkTasks.GCMRegistrationTask;
import com.example.myapplication.NetworkTasks.WCFServiceTask;
import com.example.myapplication.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.gson.reflect.TypeToken;

import java.util.UUID;

public class MainActivity extends BaseActionBarActivity implements WCFServiceCallback<User, String> {

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check device for Play Services APK.
        if (!checkPlayServices()) {
            // If this check succeeds, proceed with normal processing.
            // Otherwise, EXPLODE!
            Log.i(TAG, "Google Play is not supported on this device.");
            //TODO: FIX THIS
            finish();
        }

        if(appData.getRegistrationId().isEmpty())
        {
            Log.i(TAG, "GCM Registration Id is empty, attempting to register device.");
            GCMRegistrationTask registerGCMTask = new GCMRegistrationTask(new GCMRegistrationCallback() {
                @Override
                public void onGCMRegistrationCompleted(String registrationId) {
                    Log.i(TAG, "GCM Registration completed, the new registration id is: " + registrationId);
                    if(!registrationId.isEmpty())
                    {
                        storeRegistrationId(registrationId);
                    }
                }
            }, getApplicationContext());
            registerGCMTask.execute();
        }
        else
        {
            Log.i(TAG, "Current GCM registration id: "+ appData.getRegistrationId());
        }

        //Generate new random UUID for the duration of this session.
        appData.setUUID(UUID.randomUUID().toString());
        Log.i(TAG, "New UUID generated, " + appData.getUUID());

        setContentView(R.layout.activity_main);

        performStartupFileCheck();

        //If session exists, attempt auto-login.
        if (!appData.getSessionId().isEmpty())
        {
            new WCFServiceTask<String, User>("https://findndrive.no-ip.co.uk/Services/UserService.svc/autologin", "", new TypeToken<ServiceResponse<User>>() {}.getType(),
                    appData.getAuthorisationHeaders(), null, this).execute();
        }
        else
        {
            //If auto-login fails, start login activity and ask user to log in manually.
            try{ Thread.sleep(2000); }catch(InterruptedException e){ }
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void performStartupFileCheck()
    {
        ApplicationFileManager fileManager = new ApplicationFileManager();

        if (!fileManager.FolderExists())
        {
            fileManager.MakeApplicationDirectory();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param registrationId registration ID
     */
    private void storeRegistrationId(String registrationId) {
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.GLOBAL_APP_DATA, Context.MODE_PRIVATE);
        int appVersion = appData.getAppVersion();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.PROPERTY_REG_ID, registrationId);
        editor.putInt(Constants.PROPERTY_APP_VERSION, appVersion);
        editor.commit();
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
            } else {
                Log.i("", "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    public void onServiceCallCompleted(ServiceResponse<User> serviceResponse, String param) {
        if (serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
        {
            appData.setUser(serviceResponse.Result);
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);

        }
        else
        {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }

        finish();
    }
}
