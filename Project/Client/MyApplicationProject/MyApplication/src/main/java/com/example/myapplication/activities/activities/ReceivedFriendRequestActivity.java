package com.example.myapplication.activities.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.myapplication.R;
import com.example.myapplication.activities.base.BaseActivity;
import com.example.myapplication.constants.Decision;
import com.example.myapplication.constants.IntentConstants;
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.domain_objects.FriendRequest;
import com.example.myapplication.domain_objects.Notification;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.interfaces.WCFImageRetrieved;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WcfPictureServiceTask;
import com.example.myapplication.network_tasks.WcfPostServiceTask;
import com.example.myapplication.notification_management.NotificationProcessor;
import com.google.gson.reflect.TypeToken;

/**
 * Displays friends request that has been received from another user.
 * Provides all the necessary functionality to accept or deny the request as well as vies the other user's profile.
 */
public class ReceivedFriendRequestActivity extends BaseActivity implements View.OnClickListener,
        WCFServiceCallback<Void, Void>, WCFImageRetrieved{

    private FriendRequest friendRequest;

    private ImageView profileImageView;

    private ProgressBar progressBar;

    private Button acceptButton;
    private Button denyButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_request_received);

        // Initialise local variables.
        Bundle bundle = getIntent().getExtras();

        // Check if this activity has been started from a notification, if yes, mark it as read.
        Notification notification = getGson().fromJson(bundle.getString(IntentConstants.NOTIFICATION),
                new TypeToken<Notification>() {}.getType());

        friendRequest =  getGson().fromJson(bundle.getString(IntentConstants.FRIEND_REQUEST), new TypeToken<FriendRequest>() {}.getType());

        if(notification != null)
        {
            new NotificationProcessor().MarkDelivered(this, getAppManager(), notification, new WCFServiceCallback<Void, Void>() {
                @Override
                public void onServiceCallCompleted(ServiceResponse<Void> serviceResponse, Void parameter) {
                    Log.i(getClass().getSimpleName(), "Notification successfully marked as delivered");
                }
            });
        }

        // Initialise UI elements and setup their event handlers.
        acceptButton = (Button) findViewById(R.id.FriendRequestReceivedActivityAcceptButton);
        acceptButton.setOnClickListener(this);
        acceptButton.setEnabled(friendRequest.getDecision() == Decision.UNDECIDED);

        denyButton = (Button) findViewById(R.id.FriendRequestReceivedActivityDenyButton);
        denyButton.setOnClickListener(this);
        denyButton.setEnabled(friendRequest.getDecision() == Decision.UNDECIDED);

        TextView messageTextView = (TextView) findViewById(R.id.FriendRequestReceivedMessageTextView);

        Button showProfileButton = (Button) findViewById(R.id.FriendRequestReceivedViewProfileButton);
        showProfileButton.setOnClickListener(this);
        TextView headerTextView = (TextView) findViewById(R.id.FriendRequestReceiverHeaderTextView);
        headerTextView.setText(friendRequest.getFromUser().getUserName());
        messageTextView.setVisibility(friendRequest.getMessage() == null ? View.GONE : View.VISIBLE);
        messageTextView.setText(friendRequest.getMessage() == null ? "" : friendRequest.getMessage());

        progressBar = (ProgressBar) findViewById(R.id.FriendRequestReceivedActivityProgressBar);

        profileImageView = (ImageView) findViewById(R.id.FriendRequestReceivedActivityProfileImageView);
        retrieveProfilePicture();
        // In order to not be too narrow, set the window size based on the screen resolution:
        final int screen_width = getResources().getDisplayMetrics().widthPixels;
        final int new_window_width = screen_width * 90 / 100;
        WindowManager.LayoutParams layout = getWindow().getAttributes();
        layout.width = Math.max(layout.width, new_window_width);
        getWindow().setAttributes(layout);
    }

    /**
     * Calls the web service with the user's decision regarding this friend request.
     * @param decision - decision being submitted by the user.
     */
    private void submitDecision(int decision)
    {
        progressBar.setVisibility(View.VISIBLE);
        friendRequest.setDecision(decision);

        new WcfPostServiceTask<FriendRequest>(this,
                getResources().getString(R.string.ProcessFriendRequestDecisionURL), friendRequest,
                new TypeToken<ServiceResponse<Void>>() {}.getType(), getAppManager().getAuthorisationHeaders(), this).execute();
    }

    /**
     * Retrieves profile picture of the user who sent the friend request.
     */
    private void retrieveProfilePicture()
    {
        new WcfPictureServiceTask(this.getAppManager().getBitmapLruCache(), getResources().getString(R.string.GetProfilePictureURL),
                friendRequest.getFromUser().getUserId(), this.getAppManager().getAuthorisationHeaders(), this).execute();
    }

    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            case R.id.FriendRequestReceivedViewProfileButton:
                Bundle bundle = new Bundle();
                bundle.putInt(IntentConstants.PROFILE_VIEWER_MODE, IntentConstants.PROFILE_VIEWER_VIEWING);
                bundle.putInt(IntentConstants.USER, friendRequest.getFromUser().getUserId());
                startActivity(new Intent(this, ProfileViewerActivity.class).putExtras(bundle));
                break;
            case R.id.FriendRequestReceivedActivityAcceptButton:
                acceptButton.setEnabled(false);
                submitDecision(Decision.ACCEPTED);
                break;
            case R.id.FriendRequestReceivedActivityDenyButton:
                denyButton.setEnabled(false);
                submitDecision(Decision.DENIED);
                break;
        }
    }

    /**
     * Called after the friend request decision has been processed by the web service.
     *
     * @param serviceResponse
     * @param parameter
     */
    @Override
    public void onServiceCallCompleted(ServiceResponse<Void> serviceResponse, Void parameter)
    {
        progressBar.setVisibility(View.GONE);
        if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
        {
            finish();
        }
    }

    /**
     * Called after the profile picture of the user who sent the friend request has been retrieved.
     * @param bitmap
     */
    @Override
    public void onImageRetrieved(Bitmap bitmap) {
        if(bitmap != null)
        {
            profileImageView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, bitmap.getWidth()/4, bitmap.getHeight()/4, false));
        }
    }
}
