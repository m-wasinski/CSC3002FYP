package com.example.myapplication.activities.base;

import android.app.ActionBar;
import android.app.ListActivity;
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
public class BaseListActivity extends ListActivity
{
    protected AppManager appManager;
    protected Gson gson;
    protected ActionBar actionBar;

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Initialise local variables.
        this.appManager = ((AppManager)getApplication());
        this.gson = new Gson();
        this.actionBar = getActionBar();
        if(actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.other_menu, menu);
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
                appManager.logout(true, true);
                break;
            case android.R.id.home:
                finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
