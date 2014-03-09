package com.example.myapplication.activities.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.activities.base.BaseActivity;
import com.example.myapplication.constants.IntentConstants;
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.domain_objects.Journey;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.factories.DialogFactory;
import com.example.myapplication.interfaces.Interfaces;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WcfPostServiceTask;
import com.example.myapplication.utilities.CustomDateTimePicker;
import com.example.myapplication.utilities.DateTimeHelper;
import com.example.myapplication.utilities.Utilities;
import com.google.gson.reflect.TypeToken;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Michal on 29/01/14.
 */
public class OfferJourneyStepTwoActivity extends BaseActivity {

    private Journey journey;
    private Calendar calendar;

    private TextView journeyDateTextView;
    private TextView journeyTimeTextView;
    private TextView journeyPrivateTextView;
    private TextView journeySmokersTextView;
    private TextView journeyPetsTextView;
    private TextView journeyVehicleTypeTextView;
    private TextView journeyFeeTextView;
    private TextView journeyAvailableSeatsTextView;

    private RelativeLayout journeyDateRelativeLayout;
    private RelativeLayout journeyTimeRelativeLayout;
    private RelativeLayout journeyPrivateRelativeLayout;
    private RelativeLayout journeySmokersRelativeLayout;
    private RelativeLayout journeyPetsRelativeLayout;
    private RelativeLayout journeyVehicleTypeRelativeLayout;
    private RelativeLayout journeyFeeRelativeLayout;
    private RelativeLayout journeyAvailableSeatsRelativeLayout;

    private CheckBox journeyPrivateCheckbox;
    private CheckBox journeySmokersCheckbox;
    private CheckBox journeyPetsCheckbox;

    private EditText journeyCommentsEditText;

    private Button createButton;

    private ProgressBar progressBar;

    private final String SELECT_DATE = "Select Date";
    private final String SELECT_TIME = "Select Time";

