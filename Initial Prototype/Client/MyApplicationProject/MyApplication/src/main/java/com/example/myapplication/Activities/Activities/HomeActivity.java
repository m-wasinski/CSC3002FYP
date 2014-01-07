package com.example.myapplication.Activities.Activities;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;

import com.example.myapplication.Activities.Base.BaseFragmentActivity;
import com.example.myapplication.Adapters.TabsPagerAdapter;
import com.example.myapplication.Constants.Constants;
import com.example.myapplication.DomainObjects.ServiceResponse;
import com.example.myapplication.Interfaces.WCFServiceCallback;
import com.example.myapplication.NetworkTasks.WCFServiceTask;
import com.example.myapplication.R;
import com.google.gson.reflect.TypeToken;

/**
 * Created by Michal on 13/11/13.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class HomeActivity extends BaseFragmentActivity implements WCFServiceCallback<Boolean, String> {

    private Boolean forceLogout;

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);

        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        actionBar.setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#F2222930")));
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#B3222930")));
        final ViewPager viewPager = (ViewPager) findViewById(R.id.Pager);
        viewPager.setBackgroundResource(R.drawable.background6);
        TypedValue tv = new TypedValue();
        getApplicationContext().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true);
        int actionBarHeight = getResources().getDimensionPixelSize(tv.resourceId);
        viewPager.setPadding(0, actionBarHeight*2+2, 0, 0);
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

        actionBar.addTab(actionBar.newTab().setText("My Requests").setTabListener(new ActionBar.TabListener() {
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
        super.onBackPressed();
        ExitApp(false);
    }

    private void ExitApp(boolean b)
    {
        forceLogout = b;
        new WCFServiceTask<Boolean, Boolean>("https://findndrive.no-ip.co.uk/Services/UserService.svc/logout",
                forceLogout, new TypeToken<Boolean>(){}.getType(), appData.getAuthorisationHeaders(), null, this).execute();
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
        return super.onCreateOptionsMenu(menu);
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
    public void onServiceCallCompleted(ServiceResponse<Boolean> serviceResponse, String parameter) {
        //super.checkIfAuthorised(serviceResponse.ServiceResponseCode);
        finish();

        if(forceLogout)
        {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }
}
