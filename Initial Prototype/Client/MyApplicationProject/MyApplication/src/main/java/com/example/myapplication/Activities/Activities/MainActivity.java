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
import com.example.myapplication.network_tasks.WcfPostServiceTask;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.gson.reflect.TypeToken;
import java.util.UUID;

public class MainActivity extends BaseActivity implements WCFServiceCallback<User, Void>, GCMRegistrationCallback {

    private final String TAG = "Main Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);

        // Check device for Play Services APK.
        if (!checkPlayServices()) {
            // If this check succeeds, proceed with normal processing.
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setMessage("For optimal experience, please ensure that Google Play is installed and that your google account is set up on your device. You will not be able to receive notifications and instant messages in real time until Google Play is installed.")
                    .setCancelable(false)
                    .setTitle("Google Play is not available on this device.")
                    .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            appManager.setRegistrationId(null);
                            performAutoLogin();
                        }
                    });

            AlertDialog alert = builder.create();
            alert.show();
        }
        else //Google Play services available.
        {
            if(appManager.getRegistrationId() == null)
            {
                Log.i(TAG, "GCM Registration Id is empty, attempting to register device.");
                new GCMRegistrationTask(this, this).execute();
            }
            else
            {
                Log.i(TAG, "Current GCM registration id: "+ appManager.getRegistrationId());
                performAutoLogin();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    /**
     * Calls the WCF service and attempts to perform auto-login of the current user.
     * If auto-login succeeds, user is automatically transferred to their home screen.
     * Otherwise manual login activity is started to allow the user to log in by providing their username and password.
     **/
    private void performAutoLogin()
    {
        // Generate new random UUID for the duration of this session.
        this.appManager.setUUID(UUID.randomUUID().toString());
        Log.i(TAG, "New UUID generated, " + appManager.getUUID());

        new WcfPostServiceTask<Void>(this, getResources().getString(R.string.UserAutoLoginURL), null, new TypeToken<ServiceResponse<User>>() {}.getType(),
                appManager.getAuthorisationHeaders(), this).execute();
    }

    /**
     * WcfPostServiceTask callback function invoked when a response from the WCF service is retrieved.
     * If auto-login was successful, transfer the current user directly to their home page,
     * otherwise display the login page and ask the user to log in manually.
     **/
    @Override
    public void onServiceCallCompleted(ServiceResponse<User> serviceResponse, Void v)
    {
        this.appManager.login(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS ? serviceResponse.Result : null);

        Intent intent = new Intent(this, serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS ? HomeActivity.class : LoginActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        this.startActivity(intent);
        this.finish();
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices()
    {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS)
        {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
            {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
            return false;
        }
        return true;
    }

    /**
     * Called by the GCMRegistration task on completion of the GCM registration process.
     * @param registrationId
     */
    @Override
    public void onGCMRegistrationCompleted(String registrationId)
    {
        this.appManager.setRegistrationId(registrationId);
        if(registrationId == null)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setMessage("Could not register with Google Cloud Messaging. Instant Messenger features will not be available. The app will try to re-register at next launch.")
                    .setCancelable(false)
                    .setTitle("GCM unavailable.")
                    .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            performAutoLogin();
                        }
                    });

            AlertDialog alert = builder.create();
            alert.show();
        }
        else
        {
            Log.i(TAG, "GCM Registration completed, the new registration id is: " + registrationId);
            this.performAutoLogin();
        }
    }
}
