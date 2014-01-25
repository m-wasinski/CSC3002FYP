package com.example.myapplication.activities.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.myapplication.activities.base.BaseActivity;
import com.example.myapplication.adapters.FriendsAdapter;
import com.example.myapplication.constants.GcmConstants;
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.dtos.ServiceResponse;
import com.example.myapplication.dtos.User;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friend_list_activity);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(GcmConstants.PROPERTY_ACTION_REFRESH);
        registerReceiver(GCMReceiver, intentFilter);
        travelBuddiesListView = (ListView) findViewById(R.id.FriendListActivityFriendsListView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getFriends();
    }

    private void getFriends()
    {
        new WCFServiceTask<Integer>(this, getResources().getString(R.string.GetFriendsURL), findNDriveManager.getUser().UserId,
                new TypeToken<ServiceResponse<ArrayList<User>>>() {}.getType(), findNDriveManager.getAuthorisationHeaders(), this).execute();
    }

    @Override
    public void onServiceCallCompleted(final ServiceResponse<ArrayList<User>> serviceResponse, String parameter) {
        if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
        {
            friendsAdapter = new FriendsAdapter(this,  R.layout.friend_list_listview_row, findNDriveManager, serviceResponse.Result);
            travelBuddiesListView.setAdapter(friendsAdapter);

            travelBuddiesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Intent intent = new Intent(getApplicationContext(), InstantMessengerActivity.class);
                    Bundle extras = new Bundle();
                    extras.putString("RecipientUsername", serviceResponse.Result.get(i).UserName);
                    extras.putInt("RecipientId", serviceResponse.Result.get(i).UserId);

                    intent.putExtras(extras).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            });
        }
    }

    /**
     * Receiving push messages
     * */
    private final BroadcastReceiver GCMReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            WakeLocker.acquire(getApplicationContext());
            onResume();
            WakeLocker.release();
        }
    };
}
