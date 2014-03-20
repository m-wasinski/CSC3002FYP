package com.example.myapplication.activities.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.myapplication.R;
import com.example.myapplication.activities.base.BaseActivity;
import com.example.myapplication.adapters.NotificationsAdapter;
import com.example.myapplication.constants.IntentConstants;
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.constants.WcfConstants;
import com.example.myapplication.domain_objects.Notification;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.dtos.LoadRangeDTO;
import com.example.myapplication.factories.DialogFactory;
import com.example.myapplication.interfaces.NotificationTargetRetrieved;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WcfPostServiceTask;
import com.example.myapplication.notification_management.NotificationProcessor;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

/**
 * This activity displays user notifications. Activities which have an action associated with display an arrow to the right of the message body.
 * Clicking on a notification automatically marks it as read or delivered, which have the same meaning.
 */
public class MyNotificationsActivity extends BaseActivity implements WCFServiceCallback<ArrayList<Notification>, Void>,
        AbsListView.OnScrollListener, AdapterView.OnItemClickListener{

    private ListView notificationsListView;
    private ArrayList<Notification> notifications;
    private NotificationsAdapter notificationsAdapter;
    private ProgressBar progressBar;

    private int currentScrollIndex;
    private int currentScrollTop;
    private int previousTotalListViewItemCount;

    private boolean requestMoreData;
    private TextView noNotificationsTestView;

    private final String TAG = "Notifications Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_notifications);

        // Check if this activity has been started from a notification. If yes, we need to mark it as read.
        Bundle bundle = getIntent().getExtras();

        if(bundle != null)
        {
            Notification notification =  gson.fromJson(bundle.getString(IntentConstants.NOTIFICATION),  new TypeToken<Notification>() {}.getType());

            if(notification != null)
            {
                new NotificationProcessor().MarkDelivered(this, appManager, notification, new WCFServiceCallback<Void, Void>() {
                    @Override
                    public void onServiceCallCompleted(ServiceResponse<Void> serviceResponse, Void parameter) {
                        Log.i(TAG, "Notification successfully marked as delivered");
                    }
                });
            }
        }

        // Initialise UI elements.
        progressBar = (ProgressBar) findViewById(R.id.ActivityMyNotificationsProgressBar);
        noNotificationsTestView = (TextView) findViewById(R.id.MyNotificationsActivityNoNotificationsTextView);
        notificationsListView = (ListView) findViewById(R.id.MyNotificationsActivityMainListView);
        notifications = new ArrayList<Notification>();
        notificationsAdapter = new NotificationsAdapter(appManager, this, R.layout.listview_row_notification, notifications);
        notificationsListView.setAdapter(notificationsAdapter);
        notificationsListView.setOnScrollListener(this);

    }

    @Override
    public void onResume()
    {
        super.onResume();
        requestMoreData = notificationsListView.getCount() == 0;
        getNotifications();
    }

    private void getNotifications()
    {
        progressBar.setVisibility(View.VISIBLE);

        new WcfPostServiceTask<LoadRangeDTO>(this, getResources().getString(R.string.GetAppNotificationsURL),
                new LoadRangeDTO(appManager.getUser().getUserId(),
                        requestMoreData ? notificationsListView.getCount() : 0, requestMoreData ? WcfConstants.NotificationsPerCall : notificationsListView.getCount()),
                new TypeToken<ServiceResponse<ArrayList<Notification>>>() {}.getType(),
                appManager.getAuthorisationHeaders(), this).execute();
    }
    @Override
    public void onServiceCallCompleted(final ServiceResponse<ArrayList<Notification>> serviceResponse, Void parameter)
    {
        progressBar.setVisibility(View.GONE);

        if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
        {
            if(serviceResponse.Result.size() == 0 && notificationsListView.getCount() == 0)
            {
                noNotificationsTestView.setVisibility(View.VISIBLE);
                notificationsListView.setVisibility(View.GONE);
            }
            else
            {
                if(!requestMoreData)
                {
                    notifications.clear();
                }
                else
                {
                    notificationsListView.setSelectionFromTop(currentScrollIndex, currentScrollTop);
                }

                notifications.addAll(serviceResponse.Result);
                notificationsAdapter.notifyDataSetChanged();
                noNotificationsTestView.setVisibility(View.GONE);
                notificationsListView.setVisibility(View.VISIBLE);
                notificationsListView.setOnItemClickListener(this);
                requestMoreData = false;
            }
        }
    }

    private void markNotificationDelivered(NotificationProcessor notificationProcessor, final Notification notification, final View view)
    {
        notificationProcessor.MarkDelivered(this, appManager, notification, new WCFServiceCallback<Void, Void>() {
            public void onServiceCallCompleted(ServiceResponse<Void> serviceResponse, Void parameter) {
                notification.setDelivered(true);
                notificationsAdapter.notifyDataSetChanged();
            }
        });
    }

    private void launchNotificationProcessor(NotificationProcessor notificationProcessor, Notification notification)
    {
        notificationProcessor.process(this, appManager, notification, new NotificationTargetRetrieved() {

            @Override
            public void onNotificationTargetRetrieved(Intent intent) {
                progressBar.setVisibility(View.GONE);
                if (intent != null) {
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
        final NotificationProcessor notificationProcessor = new NotificationProcessor();

        markNotificationDelivered(notificationProcessor, notifications.get(i), view);

        if(notifications.get(i).getTargetObjectId() != -1)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle("Notification clicked");
            CharSequence userOptions[] = new CharSequence[] {"Show details"};

            builder.setItems(userOptions, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    switch (which)
                    {
                        case 0:
                            progressBar.setVisibility(View.VISIBLE);
                            launchNotificationProcessor(notificationProcessor, notifications.get(i));
                            break;
                    }
                }
            });

            builder.show();

        }
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
    {
        if (totalItemCount == 0)
        {
            return;
        }

        if (previousTotalListViewItemCount == totalItemCount)
        {
            return;
        }

        if(firstVisibleItem + visibleItemCount >= totalItemCount)
        {
            previousTotalListViewItemCount = totalItemCount;
            currentScrollIndex = notificationsListView.getFirstVisiblePosition();
            View v = notificationsListView.getChildAt(0);
            currentScrollTop= (v == null) ? 0 : v.getTop();
            requestMoreData = true;
            getNotifications();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.help:
                DialogFactory.getHelpDialog(this, "Your notifications.", getResources().getString(R.string.MyNotificationsHelp));
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
