package com.example.myapplication.activities.activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
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
import com.example.myapplication.experimental.DateTimeHelper;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WCFServiceTask;
import com.example.myapplication.utilities.Helpers;
import com.google.gson.reflect.TypeToken;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer_journey_step_two);
        actionBar.hide();
        this.calendar = Calendar.getInstance();

        //retrieve the journey.
        Bundle extras = getIntent().getExtras();
        this.journey = gson.fromJson(extras.getString(IntentConstants.JOURNEY), new TypeToken<Journey>() {}.getType());
        this.journey.AvailableSeats  = 1;
        this.journey.PetsAllowed = false;
        this.journey.SmokersAllowed = false;
        this.journey.Private = false;
        this.journey.VehicleType = 0;
        this.journey.Fee = 0.00;

        //Initialise UI elements.
        this.minimapImageView = (ImageView) findViewById(R.id.OfferJourneyStepTwoActivityMiniMapImageView);
        this.minimapImageView.setImageBitmap(BitmapFactory.decodeByteArray(
                getIntent().getByteArrayExtra(IntentConstants.MINIMAP), 0, getIntent().getByteArrayExtra(IntentConstants.MINIMAP).length));

        this.journeyTimeRelativeLayout = (RelativeLayout) findViewById(R.id.OfferJourneyStepTwoActivityJourneyTimeRelativeLayout);
        this.journeyTimeTextView = (TextView) findViewById(R.id.OfferJourneyStepTwoActivityJourneyTimeTextView);
        this.journeyTimeTextView.setText(this.SELECT_TIME);

        this.journeyDateRelativeLayout = (RelativeLayout) findViewById(R.id.OfferJourneyStepTwoActivityJourneyDateRelativeLayout);
        this.journeyDateTextView = (TextView) findViewById(R.id.OfferJourneyStepTwoActivityDateTextView);
        this.journeyDateTextView.setText(this.SELECT_DATE);

        this.journeyPrivateRelativeLayout = (RelativeLayout) findViewById(R.id.OfferJourneyStepTwoActivityJourneyPrivateRelativeLayout);
        this.journeyPrivateCheckbox = (CheckBox) findViewById(R.id.OfferJourneyStepTwoActivityJourneyPrivateCheckbox);
        this.journeyPrivateTextView = (TextView) findViewById(R.id.OfferJourneyStepTwoActivityPrivateTextView);

        this.journeySmokersRelativeLayout = (RelativeLayout) findViewById(R.id.OfferJourneyStepTwoActivityJourneySmokersRelativeLayout);
        this.journeySmokersTextView = (TextView) findViewById(R.id.OfferJourneyStepTwoActivitySmokersTextView);
        this.journeySmokersCheckbox = (CheckBox) findViewById(R.id.OfferJourneyStepTwoActivitySmokersCheckBox);

        this.journeyPetsRelativeLayout = (RelativeLayout) findViewById(R.id.OfferJourneyStepTwoActivityJourneyPetsRelativeLayout);
        this.journeyPetsTextView = (TextView) findViewById(R.id.OfferJourneyStepTwoActivityPetsTextView);
        this.journeyPetsCheckbox = (CheckBox) findViewById(R.id.OfferJourneyStepTwoActivityPetCheckBox);

        this.journeyVehicleTypeRelativeLayout = (RelativeLayout) findViewById(R.id.OfferJourneyStepTwoActivityJourneyVehicleTypeRelativeLayout);
        this.journeyVehicleTypeTextView = (TextView) findViewById(R.id.OfferJourneyStepTwoActivityVehicleTextView);
        this.journeyVehicleTypeTextView.setText(this.SELECT_VEHICLE);

        this.journeyAvailableSeatsRelativeLayout = (RelativeLayout) findViewById(R.id.OfferJourneyStepTwoActivityJourneyAvailableSeatsRelativeLayout);
        this.journeyAvailableSeatsTextView = (TextView) findViewById(R.id.OfferJourneyStepTwoActivityAvailableSeatsTextView);

        this.journeyFeeRelativeLayout = (RelativeLayout) findViewById(R.id.OfferJourneyStepTwoActivityJourneyFeeRelativeLayout);
        this.journeyFeeTextView = (TextView) findViewById(R.id.OfferJourneyStepTwoActivityFeeTextView);
        this.journeyFeeTextView.setText(this.SELECT_FEE);

        this.journeyCommentsEditText = (EditText) findViewById(R.id.OfferJourneyStepTwoActivityCommentsEditText);

        this.createButton = (Button) findViewById(R.id.OfferJourneyStepTwoActivityCreateButton);

        this.progressBar = (ProgressBar) findViewById(R.id.OfferJourneyStepTwoActivityProgressBar);

        //set the from address next to minimap.
        this.fromTextView = (TextView) findViewById(R.id.OfferJourneyStepTwoActivityFromTextView);
        this.fromTextView.setText("- "+this.journey.GeoAddresses.get(0).AddressLine);

        //set the to address nex to minimap
        this.toTextView = (TextView) findViewById(R.id.OfferJourneyStepTwoActivityToTextView);
        this.toTextView.setText("- "+this.journey.GeoAddresses.get(this.journey.GeoAddresses.size()-1).AddressLine);

        if(this.journey.GeoAddresses.size() > 2)
        {
            addViaPoints();
        }

        setupEventHandlers();
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

        for(int i = 1; i < this.journey.GeoAddresses.size()-1; i++)
        {
            TextView waypoint = (TextView)getLayoutInflater().inflate(R.layout.textview_template, null);
            waypoint.setLayoutParams(params);
            waypoint.setText("- "+this.journey.GeoAddresses.get(i).AddressLine);
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
                journey.Private = journeyPrivateCheckbox.isChecked();
                journeyPrivateTextView.setText(Helpers.translateBoolean(journeyPrivateCheckbox.isChecked()));
            }
        });

        this.journeySmokersRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                journeySmokersCheckbox.setChecked(!journeySmokersCheckbox.isChecked());
                journey.SmokersAllowed = journeySmokersCheckbox.isChecked();
                journeySmokersTextView.setText(Helpers.translateBoolean(journeySmokersCheckbox.isChecked()));
            }
        });

        this.journeyPetsRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                journeyPetsCheckbox.setChecked(!journeyPetsCheckbox.isChecked());
                journey.PetsAllowed = journeyPetsCheckbox.isChecked();
                journeyPetsTextView.setText(Helpers.translateBoolean(journeyPetsCheckbox.isChecked()));
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
                createJourney();
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
                journey.DateAndTimeOfDeparture = DateTimeHelper.convertToWCFDate(calendar.getTime());
            }
        } ,calendar
                .get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
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
                journey.DateAndTimeOfDeparture = DateTimeHelper.convertToWCFDate(calendar.getTime());
            }
        }, Calendar.HOUR_OF_DAY, Calendar.MINUTE, true);
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
                journey.VehicleType = item;
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void getFee()
    {
        // custom feeDialog
        final Dialog feeDialog = new Dialog(this);
        feeDialog.setContentView(R.layout.alert_dialog_fee_selector);
        feeDialog.setTitle("Fee & Payment options");

        Button freeButton = (Button) feeDialog.findViewById(R.id.FeeAlertDialogFreeButton);
        freeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                journeyFeeTextView.setText("Free (£0.00)");
                journeyFeeTextView.setError(null);
                journey.Fee = 0.00;
                feeDialog.dismiss();
            }
        });

        final EditText feeEditText = (EditText) feeDialog.findViewById(R.id.FeeAlertDialogFeeEditText);
        Button okButton = (Button) feeDialog.findViewById(R.id.FeeAlertDialogOKButton);
        final RadioButton cashInHandRadioButton = (RadioButton) feeDialog.findViewById(R.id.FeeAlertDialogCashInHandRadioButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                journeyFeeTextView.setText(feeEditText.getText().toString().equals("") ?
                        "Free (£0.00)" : "£"+feeEditText.getText().toString()  + (cashInHandRadioButton.isChecked() ? ", Cash in hand preferred." : " , Contact driver for payment options."));
                journey.Fee = feeEditText.getText().toString().equals("") ? 0.00 : Double.parseDouble(feeEditText.getText().toString());
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
                journey.AvailableSeats = Integer.parseInt(seats[item]);
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private boolean buildAndValidate()
    {
        boolean isValid;

        isValid = !this.journeyVehicleTypeTextView.getText().toString().equals(this.SELECT_VEHICLE);
        this.journeyVehicleTypeTextView.setError(this.journeyVehicleTypeTextView.getText().toString().equals(this.SELECT_VEHICLE) ? "Please select vehicle type" : null);

        isValid = !this.journeyFeeTextView.getText().toString().equals(this.SELECT_FEE);
        this.journeyFeeTextView.setError(this.journeyFeeTextView.getText().toString().equals(this.SELECT_FEE) ? "Please enter fee" : null);

        isValid = !this.journeyDateTextView.getText().toString().equals(this.SELECT_DATE);
        this.journeyDateTextView.setError(this.journeyDateTextView.getText().toString().equals(this.SELECT_DATE) ? "Please select date" : null);
        if(this.journeyDateTextView.getText().toString().equals(this.SELECT_DATE))

        isValid = !this.journeyTimeTextView.getText().toString().equals(this.SELECT_TIME);
        this.journeyTimeTextView.setError(this.journeyTimeTextView.getText().toString().equals(this.SELECT_TIME) ? "Please select time" : null);

        if(isValid)
        {
            this.journey.Description = this.journeyCommentsEditText.getText().toString();
            this.journey.DriverId = this.findNDriveManager.getUser().UserId;
            this.journey.CreationDate = DateTimeHelper.convertToWCFDate(Calendar.getInstance().getTime());
        }

        return  isValid;
    }

    private void createJourney()
    {
        if(buildAndValidate())
        {
            this.progressBar.setVisibility(View.VISIBLE);
            new WCFServiceTask<Journey>(this, getResources().getString(R.string.CreateNewJourneyURL),
                    journey,
                    new TypeToken<ServiceResponse<Journey>>() {}.getType(),
                    findNDriveManager.getAuthorisationHeaders(), new WCFServiceCallback<Journey, Void>() {
                @Override
                public void onServiceCallCompleted(ServiceResponse<Journey> serviceResponse, Void parameter) {
                    progressBar.setVisibility(View.GONE);

                    if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
                    {
                        journeyCreatedSuccessfully();
                    }
                    else
                    {
                        errorWhileCreatingJourney();
                    }
                }
            }).execute();
        }
    }

    private void journeyCreatedSuccessfully()
    {
        Intent intent = new Intent(this, HomeActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        Toast toast = Toast.makeText(this, "Your journey was created successfully.", Toast.LENGTH_LONG);
        toast.show();
    }

    private void errorWhileCreatingJourney()
    {

    }
}
