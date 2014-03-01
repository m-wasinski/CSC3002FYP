package com.example.myapplication.activities.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
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
 * Responsible for displaying list of current user's friends.
 * Uses FriendsAdapter to display data retrieved from the web service in an organised fashion.
 */
public class FriendsListActivity extends BaseActivity implements WCFServiceCallback<ArrayList<User>, String>, TextWatcher, AdapterView.OnItemClickListener {

    private ListView friendsListView;

    private ProgressBar progressBar;

    private TextView noFriendsTextView;

    private EditText friendsFilterEditText;

    private FriendsAdapter friendsAdapter;

    private ArrayList<User> friends;

    private final String TAG = "Friends List Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);

        Bundle bundle = getIntent().getExtras();

        // Check if a there is a pending notification that needs to be marked as read.
        if(bundle != null)
        {
             Notification notification =  gson.fromJson(bundle.getString(IntentConstants.NOTIFICATION),  new TypeToken<Notification>() {}.getType());

            if(notification != null)
            {
                new NotificationProcessor().MarkDelivered(this, appManager, notification, new WCFServiceCallback<Boolean, Void>() {
                    @Override
                    public void onServiceCallCompleted(ServiceResponse<Boolean> serviceResponse, Void parameter) {
                        Log.i(TAG, "Notification successfully marked as delivered");
                    }
                });
            }
        }

        // Initialise UI elements and local variables.
        progressBar = (ProgressBar) findViewById(R.id.FriendListActivityProgressBar);
        progressBar.setVisibility(View.VISIBLE);

        friends = new ArrayList<User>();
        friendsAdapter = new FriendsAdapter(this, R.layout.listview_row_friend, appManager, friends);

        friendsListView = (ListView) findViewById(R.id.FriendListActivityFriendsListView);
        friendsListView.setAdapter(friendsAdapter);
        friendsListView.setOnItemClickListener(this);

        noFriendsTextView = (TextView) findViewById(R.id.FriendsListActivityNoFriendsTextView);

        friendsFilterEditText = (EditText) findViewById(R.id.FriendListActivityFilterEditText);
        friendsFilterEditText.addTextChangedListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadcastTypes.BROADCAST_INSTANT_MESSENGER);
        registerReceiver(InstantMessageReceiver, intentFilter);
        retrieveFriendsList();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(InstantMessageReceiver);
    }

    private void retrieveFriendsList()
    {
        new WcfPostServiceTask<Integer>(this, getResources().getString(R.string.GetFriendsURL), appManager.getUser().getUserId(),
                new TypeToken<ServiceResponse<ArrayList<User>>>() {}.getType(), appManager.getAuthorisationHeaders(), this).execute();
    }

    /**
     * Called when friends list have been retrieved from the server.
     **/
    @Override
    public void onServiceCallCompleted(final ServiceResponse<ArrayList<User>> serviceResponse, String parameter) {
        progressBar.setVisibility(View.GONE);

        if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
        {
            noFriendsTextView.setVisibility(serviceResponse.Result.size() == 0 ? View.VISIBLE : View.GONE);
            friendsFilterEditText.setEnabled(serviceResponse.Result.size() != 0);
            friends.clear();
            friends.addAll(serviceResponse.Result);
            friendsAdapter.notifyDataSetInvalidated();
        }
    }

    /**
     * Displays chat activity after clicking on one of the contacts.
     **/
    private void showInstantMessengerActivity(User friend)
    {
        Bundle extras = new Bundle();
        extras.putString(IntentConstants.RECIPIENT_USERNAME, friend.getUserName());
        extras.putInt(IntentConstants.RECIPIENT_ID, friend.getUserId());
        startActivity(new Intent(this, InstantMessengerActivity.class).putExtras(extras).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }

    /**
     * Picks up broadcast sent from the InstantMessengerReceiver class.
     **/
    private final BroadcastReceiver InstantMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            WakeLocker.acquire(getApplicationContext());
            retrieveFriendsList();
            WakeLocker.release();
        }
    };

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        friendsAdapter.getFilter().filter(charSequence.toString());
        //friendsListView.setAdapter(friendsAdapter);
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        showInstantMessengerActivity(friends.get(i));
    }
}