    private int mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer_journey_step_two);
        calendar = DateTimeHelper.getCalendar();
        //retrieve the journey object from the bundle.
        Bundle bundle = getIntent().getExtras();

        journey = gson.fromJson(bundle.getString(IntentConstants.JOURNEY), new TypeToken<Journey>() {}.getType());
        mode = bundle.getInt(IntentConstants.JOURNEY_CREATOR_MODE);

        if(mode == IntentConstants.JOURNEY_CREATOR_MODE_EDITING)
        {
            calendar.setTime(DateTimeHelper.parseWCFDate(journey.getDateAndTimeOfDeparture()));
        }

        // Initialise both resource arrays.
        String[] vehiclesTypes = getResources().getStringArray(R.array.vehicle_types);

        //Initialise UI elements.
        ImageView minimapImageView = (ImageView) findViewById(R.id.OfferJourneyStepTwoActivityMiniMapImageView);
        minimapImageView.setImageBitmap(BitmapFactory.decodeByteArray(
                getIntent().getByteArrayExtra(IntentConstants.MINIMAP), 0, getIntent().getByteArrayExtra(IntentConstants.MINIMAP).length));

        journeyTimeRelativeLayout = (RelativeLayout) findViewById(R.id.OfferJourneyStepTwoActivityJourneyTimeRelativeLayout);
        journeyTimeTextView = (TextView) findViewById(R.id.OfferJourneyStepTwoActivityJourneyTimeTextView);
        journeyTimeTextView.setText(mode == IntentConstants.JOURNEY_CREATOR_MODE_EDITING ?
                DateTimeHelper.getSimpleTime(journey.getDateAndTimeOfDeparture()) : SELECT_TIME);

        journeyDateRelativeLayout = (RelativeLayout) findViewById(R.id.OfferJourneyStepTwoActivityJourneyDateRelativeLayout);
        journeyDateTextView = (TextView) findViewById(R.id.OfferJourneyStepTwoActivityDateTextView);
        journeyDateTextView.setText(mode == IntentConstants.JOURNEY_CREATOR_MODE_EDITING ?
                DateTimeHelper.getSimpleDate(journey.getDateAndTimeOfDeparture()) :  SELECT_DATE);

        journeyPrivateRelativeLayout = (RelativeLayout) findViewById(R.id.OfferJourneyStepTwoActivityJourneyPrivateRelativeLayout);

        journeyPrivateCheckbox = (CheckBox) findViewById(R.id.OfferJourneyStepTwoActivityJourneyPrivateCheckbox);
        journeyPrivateCheckbox.setChecked(journey.isPrivate());

        journeyPrivateTextView = (TextView) findViewById(R.id.OfferJourneyStepTwoActivityPrivateTextView);
        journeyPrivateTextView.setText(Utilities.translateBoolean(journey.isPrivate()));

        journeySmokersRelativeLayout = (RelativeLayout) findViewById(R.id.OfferJourneyStepTwoActivityJourneySmokersRelativeLayout);

        journeySmokersTextView = (TextView) findViewById(R.id.OfferJourneyStepTwoActivitySmokersTextView);
        journeySmokersTextView.setText(Utilities.translateBoolean(journey.areSmokersAllowed()));

        journeySmokersCheckbox = (CheckBox) findViewById(R.id.OfferJourneyStepTwoActivitySmokersCheckBox);
        journeySmokersCheckbox.setChecked(journey.areSmokersAllowed());

        journeyPetsRelativeLayout = (RelativeLayout) findViewById(R.id.OfferJourneyStepTwoActivityJourneyPetsRelativeLayout);

        journeyPetsTextView = (TextView) findViewById(R.id.OfferJourneyStepTwoActivityPetsTextView);
        journeyPetsTextView.setText(Utilities.translateBoolean(journey.arePetsAllowed()));

        journeyPetsCheckbox = (CheckBox) findViewById(R.id.OfferJourneyStepTwoActivityPetCheckBox);
        journeyPetsCheckbox.setChecked(journey.arePetsAllowed());

        journeyVehicleTypeRelativeLayout = (RelativeLayout) findViewById(R.id.OfferJourneyStepTwoActivityJourneyVehicleTypeRelativeLayout);

        journeyVehicleTypeTextView = (TextView) findViewById(R.id.OfferJourneyStepTwoActivityVehicleTextView);
        String SELECT_VEHICLE = "Select vehicle type";
        journeyVehicleTypeTextView.setText(mode == IntentConstants.JOURNEY_CREATOR_MODE_EDITING ? vehiclesTypes[journey.getVehicleType()] : SELECT_VEHICLE);

        journeyAvailableSeatsRelativeLayout = (RelativeLayout) findViewById(R.id.OfferJourneyStepTwoActivityJourneyAvailableSeatsRelativeLayout);
        journeyAvailableSeatsTextView = (TextView) findViewById(R.id.OfferJourneyStepTwoActivityAvailableSeatsTextView);
        journeyAvailableSeatsTextView.setText(String.valueOf(journey.getAvailableSeats()));

        journeyFeeRelativeLayout = (RelativeLayout) findViewById(R.id.OfferJourneyStepTwoActivityJourneyFeeRelativeLayout);
        journeyFeeTextView = (TextView) findViewById(R.id.OfferJourneyStepTwoActivityFeeTextView);
        String SELECT_FEE = "Select fee & payment method";
        journeyFeeTextView.setText(mode == IntentConstants.JOURNEY_CREATOR_MODE_EDITING ?
                ("£"+new DecimalFormat("0.00").format(journey.getFee())+ (journey.getPreferredPaymentMethod() == null ? "": ", " + journey.getPreferredPaymentMethod())) : SELECT_FEE);


        journeyCommentsEditText = (EditText) findViewById(R.id.OfferJourneyStepTwoActivityCommentsEditText);
        journeyCommentsEditText.setText(journey.getDescription() == null ? "" : journey.getDescription());

        createButton = (Button) findViewById(R.id.OfferJourneyStepTwoActivityCreateButton);
        createButton.setText(mode  == IntentConstants.JOURNEY_CREATOR_MODE_EDITING ? "Save changes" : "Offer journey");

        progressBar = (ProgressBar) findViewById(R.id.OfferJourneyStepTwoActivityProgressBar);

        actionBar.setTitle(mode == IntentConstants.JOURNEY_CREATOR_MODE_EDITING ? "Editing journey, step 2" : "Offering journey, step 2");

        //set the from address next to minimap.
        TextView fromTextView = (TextView) findViewById(R.id.OfferJourneyStepTwoActivityFromTextView);
        fromTextView.setText("- " + journey.getGeoAddresses().get(0).getAddressLine());

        //set the to address nex to minimap
        TextView toTextView = (TextView) findViewById(R.id.OfferJourneyStepTwoActivityToTextView);
        toTextView.setText("- " + journey.getGeoAddresses().get(journey.getGeoAddresses().size() - 1).getAddressLine());


        if(journey.getGeoAddresses().size() > 2)
        {
            addViaPoints();
        }

        // Setup all event handlers for the above UI elements.
        setupEventHandlers();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.help:
                DialogFactory.getHelpDialog(this,
                        mode == IntentConstants.JOURNEY_CREATOR_MODE_EDITING ? "Making changes to your journey" : "Offering new journey",
                        mode == IntentConstants.JOURNEY_CREATOR_MODE_EDITING ?
                                getResources().getString(R.string.EditingJourneyStepTwoHelp) :
                                getResources().getString(R.string.OfferingJourneyStepTwoHelp));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addViaPoints()
    {
        LinearLayout viaPointsLinearLayout = (LinearLayout) findViewById(R.id.OfferJourneyStepTwoActivityViaLinearLayout);
        TextView viaLabelTextView = (TextView) findViewById(R.id.OfferJourneyStepTwoActivityViaTextView);
        viaLabelTextView.setVisibility(View.VISIBLE);

        int dpValue = 20; // margin in dips
        float d = getResources().getDisplayMetrics().density;
        int margin = (int)(dpValue * d); // margin in pixels

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        params.setMargins(margin, 0, 0, 0);

        for(int i = 1; i < journey.getGeoAddresses().size()-1; i++)
        {
            TextView waypoint = (TextView)getLayoutInflater().inflate(R.layout.textview_template, null);
            waypoint.setLayoutParams(params);
            waypoint.setText("- "+journey.getGeoAddresses().get(i).getAddressLine());
            viaPointsLinearLayout.addView(waypoint);
        }
    }

    private void setupEventHandlers()
    {
        journeyDateRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDate();
            }
        });

        journeyTimeRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getTime();
            }
        });

        journeyPrivateRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                journeyPrivateCheckbox.setChecked(!journeyPrivateCheckbox.isChecked());
                journey.setPrivate(journeyPrivateCheckbox.isChecked());
                journeyPrivateTextView.setText(Utilities.translateBoolean(journeyPrivateCheckbox.isChecked()));
            }
        });

        journeySmokersRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                journeySmokersCheckbox.setChecked(!journeySmokersCheckbox.isChecked());
                journey.setSmokersAllowed(journeySmokersCheckbox.isChecked());
                journeySmokersTextView.setText(Utilities.translateBoolean(journeySmokersCheckbox.isChecked()));
            }
        });

        journeyPetsRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                journeyPetsCheckbox.setChecked(!journeyPetsCheckbox.isChecked());
                journey.setPetsAllowed(journeyPetsCheckbox.isChecked());
                journeyPetsTextView.setText(Utilities.translateBoolean(journeyPetsCheckbox.isChecked()));
            }
        });

        journeyVehicleTypeRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getVehicleType();
            }
        });

        journeyAvailableSeatsRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getAvailableSeats();
            }
        });

        journeyFeeRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFee();
            }
        });

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendServiceRequest();
            }
        });
    }

    /**
     * Prompts the user to enter journey's departure date.
     */
    private void getDate()
    {
        new CustomDateTimePicker().showDatePickerDialog(this, calendar, new Interfaces.DateSelectedListener() {
            @Override
            public void dateSelected(Calendar c) {
                if(c != null)
                {
                    calendar.set(Calendar.YEAR, c.get(Calendar.YEAR));
                    calendar.set(Calendar.MONTH, c.get(Calendar.MONTH));
                    calendar.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH));

                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMMM-yyyy", Locale.UK);

                    journeyDateTextView.setText(simpleDateFormat.format(calendar.getTime()));
                    journeyDateTextView.setError(null);
                    journey.setDateAndTimeOfDeparture(DateTimeHelper.convertToWCFDate(calendar.getTime()));
                }
            }
        }, false, true);
    }

    /**
     * Prompts the user to enter the journey's departure time.
     */
    private void getTime()
    {
        new CustomDateTimePicker().showTimePickerDialog(this, calendar, new Interfaces.TimeSelectedListener() {
            @Override
            public void timeSelected(Calendar c) {
                if(c != null)
                {
                    calendar.set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY));
                    calendar.set(Calendar.MINUTE, c.get(Calendar.MINUTE));
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.UK);
                    journeyTimeTextView.setText(sdf.format(calendar.getTime()));
                    journeyTimeTextView.setError(null);
                    journey.setDateAndTimeOfDeparture(DateTimeHelper.convertToWCFDate(calendar.getTime()));
                }
            }
        }, false);
    }

    /**
     * Prompts the user to enter the vehicle type which will be used to travel.
     */
    private void getVehicleType()
    {
        final String[] vehicleTypes = getResources().getStringArray(R.array.vehicle_types);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select vehicle type");
        builder.setItems(vehicleTypes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                journeyVehicleTypeTextView.setText(vehicleTypes[item]);
                journeyVehicleTypeTextView.setError(null);
                journey.setVehicleType(item);
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Prompts the user to enter their desired fee for this journey.
     */
    private void getFee()
    {
        // custom feeDialog
        final Dialog feeDialog = new Dialog(this);
        feeDialog.setContentView(R.layout.dialog_fee_selector);
        feeDialog.setTitle("Fee & Payment options");

        Button freeButton = (Button) feeDialog.findViewById(R.id.FeeAlertDialogFreeButton);
        freeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                journeyFeeTextView.setText("Free (£0.00)");
                journeyFeeTextView.setError(null);
                journey.setFee(0);
                journey.setPreferredPaymentMethod(null);
                feeDialog.dismiss();
            }
        });

        final EditText feeEditText = (EditText) feeDialog.findViewById(R.id.FeeAlertDialogFeeEditText);
        Button okButton = (Button) feeDialog.findViewById(R.id.FeeAlertDialogOKButton);
        final RadioButton cashInHandRadioButton = (RadioButton) feeDialog.findViewById(R.id.FeeAlertDialogCashInHandRadioButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                double fee;

                try
                {
                    fee = Double.parseDouble(feeEditText.getText().toString());
                }
                catch(NumberFormatException e)
                {
                    fee = 0.00;
                }

                journeyFeeTextView.setText(feeEditText.getText().toString().equals("") ?
                        "Free (£0.00)" : "£"+ new DecimalFormat("0.00").format(fee) + (cashInHandRadioButton.isChecked() ? ", Cash in hand preferred." : " , Contact driver for payment options."));

                journey.setFee(fee);
                journey.setPreferredPaymentMethod(cashInHandRadioButton.isChecked() ? "Cash in hand preferred." : "Contact driver for payment options.");
                journeyFeeTextView.setError(null);
                feeDialog.dismiss();
            }
        });

        feeDialog.show();
    }

    /**
     * Prompts the user to enter the number of available seats for this journey.
     */
    private void getAvailableSeats()
    {
        final String[] seats = {"1", "2", "3", "4", "5", "6", "7", "8" , "9", "10"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select number of available seats");
        builder.setItems(seats, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                journeyAvailableSeatsTextView.setText(seats[item]);
                journey.setAvailableSeats(Integer.parseInt(seats[item]));
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Validates the current journey object and checks if all required fields have been assigned.
     */
    private boolean validateJourney()
    {
        boolean isValid = true;

        if(journey.getVehicleType() == -1)
        {
            journeyVehicleTypeTextView.setError(journey.getVehicleType() == -1 ? "Please select vehicle type" : null);
            isValid = false;
        }

        if(journey.getFee() == -1)
        {
            journeyFeeTextView.setError(journey.getFee() == -1 ? "Please enter fee" : null);
            isValid = false;
        }

        if(journeyDateTextView.getText().toString().equals(SELECT_DATE))
        {
            journeyDateTextView.setError(journeyDateTextView.getText().toString().equals(SELECT_DATE) ? "Please select date" : null);
            isValid = false;
        }

        if(journeyTimeTextView.getText().toString().equals(SELECT_TIME))
        {
            journeyTimeTextView.setError(journeyTimeTextView.getText().toString().equals(SELECT_TIME) ? "Please select time" : null);
            isValid = false;
        }

        if(isValid)
        {
            journey.setDescription(journeyCommentsEditText.getText().toString());
        }

        return  isValid;
    }

    /**
     * Calls the web service to either create a new journey or edit the existing one depending on the current mode.
     */
    private void sendServiceRequest()
    {
        if(validateJourney())
        {
            createButton.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);

            new WcfPostServiceTask<Journey>(this, getResources().getString(mode ==
                    IntentConstants.JOURNEY_CREATOR_MODE_CREATING ?
                    R.string.CreateNewJourneyURL : R.string.EditJourneyURL),
                    journey,
                    new TypeToken<ServiceResponse<Boolean>>() {}.getType(),
                    appManager.getAuthorisationHeaders(), new WCFServiceCallback<Boolean, Void>() {
                @Override
                public void onServiceCallCompleted(ServiceResponse<Boolean> serviceResponse, Void parameter) {
                    progressBar.setVisibility(View.GONE);

                    if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
                    {
                        operationCompletedSuccessfully();
                    }
                    else
                    {
                        createButton.setEnabled(true);
                    }
                }
            }).execute();

        }
    }

    /**
     * Called after web service operation is completed.
     */
    private void operationCompletedSuccessfully()
    {
        startActivity(new Intent(this, HomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        Toast.makeText(this, mode == IntentConstants.JOURNEY_CREATOR_MODE_CREATING ?
                "Your journey was created successfully." : "Changes to your journey were saved successfully.", Toast.LENGTH_LONG).show();
    }
}
