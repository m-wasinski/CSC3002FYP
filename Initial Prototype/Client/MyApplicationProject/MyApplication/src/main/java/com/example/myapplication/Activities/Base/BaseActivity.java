package com.example.myapplication.activities.base;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.example.myapplication.R;
import com.example.myapplication.activities.activities.HomeActivity;
import com.example.myapplication.activities.activities.LeaderboardActivity;
import com.example.myapplication.app_management.AppManager;
import com.google.gson.Gson;

/**
 * Serves as a base activity for other activities providing the necessary variables.
 * This activity is only responsible for instantiation and initialisation of the variables shared by all the
 * activities which extend this class.
 **/
public class BaseActivity extends Activity {

    protected AppManager appManager;
    protected Gson gson;
    protected ActionBar actionBar;
    protected final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

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
        appManager = ((AppManager)getApplication());
        gson = new Gson();
        actionBar = getActionBar();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.other_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_home:
                startActivity(new Intent(this, HomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
                break;
            case R.id.logout_menu_option:
                appManager.logout(true, true);
                break;
            case R.id.action_show_leaderboard:
                startActivity(new Intent(this, LeaderboardActivity.class));
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return super.onOptionsItemSelected(item);
    }
}
