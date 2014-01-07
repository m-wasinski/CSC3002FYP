package com.example.myapplication.Activities.Activities;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.Activities.Base.BaseActivity;
import com.example.myapplication.Constants.RequestDecision;
import com.example.myapplication.Constants.ServiceResponseCode;
import com.example.myapplication.DomainObjects.CarShareRequest;
import com.example.myapplication.DomainObjects.ServiceResponse;
import com.example.myapplication.DomainObjects.TravelBuddyDTO;
import com.example.myapplication.Experimental.DateTimeHelper;
import com.example.myapplication.Helpers.Helpers;
import com.example.myapplication.Interfaces.WCFServiceCallback;
import com.example.myapplication.NetworkTasks.AddToBuddiesTask;
import com.example.myapplication.NetworkTasks.WCFServiceTask;
import com.example.myapplication.R;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Calendar;

/**
 * Created by Michal on 02/01/14.
 */
public class CarShareRequestDetailsActivity extends BaseActivity implements WCFServiceCallback<CarShareRequest, String>{

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.car_share_request_details);

        userNameTextView = (TextView) findViewById(R.id.CarShareRequestDetailsUserNameTextView);
        firstNameTextView = (TextView) findViewById(R.id.CarShareRequestDetailsFirstNameTextView);
        lastNameTextView = (TextView) findViewById(R.id.CarShareRequestDetailsLastNameTextView);
        genderTextView = (TextView) findViewById(R.id.CarShareRequestDetailsGenderTextView);
        emailAddressTextView = (TextView) findViewById(R.id.CarShareRequestDetailsEmailAddressTextView);
        dateOfBirthTextView = (TextView) findViewById(R.id.CarShareRequestDetailsDateOfBirthTextView);
        messageTextView = (TextView) findViewById(R.id.CarShareRequestDetailsMessageTextView);

        Type carShareRequestType = new TypeToken<CarShareRequest>() {}.getType();
        carShareRequest = gson.fromJson(getIntent().getExtras().getString("CurrentCarShareRequest"), carShareRequestType);

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

        if(carShareRequest.Decision != RequestDecision.UNDECIDED)
        {
            acceptButton.setEnabled(false);
            denyButton.setEnabled(false);

            if(carShareRequest.Decision == RequestDecision.ACCEPTED)
            {
                decisionMessageTextView.setText("You have already accepted this request!");
                decisionMessageTextView.setTextColor(Color.GREEN);
            }

            if(carShareRequest.Decision == RequestDecision.DENIED)
            {
                decisionMessageTextView.setText("You have already denied this request!");
                decisionMessageTextView.setTextColor(Color.RED);
            }
        }

        fillDetails();
    }

    private void setAccept()
    {
        this.carShareRequest.Decision = RequestDecision.ACCEPTED;
    }

    private void setDeny()
    {
        this.carShareRequest.Decision = RequestDecision.DENIED;
    }

    private void submitDecision()
    {
        this.carShareRequest.DecidedOnDate = DateTimeHelper.convertToWCFDate(Calendar.getInstance().getTime());
        Type carSharerequestType = new TypeToken<ServiceResponse<CarShareRequest>>() {}.getType();

        new WCFServiceTask<CarShareRequest, CarShareRequest>("https://findndrive.no-ip.co.uk/Services/RequestService.svc/processdecision", this.carShareRequest, carSharerequestType,
                appData.getAuthorisationHeaders(),null, this).execute();
    }

    private void fillDetails()
    {
        userNameTextView.setText(userNameTextView.getText() + carShareRequest.User.UserName);
        firstNameTextView.setText(firstNameTextView.getText() + carShareRequest.User.FirstName);
        lastNameTextView.setText(lastNameTextView.getText() + carShareRequest.User.LastName);
        genderTextView.setText(genderTextView.getText() + Helpers.TranslateGender(carShareRequest.User.Gender));
        emailAddressTextView.setText(emailAddressTextView.getText() + carShareRequest.User.EmailAddress);
        dateOfBirthTextView.setText(dateOfBirthTextView.getText() + DateTimeHelper.getSimpleDate(carShareRequest.User.DateOfBirth));
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

    @Override
    public void onServiceCallCompleted(ServiceResponse<CarShareRequest> serviceResponse, String parameter) {
        super.checkIfAuthorised(serviceResponse.ServiceResponseCode);
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
