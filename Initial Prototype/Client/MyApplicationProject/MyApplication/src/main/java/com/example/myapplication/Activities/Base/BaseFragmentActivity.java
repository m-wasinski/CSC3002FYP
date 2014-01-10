package com.example.myapplication.Activities.Base;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Window;
import android.widget.Toast;
import com.example.myapplication.Activities.Activities.LoginActivity;
import com.example.myapplication.Constants.ServiceResponseCode;
import com.example.myapplication.Experimental.AppData;

/**
 * Created by Michal on 05/01/14.
 */
public class BaseFragmentActivity extends FragmentActivity {

    protected ActionBar actionBar;
    protected AppData appData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        appData = ((AppData)getApplication());
        actionBar = getActionBar();
    }

    protected void checkIfAuthorised(int serviceResponseCode) {

        if(serviceResponseCode == ServiceResponseCode.UNAUTHORISED)
        {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            finish();
            startActivity(intent);
            Toast toast = Toast.makeText(this, "Your session has expired, you must log in again.", Toast.LENGTH_LONG);
            toast.show();
        }
    }
}
