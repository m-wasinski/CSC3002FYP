package com.example.myapplication.activities.base;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.example.myapplication.R;
import com.example.myapplication.activities.activities.HomeActivity;
import com.example.myapplication.app_management.AppManager;
import com.google.gson.Gson;

/**
 * Serves as a base activity for other activities providing the necessary variables.
 * This activity is only responsible for instantiation and initialisation of the variables shared by all the
 * activities which extend this class.
 **/
public class BaseActivity extends Activity {

    private AppManager appManager;
    private Gson gson;
    private ActionBar actionBar;
    private final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    protected AppManager getAppManager() {
        return appManager;
    }

    protected Gson getGson() {
        return gson;
    }

    public ActionBar getActionBar() {
        return actionBar;
    }

    protected int getPLAY_SERVICES_RESOLUTION_REQUEST() {
        return PLAY_SERVICES_RESOLUTION_REQUEST;
    }

    /**
     * Called when the Activity is first launched.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        appManager = ((AppManager)getApplication());
        gson = new Gson();
        actionBar = getActionBar();
        if(actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Called when user presses the back button on their device.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    /**
     * Called during the initialisation process to setup the Activity menu.
     * @param menu - menu object passed from the base activity.
     * @return
     */
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
            case android.R.id.home:
                finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
