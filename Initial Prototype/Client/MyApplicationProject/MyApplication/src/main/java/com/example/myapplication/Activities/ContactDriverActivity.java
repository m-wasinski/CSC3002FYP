package com.example.myapplication.Activities;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.Constants.Constants;
import com.example.myapplication.DomainObjects.CarShare;
import com.example.myapplication.DomainObjects.CarShareRequest;
import com.example.myapplication.DomainObjects.ServiceResponse;
import com.example.myapplication.DomainObjects.User;
import com.example.myapplication.Experimental.AppData;
import com.example.myapplication.Experimental.WCFDateTimeHelper;
import com.example.myapplication.Helpers.Helpers;
import com.example.myapplication.Interfaces.ContactDriverActivityInteface;
import com.example.myapplication.NetworkTasks.ContactDriverTask;
import com.example.myapplication.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by Michal on 02/01/14.
 */
public class ContactDriverActivity extends Activity implements ContactDriverActivityInteface{

    private TextView messageTextView;
    private Button sendRequestButton;
    private CheckBox addToBuddies;
    private AppData appData;
    private CarShare carShare;
    private ArrayList<CarShareRequest> carShareRequests;
    private User user;
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
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(com.example.myapplication.R.layout.contact_driver);
        appData = ((AppData)getApplication());
        user = appData.getUser();
        decimalFormat = new DecimalFormat("0.00");
        Gson gson = new Gson();
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
            if(request.Decision == Constants.UNDECIDED && request.UserId == user.UserId)
            {
                requestStatusTextView.setText("You already have a pending request for this journey.");
                requestStatusTextView.setTextColor(Color.RED);
                sendRequestButton.setEnabled(false);
                requestStatusTextView.setVisibility(View.VISIBLE);
            }

            if(request.Decision == Constants.ACCEPTED && request.UserId == user.UserId)
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
        carShareRequest.UserId = user.UserId;
        carShareRequest.CarShareId = carShare.CarShareId;
        carShareRequest.Message = messageTextView.getText().toString();
        carShareRequest.Read = false;
        carShareRequest.Decision = Constants.UNDECIDED;

        ContactDriverTask contactDriverTask = new ContactDriverTask(carShareRequest, this);
        contactDriverTask.execute();
    }

    @Override
    public void carShareRequestSent(ServiceResponse<CarShareRequest> serviceResponse) {
            if(serviceResponse.ServiceResponseCode == Constants.SERVICE_RESPONSE_SUCCESS)
            {
                Toast toast = Toast.makeText(this, "Your request was sent successfully!", Toast.LENGTH_LONG);
                toast.show();
                finish();
            }
    }

    private void populateFields()
    {
        driverUserNameTextView.setText(carShare.Driver.UserName);
        driverNameTextView.setText(carShare.Driver.FirstName + " " + carShare.Driver.LastName);
        driverGenderTextView.setText(Helpers.TranslateGender(carShare.Driver.Gender));
        driverDateOfBirthTextView.setText(WCFDateTimeHelper.GetSimpleDate(carShare.Driver.DateOfBirth));
        driverRatingTextView.setText("TODO");

        carShareCitiesTextView.setText(carShare.DepartureCity +" to " + carShare.DestinationCity);
        carShareDateAndTimeTextView.setText("Leaving on: " + carShare.DateOfDepartureAsString() + " at: " + carShare.TimeOfDepartureAsString());
        carShareSmokersTextView.setText(carShareSmokersTextView.getText() + Helpers.TranslateBoolean(carShare.SmokersAllowed));
        carSharePetsTextView.setText(carSharePetsTextView.getText() + Helpers.TranslateBoolean(carShare.PetsAllowed));
        carShareGenderTextView.setText(carShareGenderTextView.getText() + "TODO");
        carShareSeatsTextView.setText(carShareSeatsTextView.getText() + ""+carShare.AvailableSeats);
        carShareFeeTextView.setText(carShareFeeTextView.getText() + "£"+decimalFormat.format(carShare.Fee));
        carShareVehicleTextView.setText(carShareVehicleTextView.getText() + "TODO");
        carShareDescriptionTextView.setText(carShareDescriptionTextView.getText() + carShare.Description);
    }
}
