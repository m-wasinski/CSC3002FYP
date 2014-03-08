package com.example.myapplication.activities.activities;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.activities.base.BaseActivity;
import com.example.myapplication.adapters.FriendsAdapter;
import com.example.myapplication.constants.BroadcastTypes;
import com.example.myapplication.constants.IntentConstants;
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.domain_objects.Notification;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.domain_objects.User;
import com.example.myapplication.dtos.FriendDeletionDTO;
import com.example.myapplication.factories.DialogFactory;
import com.example.myapplication.factories.ServiceTaskFactory;
import com.example.myapplication.interfaces.Interfaces;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WcfPostServiceTask;
import com.example.myapplication.notification_management.NotificationProcessor;
import com.example.myapplication.utilities.WakeLocker;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

/**
 * Responsible for displaying list of current user's friends.
 * Uses FriendsAdapter to display data retrieved from the web service in an organised fashion.
 */
public class FriendsListActivity extends BaseActivity implements WCFServiceCallback<ArrayList<User>, Void>, TextWatcher,
        AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

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

        ListView friendsListView = (ListView) findViewById(R.id.FriendListActivityFriendsListView);
        friendsListView.setAdapter(friendsAdapter);
        friendsListView.setOnItemClickListener(this);
        friendsListView.setOnItemLongClickListener(this);

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.help:
                DialogFactory.getHelpDialog(this, "Your friends list", getResources().getString(R.string.MyFriendsHelp));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void retrieveFriendsList()
    {
        ServiceTaskFactory.getFriendsList(this, appManager.getAuthorisationHeaders(), appManager.getUser().getUserId(), this).execute();
    }

    /**
     * Called when friends list have been retrieved from the server.
     **/
    @Override
    public void onServiceCallCompleted(final ServiceResponse<ArrayList<User>> serviceResponse, Void v) {
        progressBar.setVisibility(View.GONE);

        if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
        {
            noFriendsTextView.setVisibility(serviceResponse.Result.size() == 0 ? View.VISIBLE : View.GONE);
            friendsFilterEditText.setEnabled(serviceResponse.Result.size() != 0);
            friends.clear();
            friends.addAll(serviceResponse.Result);
            friendsAdapter.notifyDataSetChanged();
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
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        showInstantMessengerActivity(friends.get(i));
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {
        final String items[] = {"Show profile", "Start chat", "Delete from friends list"};
        final AlertDialog.Builder ab=new AlertDialog.Builder(this);
        ab.setTitle(friends.get(i).getUserName());

        ab.setItems(items, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface d, int choice) {
                switch (choice)
                {
                    case 0:
                        Bundle bundle = new Bundle();
                        bundle.putInt(IntentConstants.PROFILE_VIEWER_MODE, IntentConstants.PROFILE_VIEWER_VIEWING);
                        bundle.putInt(IntentConstants.USER, friends.get(i).getUserId());
                        startActivity(new Intent(FriendsListActivity.this, ProfileViewerActivity.class).putExtras(bundle));
                        break;
                    case 1:
                        showInstantMessengerActivity(friends.get(i));
                        break;
                    case 2:
                        DialogFactory.getYesNoDialog(ab.getContext(), "Delete " + friends.get(i).getUserName()+"?", "Are you sure you want to delete "
                                + friends.get(i).getUserName() + " from your list of friends?", new Interfaces.YesNoDialogPositiveButtonListener() {
                            @Override
                            public void positiveButtonClicked() {
                                progressBar.setVisibility(View.VISIBLE);
                                new WcfPostServiceTask<FriendDeletionDTO>(FriendsListActivity.this, getResources().getString(R.string.DeleteFriendURL),
                                        new FriendDeletionDTO(appManager.getUser().getUserId(), friends.get(i).getUserId()), new TypeToken<ServiceResponse<Boolean>>(){}.getType(), appManager.getAuthorisationHeaders(), new WCFServiceCallback() {
                                    @Override
                                    public void onServiceCallCompleted(ServiceResponse serviceResponse, Object parameter) {
                                        progressBar.setVisibility(View.GONE);
                                        if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
                                            {
                                                Toast.makeText(FriendsListActivity.this, friends.get(i).getUserName()+" was successfully deleted from your friends list." ,Toast.LENGTH_LONG).show();
                                                retrieveFriendsList();
                                            }
                                    }
                                }).execute();
                            }
                        });
                }
            }
        });
        ab.show();
        return true;
    }
}
