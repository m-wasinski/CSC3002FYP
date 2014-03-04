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
import android.widget.TextView;

import com.example.myapplication.R;
import com.example.myapplication.activities.base.BaseActivity;
import com.example.myapplication.constants.FriendRequestDecisions;
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
 * Created by Michal on 10/02/14.
 */
public class ReceivedFriendRequestDialogActivity extends BaseActivity implements View.OnClickListener,
        WCFServiceCallback<Boolean, Void>, WCFImageRetrieved{

    private FriendRequest friendRequest;

    private ImageView profileImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_request_received);

        // Initialise local variables.
        Bundle bundle = getIntent().getExtras();

        Notification notification = gson.fromJson(bundle.getString(IntentConstants.NOTIFICATION),
                new TypeToken<Notification>() {}.getType());

        friendRequest =  gson.fromJson(bundle.getString(IntentConstants.FRIEND_REQUEST), new TypeToken<FriendRequest>() {}.getType());

        if(notification != null)
        {
            new NotificationProcessor().MarkDelivered(this, appManager, notification, new WCFServiceCallback<Boolean, Void>() {
                @Override
                public void onServiceCallCompleted(ServiceResponse<Boolean> serviceResponse, Void parameter) {
                    Log.i(getClass().getSimpleName(), "Notification successfully marked as delivered");
                }
            });
        }

        // Initialise UI elements and setup their event handlers.
        Button acceptButton = (Button) findViewById(R.id.FriendRequestReceivedActivityAcceptButton);
        acceptButton.setOnClickListener(this);
        acceptButton.setEnabled(friendRequest.getFriendRequestDecision() == FriendRequestDecisions.Undecided);
        Button denyButton = (Button) findViewById(R.id.FriendRequestReceivedActivityDenyButton);
        denyButton.setOnClickListener(this);
        denyButton.setEnabled(friendRequest.getFriendRequestDecision() == FriendRequestDecisions.Undecided);
        TextView messageTextView = (TextView) findViewById(R.id.FriendRequestReceivedMessageTextView);

        Button showProfileButton = (Button) findViewById(R.id.FriendRequestReceivedViewProfileButton);
        showProfileButton.setOnClickListener(this);
        TextView headerTextView = (TextView) findViewById(R.id.FriendRequestReceiverHeaderTextView);
        headerTextView.setText(friendRequest.getFromUser().getUserName());
        messageTextView.setVisibility(friendRequest.getMessage() == null ? View.GONE : View.VISIBLE);
        messageTextView.setText(friendRequest.getMessage() == null ? "" : friendRequest.getMessage());

        profileImageView = (ImageView) findViewById(R.id.FriendRequestReceivedActivityProfileImageView);
        retrieveProfilePicture();
        // In order to not be too narrow, set the window size based on the screen resolution:
        final int screen_width = getResources().getDisplayMetrics().widthPixels;
        final int new_window_width = screen_width * 90 / 100;
        WindowManager.LayoutParams layout = getWindow().getAttributes();
        layout.width = Math.max(layout.width, new_window_width);
        getWindow().setAttributes(layout);
    }

    private void submitDecision(int decision)
    {
        friendRequest.setFriendRequestDecision(decision);

        new WcfPostServiceTask<FriendRequest>(this,
                getResources().getString(R.string.ProcessFriendRequestDecisionURL), friendRequest,
                new TypeToken<ServiceResponse<Boolean>>() {}.getType(), appManager.getAuthorisationHeaders(), this).execute();
    }

    private void retrieveProfilePicture()
    {
        new WcfPictureServiceTask(this.appManager.getBitmapLruCache(), getResources().getString(R.string.GetProfilePictureURL),
                friendRequest.getFromUser().getUserId(), this.appManager.getAuthorisationHeaders(), this).execute();
    }

    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            case R.id.FriendRequestReceivedViewProfileButton:
                startActivity(new Intent(this, ProfileViewerActivity.class).putExtra(IntentConstants.USER, gson.toJson(friendRequest.getFromUser())));
                break;
            case R.id.FriendRequestReceivedActivityAcceptButton:
                submitDecision(FriendRequestDecisions.Accepted);
                break;
            case R.id.FriendRequestReceivedActivityDenyButton:
                submitDecision(FriendRequestDecisions.Denied);
                break;
        }
    }

    @Override
    public void onServiceCallCompleted(ServiceResponse<Boolean> serviceResponse, Void parameter)
    {
        if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
        {
            finish();
        }
    }


    @Override
    public void onImageRetrieved(Bitmap bitmap) {
        if(bitmap != null)
        {
            profileImageView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, bitmap.getWidth()/4, bitmap.getHeight()/4, false));
        }
    }
}
