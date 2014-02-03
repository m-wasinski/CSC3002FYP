package com.example.myapplication.activities.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.activities.base.BaseActivity;
import com.example.myapplication.constants.RequestDecision;
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.dtos.FriendDTO;
import com.example.myapplication.domain_objects.JourneyRequest;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.experimental.DateTimeHelper;
import com.example.myapplication.utilities.Helpers;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WCFServiceTask;
import com.example.myapplication.R;
import com.google.gson.reflect.TypeToken;

import java.util.Calendar;

/**
 * Created by Michal on 02/01/14.
 */
public class JourneyRequestDetailsActivity extends BaseActivity implements WCFServiceCallback<JourneyRequest, String>{

    private Button acceptButton;
    private Button denyButton;
    private Button addToBuddiesButton;
    private JourneyRequest journeyRequest;
    private TextView userNameTextView;
    private TextView firstNameTextView;
    private TextView lastNameTextView;
    private TextView genderTextView;
    private TextView emailAddressTextView;
    private TextView dateOfBirthTextView;
    private TextView messageTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journey_request_details);

        userNameTextView = (TextView) findViewById(R.id.CarShareRequestDetailsUserNameTextView);
        firstNameTextView = (TextView) findViewById(R.id.CarShareRequestDetailsFirstNameTextView);
        lastNameTextView = (TextView) findViewById(R.id.CarShareRequestDetailsLastNameTextView);
        genderTextView = (TextView) findViewById(R.id.CarShareRequestDetailsGenderTextView);
        emailAddressTextView = (TextView) findViewById(R.id.CarShareRequestDetailsEmailAddressTextView);
        dateOfBirthTextView = (TextView) findViewById(R.id.CarShareRequestDetailsDateOfBirthTextView);
        messageTextView = (TextView) findViewById(R.id.CarShareRequestDetailsMessageTextView);

        journeyRequest = gson.fromJson(getIntent().getExtras().getString("CurrentCarShareRequest"), new TypeToken<JourneyRequest>() {}.getType());

        TextView decisionMessageTextView = (TextView) findViewById(R.id.CarShareRequestDecisionMessage);

        addToBuddiesButton = (Button) findViewById(R.id.CarShareRequestDetailsAddToTravelBuddiesButton);
        addToBuddiesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addTravelBuddy();
            }
        });

        acceptButton = (Button) findViewById(R.id.CarShareRequestDetailsAcceptButton);
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setAccept();
                submitDecision();
            }
        });
        denyButton = (Button) findViewById(R.id.CarShareRequestDetailsDenyButton);
        denyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDeny();
                submitDecision();
            }
        });

        if(journeyRequest.Decision != RequestDecision.UNDECIDED)
        {
            acceptButton.setEnabled(false);
            denyButton.setEnabled(false);

            if(journeyRequest.Decision == RequestDecision.ACCEPTED)
            {
                decisionMessageTextView.setText("You have already accepted this request!");
                decisionMessageTextView.setTextColor(Color.GREEN);
            }

            if(journeyRequest.Decision == RequestDecision.DENIED)
            {
                decisionMessageTextView.setText("You have already denied this request!");
                decisionMessageTextView.setTextColor(Color.RED);
            }
        }

        fillDetails();
    }

    private void setAccept()
    {
        this.journeyRequest.Decision = RequestDecision.ACCEPTED;
    }

    private void setDeny()
    {
        this.journeyRequest.Decision = RequestDecision.DENIED;
    }

    private void submitDecision()
    {
        this.journeyRequest.DecidedOnDate = DateTimeHelper.convertToWCFDate(Calendar.getInstance().getTime());

        new WCFServiceTask<JourneyRequest>(this, getResources().getString(R.string.ProcessRequestDecisionURL),
                this.journeyRequest, new TypeToken<ServiceResponse<JourneyRequest>>() {}.getType(),
                findNDriveManager.getAuthorisationHeaders(), this).execute();
    }

    private void fillDetails()
    {
        userNameTextView.setText(userNameTextView.getText() + journeyRequest.User.UserName);
        firstNameTextView.setText(firstNameTextView.getText() + journeyRequest.User.FirstName);
        lastNameTextView.setText(lastNameTextView.getText() + journeyRequest.User.LastName);
        genderTextView.setText(genderTextView.getText() + Helpers.TranslateGender(journeyRequest.User.Gender));
        emailAddressTextView.setText(emailAddressTextView.getText() + journeyRequest.User.EmailAddress);
        dateOfBirthTextView.setText(dateOfBirthTextView.getText() + DateTimeHelper.getSimpleDate(journeyRequest.User.DateOfBirth));
        messageTextView.setText(messageTextView.getText() + journeyRequest.Message);
    }

    private void addTravelBuddy()
    {
        FriendDTO friendDTO = new FriendDTO();
        friendDTO.TargetUserId = findNDriveManager.getUser().UserId;
        friendDTO.FriendUserId = journeyRequest.UserId;
    }

    @Override
    public void onServiceCallCompleted(ServiceResponse<JourneyRequest> serviceResponse, String parameter) {
        if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
        {
            Toast toast = Toast.makeText(this, "Your reply was processed successfully.", Toast.LENGTH_LONG);
            toast.show();

            acceptButton.setEnabled(false);
            denyButton.setEnabled(false);

            finish();
        }
    }
}
