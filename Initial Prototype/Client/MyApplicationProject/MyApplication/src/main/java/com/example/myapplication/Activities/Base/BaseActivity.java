package com.example.myapplication.activities.base;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.myapplication.R;
import com.example.myapplication.activities.activities.HomeActivity;
import com.example.myapplication.experimental.FindNDriveManager;
import com.google.gson.Gson;

/**
 * Created by Michal on 05/01/14.
 */
public class BaseActivity extends Activity {

    protected FindNDriveManager findNDriveManager;
    protected Gson gson;
    protected ActionBar actionBar;
    protected final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    protected final String TAG = this.getClass().getSimpleName();

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        findNDriveManager = ((FindNDriveManager)getApplication());
        gson = new Gson();
        //getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        actionBar = getActionBar();
        if(actionBar != null)
        {
            //actionBar.setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#D9222930")));
            //actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#B3151515")));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.other_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.action_home:
                intent = new Intent(this, HomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                break;
            case R.id.logout_menu_option:
                findNDriveManager.logout(true, true);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return super.onOptionsItemSelected(item);
    }
}
