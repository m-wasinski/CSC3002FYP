package com.example.myapplication.Activities;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.Constants.Constants;
import com.example.myapplication.DomainObjects.CarShareRequest;
import com.example.myapplication.DomainObjects.ServiceResponse;
import com.example.myapplication.DomainObjects.TravelBuddyDTO;
import com.example.myapplication.Experimental.AppData;
import com.example.myapplication.Experimental.WCFDateTimeHelper;
import com.example.myapplication.Helpers.Helpers;
import com.example.myapplication.Interfaces.DecisionSubmittedInterface;
import com.example.myapplication.NetworkTasks.AddToBuddiesTask;
import com.example.myapplication.NetworkTasks.SubmitRequestDecisionTask;
import com.example.myapplication.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

/**
 * Created by Michal on 02/01/14.
 */
public class CarShareRequestDetailsActivity extends Activity implements DecisionSubmittedInterface{

    private Button acceptButton;
    private Button denyButton;
    private Button addToBuddiesButton;
    private CarShareRequest carShareRequest;
    private TextView userNameTextView;
    private TextView firstNameTextView;
    private TextView lastNameTextView;
    private TextView genderTextView;
    private TextView emailAddressTextView;
    private TextView dateOfBirthTextView;
    private TextView messageTextView;
    private AppData appData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.car_share_request_details);
        appData = ((AppData)getApplication());
        userNameTextView = (TextView) findViewById(R.id.CarShareRequestDetailsUserNameTextView);
        firstNameTextView = (TextView) findViewById(R.id.CarShareRequestDetailsFirstNameTextView);
        lastNameTextView = (TextView) findViewById(R.id.CarShareRequestDetailsLastNameTextView);
        genderTextView = (TextView) findViewById(R.id.CarShareRequestDetailsGenderTextView);
        emailAddressTextView = (TextView) findViewById(R.id.CarShareRequestDetailsEmailAddressTextView);
        dateOfBirthTextView = (TextView) findViewById(R.id.CarShareRequestDetailsDateOfBirthTextView);
        messageTextView = (TextView) findViewById(R.id.CarShareRequestDetailsMessageTextView);

        Gson gson = new Gson();
        Type carShareRequestType = new TypeToken<CarShareRequest>() {}.getType();
        carShareRequest = gson.fromJson(getIntent().getExtras().getString("CurrentCarShareRequest"), carShareRequestType);
        Log.i("Request1234", getIntent().getExtras().getString("CurrentCarShareRequest"));
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

        if(carShareRequest.Decision != Constants.UNDECIDED)
        {
            acceptButton.setEnabled(false);
            denyButton.setEnabled(false);

            if(carShareRequest.Decision == Constants.ACCEPTED)
            {
                decisionMessageTextView.setText("You have already accepted this request!");
                decisionMessageTextView.setTextColor(Color.GREEN);
            }

            if(carShareRequest.Decision == Constants.DENIED)
            {
                decisionMessageTextView.setText("You have already denied this request!");
                decisionMessageTextView.setTextColor(Color.RED);
            }
        }

        fillDetails();
    }

    private void setAccept()
    {
        this.carShareRequest.Decision = Constants.ACCEPTED;
    }

    private void setDeny()
    {
        this.carShareRequest.Decision = Constants.DENIED;
    }

    private void submitDecision()
    {
        SubmitRequestDecisionTask submitRequestDecisionTask = new SubmitRequestDecisionTask(this.carShareRequest, this);
        submitRequestDecisionTask.execute();
    }

    @Override
    public void decisionSubmitted(ServiceResponse<CarShareRequest> serviceResponse) {
        if(serviceResponse.ServiceResponseCode == Constants.SERVICE_RESPONSE_SUCCESS)
        {
            Toast toast = Toast.makeText(this, "Your reply was processed successfully.", Toast.LENGTH_LONG);
            toast.show();

            acceptButton.setEnabled(false);
            denyButton.setEnabled(false);

            finish();
        }
    }

    private void fillDetails()
    {
        userNameTextView.setText(userNameTextView.getText() + carShareRequest.User.UserName);
        firstNameTextView.setText(firstNameTextView.getText() + carShareRequest.User.FirstName);
        lastNameTextView.setText(lastNameTextView.getText() + carShareRequest.User.LastName);
        genderTextView.setText(genderTextView.getText() + Helpers.TranslateGender(carShareRequest.User.Gender));
        emailAddressTextView.setText(emailAddressTextView.getText() + carShareRequest.User.EmailAddress);
        dateOfBirthTextView.setText(dateOfBirthTextView.getText() + WCFDateTimeHelper.GetSimpleDate(carShareRequest.User.DateOfBirth));
        messageTextView.setText(messageTextView.getText() + carShareRequest.Message);
    }

    private void addTravelBuddy()
    {
        TravelBuddyDTO travelBuddyDTO = new TravelBuddyDTO();
        travelBuddyDTO.TargetUserId = appData.getUser().UserId;
        travelBuddyDTO.TravelBuddyUserId = carShareRequest.UserId;
        AddToBuddiesTask addToBuddiesTask = new AddToBuddiesTask(travelBuddyDTO);
        addToBuddiesTask.execute();
    }
}
