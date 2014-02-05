package com.example.myapplication.activities.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.activities.base.BaseActivity;
import com.example.myapplication.constants.RequestDecision;
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.domain_objects.Journey;
import com.example.myapplication.domain_objects.JourneyRequest;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.experimental.DateTimeHelper;
import com.example.myapplication.utilities.Helpers;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WCFServiceTask;
import com.example.myapplication.R;
import com.google.gson.reflect.TypeToken;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Michal on 02/01/14.
 */
public class ContactDriverActivity extends BaseActivity implements WCFServiceCallback<JourneyRequest, String>{

    private TextView messageTextView;
    private Button sendRequestButton;
    private CheckBox addToBuddies;
    private Journey journey;
    private ArrayList<JourneyRequest> journeyRequests;
    private JourneyRequest journeyRequest;
    private DecimalFormat decimalFormat;

    private TextView driverUserNameTextView;
    private TextView driverNameTextView;
    private TextView driverGenderTextView;
    private TextView driverDateOfBirthTextView;
    private TextView driverRatingTextView;

    private TextView carShareCitiesTextView;
    private TextView carShareDateAndTimeTextView;
    private TextView carShareSmokersTextView;
    private TextView carSharePetsTextView;
    private TextView carShareGenderTextView;
    private TextView carShareSeatsTextView;
    private TextView carShareFeeTextView;
    private TextView carShareVehicleTextView;
    private TextView carShareDescriptionTextView;
    private TextView requestStatusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(com.example.myapplication.R.layout.activity_contact_driver);

        decimalFormat = new DecimalFormat("0.00");
        journey = gson.fromJson(getIntent().getExtras().getString("CurrentCarShare"),  new TypeToken<Journey>() {}.getType());
        journeyRequests = gson.fromJson(getIntent().getExtras().getString("CurrentRequests"),  new TypeToken<ArrayList<JourneyRequest>>() {}.getType());

