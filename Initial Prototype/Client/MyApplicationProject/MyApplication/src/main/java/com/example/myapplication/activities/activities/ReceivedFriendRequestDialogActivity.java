package com.example.myapplication.activities.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.example.myapplication.R;
import com.example.myapplication.activities.base.BaseActivity;
import com.example.myapplication.constants.FriendRequestDecisions;
import com.example.myapplication.constants.IntentConstants;
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.domain_objects.FriendRequest;
import com.example.myapplication.domain_objects.Notification;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.notification_management.NotificationProcessor;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WcfPostServiceTask;
import com.google.gson.reflect.TypeToken;

/**
 * Created by Michal on 10/02/14.
 */
public class ReceivedFriendRequestDialogActivity extends BaseActivity {

    private Button acceptButton;
    private Button denyButton;

    private Notification notification;
    private FriendRequest friendRequest;

    private TextView headerTextView;
    private TextView messageTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_friend_request_received);

        // Initialise local variables.
        Bundle bundle = getIntent().getExtras();

        this.notification =  gson.fromJson(bundle.getString(IntentConstants.NOTIFICATION),  new TypeToken<Notification>() {}.getType());
        this.friendRequest =  gson.fromJson(bundle.getString(IntentConstants.FRIEND_REQUEST), new TypeToken<FriendRequest>() {}.getType());

        if(this.notification != null)
        {
            new NotificationProcessor().MarkDelivered(this, this.appManager, notification, new WCFServiceCallback<Boolean, Void>() {
                @Override
                public void onServiceCallCompleted(ServiceResponse<Boolean> serviceResponse, Void parameter) {
                    Log.i(this.getClass().getSimpleName(), "Notification successfully marked as delivered");
                }
            });
        }

        Log.i(this.getClass().getSimpleName(), gson.toJson(friendRequest));
        // Initialise UI elements.
        this.acceptButton = (Button) this.findViewById(R.id.FriendRequestReceivedActivityAcceptButton);
        this.acceptButton.setEnabled(this.friendRequest.FriendRequestDecision == FriendRequestDecisions.Undecided);
        this.denyButton = (Button) this.findViewById(R.id.FriendRequestReceivedActivityDenyButton);
        this.denyButton.setEnabled(this.friendRequest.FriendRequestDecision == FriendRequestDecisions.Undecided);
        this.messageTextView = (TextView) this.findViewById(R.id.FriendRequestReceivedMessageTextView);

        this.headerTextView = (TextView) this.findViewById(R.id.FriendRequestReceiverHeaderTextView);
        this.headerTextView.setText(this.friendRequest.RequestingUserName);
        this.messageTextView.setText(this.friendRequest.Message == null ? "" : this.friendRequest.Message);
        // Setup event handlers.
        this.setupEventHandlers();

        // In order to not be too narrow, set the window size based on the screen resolution:
        final int screen_width = getResources().getDisplayMetrics().widthPixels;
        final int new_window_width = screen_width * 90 / 100;
        WindowManager.LayoutParams layout = getWindow().getAttributes();
        layout.width = Math.max(layout.width, new_window_width);
        getWindow().setAttributes(layout);
    }

    private void setupEventHandlers()
    {
        this.acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitDecision(FriendRequestDecisions.Accepted);
            }
        });

        this.denyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitDecision(FriendRequestDecisions.Denied);
            }
        });
    }

    private void submitDecision(int decision)
    {
        this.friendRequest.FriendRequestDecision = decision;

        new WcfPostServiceTask<FriendRequest>(this,
                getResources().getString(R.string.ProcessFriendRequestDecisionURL), friendRequest,
                new TypeToken<ServiceResponse<Boolean>>() {}.getType(), appManager.getAuthorisationHeaders(), new WCFServiceCallback<Boolean, Void>() {
            @Override
            public void onServiceCallCompleted(ServiceResponse<Boolean> serviceResponse, Void parameter) {
                if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
                {
                    decisionSubmitted();
                }
            }
        }).execute();
    }

    private void decisionSubmitted()
    {
        this.finish();
    }
}
