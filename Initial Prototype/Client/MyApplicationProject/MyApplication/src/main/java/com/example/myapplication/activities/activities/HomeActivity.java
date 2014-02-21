package com.example.myapplication.activities.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.example.myapplication.activities.base.BaseActivity;
import com.example.myapplication.constants.BroadcastTypes;
import com.example.myapplication.constants.IntentConstants;
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.domain_objects.Notification;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.experimental.FindNDriveService;
import com.example.myapplication.network_tasks.WcfPostServiceTask;
import com.example.myapplication.notification_management.NotificationProcessor;
import com.example.myapplication.experimental.WakeLocker;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.R;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Michal on 14/01/14.
 */
public class HomeActivity extends BaseActivity {

    private LinearLayout myJourneysLayout;
    private LinearLayout searchLayout;
    private LinearLayout notificationsLayout;
    private LinearLayout friendsLayout;
    private LinearLayout addNewJourneyLayout;
    private LinearLayout myProfileLinearLayout;

    private ImageView notificationsImageView;
    private ImageView friendsImageView;

    private ProgressBar progressBar;

    private int refreshedItems;

    private AlarmManager alarmManager;

    private long FIVE_MINUTES = 300000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_home);

        //Initialise local variables.
        this.actionBar.setTitle(" Hi " + findNDriveManager.getUser().getUserName());

        /*
         * Alarm manager is used to trigger a task every 5 minutes.
         * In this case, it's sending the heartbeat to the GCM service to keep the socket between the device and the GCM servers open.
         * This ensures GCM notifications arrive instantaneously.
         */
        this.alarmManager = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
        this.alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                Calendar.getInstance().getTimeInMillis(),
                FIVE_MINUTES, PendingIntent.getService(this, 0, new Intent(this, FindNDriveService.class), 0));

        // Initialise UI elements.
        this.myJourneysLayout = (LinearLayout) this.findViewById(R.id.ActivityHomeMyJourneysLayout);
        this.searchLayout = (LinearLayout) this.findViewById(R.id.ActivityHomeSearchLayout);
        this.notificationsLayout = (LinearLayout) this.findViewById(R.id.ActivityHomeNotificationsLayout);
        this.friendsLayout = (LinearLayout) this.findViewById(R.id.ActivityHomeFriendsLayout);
        this.addNewJourneyLayout = (LinearLayout) this.findViewById(R.id.ActivityHomeMyOfferJourneyLayout);
        this.myProfileLinearLayout = (LinearLayout) this.findViewById(R.id.ActivityHomeMyProfileLayout);

        this.notificationsImageView = (ImageView) this.findViewById(R.id.HomeActivityNotificationsImageView);
        this.friendsImageView =(ImageView) this.findViewById(R.id.HomeActivityFriendsImageView);

        this.progressBar = (ProgressBar) this.findViewById(R.id.HomeActivityProgressBar);

        // Setup event handlers for UI elements.
        this.setupEventHandlers();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.findNDriveManager.logout(false, true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout_menu_option:
                findNDriveManager.logout(true, true);
                break;
            case R.id.action_refresh:
                this.refreshInformation();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(GCMReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadcastTypes.BROADCAST_ACTION_REFRESH);
        this.registerReceiver(GCMReceiver, intentFilter);
        this.refreshInformation();
        this.retrieveDeviceNotifications();
    }

    private void refreshInformation()
    {
        this.progressBar.setVisibility(View.VISIBLE);
        this.refreshedItems = 0;

        new WcfPostServiceTask<Integer>(this, getResources().getString(R.string.GetUnreadAppNotificationsCountURL), findNDriveManager.getUser().getUserId(),
                new TypeToken<ServiceResponse<Integer>>() {}.getType(),
                findNDriveManager.getAuthorisationHeaders(), new WCFServiceCallback<Integer, Void>() {
            @Override
            public void onServiceCallCompleted(ServiceResponse<Integer> serviceResponse, Void parameter) {
                if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
                {
                    notificationCountRetrieved(serviceResponse.Result);
                }
            }
        }).execute();

        new WcfPostServiceTask<Integer>(this, getResources().getString(R.string.GetUnreadMessagesCountURL), findNDriveManager.getUser().getUserId(),
                new TypeToken<ServiceResponse<Integer>>() {}.getType(),
                findNDriveManager.getAuthorisationHeaders(), new WCFServiceCallback<Integer, Void>() {
            @Override
            public void onServiceCallCompleted(ServiceResponse<Integer> serviceResponse, Void parameter) {
                if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
                {
                    unreadMessagesCountRetrieved(serviceResponse.Result);
                }
            }
        }).execute();
    }

    private void retrieveDeviceNotifications()
    {
        new WcfPostServiceTask<Integer>(this, getResources().getString(R.string.GetDeviceNotificationsURL), findNDriveManager.getUser().getUserId(),
                new TypeToken<ServiceResponse<ArrayList<Notification>>>() {}.getType(),
                findNDriveManager.getAuthorisationHeaders(), new WCFServiceCallback<ArrayList<Notification>, Void>() {
            @Override
            public void onServiceCallCompleted(ServiceResponse<ArrayList<Notification>> serviceResponse, Void parameter) {
                if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
                {
                    deviceNotificationsRetrieved(serviceResponse.Result);
                }
            }
        }).execute();
    }

    private void setupEventHandlers()
    {
        this.myJourneysLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), MyJourneysActivity.class));
            }
        });

        this.searchLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), SearchActivity.class));
            }
        });

        this.notificationsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MyNotificationsActivity.class);
                startActivity(intent);
            }
        });

        this.friendsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), FriendsListActivity.class));
            }
        });

        this.addNewJourneyLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putInt(IntentConstants.JOURNEY_CREATOR_MODE, IntentConstants.JOURNEY_CREATOR_MODE_CREATING);

                Intent intent = new Intent(getApplicationContext(), OfferJourneyStepOneActivity.class);
                intent.putExtras(bundle);

                startActivity(intent);
            }
        });

        this.myProfileLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), ProfileEditorActivity.class).putExtra(IntentConstants.USER, gson.toJson(findNDriveManager.getUser())));
            }
        });
    }

    private final BroadcastReceiver GCMReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            WakeLocker.acquire(getApplicationContext());
            refreshInformation();
            WakeLocker.release();
        }
    };

    private void notificationCountRetrieved(int count)
    {
        this.refreshedItems += 1;
        this.progressBar.setVisibility(this.refreshedItems >= 2 ? View.GONE : View.VISIBLE);
        this.notificationsImageView.setImageResource(count == 0 ? R.drawable.home_activity_notification : R.drawable.home_activity_notification_new);
    }

    private void unreadMessagesCountRetrieved(int count)
    {
        this.refreshedItems += 1;
        this.progressBar.setVisibility(this.refreshedItems >= 2 ? View.GONE : View.VISIBLE);
        this.friendsImageView.setImageResource(count == 0 ? R.drawable.home_activity_friends : R.drawable.home_activity_friends_new_message);
    }

    private void deviceNotificationsRetrieved(ArrayList<Notification> notifications)
    {
        NotificationProcessor.DisplayNotification(this, this.findNDriveManager, notifications);
    }
}
