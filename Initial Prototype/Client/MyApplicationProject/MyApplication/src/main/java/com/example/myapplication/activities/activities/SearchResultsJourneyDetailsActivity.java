package com.example.myapplication.activities.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.activities.base.BaseMapActivity;
import com.example.myapplication.constants.IntentConstants;
import com.example.myapplication.constants.RequestDecision;
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.domain_objects.Journey;
import com.example.myapplication.domain_objects.JourneyRequest;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.experimental.DateTimeHelper;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WCFServiceTask;
import com.example.myapplication.utilities.Utilities;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.gson.reflect.TypeToken;

import java.text.DecimalFormat;
import java.util.Calendar;

/**
 * Created by Michal on 04/02/14.
 */
public class SearchResultsJourneyDetailsActivity extends BaseMapActivity implements WCFServiceCallback<JourneyRequest, Void>{

    private Journey journey;

    private MapFragment mapFragment;

    private TextView journeyIdTextView;
    private TextView journeyDriverTextView;
    private TextView journeyDateTextView;
    private TextView journeyTimeTextView;
    private TextView journeySeatsAvailableTextView;
    private TextView journeySmokersTextView;
    private TextView journeyPetsTextView;
    private TextView journeyFeeTextView;
    private TextView journeyHeaderTextView;
    private TextView journeyVehicleTypeTextView;

    private EditText journeyMessageToDriverEditText;

    private TableRow journeyDriverTableRow;

    private Button sendRequestButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_journey_details);

        // Initialise variables.
        Bundle extras = getIntent().getExtras();
        this.journey = gson.fromJson(extras.getString(IntentConstants.JOURNEY), new TypeToken<Journey>() {}.getType());
        DisplayMetrics metrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int halfScreen = (int) (metrics.heightPixels/2.5f);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, halfScreen);
        layoutParams.setMargins(10, 0, 0, 10);
        this.mapFragment = ((MapFragment)  getFragmentManager().findFragmentById(R.id.AlertJourneyDetailsMap));
        this.mapFragment.getView().setLayoutParams(layoutParams);

        // Initialise UI elements.
        this.journeyIdTextView = (TextView) this.findViewById(R.id.JourneyDetailsActivityJourneyIdTextView);
        this.journeyDriverTextView = (TextView) this.findViewById(R.id.JourneyDetailsActivityJourneyDriverTextView);
        this.journeyDateTextView = (TextView) this.findViewById(R.id.JourneyDetailsActivityJourneyDateTextView);
        this.journeyTimeTextView = (TextView) this.findViewById(R.id.JourneyDetailsActivityJourneyTimeTextView);
        this.journeySeatsAvailableTextView = (TextView) this.findViewById(R.id.JourneyDetailsActivityJourneySeatsTextView);
        this.journeyPetsTextView = (TextView) this.findViewById(R.id.JourneyDetailsActivityJourneyPetsTextView);
        this.journeySmokersTextView = (TextView) this.findViewById(R.id.JourneyDetailsActivityJourneySmokersTextView);
        this.journeyFeeTextView = (TextView) this.findViewById(R.id.JourneyDetailsActivityJourneyFeeTextView);
        this.journeyHeaderTextView = (TextView) this.findViewById(R.id.JourneyDetailsActivityJourneyTitleTextView);
        this.journeyVehicleTypeTextView = (TextView) this.findViewById(R.id.JourneyDetailsActivityJourneyVehicleTypeTextView);

        this.journeyDriverTableRow = (TableRow) this.findViewById(R.id.JourneyDetailsActivityJourneyDriverTableRow);

        this.sendRequestButton = (Button) this.findViewById(R.id.JourneyDetailsActivityJourneySendRequestButton);

        this.journeyMessageToDriverEditText = (EditText) this.findViewById(R.id.JourneyDetailsActivityMessageToDriverEditText);

        // Fill journey information
        this.fillJourneyInformation();

        // Setup event handlers.
        this.setupEventHandlers();

        try {
            // Loading map
            this.initialiseMap();
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                drawDrivingDirectionsOnMap(googleMap, journey.GeoAddresses);
            }
        });
    }

    private void fillJourneyInformation()
    {
        String[] vehicleTypes = getResources().getStringArray(R.array.vehicle_types);
        String[] paymentOptions = getResources().getStringArray(R.array.payment_options);

        this.journeyHeaderTextView.setText(Utilities.getJourneyHeader(this.journey.GeoAddresses));
        this.journeyIdTextView.setText(String.valueOf(this.journey.getJourneyId()));
        this.journeyDriverTextView.setText(this.journey.Driver.FirstName + " " + this.journey.Driver.LastName);
        this.journeyDateTextView.setText(DateTimeHelper.getSimpleDate(this.journey.DateAndTimeOfDeparture));
        this.journeyTimeTextView.setText(DateTimeHelper.getSimpleTime(this.journey.DateAndTimeOfDeparture));
        this.journeySmokersTextView.setText(Utilities.translateBoolean(this.journey.SmokersAllowed));
        this.journeyPetsTextView.setText(Utilities.translateBoolean(this.journey.PetsAllowed));
        this.journeyVehicleTypeTextView.setText(vehicleTypes[this.journey.VehicleType]);
        this.journeySeatsAvailableTextView.setText(String.valueOf(this.journey.AvailableSeats));
        this.journeyFeeTextView.setText("£"+new DecimalFormat("0.00").format(this.journey.Fee)+" "+paymentOptions[this.journey.PaymentOption]);
    }

    private void initialiseMap() {

        if (this.googleMap == null && this.mapFragment != null) {
            this.googleMap = this.mapFragment.getMap();

            if (googleMap == null) {
                Toast.makeText(this,
                        "Unable to initialise Google Maps, please check your network connection.", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    private void setupEventHandlers()
    {
        this.journeyDriverTableRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 showDriverAlertDialog();
            }
        });

        this.sendRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendRequest();
            }
        });
    }

    private void sendRequest()
    {
        JourneyRequest journeyRequest = new JourneyRequest();
        journeyRequest.UserId = this.findNDriveManager.getUser().UserId;
        journeyRequest.User = this.findNDriveManager.getUser();
        journeyRequest.JourneyId = this.journey.getJourneyId();
        journeyRequest.Message = this.journeyMessageToDriverEditText.getText().toString();
        journeyRequest.Read = false;
        journeyRequest.Decision = RequestDecision.UNDECIDED;
        journeyRequest.SentOnDate = DateTimeHelper.convertToWCFDate(Calendar.getInstance().getTime());

        new WCFServiceTask<JourneyRequest>(this, getResources().getString(R.string.SendRequestURL),
                journeyRequest, new TypeToken<ServiceResponse<JourneyRequest>>() {}.getType(), findNDriveManager.getAuthorisationHeaders(), this).execute();
    }

    private void showDriverAlertDialog()
    {
        CharSequence options[] = new CharSequence[] {"Show profile", "Send friend request"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(this.journey.Driver.FirstName + " " + this.journey.Driver.LastName);
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }


    @Override
    public void onServiceCallCompleted(ServiceResponse<JourneyRequest> serviceResponse, Void parameter) {
        if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
        {
            Intent intent = new Intent(this, HomeActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            Toast toast = Toast.makeText(this, "Your request was sent successfully!", Toast.LENGTH_LONG);
            toast.show();
        }
    }
}
