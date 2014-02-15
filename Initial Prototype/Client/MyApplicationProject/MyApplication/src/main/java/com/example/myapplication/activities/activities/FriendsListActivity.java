package com.example.myapplication.activities.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.example.myapplication.activities.base.BaseActivity;
import com.example.myapplication.adapters.FriendsAdapter;
import com.example.myapplication.constants.BroadcastTypes;
import com.example.myapplication.constants.IntentConstants;
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.domain_objects.User;
import com.example.myapplication.experimental.WakeLocker;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WCFServiceTask;
import com.example.myapplication.R;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

/**
 * Created by Michal on 04/01/14.
 */
public class FriendsListActivity extends BaseActivity implements WCFServiceCallback<ArrayList<User>, String> {

    private ListView travelBuddiesListView;
    private FriendsAdapter friendsAdapter;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_friends_list);

        // Initialise UI elements.
        this.progressBar = (ProgressBar) this.findViewById(R.id.FriendListActivityProgressBar);
        this.progressBar.setVisibility(View.VISIBLE);
        this.travelBuddiesListView = (ListView) this.findViewById(R.id.FriendListActivityFriendsListView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadcastTypes.BROADCAST_INSTANT_MESSENGER);
        registerReceiver(GCMReceiver, intentFilter);
        retrieveFriendsList();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(GCMReceiver);
    }

    private void retrieveFriendsList()
    {
        new WCFServiceTask<Integer>(this, getResources().getString(R.string.GetFriendsURL), findNDriveManager.getUser().UserId,
                new TypeToken<ServiceResponse<ArrayList<User>>>() {}.getType(), findNDriveManager.getAuthorisationHeaders(), this).execute();
    }

    @Override
    public void onServiceCallCompleted(final ServiceResponse<ArrayList<User>> serviceResponse, String parameter) {
        this.progressBar.setVisibility(View.GONE);
        if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
        {
            friendsAdapter = new FriendsAdapter(this,  R.layout.listview_row_friend, findNDriveManager, serviceResponse.Result);
            travelBuddiesListView.setAdapter(friendsAdapter);

            travelBuddiesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    showInstantMessengerActivity(serviceResponse.Result.get(i));
                }
            });
        }
    }

    private void showInstantMessengerActivity(User friend)
    {
        Intent intent = new Intent(this, InstantMessengerActivity.class);
        Bundle extras = new Bundle();
        extras.putString(IntentConstants.RECIPIENT_USERNAME, friend.UserName);
        extras.putInt(IntentConstants.RECIPIENT_ID, friend.UserId);
        intent.putExtras(extras).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    /**
     * Receiving push messages
     * */
    private final BroadcastReceiver GCMReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            WakeLocker.acquire(getApplicationContext());
            retrieveFriendsList();
            WakeLocker.release();
        }
    };
}
