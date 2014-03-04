package com.example.myapplication.activities.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.activities.base.BaseMapActivity;
import com.example.myapplication.constants.IntentConstants;
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.domain_objects.Journey;
import com.example.myapplication.domain_objects.JourneyRequest;
import com.example.myapplication.domain_objects.Notification;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.interfaces.OnDrivingDirectionsRetrrievedListener;
import com.example.myapplication.interfaces.WCFImageRetrieved;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WcfPictureServiceTask;
import com.example.myapplication.network_tasks.WcfPostServiceTask;
import com.example.myapplication.notification_management.NotificationProcessor;
import com.example.myapplication.utilities.BitmapUtilities;
import com.example.myapplication.utilities.DateTimeHelper;
import com.example.myapplication.utilities.Utilities;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.gson.reflect.TypeToken;

import java.text.DecimalFormat;

/**
 * Created by Michal on 04/02/14.
 */
public class SearchResultsJourneyDetailsActivity extends BaseMapActivity implements WCFServiceCallback<JourneyRequest, Void>,
        OnDrivingDirectionsRetrrievedListener, GoogleMap.OnMapLoadedCallback, WCFImageRetrieved, View.OnClickListener{

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

    private ImageView driverImageView;

    private EditText journeyMessageToDriverEditText;

    private Button sendRequestButton;

    private ProgressBar progressBar;

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
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, (int)(metrics.heightPixels/2.5f));
        layoutParams.setMargins(10, 0, 0, 10);
        mapFragment = ((MapFragment)  getFragmentManager().findFragmentById(R.id.AlertJourneyDetailsMap));

        if(mapFragment.getView() != null)
        {
            mapFragment.getView().setLayoutParams(layoutParams);
        }

        // Initialise UI elements and setup their event handlers..
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
        progressBar = (ProgressBar) findViewById(R.id.JourneyDetailsActivityProgressBar);
        findViewById(R.id.JourneyDetailsActivityJourneyDriverTableRow).setOnClickListener(this);
        sendRequestButton = (Button) findViewById(R.id.JourneyDetailsActivityJourneySendRequestButton);
        sendRequestButton.setOnClickListener(this);
        journeyMessageToDriverEditText = (EditText) findViewById(R.id.JourneyDetailsActivityMessageToDriverEditText);
        driverImageView = (ImageView) findViewById(R.id.JourneySearchResultsActivityDriverImageView);

        retrieveDriverImage();

        // Fill journey information
        fillJourneyInformation();

        try {
            // Loading map
            initialiseMap();
        } catch (Exception e) {
            e.printStackTrace();
        }

        googleMap.setOnMapLoadedCallback(this);
    }

    private void fillJourneyInformation()
    {
        String[] vehicleTypes = getResources().getStringArray(R.array.vehicle_types);

        journeyHeaderTextView.setText(Utilities.getJourneyHeader(journey.getGeoAddresses()));
        journeyIdTextView.setText(String.valueOf(journey.getJourneyId()));
        journeyDriverTextView.setText(journey.getDriver().getUserName());
        journeyDateTextView.setText(DateTimeHelper.getSimpleDate(journey.getDateAndTimeOfDeparture()));
        journeyTimeTextView.setText(DateTimeHelper.getSimpleTime(journey.getDateAndTimeOfDeparture()));
        journeySmokersTextView.setText(Utilities.translateBoolean(journey.isSmokersAllowed()));
        journeyPetsTextView.setText(Utilities.translateBoolean(journey.isPetsAllowed()));
        journeyVehicleTypeTextView.setText(vehicleTypes[journey.getVehicleType()]);
        journeySeatsAvailableTextView.setText(String.valueOf(journey.getAvailableSeats()));
        journeyFeeTextView.setText(("£"+new DecimalFormat("0.00").format(journey.getFee())) + (journey.getPreferredPaymentMethod() == null ? "" : ", " +journey.getPreferredPaymentMethod()));
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

    private void retrieveDriverImage()
    {
        new WcfPictureServiceTask(appManager.getBitmapLruCache(), getResources().getString(R.string.GetProfilePictureURL),
                journey.getDriver().getUserId(), appManager.getAuthorisationHeaders(), this).execute();
    }

    private void sendRequest()
    {
        sendRequestButton.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        JourneyRequest journeyRequest = new JourneyRequest();
        journeyRequest.setFromUser(appManager.getUser());
        journeyRequest.setJourneyId(journey.getJourneyId());
        journeyRequest.setMessage(journeyMessageToDriverEditText.getText().toString());

        new WcfPostServiceTask<JourneyRequest>(this, getResources().getString(R.string.SendRequestURL),
                journeyRequest, new TypeToken<ServiceResponse<JourneyRequest>>() {}.getType(), appManager.getAuthorisationHeaders(), this).execute();
    }

    @Override
    public void onServiceCallCompleted(ServiceResponse<JourneyRequest> serviceResponse, Void parameter) {
        progressBar.setVisibility(View.GONE);
        sendRequestButton.setEnabled(true);
        if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
        {
            startActivity(new Intent(this, HomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            Toast.makeText(this, "Your request was sent successfully!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDrivingDirectionsRetrieved() {
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onMapLoaded() {
        drawDrivingDirectionsOnMap(googleMap, journey.getGeoAddresses(), this);
    }

    @Override
    public void onImageRetrieved(Bitmap bitmap) {
        if(bitmap != null)
        {
            int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(ViewGroup.LayoutParams.MATCH_PARENT, View.MeasureSpec.UNSPECIFIED);
            int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(ViewGroup.LayoutParams.WRAP_CONTENT, View.MeasureSpec.UNSPECIFIED);
            driverImageView.measure(widthMeasureSpec, heightMeasureSpec);
            driverImageView.setImageBitmap(BitmapUtilities.rescaleBitmap(bitmap, driverImageView.getMeasuredWidth(), driverImageView.getMeasuredHeight()));
        }
    }

    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            case R.id.JourneyDetailsActivityJourneySendRequestButton:
                sendRequest();
                break;
            case R.id.JourneyDetailsActivityJourneyDriverTableRow:
                startActivity(new Intent(this, ProfileViewerActivity.class).putExtra(IntentConstants.USER, gson.toJson(journey.getDriver())));
                break;
        }
    }
}
