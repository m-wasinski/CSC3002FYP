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
 * Displays details of a journey and provides user with the ability to send a request to its driver.
 */
public class SearchResultDetailsActivity extends BaseMapActivity implements WCFServiceCallback<JourneyRequest, Void>,
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

    private final String TAG = "Search Result Details Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result_journey_details);

        // Check if this activity has been started from a notification, if so, mark is as read.
        Bundle extras = getIntent().getExtras();
        Notification notification = getGson().fromJson(extras.getString(IntentConstants.NOTIFICATION), new TypeToken<Notification>() {}.getType());

        if(notification != null)
        {
            new NotificationProcessor().MarkDelivered(this, getAppManager(), notification, new WCFServiceCallback<Void, Void>() {
                @Override
                public void onServiceCallCompleted(ServiceResponse<Void> serviceResponse, Void parameter) {
                    Log.i(TAG, "Notification successfully marked as delivered");
                }
            });
        }

        // Initialise UI elements, local variables and setup event handlers
        journey = getGson().fromJson(extras.getString(IntentConstants.JOURNEY), new TypeToken<Journey>() {}.getType());

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, (int)(metrics.heightPixels/2.5f));
        layoutParams.setMargins(10, 0, 0, 10);
        mapFragment = ((MapFragment)  getFragmentManager().findFragmentById(R.id.AlertJourneyDetailsMap));

        if(mapFragment.getView() != null)
        {
            mapFragment.getView().setLayoutParams(layoutParams);
        }

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

        retrieveDriverProfilePicture();

        // Fill journey information
        fillJourneyInformation();

        try {
            // Loading map
            initialiseMap();
        } catch (Exception e) {
            e.printStackTrace();
        }

        getGoogleMap().setOnMapLoadedCallback(this);
    }

    /**
     * Display the information about this journey in the UI elements.
     */
    private void fillJourneyInformation()
    {
        String[] vehicleTypes = getResources().getStringArray(R.array.vehicle_types);

        journeyHeaderTextView.setText(Utilities.getJourneyHeader(journey.getGeoAddresses()));
        journeyIdTextView.setText(String.valueOf(journey.getJourneyId()));
        journeyDriverTextView.setText(journey.getDriver().getUserName());
        journeyDateTextView.setText(DateTimeHelper.getSimpleDate(journey.getDateAndTimeOfDeparture()));
        journeyTimeTextView.setText(DateTimeHelper.getSimpleTime(journey.getDateAndTimeOfDeparture()));
        journeySmokersTextView.setText(Utilities.translateBoolean(journey.areSmokersAllowed()));
        journeyPetsTextView.setText(Utilities.translateBoolean(journey.arePetsAllowed()));
        journeyVehicleTypeTextView.setText(vehicleTypes[journey.getVehicleType()]);
        journeySeatsAvailableTextView.setText(String.valueOf(journey.getAvailableSeats()));
        journeyFeeTextView.setText(("£"+new DecimalFormat("0.00").format(journey.getFee())) + (journey.getPreferredPaymentMethod() == null ? "" : ", " +journey.getPreferredPaymentMethod()));
    }

    /**
     * Initialises the Google Map present in this activity.
     */
    private void initialiseMap() {

        if (getGoogleMap() == null && mapFragment != null) {
            setGoogleMap(mapFragment.getMap());

            if (getGoogleMap() == null) {
                Toast.makeText(this,"Check if Google Play services are installed.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Retrieves profile picture of the driver.
     */
    private void retrieveDriverProfilePicture()
    {
        new WcfPictureServiceTask(getAppManager().getBitmapLruCache(), getResources().getString(R.string.GetProfilePictureURL),
                journey.getDriver().getUserId(), getAppManager().getAuthorisationHeaders(), this).execute();
    }

    /**
     * Builds and sends a new journey request from the current user to the driver of the current journey.
     */
    private void sendRequest()
    {
        sendRequestButton.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        JourneyRequest journeyRequest = new JourneyRequest();
        journeyRequest.setFromUser(getAppManager().getUser());
        journeyRequest.setJourneyId(journey.getJourneyId());
        journeyRequest.setMessage(journeyMessageToDriverEditText.getText().toString());

        new WcfPostServiceTask<JourneyRequest>(this, getResources().getString(R.string.SendRequestURL),
                journeyRequest, new TypeToken<ServiceResponse<Void>>() {}.getType(), getAppManager().getAuthorisationHeaders(), this).execute();
    }

    /**
     * Callback method called upon completion of the Service Task whose responsibility was to send the journey request.
     *
     * @param serviceResponse
     * @param parameter
     */
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
        drawDrivingDirectionsOnMap(getGoogleMap(), journey.getGeoAddresses(), this);
    }

    /**
     * Called upon successful picture retrieval from the web service.
     *
     * @param bitmap - bitmap containing the image.
     */
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
                Bundle bundle = new Bundle();
                bundle.putInt(IntentConstants.PROFILE_VIEWER_MODE, IntentConstants.PROFILE_VIEWER_VIEWING);
                bundle.putInt(IntentConstants.USER, journey.getDriver().getUserId());
                startActivity(new Intent(this, ProfileViewerActivity.class).putExtras(bundle));
                break;
        }
    }
}
