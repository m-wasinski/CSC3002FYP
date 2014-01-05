package com.example.myapplication.Activities;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.myapplication.Adapters.TabsPagerAdapter;
import com.example.myapplication.Constants.Constants;
import com.example.myapplication.DomainObjects.ServiceResponse;
import com.example.myapplication.DomainObjects.User;
import com.example.myapplication.Experimental.AppData;
import com.example.myapplication.Helpers.ServiceHelpers;
import com.example.myapplication.Interfaces.UserHomeActivity;
import com.example.myapplication.R;

/**
 * Created by Michal on 13/11/13.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class HomeActivity extends FragmentActivity implements UserHomeActivity<User>{

    // action bar
    private ActionBar actionBar;
    private User user;
    private AppData appData;


    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.home_activity);

        appData = ((AppData)getApplication());
        user = appData.getUser();

        actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Initilization
        final ViewPager viewPager = (ViewPager) findViewById(R.id.Pager);
        TabsPagerAdapter mAdapter = new TabsPagerAdapter(getSupportFragmentManager());

        viewPager.setAdapter(mAdapter);
        actionBar.setHomeButtonEnabled(false);

        actionBar.addTab(actionBar.newTab().setText(Constants.JOURNEYS_TAB).setTabListener(new ActionBar.TabListener() {
            @Override
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

            }

            @Override
            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

            }
        }));

        actionBar.addTab(actionBar.newTab().setText(Constants.SEARCH).setTabListener(new ActionBar.TabListener() {
            @Override
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

            }

            @Override
            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

            }
        }));

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                // on changing the page
                // make respected tab selected
                actionBar.setSelectedNavigationItem(position);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });

        if(savedInstanceState != null) {
            int index = savedInstanceState.getInt("index");
            actionBar.setSelectedNavigationItem(index);
        }
    }

    @Override
    public void onBackPressed() {
        ExitApp(false);
    }

    private void ExitApp(boolean forceLogout)
    {
        ServiceHelpers.LogoutUser(this, forceLogout);
        finish();

        if(forceLogout)
        {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        int i = actionBar.getSelectedNavigationIndex();
        outState.putInt("index", i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.main, menu);

        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.LogoutMenuOption:
                ExitApp(true);
                break;
            case R.id.action_add_new_car_share:
                 intent = new Intent(this, PostNewCarShareActivity.class);
                startActivity(intent);
                break;
            case R.id.TravelBuddiesMenuOption:
                intent = new Intent(this, TravelBuddyListActivity.class);
                startActivity(intent);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void OnLogoutCompleted(ServiceResponse<Boolean> serviceResponse) {

    }
}
