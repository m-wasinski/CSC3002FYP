package com.example.myapplication.Fragments;

import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.myapplication.Activities.SearchResultsActivity;
import com.example.myapplication.DomainObjects.CarShare;
import com.example.myapplication.DomainObjects.ServiceResponse;
import com.example.myapplication.Experimental.WCFDateTimeHelper;
import com.example.myapplication.Helpers.SearchHelper;
import com.example.myapplication.Interfaces.SearchCompleted;
import com.example.myapplication.R;
import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Michal on 30/11/13.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class FragmentSearch extends android.support.v4.app.Fragment implements SearchCompleted{

    private Calendar myCalendar;
    private TextView dateTextView;
    private TextView timeTextView;
    private View theView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myCalendar = Calendar.getInstance();
        theView = inflater.inflate(R.layout.search_car_shares_fragment, container, false);

        dateTextView = (TextView) theView.findViewById(R.id.SearchDateTextView);
        timeTextView = (TextView) theView.findViewById(R.id.SearchTimeTextView);

        final DatePickerDialog dateDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener(){
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }
        } ,myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH));

        dateTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                dateDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == DialogInterface.BUTTON_NEGATIVE) {
                            dialog.dismiss();
                            dateTextView.setText("");
                        }
                    }
                });
                dateDialog.show();
                return false;
            }
        });

        final TimePickerDialog timeDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
            boolean fired = false;
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
                timeDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == DialogInterface.BUTTON_NEGATIVE) {
                            dialog.dismiss();
                            timeTextView.setText("");
                        }
                    }
                });
                timeDialog.updateTime(Calendar.HOUR_OF_DAY, Calendar.MINUTE);
                timeDialog.show();
                return false;
            }
        });

        SetupUIEvents();
        return theView;
    }


    private void SetupUIEvents()
    {
        Button quickSearchButton = (Button) theView.findViewById(R.id.SearchButton);
        quickSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    BuildObjectAndSearch();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void BuildObjectAndSearch() throws ParseException {
        TextView departureCityTextView = (TextView)  theView.findViewById(R.id.SearchDepartureCityTextView);
        TextView destinationCityTextView = (TextView) theView.findViewById(R.id.SearchDestinationCityTextView);


        CheckBox smokers = (CheckBox)  theView.findViewById(R.id.SmokersCheckbox);
        CheckBox womenOnly = (CheckBox)  theView.findViewById(R.id.SearchWomenOnlyCheckbox);
        CheckBox free = (CheckBox) theView.findViewById(R.id.SearchFreeCheckbox);
        CheckBox petsAllowed = (CheckBox) theView.findViewById(R.id.SearchPetsCheckBox);

        CarShare carShare = new CarShare();

        carShare.DestinationCity = destinationCityTextView.getText().toString();
        carShare.DepartureCity = departureCityTextView.getText().toString();
        carShare.SmokersAllowed = smokers.isChecked();
        carShare.WomenOnly = womenOnly.isChecked();
        carShare.PetsAllowed = petsAllowed.isChecked();
        carShare.Free = free.isChecked();

        Log.e("DateTime", myCalendar.getTime().toString());
        Log.e("DateTimeWCF", WCFDateTimeHelper.ConvertToWCFDateTime(myCalendar.getTime()));

        carShare.DateAndTimeOfDeparture = WCFDateTimeHelper.ConvertToWCFDateTime(myCalendar.getTime());
        carShare.SearchByDate = dateTextView.getText().toString().length() != 0;
        carShare.SearchByTime = timeTextView.getText().toString().length() != 0;

        SearchHelper.SearchCarShares(carShare, this);
    }

    @Override
    public void OnSearchCompleted(ServiceResponse<ArrayList<CarShare>> serviceResponse) {
        Intent intent = new Intent(getActivity(), SearchResultsActivity.class);

        Gson gson = new Gson();
        intent.putExtra("CarShares", gson.toJson(serviceResponse.Result));

        startActivity(intent);
    }

    private void updateLabel() {

        String myFormat = "MM/dd/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        dateTextView.setText(sdf.format(myCalendar.getTime()));
    }
}
