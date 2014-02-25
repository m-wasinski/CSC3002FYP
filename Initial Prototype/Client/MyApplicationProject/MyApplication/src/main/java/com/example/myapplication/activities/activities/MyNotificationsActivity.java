package com.example.myapplication.activities.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.myapplication.activities.base.BaseActivity;
import com.example.myapplication.adapters.NotificationsAdapter;
import com.example.myapplication.constants.IntentConstants;
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.constants.WcfConstants;
import com.example.myapplication.dtos.LoadRangeDTO;
import com.example.myapplication.domain_objects.Notification;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.interfaces.NotificationTargetRetrieved;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WcfPostServiceTask;
import com.example.myapplication.R;
import com.example.myapplication.notification_management.NotificationProcessor;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

/**
 * Created by Michal on 06/01/14.
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

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_my_notifications);

        Bundle bundle = getIntent().getExtras();

        if(bundle != null)
        {
            Notification notification =  gson.fromJson(bundle.getString(IntentConstants.NOTIFICATION),  new TypeToken<Notification>() {}.getType());

            if(notification != null)
            {
                new NotificationProcessor().MarkDelivered(this, this.appManager, notification, new WCFServiceCallback<Boolean, Void>() {
                    @Override
                    public void onServiceCallCompleted(ServiceResponse<Boolean> serviceResponse, Void parameter) {
                        Log.i(this.getClass().getSimpleName(), "Notification successfully marked as delivered");
                    }
                });
            }
        }

        // Initialise UI elements.
        this.progressBar = (ProgressBar) this.findViewById(R.id.ActivityMyNotificationsProgressBar);
        this.noNotificationsTestView = (TextView) this.findViewById(R.id.MyNotificationsActivityNoNotificationsTextView);
        this.notificationsListView = (ListView) this.findViewById(R.id.MyNotificationsActivityMainListView);
        this.notifications = new ArrayList<Notification>();
        this.notificationsAdapter = new NotificationsAdapter(this.appManager, this, R.layout.listview_row_notification, notifications);
        this.notificationsListView.setAdapter(notificationsAdapter);

        // Wire up event handlers.
        this.setupEventHandlers();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        this.requestMoreData = this.notificationsListView.getCount() == 0;
        this.getNotifications();
    }

    private void setupEventHandlers()
    {
        this.notificationsListView.setOnScrollListener(this);
    }

    private void getNotifications()
    {
        this.progressBar.setVisibility(View.VISIBLE);

        new WcfPostServiceTask<LoadRangeDTO>(this, getResources().getString(R.string.GetAppNotificationsURL),
                new LoadRangeDTO(appManager.getUser().getUserId(),
                        this.requestMoreData ? this.notificationsListView.getCount() : 0, this.requestMoreData ? WcfConstants.NotificationsPerCall : this.notificationsListView.getCount()),
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
                if(!this.requestMoreData)
                {
                    this.notifications.clear();
                }
                else
                {
                    this.notificationsListView.setSelectionFromTop(currentScrollIndex, currentScrollTop);
                }

                this.notifications.addAll(serviceResponse.Result);
                this.notificationsAdapter.notifyDataSetInvalidated();
                this.noNotificationsTestView.setVisibility(View.GONE);
                this.notificationsListView.setVisibility(View.VISIBLE);
                this.notificationsListView.setOnItemClickListener(this);
                this.requestMoreData = false;
            }
        }
    }

    private void markNotificationRead(NotificationProcessor notificationProcessor, Notification notification, final View view)
    {
        notificationProcessor.MarkDelivered(this, appManager, notification, new WCFServiceCallback<Boolean, Void>() {
            public void onServiceCallCompleted(ServiceResponse<Boolean> serviceResponse, Void parameter) {
                view.findViewById(R.id.NotificationListViewRowParentRelativeLayout).setBackgroundColor(Color.parseColor("#80151515"));
            }
        });
    }

    private void launchNotificationProcessor(NotificationProcessor notificationProcessor, Notification notification, final View view)
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
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        NotificationProcessor notificationProcessor = new NotificationProcessor();

        markNotificationRead(notificationProcessor, notifications.get(i), view);

        if(this.notifications.get(i).TargetObjectId != -1)
        {
            this.progressBar.setVisibility(View.VISIBLE);
            launchNotificationProcessor(notificationProcessor, notifications.get(i), view);
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

        if (this.previousTotalListViewItemCount == totalItemCount)
        {
            return;
        }

        if(firstVisibleItem + visibleItemCount >= totalItemCount)
        {
            this.previousTotalListViewItemCount = totalItemCount;
            this.currentScrollIndex = this.notificationsListView.getFirstVisiblePosition();
            View v = this.notificationsListView.getChildAt(0);
            this.currentScrollTop= (v == null) ? 0 : v.getTop();
            this.requestMoreData = true;
            this.getNotifications();
        }
    }
}
