package com.example.myapplication.activities.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.activities.base.BaseActivity;
import com.example.myapplication.constants.IntentConstants;
import com.example.myapplication.constants.JourneyRequestDecisions;
import com.example.myapplication.constants.RequestDecision;
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.domain_objects.JourneyRequest;
import com.example.myapplication.domain_objects.Notification;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WcfPostServiceTask;
import com.example.myapplication.R;
import com.example.myapplication.notification_management.NotificationProcessor;
import com.google.gson.reflect.TypeToken;

/**
 * Created by Michal on 02/01/14.
 */
public class JourneyRequestDialogActivity extends BaseActivity{

    private JourneyRequest journeyRequest;

    private TextView headerTextView;

    private Button acceptButton;
    private Button denyButton;
    private Button showProfileButton;
    private Button sendFriendRequestButton;

    private Notification notification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journey_request_dialog);

        // Initialise local variables.
        Bundle bundle = getIntent().getExtras();

        notification =  gson.fromJson(bundle.getString(IntentConstants.NOTIFICATION),  new TypeToken<Notification>() {}.getType());

        journeyRequest = gson.fromJson(bundle.getString(IntentConstants.JOURNEY_REQUEST),  new TypeToken<JourneyRequest>() {}.getType());

        if(notification != null)
        {
            new NotificationProcessor().MarkDelivered(this, appManager, notification, new WCFServiceCallback<Boolean, Void>() {
                @Override
                public void onServiceCallCompleted(ServiceResponse<Boolean> serviceResponse, Void parameter) {
                    Log.i(getClass().getSimpleName(), "Notification successfully marked as delivered");
                }
            });
        }

        // Initialise UI elements.
        headerTextView = (TextView) findViewById(R.id.JourneyRequestDialogActivityHeaderTextView);
        headerTextView.setText("Request from " + journeyRequest.User.getFirstName() + " " + journeyRequest.User.getLastName() + " ("+journeyRequest.User.getUserName()+")");

        acceptButton = (Button) findViewById(R.id.JourneyRequestDialogActivityAcceptButton);
        acceptButton.setEnabled(journeyRequest.Decision == JourneyRequestDecisions.Undecided);

        denyButton = (Button) findViewById(R.id.JourneyRequestDialogActivityDenyButton);
        denyButton.setEnabled(journeyRequest.Decision == JourneyRequestDecisions.Undecided);

        showProfileButton = (Button) findViewById(R.id.JourneyRequestDialogActivityShowProfileButton);
        sendFriendRequestButton = (Button) findViewById(R.id.JourneyRequestDialogActivitySendFriendRequestButton);

        // Setup event handlers.
        setupEventHandlers();

        // In order to not be too narrow, set the window size based on the screen resolution:
        final int screen_width = getResources().getDisplayMetrics().widthPixels;
        final int new_window_width = screen_width * 90 / 100;
        WindowManager.LayoutParams layout = getWindow().getAttributes();
        layout.width = Math.max(layout.width, new_window_width);
        getWindow().setAttributes(layout);
    }

    private void setupEventHandlers()
    {
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitDecision(RequestDecision.ACCEPTED);
            }
        });

        denyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitDecision(RequestDecision.DENIED);
            }
        });

        sendFriendRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFriendRequestActivity();
            }
        });
    }

    private void showFriendRequestActivity()
    {
        startActivity(new Intent(this, SendFriendRequestDialogActivity.class).putExtra(IntentConstants.USER, gson.toJson(journeyRequest.User)));
    }

    private void submitDecision(int decision)
    {
        if(decision == RequestDecision.UNDECIDED)
        {
            return;
        }

        journeyRequest.Decision = decision;

        new WcfPostServiceTask<JourneyRequest>(this, getResources().getString(R.string.ProcessRequestDecisionURL),
                journeyRequest,  new TypeToken<ServiceResponse<JourneyRequest>>() {}.getType(),
                appManager.getAuthorisationHeaders(), new WCFServiceCallback<JourneyRequest, Void>() {
            @Override
            public void onServiceCallCompleted(ServiceResponse<JourneyRequest> serviceResponse, Void parameter) {
                if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
                {
                    decisionSubmittedSuccessfully();
                }
            }
        }).execute();
    }

    private void decisionSubmittedSuccessfully()
    {
        Toast toast = Toast.makeText(this, "Your decision was submitted successfully.", Toast.LENGTH_LONG);
        toast.show();
        finish();
    }
}
