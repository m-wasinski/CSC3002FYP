package com.example.myapplication.Activities.Activities;

import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.myapplication.Activities.Base.BaseActivity;
import com.example.myapplication.Constants.ServiceResponseCode;
import com.example.myapplication.DomainObjects.CarShare;
import com.example.myapplication.DomainObjects.ServiceResponse;
import com.example.myapplication.Experimental.DateTimeHelper;
import com.example.myapplication.Interfaces.WCFServiceCallback;
import com.example.myapplication.NetworkTasks.WCFServiceTask;
import com.example.myapplication.R;
import com.google.gson.reflect.TypeToken;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Michal on 07/12/13.
 */
public class PostNewCarShareActivity extends BaseActivity implements WCFServiceCallback<CarShare, String> {

    private Calendar myCalendar;
    private EditText dateTextView;
    private EditText timeTextView;
    private Spinner vehicleTypes;
    private EditText destinationCity;
    private EditText departureCity;
    private EditText availableSeats;
    private CheckBox smokers, womenOnly, pets, privateShare;
    private EditText fee;
    private EditText description;

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_new_car_share);

        initialiseUIElements();

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.vehicle_types, R.layout.vehicle_types_custom_layout);
        vehicleTypes.setAdapter(adapter);


        final DatePickerDialog dateDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener(){
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, view.getYear());
                myCalendar.set(Calendar.MONTH, view.getMonth());
                myCalendar.set(Calendar.DAY_OF_MONTH, view.getDayOfMonth());
                String myFormat = "dd-MM-yyyy";
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.UK);
                dateTextView.setText(sdf.format(myCalendar.getTime()));
            }
        } ,myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH));

        dateTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                dateDialog.show();
                return false;
            }
        });

        final TimePickerDialog timeDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int i, int i2) {
                myCalendar.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
                myCalendar.set(Calendar.MINUTE, timePicker.getCurrentMinute());
                String myFormat = "HH:mm";
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.UK);
                timeTextView.setText(sdf.format(myCalendar.getTime()));
            }
        }, Calendar.HOUR_OF_DAY, Calendar.MINUTE, true);

        timeTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                timeDialog.updateTime(Calendar.HOUR_OF_DAY, Calendar.MINUTE);
                timeDialog.show();
                return false;
            }
        });

        setupUIEvents();
    }


    private void setupUIEvents()
    {
        Button doneButton = (Button) findViewById(R.id.PostNewCarShareDoneButton);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    postNewCarShare();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void postNewCarShare() throws ParseException {
          CarShare carShare = new CarShare();
          carShare.DestinationCity = destinationCity.getText().toString();
          carShare.DepartureCity = departureCity.getText().toString();
          carShare.DriverId = appData.getUser().UserId;
          carShare.Description = description.getText().toString();
          carShare.Fee = Double.parseDouble(fee.getText().toString());
          carShare.WomenOnly = womenOnly.isChecked();
          carShare.PetsAllowed = pets.isChecked();
          carShare.SmokersAllowed = smokers.isChecked();
          carShare.VehicleType = vehicleTypes.getSelectedItemPosition();
          carShare.AvailableSeats = Integer.parseInt(availableSeats.getText().toString());
          carShare.DateAndTimeOfDeparture = DateTimeHelper.convertToWCFDate(myCalendar.getTime());
          carShare.Private = privateShare.isChecked();
          carShare.PetsAllowed = pets.isChecked();

          progressDialog = new ProgressDialog(this);
          progressDialog.setTitle("Contacting server...");
          progressDialog.setMessage("Please wait.");
          progressDialog.show();

        new WCFServiceTask<CarShare, CarShare>("https://findndrive.no-ip.co.uk/Services/CarShareService.svc/create",
                carShare,
                new TypeToken<ServiceResponse<CarShare>>() {}.getType(),
                appData.getAuthorisationHeaders(),null, this).execute();
    }

    private void initialiseUIElements()
    {
        description = (EditText) findViewById(R.id.PostNewCarShareDescriptionTextView);
        departureCity = (EditText) findViewById(R.id.PostNewCarShareDepartureCityTextView);
        destinationCity = (EditText) findViewById(R.id.PostNewCarShareDestinationCityTextView);
        smokers = (CheckBox) findViewById(R.id.PostNewCarShareSmokers);
        womenOnly = (CheckBox) findViewById(R.id.PostNewCarShareWomenOnly);
        privateShare = (CheckBox) findViewById(R.id.PostNewCarSharePrivate);
        pets = (CheckBox) findViewById(R.id.PostNewCarSharePets);
        dateTextView = (EditText) findViewById(R.id.PostNewCarShareDateTextView);
        timeTextView = (EditText) findViewById(R.id.PostNewCarShareTimeTextView);
        fee = (EditText) findViewById(R.id.PostNewCarShareFeeTextView);
        fee.setRawInputType(Configuration.KEYBOARD_12KEY);
        availableSeats = (EditText) findViewById(R.id.PostNewCarShareAvailableSeatsTextView);
        availableSeats.setRawInputType(Configuration.KEYBOARD_12KEY);
        vehicleTypes = (Spinner) findViewById(R.id.PostNewCarShareVehicleTypeSpinner);
        myCalendar = Calendar.getInstance();
    }

    @Override
    public void onServiceCallCompleted(ServiceResponse<CarShare> serviceResponse, String parameter) {
        super.checkIfAuthorised(serviceResponse.ServiceResponseCode);
        if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
        {
            Toast toast = Toast.makeText(this, "Car share posted successfully.", Toast.LENGTH_LONG);
            toast.show();
            progressDialog.dismiss();
            finish();
        }
    }
}
