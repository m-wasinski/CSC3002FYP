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
public class FriendListActivity extends BaseActivity implements WCFServiceCallback<ArrayList<User>, String> {

    private ListView travelBuddiesListView;
    private FriendsAdapter friendsAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.travel_buddy_list_activity);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(GcmConstants.PROPERTY_ACTION_REFRESH);
        registerReceiver(GCMReceiver, intentFilter);
        travelBuddiesListView = (ListView) findViewById(R.id.TravelBuddyListActivityListView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getFriends();
    }

    private void getFriends()
    {
        new WCFServiceTask<Integer>(getResources().getString(R.string.GetFriendsURL), appData.getUser().UserId,
                new TypeToken<ServiceResponse<ArrayList<User>>>() {}.getType(), appData.getAuthorisationHeaders(), this).execute();
    }

    @Override
    public void onServiceCallCompleted(final ServiceResponse<ArrayList<User>> serviceResponse, String parameter) {
        super.checkIfAuthorised(serviceResponse.ServiceResponseCode);
        if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
        {
            friendsAdapter = new FriendsAdapter(this,  R.layout.travel_buddy_list_row, serviceResponse.Result, serviceResponse.Result, appData);
            travelBuddiesListView.setAdapter(friendsAdapter);

            travelBuddiesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Intent intent = new Intent(getApplicationContext(), InstantMessengerActivity.class);
                    intent.putExtra("Recipient", gson.toJson(serviceResponse.Result.get(i)));
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
