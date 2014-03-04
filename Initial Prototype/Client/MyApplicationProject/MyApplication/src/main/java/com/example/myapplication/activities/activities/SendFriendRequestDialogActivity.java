package com.example.myapplication.activities.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.activities.base.BaseActivity;
import com.example.myapplication.constants.IntentConstants;
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.domain_objects.FriendRequest;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.domain_objects.User;
import com.example.myapplication.interfaces.WCFImageRetrieved;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WcfPictureServiceTask;
import com.example.myapplication.network_tasks.WcfPostServiceTask;
import com.google.gson.reflect.TypeToken;

/**
 * Created by Michal on 10/02/14.
 */
public class SendFriendRequestDialogActivity extends BaseActivity implements WCFServiceCallback<Boolean, Void> {

    private EditText messageEditText;

    private Button okButton;

    private TextView headerTextView;

    private ImageView profileIconImageView;

    private User targetUser;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_friend_request);

        //Initialise local variables.
        targetUser = gson.fromJson(getIntent().getExtras().getString(IntentConstants.USER), new TypeToken<User>() {}.getType());

        //Initialise UI elements.
        messageEditText = (EditText) findViewById(R.id.SendFriendRequestActivityMessageEditText);
        okButton = (Button) findViewById(R.id.SendFriendRequestActivitySendButton);
        headerTextView = (TextView) findViewById(R.id.SendFriendRequestActivityHeaderTextView);
        headerTextView.setText(headerTextView.getText().toString() + " " + targetUser.getFirstName() + " " + targetUser.getLastName() + " ("+targetUser.getUserName()+")");
        profileIconImageView = (ImageView) findViewById(R.id.AlertDialogSendFriendRequestImageView);
        progressBar = (ProgressBar) findViewById(R.id.SendFriendRequestProgressBar);

        retrieveProfilePicture();

        // Setup event handlers.
        setupEventHandlers();
    }

    private void setupEventHandlers()
    {
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendFriendRequest();
            }
        });
    }

    private void sendFriendRequest()
    {
        progressBar.setVisibility(View.VISIBLE);
        okButton.setEnabled(false);
        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setMessage(messageEditText.getText().toString());
        friendRequest.setToUser(targetUser);
        friendRequest.setFromUser(appManager.getUser());

        new WcfPostServiceTask<FriendRequest>(this,
                getResources().getString(R.string.SendFriendRequestURL), friendRequest,
                new TypeToken<ServiceResponse<Boolean>>() {}.getType(), appManager.getAuthorisationHeaders(), this).execute();
    }

    @Override
    public void onServiceCallCompleted(ServiceResponse<Boolean> serviceResponse, Void parameter) {
        progressBar.setVisibility(View.GONE);
        if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
        {
            Toast.makeText(this, "Your friend request was sent successfully.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void retrieveProfilePicture()
    {
        new WcfPictureServiceTask(appManager.getBitmapLruCache(), getResources().getString(R.string.GetProfilePictureURL),
                targetUser.getUserId(), appManager.getAuthorisationHeaders(), new WCFImageRetrieved() {
            @Override
            public void onImageRetrieved(Bitmap bitmap) {
                if(bitmap != null)
                {
                    profileIconImageView.setImageBitmap(bitmap);
                }
            }
        }).execute();
    }
}
