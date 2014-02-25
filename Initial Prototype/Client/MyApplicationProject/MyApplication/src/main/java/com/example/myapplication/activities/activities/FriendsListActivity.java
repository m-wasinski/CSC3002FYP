package com.example.myapplication.activities.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.myapplication.activities.base.BaseActivity;
import com.example.myapplication.adapters.FriendsAdapter;
import com.example.myapplication.constants.BroadcastTypes;
import com.example.myapplication.constants.IntentConstants;
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.domain_objects.Notification;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.domain_objects.User;
import com.example.myapplication.utilities.WakeLocker;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WcfPostServiceTask;
import com.example.myapplication.R;
import com.example.myapplication.notification_management.NotificationProcessor;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

/**
 * Created by Michal on 04/01/14.
 */
public class FriendsListActivity extends BaseActivity implements WCFServiceCallback<ArrayList<User>, String> {

    private ListView travelBuddiesListView;

    private ProgressBar progressBar;

    private TextView noFriendsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_friends_list);

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
        this.progressBar = (ProgressBar) this.findViewById(R.id.FriendListActivityProgressBar);
        this.progressBar.setVisibility(View.VISIBLE);
        this.travelBuddiesListView = (ListView) this.findViewById(R.id.FriendListActivityFriendsListView);
        this.noFriendsTextView = (TextView) this.findViewById(R.id.FriendsListActivityNoFriendsTextView);
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadcastTypes.BROADCAST_INSTANT_MESSENGER);
        registerReceiver(InstantMessageReceiver, intentFilter);

        this.retrieveFriendsList();
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(InstantMessageReceiver);
    }

    private void retrieveFriendsList()
    {
        new WcfPostServiceTask<Integer>(this, getResources().getString(R.string.GetFriendsURL), appManager.getUser().getUserId(),
                new TypeToken<ServiceResponse<ArrayList<User>>>() {}.getType(), appManager.getAuthorisationHeaders(), this).execute();
    }

    /*
     * Called when friends list have been retrieved from the server.
     */
    @Override
    public void onServiceCallCompleted(final ServiceResponse<ArrayList<User>> serviceResponse, String parameter) {
        this.progressBar.setVisibility(View.GONE);

        if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
        {
            this.noFriendsTextView.setVisibility(serviceResponse.Result.size() == 0 ? View.VISIBLE : View.GONE);

            FriendsAdapter friendsAdapter = new FriendsAdapter(this, R.layout.listview_row_friend, appManager, serviceResponse.Result);

            travelBuddiesListView.setAdapter(friendsAdapter);

            travelBuddiesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    showInstantMessengerActivity(serviceResponse.Result.get(i));
                }
            });
        }
    }

    /*
     * Displays chat activity after clicking on one of the contacts.
     */
    private void showInstantMessengerActivity(User friend)
    {
        Intent intent = new Intent(this, InstantMessengerActivity.class);
        Bundle extras = new Bundle();
        extras.putString(IntentConstants.RECIPIENT_USERNAME, friend.getUserName());
        extras.putInt(IntentConstants.RECIPIENT_ID, friend.getUserId());
        intent.putExtras(extras).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    /*
     * Picks up broadcast sent from the InstantMessengerReceiver class.
     */
    private final BroadcastReceiver InstantMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            WakeLocker.acquire(getApplicationContext());
            retrieveFriendsList();
            WakeLocker.release();
        }
    };
}
