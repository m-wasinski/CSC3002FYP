package com.example.myapplication.activities.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
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
import com.example.myapplication.domain_objects.Notification;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.utilities.DateTimeHelper;
import com.example.myapplication.utilities.DialogCreator;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WcfPostServiceTask;
import com.example.myapplication.notification_management.NotificationProcessor;
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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result_journey_details);

        // Initialise variables.
        Bundle extras = getIntent().getExtras();
        Notification notification = gson.fromJson(extras.getString(IntentConstants.NOTIFICATION), new TypeToken<Notification>() {}.getType());

        if(notification != null)
        {
            new NotificationProcessor().MarkDelivered(this, appManager, notification, new WCFServiceCallback<Boolean, Void>() {
                @Override
                public void onServiceCallCompleted(ServiceResponse<Boolean> serviceResponse, Void parameter) {
                    Log.i(getClass().getSimpleName(), "Notification successfully marked as delivered");
                }
            });
        }

        journey = gson.fromJson(extras.getString(IntentConstants.JOURNEY), new TypeToken<Journey>() {}.getType());

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int halfScreen = (int) (metrics.heightPixels/2.5f);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, halfScreen);
        layoutParams.setMargins(10, 0, 0, 10);
        mapFragment = ((MapFragment)  getFragmentManager().findFragmentById(R.id.AlertJourneyDetailsMap));
        mapFragment.getView().setLayoutParams(layoutParams);

        // Initialise UI elements.
        journeyIdTextView = (TextView) findViewById(R.id.JourneyDetailsActivityJourneyIdTextView);
        journeyDriverTextView = (TextView) findViewById(R.id.JourneyDetailsActivityJourneyDriverTextView);
        journeyDateTextView = (TextView) findViewById(R.id.JourneyDetailsActivityJourneyDateTextView);
        journeyTimeTextView = (TextView) findViewById(R.id.JourneyDetailsActivityJourneyTimeTextView);
        journeySeatsAvailableTextView = (TextView) findViewById(R.id.JourneyDetailsActivityJourneySeatsTextView);
        journeyPetsTextView = (TextView) findViewById(R.id.JourneyDetailsActivityJourneyPetsTextView);
        journeySmokersTextView = (TextView) findViewById(R.id.JourneyDetailsActivityJourneySmokersTextView);
        journeyFeeTextView = (TextView) findViewById(R.id.JourneyDetailsActivityJourneyFeeTextView);
        journeyHeaderTextView = (TextView) findViewById(R.id.JourneyDetailsActivityJourneyTitleTextView);
        journeyVehicleTypeTextView = (TextView) findViewById(R.id.JourneyDetailsActivityJourneyVehicleTypeTextView);

        journeyDriverTableRow = (TableRow) findViewById(R.id.JourneyDetailsActivityJourneyDriverTableRow);

        sendRequestButton = (Button) findViewById(R.id.JourneyDetailsActivityJourneySendRequestButton);

        journeyMessageToDriverEditText = (EditText) findViewById(R.id.JourneyDetailsActivityMessageToDriverEditText);

        // Fill journey information
        fillJourneyInformation();

        // Setup event handlers.
        setupEventHandlers();

        try {
            // Loading map
            initialiseMap();
        } catch (Exception e) {
            e.printStackTrace();
        }

        googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                drawDrivingDirectionsOnMap(googleMap, journey.GeoAddresses);
            }
        });
    }

    private void fillJourneyInformation()
    {
        String[] vehicleTypes = getResources().getStringArray(R.array.vehicle_types);

        journeyHeaderTextView.setText(Utilities.getJourneyHeader(journey.GeoAddresses));
        journeyIdTextView.setText(String.valueOf(journey.getJourneyId()));
        journeyDriverTextView.setText(journey.Driver.getUserName());
        journeyDateTextView.setText(DateTimeHelper.getSimpleDate(journey.DateAndTimeOfDeparture));
        journeyTimeTextView.setText(DateTimeHelper.getSimpleTime(journey.DateAndTimeOfDeparture));
        journeySmokersTextView.setText(Utilities.translateBoolean(journey.SmokersAllowed));
        journeyPetsTextView.setText(Utilities.translateBoolean(journey.PetsAllowed));
        journeyVehicleTypeTextView.setText(vehicleTypes[journey.VehicleType]);
        journeySeatsAvailableTextView.setText(String.valueOf(journey.AvailableSeats));
        journeyFeeTextView.setText(("£"+new DecimalFormat("0.00").format(journey.Fee)) + (journey.PreferredPaymentMethod == null ? "" : ", " +journey.PreferredPaymentMethod));
    }

    private void initialiseMap() {

        if (googleMap == null && mapFragment != null) {
            googleMap = mapFragment.getMap();

            if (googleMap == null) {
                Toast.makeText(this,
                        "Unable to initialise Google Maps, please check your network connection.", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    private void setupEventHandlers()
    {
        journeyDriverTableRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDriverOptionsDialog();
            }
        });

        sendRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendRequest();
            }
        });
    }

    private void showDriverOptionsDialog()
    {
        DialogCreator.ShowProfileOptionsDialog(this, journey.Driver);
    }

    private void sendRequest()
    {
        JourneyRequest journeyRequest = new JourneyRequest();
        journeyRequest.UserId = appManager.getUser().getUserId();
        journeyRequest.User = appManager.getUser();
        journeyRequest.JourneyId = journey.getJourneyId();
        journeyRequest.Message = journeyMessageToDriverEditText.getText().toString();
        journeyRequest.Read = false;
        journeyRequest.Decision = RequestDecision.UNDECIDED;
        journeyRequest.SentOnDate = DateTimeHelper.convertToWCFDate(Calendar.getInstance().getTime());

        new WcfPostServiceTask<JourneyRequest>(this, getResources().getString(R.string.SendRequestURL),
                journeyRequest, new TypeToken<ServiceResponse<JourneyRequest>>() {}.getType(), appManager.getAuthorisationHeaders(), this).execute();
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
