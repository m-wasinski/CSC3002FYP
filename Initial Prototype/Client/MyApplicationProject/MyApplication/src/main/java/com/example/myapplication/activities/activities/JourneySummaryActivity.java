package com.example.myapplication.activities.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.myapplication.R;
import com.example.myapplication.activities.base.BaseActivity;
import com.example.myapplication.constants.IntentConstants;
import com.example.myapplication.domain_objects.Journey;
import com.example.myapplication.utilities.DateTimeHelper;
import com.example.myapplication.utilities.DialogCreator;
import com.example.myapplication.interfaces.WCFImageRetrieved;
import com.example.myapplication.network_tasks.WcfPictureServiceTask;
import com.example.myapplication.utilities.Utilities;
import com.google.gson.reflect.TypeToken;

import java.text.DecimalFormat;

/**
 * Created by Michal on 19/02/14.
 */
public class JourneySummaryActivity extends BaseActivity implements WCFImageRetrieved{

    private TextView journeyIdTextView;
    private TextView journeyDriverTextView;
    private TextView journeyDateTextView;
    private TextView journeyTimeTextView;
    private TextView journeySeatsAvailableTextView;
    private TextView journeySmokersTextView;
    private TextView journeyPetsTextView;
    private TextView journeyFeeTextView;
    private TextView journeyVehicleTypeTextView;

    private TableRow journeyDriverTableRow;

    private Journey journey;

    private ImageView driverIconImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_journey_summary);

        // Initialise local variables.
        this.journey = gson .fromJson(getIntent().getStringExtra(IntentConstants.JOURNEY), new TypeToken<Journey>(){}.getType());

        // Initialise UI elements.
        this.journeyIdTextView = (TextView) this.findViewById(R.id.JourneySummaryJourneyIdTextView);
        this.journeyDriverTextView = (TextView) this.findViewById(R.id.JourneySummaryJourneyDriverTextView);
        this.journeyDateTextView = (TextView) this.findViewById(R.id.JourneySummaryJourneyDateTextView);
        this.journeyTimeTextView = (TextView) this.findViewById(R.id.JourneySummaryJourneyTimeTextView);
        this.journeySeatsAvailableTextView = (TextView) this.findViewById(R.id.JourneySummaryJourneySeatsTextView);
        this.journeyPetsTextView = (TextView) this.findViewById(R.id.JourneySummaryJourneyPetsTextView);
        this.journeySmokersTextView = (TextView) this.findViewById(R.id.JourneySummaryJourneySmokersTextView);
        this.journeyFeeTextView = (TextView) this.findViewById(R.id.JourneySummaryJourneyFeeTextView);
        this.journeyVehicleTypeTextView = (TextView) this.findViewById(R.id.JourneySummaryJourneyVehicleTypeTextView);

        this.driverIconImageView = (ImageView) this.findViewById(R.id.JourneySummaryActivityDriverImageView);

        this.journeyDriverTableRow = (TableRow) this.findViewById(R.id.JourneySummaryActivityJourneyDriverTableRow);

        // Fill in the details.
        this.fillJourneyDetails();

        // Setup event handlers.
        this.setupEventHandlers();

        // Retrieve picture of the driver.
        this.getDriverPicture();
    }

    private void setupEventHandlers()
    {
        this.journeyDriverTableRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDriverProfileDialog();
            }
        });
    }

    private void showDriverProfileDialog()
    {
        DialogCreator.ShowProfileOptionsDialog(this, journey.Driver);
    }

    private void fillJourneyDetails()
    {
        String[] vehicleTypes = getResources().getStringArray(R.array.vehicle_types);

        this.journeyIdTextView.setText(String.valueOf(this.journey.getJourneyId()));
        this.journeyDriverTextView.setText(this.journey.Driver.getUserName());
        this.journeyDateTextView.setText(DateTimeHelper.getSimpleDate(this.journey.DateAndTimeOfDeparture));
        this.journeyTimeTextView.setText(DateTimeHelper.getSimpleTime(this.journey.DateAndTimeOfDeparture));
        this.journeySmokersTextView.setText(Utilities.translateBoolean(this.journey.SmokersAllowed));
        this.journeyPetsTextView.setText(Utilities.translateBoolean(this.journey.PetsAllowed));
        this.journeyVehicleTypeTextView.setText(vehicleTypes[this.journey.VehicleType]);
        this.journeySeatsAvailableTextView.setText(String.valueOf(this.journey.AvailableSeats));
        this.journeyFeeTextView.setText(("£"+new DecimalFormat("0.00").format(this.journey.Fee)) + (this.journey.PreferredPaymentMethod.isEmpty() ? "" : ", " +this.journey.PreferredPaymentMethod));

    }

    private void getDriverPicture()
    {
        new WcfPictureServiceTask(this.appManager.getBitmapLruCache(), this.getResources().getString(R.string.GetProfilePictureURL),
                this.journey.DriverId, this.appManager.getAuthorisationHeaders(), this).execute();
    }

    @Override
    public void onImageRetrieved(Bitmap bitmap) {
        if(bitmap != null)
        {
            this.driverIconImageView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, bitmap.getWidth()/2, bitmap.getHeight()/2, false));
        }
    }
}
