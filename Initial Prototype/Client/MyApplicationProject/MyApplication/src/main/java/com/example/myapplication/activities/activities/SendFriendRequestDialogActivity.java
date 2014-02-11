package com.example.myapplication.activities.activities;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.activities.base.BaseActivity;
import com.example.myapplication.constants.IntentConstants;
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.constants.TokenTypes;
import com.example.myapplication.domain_objects.FriendRequest;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.domain_objects.User;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WCFServiceTask;
import com.google.gson.reflect.TypeToken;

/**
 * Created by Michal on 10/02/14.
 */
public class SendFriendRequestDialogActivity extends BaseActivity implements WCFServiceCallback<Boolean, Void> {

    private EditText messageEditText;
    private Button okButton;
    private TextView headerTextView;

    private User targetUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_send_friend_request);

        //Initialise local variables.
        this.targetUser = this.gson.fromJson(getIntent().getExtras().getString(IntentConstants.USER), TokenTypes.getUserToken());

        //Initialise UI elements.
        this.messageEditText = (EditText) this.findViewById(R.id.SendFriendRequestActivityMessageEditText);
        this.okButton = (Button) this.findViewById(R.id.SendFriendRequestActivitySendButton);
        this.headerTextView = (TextView) this.findViewById(R.id.SendFriendRequestActivityHeaderTextView);

        // Setup event handlers.
        this.setupEventHandlers();
    }

    private void setupEventHandlers()
    {
        this.okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendFriendRequest();
            }
        });
    }

    private void sendFriendRequest()
    {
        FriendRequest friendRequest = new FriendRequest();
        friendRequest.Message = this.messageEditText.getText().toString();
        friendRequest.TargetUserId = this.targetUser.UserId;
        friendRequest.RequestingUserId = this.findNDriveManager.getUser().UserId;

        new WCFServiceTask<FriendRequest>(this,
                this.getResources().getString(R.string.SendFriendRequestURL), friendRequest,
                TokenTypes.getServiceResponseBooleanToken(), findNDriveManager.getAuthorisationHeaders(), this).execute();
    }

    @Override
    public void onServiceCallCompleted(ServiceResponse<Boolean> serviceResponse, Void parameter) {
        if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
        {
            Toast toast = Toast.makeText(this, "Friend request was sent successfully.", Toast.LENGTH_LONG);
            toast.show();
            this.finish();
        }
    }
}
