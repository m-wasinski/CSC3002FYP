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
import com.example.myapplication.domain_objects.FriendRequest;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.domain_objects.User;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WcfPostServiceTask;
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
        this.targetUser = this.gson.fromJson(getIntent().getExtras().getString(IntentConstants.USER), new TypeToken<User>() {}.getType());

        //Initialise UI elements.
        this.messageEditText = (EditText) this.findViewById(R.id.SendFriendRequestActivityMessageEditText);
        this.okButton = (Button) this.findViewById(R.id.SendFriendRequestActivitySendButton);
        this.headerTextView = (TextView) this.findViewById(R.id.SendFriendRequestActivityHeaderTextView);
        this.headerTextView.setText(this.headerTextView.getText().toString() + " " + this.targetUser.getFirstName() + " " + this.targetUser.getLastName() + " ("+this.targetUser.getUserName()+")");

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
        friendRequest.TargetUserId = this.targetUser.getUserId();
        friendRequest.RequestingUserId = this.appManager.getUser().getUserId();

        new WcfPostServiceTask<FriendRequest>(this,
                this.getResources().getString(R.string.SendFriendRequestURL), friendRequest,
                new TypeToken<ServiceResponse<Boolean>>() {}.getType(), appManager.getAuthorisationHeaders(), this).execute();
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
