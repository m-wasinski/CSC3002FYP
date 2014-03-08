package com.example.myapplication.activities.activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.activities.base.BaseActivity;
import com.example.myapplication.constants.IntentConstants;
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.domain_objects.Journey;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WcfPostServiceTask;
import com.example.myapplication.utilities.DateTimeHelper;
import com.example.myapplication.factories.DialogFactory;
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

    // Private variables.
    private ImageView minimapImageView;
    private Journey journey;
    private Calendar calendar;

    private TextView fromTextView;
    private TextView toTextView;
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
    private final String SELECT_FEE = "Select fee & payment method";
    private final String SELECT_VEHICLE = "Select vehicle type";

    private int mode;
    private String[] vehiclesTypes;
    private String[] paymentOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_offer_journey_step_two);
        this.calendar = Calendar.getInstance();

        //retrieve the journey.
        Bundle bundle = getIntent().getExtras();
        this.journey = gson.fromJson(bundle.getString(IntentConstants.JOURNEY), new TypeToken<Journey>() {}.getType());
        this.mode = bundle.getInt(IntentConstants.JOURNEY_CREATOR_MODE);

        // Initialise both resource arrays.
        this.vehiclesTypes = this.getResources().getStringArray(R.array.vehicle_types);
        this.paymentOptions = this.getResources().getStringArray(R.array.payment_options);

        //Initialise UI elements.
        this.minimapImageView = (ImageView) findViewById(R.id.OfferJourneyStepTwoActivityMiniMapImageView);
        this.minimapImageView.setImageBitmap(BitmapFactory.decodeByteArray(
                getIntent().getByteArrayExtra(IntentConstants.MINIMAP), 0, getIntent().getByteArrayExtra(IntentConstants.MINIMAP).length));

        this.journeyTimeRelativeLayout = (RelativeLayout) findViewById(R.id.OfferJourneyStepTwoActivityJourneyTimeRelativeLayout);
        this.journeyTimeTextView = (TextView) findViewById(R.id.OfferJourneyStepTwoActivityJourneyTimeTextView);
        this.journeyTimeTextView.setText(this.mode == IntentConstants.JOURNEY_CREATOR_MODE_EDITING ?
                DateTimeHelper.getSimpleTime(this.journey.getDateAndTimeOfDeparture()) : this.SELECT_TIME);

        this.journeyDateRelativeLayout = (RelativeLayout) findViewById(R.id.OfferJourneyStepTwoActivityJourneyDateRelativeLayout);
        this.journeyDateTextView = (TextView) findViewById(R.id.OfferJourneyStepTwoActivityDateTextView);
        this.journeyDateTextView.setText(this.mode == IntentConstants.JOURNEY_CREATOR_MODE_EDITING ?
                DateTimeHelper.getSimpleDate(this.journey.getDateAndTimeOfDeparture()) :  this.SELECT_DATE);

        this.journeyPrivateRelativeLayout = (RelativeLayout) findViewById(R.id.OfferJourneyStepTwoActivityJourneyPrivateRelativeLayout);

        this.journeyPrivateCheckbox = (CheckBox) findViewById(R.id.OfferJourneyStepTwoActivityJourneyPrivateCheckbox);
        this.journeyPrivateCheckbox.setChecked(this.journey.isPrivate());

        this.journeyPrivateTextView = (TextView) findViewById(R.id.OfferJourneyStepTwoActivityPrivateTextView);
        this.journeyPrivateTextView.setText(Utilities.translateBoolean(this.journey.isPrivate()));

        this.journeySmokersRelativeLayout = (RelativeLayout) findViewById(R.id.OfferJourneyStepTwoActivityJourneySmokersRelativeLayout);

        this.journeySmokersTextView = (TextView) findViewById(R.id.OfferJourneyStepTwoActivitySmokersTextView);
        this.journeySmokersTextView.setText(Utilities.translateBoolean(this.journey.areSmokersAllowed()));

        this.journeySmokersCheckbox = (CheckBox) findViewById(R.id.OfferJourneyStepTwoActivitySmokersCheckBox);
        this.journeySmokersCheckbox.setChecked(this.journey.areSmokersAllowed());

        this.journeyPetsRelativeLayout = (RelativeLayout) findViewById(R.id.OfferJourneyStepTwoActivityJourneyPetsRelativeLayout);

        this.journeyPetsTextView = (TextView) findViewById(R.id.OfferJourneyStepTwoActivityPetsTextView);
        this.journeyPetsTextView.setText(Utilities.translateBoolean(this.journey.arePetsAllowed()));

        this.journeyPetsCheckbox = (CheckBox) findViewById(R.id.OfferJourneyStepTwoActivityPetCheckBox);
        this.journeyPetsCheckbox.setChecked(this.journey.arePetsAllowed());

        this.journeyVehicleTypeRelativeLayout = (RelativeLayout) findViewById(R.id.OfferJourneyStepTwoActivityJourneyVehicleTypeRelativeLayout);

        this.journeyVehicleTypeTextView = (TextView) findViewById(R.id.OfferJourneyStepTwoActivityVehicleTextView);
        this.journeyVehicleTypeTextView.setText(this.mode == IntentConstants.JOURNEY_CREATOR_MODE_EDITING ? this.vehiclesTypes[this.journey.getVehicleType()] : this.SELECT_VEHICLE);

        this.journeyAvailableSeatsRelativeLayout = (RelativeLayout) findViewById(R.id.OfferJourneyStepTwoActivityJourneyAvailableSeatsRelativeLayout);
        this.journeyAvailableSeatsTextView = (TextView) findViewById(R.id.OfferJourneyStepTwoActivityAvailableSeatsTextView);
        this.journeyAvailableSeatsTextView.setText(String.valueOf(this.journey.getAvailableSeats()));

        this.journeyFeeRelativeLayout = (RelativeLayout) findViewById(R.id.OfferJourneyStepTwoActivityJourneyFeeRelativeLayout);
        this.journeyFeeTextView = (TextView) findViewById(R.id.OfferJourneyStepTwoActivityFeeTextView);
        this.journeyFeeTextView.setText(this.mode == IntentConstants.JOURNEY_CREATOR_MODE_EDITING ?
                ("£"+new DecimalFormat("0.00").format(this.journey.getFee())+ (journey.getPreferredPaymentMethod() == null ? "": ", " + journey.getPreferredPaymentMethod())) : this.SELECT_FEE);


        this.journeyCommentsEditText = (EditText) findViewById(R.id.OfferJourneyStepTwoActivityCommentsEditText);
        this.journeyCommentsEditText.setText(journey.getDescription() == null ? "" : journey.getDescription());

        this.createButton = (Button) findViewById(R.id.OfferJourneyStepTwoActivityCreateButton);
        this.createButton.setText(this.mode  == IntentConstants.JOURNEY_CREATOR_MODE_EDITING ? "Save changes" : "Offer journey");

        this.progressBar = (ProgressBar) findViewById(R.id.OfferJourneyStepTwoActivityProgressBar);

        this.actionBar.setTitle(this.mode == IntentConstants.JOURNEY_CREATOR_MODE_EDITING ? "Editing journey, step 2" : "Offering journey, step 2");

        //set the from address next to minimap.
        this.fromTextView = (TextView) findViewById(R.id.OfferJourneyStepTwoActivityFromTextView);
        this.fromTextView.setText("- "+this.journey.getGeoAddresses().get(0).AddressLine);

        //set the to address nex to minimap
        this.toTextView = (TextView) findViewById(R.id.OfferJourneyStepTwoActivityToTextView);
        this.toTextView.setText("- "+this.journey.getGeoAddresses().get(this.journey.getGeoAddresses().size() - 1).AddressLine);


        if(this.journey.getGeoAddresses().size() > 2)
        {
            addViaPoints();
        }

        // Setup all event handlers for the above UI elements.
        this.setupEventHandlers();
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
        float d = this.getResources().getDisplayMetrics().density;
        int margin = (int)(dpValue * d); // margin in pixels

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        params.setMargins(margin, 0, 0, 0);

        for(int i = 1; i < this.journey.getGeoAddresses().size()-1; i++)
        {
            TextView waypoint = (TextView)getLayoutInflater().inflate(R.layout.textview_template, null);
            waypoint.setLayoutParams(params);
            waypoint.setText("- "+this.journey.getGeoAddresses().get(i).AddressLine);
            viaPointsLinearLayout.addView(waypoint);
        }
    }

    private void setupEventHandlers()
    {
        this.journeyDateRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDate();
            }
        });

        this.journeyTimeRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getTime();
            }
        });

        this.journeyPrivateRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                journeyPrivateCheckbox.setChecked(!journeyPrivateCheckbox.isChecked());
                journey.setPrivate(journeyPrivateCheckbox.isChecked());
                journeyPrivateTextView.setText(Utilities.translateBoolean(journeyPrivateCheckbox.isChecked()));
            }
        });

        this.journeySmokersRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                journeySmokersCheckbox.setChecked(!journeySmokersCheckbox.isChecked());
                journey.setSmokersAllowed(journeySmokersCheckbox.isChecked());
                journeySmokersTextView.setText(Utilities.translateBoolean(journeySmokersCheckbox.isChecked()));
            }
        });

        this.journeyPetsRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                journeyPetsCheckbox.setChecked(!journeyPetsCheckbox.isChecked());
                journey.setPetsAllowed(journeyPetsCheckbox.isChecked());
                journeyPetsTextView.setText(Utilities.translateBoolean(journeyPetsCheckbox.isChecked()));
            }
        });

        this.journeyVehicleTypeRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getVehicleType();
            }
        });

        this.journeyAvailableSeatsRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getAvailableSeats();
            }
        });

        this.journeyFeeRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFee();
            }
        });

        this.createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buildOrEditJourney();
            }
        });
    }

    private void getDate()
    {
        final DatePickerDialog dateDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener(){
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMMM-yyyy", Locale.UK);

                journeyDateTextView.setText(simpleDateFormat.format(calendar.getTime()));
                journeyDateTextView.setError(null);
                journey.setDateAndTimeOfDeparture(DateTimeHelper.convertToWCFDate(calendar.getTime()));

            }
        } ,calendar
                .get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        dateDialog.getDatePicker().setMinDate(Calendar.getInstance().getTimeInMillis()- 1000);

        if(this.journey.getDateAndTimeOfDeparture() != null)
        {
            Calendar calendar1 = Calendar.getInstance();
            calendar1.setTime(DateTimeHelper.parseWCFDate(this.journey.getDateAndTimeOfDeparture()));

            calendar.set(Calendar.YEAR, calendar1.get(Calendar.YEAR));
            calendar.set(Calendar.MONTH, calendar1.get(Calendar.MONTH));
            calendar.set(Calendar.DAY_OF_MONTH, calendar1.get(Calendar.DAY_OF_MONTH));

            dateDialog.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        }

        dateDialog.show();
    }

    private void getTime()
    {
        final TimePickerDialog timeDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int i, int i2) {
                calendar.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
                calendar.set(Calendar.MINUTE, timePicker.getCurrentMinute());
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.UK);
                journeyTimeTextView.setText(sdf.format(calendar.getTime()));
                journeyTimeTextView.setError(null);
                journey.setDateAndTimeOfDeparture(DateTimeHelper.convertToWCFDate(calendar.getTime()));
            }
        }, Calendar.HOUR_OF_DAY, Calendar.MINUTE, true);

        if(this.journey.getDateAndTimeOfDeparture() != null)
        {
            Calendar calendar1 = Calendar.getInstance();
            calendar1.setTime(DateTimeHelper.parseWCFDate(this.journey.getDateAndTimeOfDeparture()));

            calendar.set(Calendar.HOUR_OF_DAY, calendar1.get(Calendar.HOUR_OF_DAY));
            calendar.set(Calendar.MINUTE, calendar1.get(Calendar.MINUTE));

            timeDialog.updateTime(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
        }

        timeDialog.show();
    }

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

    private boolean buildAndValidate()
    {
        boolean isValid = true;

        if(this.journey.getVehicleType() == -1)
        {
            this.journeyVehicleTypeTextView.setError(this.journey.getVehicleType() == -1 ? "Please select vehicle type" : null);
            isValid = false;
        }

        if(this.journey.getFee() == -1)
        {
            this.journeyFeeTextView.setError(this.journey.getFee() == -1 ? "Please enter fee" : null);
            isValid = false;
        }

        if(this.journeyDateTextView.getText().toString().equals(this.SELECT_DATE))
        {
            this.journeyDateTextView.setError(this.journeyDateTextView.getText().toString().equals(this.SELECT_DATE) ? "Please select date" : null);
            isValid = false;
        }

        if(this.journeyTimeTextView.getText().toString().equals(this.SELECT_TIME))
        {
            this.journeyTimeTextView.setError(this.journeyTimeTextView.getText().toString().equals(this.SELECT_TIME) ? "Please select time" : null);
            isValid = false;
        }

        if(isValid)
        {
            this.journey.setDescription(this.journeyCommentsEditText.getText().toString());
        }

        return  isValid;
    }

    private void buildOrEditJourney()
    {
        if(buildAndValidate())
        {
            createButton.setEnabled(false);
            this.progressBar.setVisibility(View.VISIBLE);

            new WcfPostServiceTask<Journey>(this, getResources().getString(this.mode ==
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

    private void operationCompletedSuccessfully()
    {
        startActivity(new Intent(this, HomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        Toast.makeText(this, mode == IntentConstants.JOURNEY_CREATOR_MODE_CREATING ?
                "Your journey was created successfully." : "Changes to your journey were saved successfully.", Toast.LENGTH_LONG).show();
    }
}
