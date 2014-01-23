package com.example.myapplication.activities.base;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.widget.Toast;
import com.example.myapplication.activities.activities.LoginActivity;
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.constants.SharedPreferencesConstants;
import com.example.myapplication.dtos.ServiceResponse;
import com.example.myapplication.experimental.AppData;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WCFServiceTask;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Created by Michal on 05/01/14.
 */
public class BaseActivity extends Activity {

    protected AppData appData;
    protected Gson gson;
    protected ActionBar actionBar;
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appData = ((AppData)getApplication());
        gson = new Gson();
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        actionBar = getActionBar();
        if(actionBar != null)
        {
            //actionBar.setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#D9222930")));
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#B3151515")));
        }
    }

    protected void checkIfAuthorised(int serviceResponseCode) {

        if(serviceResponseCode == ServiceResponseCode.SERVER_ERROR)
        {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setCancelable(false);
            alertDialog.setMessage("Server error,, please try again later.");
            alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alertDialog.show();
        }

        if(serviceResponseCode == ServiceResponseCode.UNAUTHORISED)
        {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            finish();
            startActivity(intent);
            appData.setUser(null);
            Toast toast = Toast.makeText(this, "Your session has expired, you must log in again.", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    protected void exitApp(final boolean forceLogout)
    {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

        new WCFServiceTask<Boolean>("https://findndrive.no-ip.co.uk/Services/UserService.svc/logout",
                forceLogout, new TypeToken<ServiceResponse<Boolean>>(){}.getType(), appData.getAuthorisationHeaders(), new WCFServiceCallback<Boolean, Void>() {
            @Override
            public void onServiceCallCompleted(ServiceResponse<Boolean> serviceResponse, Void parameter) {

            }
        }).execute();

        this.finish();

        if(appData.getSessionId().endsWith("0") || forceLogout)
        {
            appData.setUser(null);
            appData.setSessionId("");
            SharedPreferences sharedPreferences = getSharedPreferences(SharedPreferencesConstants.GLOBAL_APP_DATA, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(SharedPreferencesConstants.PROPERTY_SESSION_ID, "");
            editor.commit();
            /*new GCMUnregisterTask(new GCMUnregistrationCallback() {
                @Override
                public void onGCMUnregistrationCompleted() {
                    Log.i("Base Activity: ", "Device successfully unregistered from GCM");

                }
            }, this).execute();*/
        }

        if(forceLogout)
        {
            startActivity(intent);
        }
    }
}
