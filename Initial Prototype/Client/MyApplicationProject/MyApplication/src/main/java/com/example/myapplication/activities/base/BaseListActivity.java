package com.example.myapplication.activities.base;

import android.app.ActionBar;
import android.app.ListActivity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;

import com.example.myapplication.experimental.FindNDriveManager;
import com.google.gson.Gson;

/**
 * Created by Michal on 18/01/14.
 */
public class BaseListActivity extends ListActivity {
    protected FindNDriveManager findNDriveManager;
    protected Gson gson;
    protected ActionBar actionBar;
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        findNDriveManager = ((FindNDriveManager)getApplication());
        gson = new Gson();
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        actionBar = getActionBar();
        if(actionBar != null)
        {
            //actionBar.setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#D9222930")));
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#B3151515")));
        }
    }
}