        messageTextView = (TextView) findViewById(R.id.ContactDriverMessageTextView);
        sendRequestButton = (Button) findViewById(R.id.ContactDriverSendRequestButton);
        addToBuddies = (CheckBox) findViewById(R.id.ContactDriverAddToTravelBuddiesCheckbox);
        sendRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendRequest();
            }
        });

        driverUserNameTextView = (TextView) findViewById(R.id.ContactDriverUserNameTextView);
        driverNameTextView = (TextView) findViewById(R.id.ContactDriverDriverNameTextView);
        driverGenderTextView = (TextView) findViewById(R.id.ContactDriverDriverGenderTextView);
        driverDateOfBirthTextView = (TextView) findViewById(R.id.ContactDriverDriverDoBTextView);
        driverRatingTextView = (TextView) findViewById(R.id.ContactDriverDriverRatingTextView);

        carShareCitiesTextView = (TextView) findViewById(R.id.ContactDriverCitiesTextView);
        carShareDateAndTimeTextView = (TextView) findViewById(R.id.ContactDriverDateAndTimeTextView);
        carShareSmokersTextView = (TextView) findViewById(R.id.ContactDriverSmokersTextView);
        carSharePetsTextView = (TextView) findViewById(R.id.ContactDriverPetsTextView);
        carShareGenderTextView = (TextView) findViewById(R.id.ContactDriverGenderTextView);
        carShareSeatsTextView = (TextView) findViewById(R.id.ContactDriverSeatsTextView);
        carShareFeeTextView = (TextView) findViewById(R.id.ContactDriverFeeTextView);
        carShareVehicleTextView = (TextView) findViewById(R.id.ContactDriverVehicleTextView);
        carShareDescriptionTextView = (TextView) findViewById(R.id.ContactDriverDescriptionTextView);

        requestStatusTextView = (TextView) findViewById(R.id.ContactDriverStatusTextView);

        for(JourneyRequest request : journeyRequests)
        {
            if(request.Decision == RequestDecision.UNDECIDED && request.UserId == findNDriveManager.getUser().UserId)
            {
                requestStatusTextView.setText("You already have a pending request for this journey.");
                requestStatusTextView.setTextColor(Color.RED);
                sendRequestButton.setEnabled(false);
                requestStatusTextView.setVisibility(View.VISIBLE);
            }

            if(request.Decision == RequestDecision.ACCEPTED && request.UserId == findNDriveManager.getUser().UserId)
            {
                requestStatusTextView.setText("You are already a passenger in this journey.");
                requestStatusTextView.setTextColor(Color.GREEN);
                sendRequestButton.setEnabled(false);
                requestStatusTextView.setVisibility(View.VISIBLE);
            }
        }

        if(journey.Driver.UserId == findNDriveManager.getUser().UserId)
        {
            requestStatusTextView.setText("You are the driver in this journey!");
            requestStatusTextView.setTextColor(Color.GREEN);
            requestStatusTextView.setVisibility(View.VISIBLE);
            sendRequestButton.setEnabled(false);
        }

        populateFields();
    }

    private void sendRequest(){
        journeyRequest = new JourneyRequest();
        journeyRequest.AddToTravelBuddies = addToBuddies.isChecked();
        journeyRequest.UserId = findNDriveManager.getUser().UserId;
        journeyRequest.User = findNDriveManager.getUser();
        journeyRequest.JourneyId = journey.JourneyId;
        journeyRequest.Message = messageTextView.getText().toString();
        journeyRequest.Read = false;
        journeyRequest.Decision = RequestDecision.UNDECIDED;
        journeyRequest.SentOnDate = DateTimeHelper.convertToWCFDate(Calendar.getInstance().getTime());
        new WCFServiceTask<JourneyRequest>(this, getResources().getString(R.string.SendRequestURL),
                this.journeyRequest, new TypeToken<ServiceResponse<JourneyRequest>>() {}.getType(), findNDriveManager.getAuthorisationHeaders(), this).execute();
    }

    private void populateFields()
    {
        driverUserNameTextView.setText(journey.Driver.UserName);
        driverNameTextView.setText(journey.Driver.FirstName + " " + journey.Driver.LastName);
        driverGenderTextView.setText(Helpers.translateGender(journey.Driver.Gender));
        driverDateOfBirthTextView.setText(DateTimeHelper.getSimpleDate(journey.Driver.DateOfBirth));
        driverRatingTextView.setText("TODO");

        //carShareCitiesTextView.setText(journey.DepartureCity +" to " + journey.DestinationCity);
        //carShareDateAndTimeTextView.setText("Leaving on: " + DateTimeHelper.getSimpleDate(journey.DateAndTimeOfDeparture)
        //        + " at: " + DateTimeHelper.getSimpleTime(journey.DateAndTimeOfDeparture));
        carShareSmokersTextView.setText(carShareSmokersTextView.getText() + Helpers.translateBoolean(journey.SmokersAllowed));
        carSharePetsTextView.setText(carSharePetsTextView.getText() + Helpers.translateBoolean(journey.PetsAllowed));
        carShareGenderTextView.setText(carShareGenderTextView.getText() + "TODO");
        carShareSeatsTextView.setText(carShareSeatsTextView.getText() + ""+ journey.AvailableSeats);
        carShareFeeTextView.setText(carShareFeeTextView.getText() + "£"+decimalFormat.format(journey.Fee));
        carShareVehicleTextView.setText(carShareVehicleTextView.getText() + "TODO");
        carShareDescriptionTextView.setText(carShareDescriptionTextView.getText() + journey.Description);
    }

    @Override
    public void onServiceCallCompleted(ServiceResponse<JourneyRequest> serviceResponse, String parameter) {
        if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
        {
            Toast toast = Toast.makeText(this, "Your request was sent successfully!", Toast.LENGTH_LONG);
            toast.show();
            finish();
        }
    }
}
