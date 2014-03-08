package com.example.myapplication.activities.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.example.myapplication.R;
import com.example.myapplication.activities.base.BaseActivity;
import com.example.myapplication.app_management.GcmHeartbeatService;
import com.example.myapplication.constants.BroadcastTypes;
import com.example.myapplication.constants.IntentConstants;
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.domain_objects.Notification;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.factories.DialogFactory;
import com.example.myapplication.factories.ServiceTaskFactory;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.notification_management.NotificationProcessor;
import com.example.myapplication.utilities.WakeLocker;

import java.util.ArrayList;
import java.util.Calendar;

/**
 *Home Activity is the screen that the user sees immediately after logging in.
 *It is the 'home page' of the application which can only be instantiated once.
 */
public class HomeActivity extends BaseActivity implements View.OnClickListener {

    private ImageView notificationsImageView;
    private ImageView friendsImageView;

    private ProgressBar progressBar;

    private int refreshedItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //Initialise local variables.
        actionBar.setTitle(appManager.getUser().getUserName());
        actionBar.setHomeButtonEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);

        /*
         * Alarm manager is used to trigger a task every 5 minutes.
         * In this case, it's sending the heartbeat to the GCM service to keep the socket between the device and the GCM servers open.
         * This ensures GCM notifications arrive instantaneously.
         */
        long FIVE_MINUTES = 300000;
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                Calendar.getInstance().getTimeInMillis(),
                FIVE_MINUTES, PendingIntent.getService(this, 0, new Intent(this, GcmHeartbeatService.class), 0));

        // Initialise UI elements and setup event handlers.
        findViewById(R.id.ActivityHomeMyJourneysLayout).setOnClickListener(this);
        findViewById(R.id.ActivityHomeSearchLayout).setOnClickListener(this);
        findViewById(R.id.ActivityHomeNotificationsLayout).setOnClickListener(this);
        findViewById(R.id.ActivityHomeFriendsLayout).setOnClickListener(this);
        findViewById(R.id.ActivityHomeMyOfferJourneyLayout).setOnClickListener(this);
        findViewById(R.id.ActivityHomeMyProfileLayout).setOnClickListener(this);
        findViewById(R.id.ActivityHomeLeaderboardLayout).setOnClickListener(this);
        findViewById(R.id.ActivityHomeTemplatesLayout).setOnClickListener(this);

        notificationsImageView = (ImageView) findViewById(R.id.HomeActivityNotificationsImageView);
        friendsImageView =(ImageView) findViewById(R.id.HomeActivityFriendsImageView);
        progressBar = (ProgressBar) findViewById(R.id.HomeActivityProgressBar);


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.appManager.logout(false, true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout_menu_option:
                appManager.logout(true, true);
                break;
            case R.id.action_refresh:
                refreshInformation();
                break;
            case R.id.help:
                DialogFactory.getHelpDialog(this, "Home Screen", getResources().getString(R.string.HomeScreenHelp));
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(GCMReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadcastTypes.BROADCAST_ACTION_REFRESH);
        registerReceiver(GCMReceiver, intentFilter);
        refreshInformation();
        retrieveDeviceNotifications();
    }

    private void refreshInformation()
    {
        progressBar.setVisibility(View.VISIBLE);
        refreshedItems = 0;

        ServiceTaskFactory.getUnreadAppNotificationsCount(this, appManager.getAuthorisationHeaders(), appManager.getUser().getUserId(), new WCFServiceCallback<Integer, Void>() {
            @Override
            public void onServiceCallCompleted(ServiceResponse<Integer> serviceResponse, Void parameter) {
                if (serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS) {
                    refreshedItems += 1;
                    progressBar.setVisibility(refreshedItems >= 2 ? View.GONE : View.VISIBLE);
                    notificationsImageView.setImageResource(serviceResponse.Result == 0 ? R.drawable.home_activity_notification : R.drawable.home_activity_notification_new);
                }
            }
        }).execute();

        ServiceTaskFactory.getUnreadMessagesCount(this, appManager.getAuthorisationHeaders(), appManager.getUser().getUserId(), new WCFServiceCallback<Integer, Void>() {
            @Override
            public void onServiceCallCompleted(ServiceResponse<Integer> serviceResponse, Void parameter) {
                if (serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS) {
                    refreshedItems += 1;
                    progressBar.setVisibility(refreshedItems >= 2 ? View.GONE : View.VISIBLE);
                    friendsImageView.setImageResource(serviceResponse.Result == 0 ? R.drawable.home_activity_friends : R.drawable.home_activity_friends_new_message);
                }
            }
        }).execute();

    }

    /**
     * Calls the server to retrieve any new pending device notifications.
     * Device notifications are the type of notifications which appear in the top-left corner of user's device.
     **/
    private void retrieveDeviceNotifications()
    {
        ServiceTaskFactory.getDeviceNotifications(this, appManager.getAuthorisationHeaders(), appManager.getUser().getUserId(), new WCFServiceCallback<ArrayList<Notification>, Void>() {
            @Override
            public void onServiceCallCompleted(ServiceResponse<ArrayList<Notification>> serviceResponse, Void parameter) {
                if (serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS) {
                    deviceNotificationsRetrieved(serviceResponse.Result);
                }
            }
        }).execute();
    }

    private final BroadcastReceiver GCMReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            WakeLocker.acquire(getApplicationContext());
            refreshInformation();
            WakeLocker.release();
        }
    };

    private void deviceNotificationsRetrieved(ArrayList<Notification> notifications)
    {
        NotificationProcessor notificationProcessor = new NotificationProcessor();

        for(Notification notification : notifications)
        {
            notificationProcessor.process(this, appManager, notification, null);
        }
    }

    @Override
    public void onClick(View view) {
        Bundle bundle = new Bundle();
        switch(view.getId())
        {
            case R.id.ActivityHomeMyJourneysLayout:
                startActivity(new Intent(this, MyJourneysActivity.class).putExtra(IntentConstants.USER, gson.toJson(appManager.getUser())));
                break;
            case R.id.ActivityHomeSearchLayout:
                startActivity(new Intent(this, SearchTypeActivity.class));
                break;
            case R.id.ActivityHomeNotificationsLayout:
                startActivity(new Intent(this, MyNotificationsActivity.class));
                break;
            case R.id.ActivityHomeFriendsLayout:
                startActivity(new Intent(this, FriendsListActivity.class));
                break;
            case R.id.ActivityHomeMyOfferJourneyLayout:
                bundle.putInt(IntentConstants.JOURNEY_CREATOR_MODE, IntentConstants.JOURNEY_CREATOR_MODE_CREATING);
                startActivity( new Intent(this, OfferJourneyStepOneActivity.class).putExtras(bundle));
                break;
            case R.id.ActivityHomeMyProfileLayout:
                Bundle bundle1 = new Bundle();
                bundle.putInt(IntentConstants.PROFILE_VIEWER_MODE, IntentConstants.PROFILE_VIEWER_EDITING);
                startActivity(new Intent(this, ProfileEditorActivity.class).putExtras(bundle1));
                break;
            case R.id.ActivityHomeLeaderboardLayout:
                startActivity(new Intent(this, LeaderboardActivity.class));
                break;
            case R.id.ActivityHomeTemplatesLayout:
                bundle.putInt(IntentConstants.SEARCH_MODE, IntentConstants.EDITING_TEMPLATE);
                startActivity(new Intent(this, MyJourneyTemplatesActivity.class).putExtras(bundle));
                break;

        }
    }
}
