package com.example.myapplication.activities.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.activities.base.BaseActivity;
import com.example.myapplication.constants.Decision;
import com.example.myapplication.constants.IntentConstants;
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.domain_objects.JourneyRequest;
import com.example.myapplication.domain_objects.Notification;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WcfPostServiceTask;
import com.example.myapplication.notification_management.NotificationProcessor;
import com.google.gson.reflect.TypeToken;

/**
 * Activity used to display a journey request sent by another user.
 * Gives current user the ability to accept, deny or view the profile of the person who sent the request.
 */
public class JourneyRequestDialogActivity extends BaseActivity implements View.OnClickListener, WCFServiceCallback<JourneyRequest, Void>{

    private JourneyRequest journeyRequest;

    private ProgressBar progressBar;

    private Button acceptButton;

    private Button denyButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journey_request_dialog);

        // Initialise local variables.
        Bundle bundle = getIntent().getExtras();

        Notification notification = getGson().fromJson(bundle.getString(IntentConstants.NOTIFICATION), new TypeToken<Notification>() {}.getType());

        journeyRequest = getGson().fromJson(bundle.getString(IntentConstants.JOURNEY_REQUEST),  new TypeToken<JourneyRequest>() {}.getType());

        // Check if this activity was started from a notification. If so, mark it as read.
        if(notification != null)
        {
            new NotificationProcessor().MarkDelivered(this, getAppManager(), notification, new WCFServiceCallback<Void, Void>() {
                @Override
                public void onServiceCallCompleted(ServiceResponse<Void> serviceResponse, Void parameter) {
                    Log.i(getClass().getSimpleName(), "Notification successfully marked as delivered");
                }
            });
        }

        // Initialise UI elements.

        //TextViews.
        TextView headerTextView = (TextView) findViewById(R.id.JourneyRequestDialogActivityHeaderTextView);
        headerTextView.setText("Request from " + journeyRequest.getFromUser().getFirstName() + " " + journeyRequest.getFromUser().getLastName() + " (" + journeyRequest.getFromUser().getUserName() + ")");

        // Buttons
        acceptButton = (Button) findViewById(R.id.JourneyRequestDialogActivityAcceptButton);
        acceptButton.setOnClickListener(this);
        acceptButton.setEnabled(journeyRequest.getDecision() == Decision.UNDECIDED);

        denyButton = (Button) findViewById(R.id.JourneyRequestDialogActivityDenyButton);
        denyButton.setOnClickListener(this);
        denyButton.setEnabled(journeyRequest.getDecision() == Decision.UNDECIDED);

        Button showProfileButton = (Button) findViewById(R.id.JourneyRequestDialogActivityShowProfileButton);
        showProfileButton.setOnClickListener(this);

        Button sendFriendRequestButton = (Button) findViewById(R.id.JourneyRequestDialogActivitySendFriendRequestButton);
        sendFriendRequestButton.setOnClickListener(this);

        // Progressbar
        progressBar = (ProgressBar) findViewById(R.id.JourneyRequestDialogActivityProgressBar);

        // In order to not be too narrow, set the window size based on the screen resolution:
        final int screen_width = getResources().getDisplayMetrics().widthPixels;
        final int new_window_width = screen_width * 90 / 100;
        WindowManager.LayoutParams layout = getWindow().getAttributes();
        layout.width = Math.max(layout.width, new_window_width);
        getWindow().setAttributes(layout);
    }

    /**
     * Sets the decision accepted/denied and triggers a new call to the web service to process it.
     *
     * @param decision
     */
    private void submitDecision(int decision)
    {
        progressBar.setVisibility(View.VISIBLE);
        if(decision == Decision.UNDECIDED)
        {
            return;
        }

        // Set the decision.
        journeyRequest.setDecision(decision);

        // Call the web service to process it.
        new WcfPostServiceTask<JourneyRequest>(this, getResources().getString(R.string.ProcessRequestDecisionURL),
                journeyRequest,  new TypeToken<ServiceResponse<JourneyRequest>>() {}.getType(),
                getAppManager().getAuthorisationHeaders(), this).execute();
    }

    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            case R.id.JourneyRequestDialogActivityAcceptButton:
                acceptButton.setEnabled(false);
                submitDecision(Decision.ACCEPTED);
                break;
            case R.id.JourneyRequestDialogActivityDenyButton:
                denyButton.setEnabled(false);
                submitDecision(Decision.DENIED);
                break;
            case R.id.JourneyRequestDialogActivityShowProfileButton:
                Bundle bundle = new Bundle();
                bundle.putInt(IntentConstants.PROFILE_VIEWER_MODE, IntentConstants.PROFILE_VIEWER_VIEWING);
                bundle.putInt(IntentConstants.USER, journeyRequest.getFromUser().getUserId());
                startActivity(new Intent(this, ProfileViewerActivity.class).putExtras(bundle));
                break;
            case R.id.JourneyRequestDialogActivitySendFriendRequestButton:
                startActivity(new Intent(this, SendFriendRequestActivity.class).putExtra(IntentConstants.USER, getGson().toJson(journeyRequest.getFromUser())));
                break;
        }
    }

    /**
     * Called after response to this request is processed by the web service.
     *
     * @param serviceResponse
     * @param parameter
     */
    @Override
    public void onServiceCallCompleted(ServiceResponse<JourneyRequest> serviceResponse, Void parameter) {
        progressBar.setVisibility(View.GONE);
        if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
        {
            Toast.makeText(this, "Your decision was submitted successfully.", Toast.LENGTH_LONG).show();
            finish();
        }
    }
}
