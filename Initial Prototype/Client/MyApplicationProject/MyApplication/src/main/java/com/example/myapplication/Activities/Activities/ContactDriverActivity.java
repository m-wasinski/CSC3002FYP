package com.example.myapplication.Activities.Activities;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.Activities.Base.BaseActivity;
import com.example.myapplication.Constants.RequestDecision;
import com.example.myapplication.Constants.ServiceResponseCode;
import com.example.myapplication.DomainObjects.CarShare;
import com.example.myapplication.DomainObjects.CarShareRequest;
import com.example.myapplication.DomainObjects.ServiceResponse;
import com.example.myapplication.Experimental.DateTimeHelper;
import com.example.myapplication.Helpers.Helpers;
import com.example.myapplication.Interfaces.WCFServiceCallback;
import com.example.myapplication.NetworkTasks.WCFServiceTask;
import com.example.myapplication.R;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Michal on 02/01/14.
 */
public class ContactDriverActivity extends BaseActivity implements WCFServiceCallback<CarShareRequest, String>{

    private TextView messageTextView;
    private Button sendRequestButton;
    private CheckBox addToBuddies;
    private CarShare carShare;
    private ArrayList<CarShareRequest> carShareRequests;
    private CarShareRequest carShareRequest;
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
        Type carShareType = new TypeToken<CarShare>() {}.getType();
        Type carShareRequestsType = new TypeToken<ArrayList<CarShareRequest>>() {}.getType();

        carShare = gson.fromJson(getIntent().getExtras().getString("CurrentCarShare"), carShareType);
        carShareRequests = gson.fromJson(getIntent().getExtras().getString("CurrentRequests"), carShareRequestsType);

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

        for(CarShareRequest request : carShareRequests)
        {
            if(request.Decision == RequestDecision.UNDECIDED && request.UserId == appData.getUser().UserId)
            {
                requestStatusTextView.setText("You already have a pending request for this journey.");
                requestStatusTextView.setTextColor(Color.RED);
                sendRequestButton.setEnabled(false);
                requestStatusTextView.setVisibility(View.VISIBLE);
            }

            if(request.Decision == RequestDecision.ACCEPTED && request.UserId == appData.getUser().UserId)
            {
                requestStatusTextView.setText("You are already a passenger in this journey.");
                requestStatusTextView.setTextColor(Color.GREEN);
                sendRequestButton.setEnabled(false);
                requestStatusTextView.setVisibility(View.VISIBLE);
            }
        }

        if(carShare.Driver.UserId == appData.getUser().UserId)
        {
            requestStatusTextView.setText("You are the driver in this journey!");
            requestStatusTextView.setTextColor(Color.GREEN);
            requestStatusTextView.setVisibility(View.VISIBLE);
            sendRequestButton.setEnabled(false);
        }

        populateFields();
    }

    private void sendRequest(){
        carShareRequest = new CarShareRequest();
        carShareRequest.AddToTravelBuddies = addToBuddies.isChecked();
        carShareRequest.UserId = appData.getUser().UserId;
        carShareRequest.CarShareId = carShare.CarShareId;
        carShareRequest.Message = messageTextView.getText().toString();
        carShareRequest.Read = false;
        carShareRequest.Decision = RequestDecision.UNDECIDED;
        carShareRequest.SentOnDate = DateTimeHelper.convertToWCFDate(Calendar.getInstance().getTime());
        Type type = new TypeToken<ServiceResponse<CarShareRequest>>() {}.getType();
        new WCFServiceTask<CarShareRequest, CarShareRequest>("https://findndrive.no-ip.co.uk/Services/RequestService.svc/sendrequest",
                this.carShareRequest, type, appData.getAuthorisationHeaders(), null, this).execute();
    }

    private void populateFields()
    {
        driverUserNameTextView.setText(carShare.Driver.UserName);
        driverNameTextView.setText(carShare.Driver.FirstName + " " + carShare.Driver.LastName);
        driverGenderTextView.setText(Helpers.TranslateGender(carShare.Driver.Gender));
        driverDateOfBirthTextView.setText(DateTimeHelper.getSimpleDate(carShare.Driver.DateOfBirth));
        driverRatingTextView.setText("TODO");

        //carShareCitiesTextView.setText(carShare.DepartureCity +" to " + carShare.DestinationCity);
        //carShareDateAndTimeTextView.setText("Leaving on: " + DateTimeHelper.getSimpleDate(carShare.DateAndTimeOfDeparture)
        //        + " at: " + DateTimeHelper.getSimpleTime(carShare.DateAndTimeOfDeparture));
        carShareSmokersTextView.setText(carShareSmokersTextView.getText() + Helpers.TranslateBoolean(carShare.SmokersAllowed));
        carSharePetsTextView.setText(carSharePetsTextView.getText() + Helpers.TranslateBoolean(carShare.PetsAllowed));
        carShareGenderTextView.setText(carShareGenderTextView.getText() + "TODO");
        carShareSeatsTextView.setText(carShareSeatsTextView.getText() + ""+carShare.AvailableSeats);
        carShareFeeTextView.setText(carShareFeeTextView.getText() + "£"+decimalFormat.format(carShare.Fee));
        carShareVehicleTextView.setText(carShareVehicleTextView.getText() + "TODO");
        carShareDescriptionTextView.setText(carShareDescriptionTextView.getText() + carShare.Description);
    }

    @Override
    public void onServiceCallCompleted(ServiceResponse<CarShareRequest> serviceResponse, String parameter) {
        super.checkIfAuthorised(serviceResponse.ServiceResponseCode);
        if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
        {
            Toast toast = Toast.makeText(this, "Your request was sent successfully!", Toast.LENGTH_LONG);
            toast.show();
            finish();
        }
    }
}
