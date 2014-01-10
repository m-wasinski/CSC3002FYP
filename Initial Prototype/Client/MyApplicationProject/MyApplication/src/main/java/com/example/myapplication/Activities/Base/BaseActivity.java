package com.example.myapplication.Activities.Base;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.widget.Toast;
import com.example.myapplication.Activities.Activities.LoginActivity;
import com.example.myapplication.Constants.ServiceResponseCode;
import com.example.myapplication.Experimental.AppData;
import com.google.gson.Gson;

/**
 * Created by Michal on 05/01/14.
 */
public class BaseActivity extends Activity {

    protected AppData appData;
    protected Gson gson;
    protected ProgressDialog progressDialog;
    protected ActionBar actionBar;
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appData = ((AppData)getApplication());
        gson = new Gson();
        actionBar = getActionBar();
        if(actionBar != null)
        {
            actionBar.setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#D9222930")));
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#B3222930")));
        }
    }

    protected void checkIfAuthorised(int serviceResponseCode) {

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
}
