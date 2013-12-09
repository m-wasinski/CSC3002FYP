package com.example.myapplication.Fragments;

import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.app.Fragment;
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

import com.example.myapplication.Activities.HomeActivity;
import com.example.myapplication.Activities.SearchResultsActivity;
import com.example.myapplication.DomainObjects.CarShare;
import com.example.myapplication.DomainObjects.ServiceResponse;
import com.example.myapplication.Helpers.SearchHelper;
import com.example.myapplication.Interfaces.SearchCompleted;
import com.example.myapplication.R;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Michal on 30/11/13.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class FragmentQuickSearch extends Fragment implements SearchCompleted{

    private Calendar myCalendar;
    private TextView dateTextView;

    private View theView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myCalendar = Calendar.getInstance();
        theView = inflater.inflate(R.layout.quick_search_fragment, container, false);
        dateTextView = (TextView) theView.findViewById(R.id.QuickSearchDateTextView);

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
                dateDialog.show();
                return false;
            }
        });



        SetupUIEvents();
        return theView;
    }


    private void SetupUIEvents()
    {
        Button quickSearchButton = (Button) theView.findViewById(R.id.QuickSearchButton);
        quickSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BuildObjectAndSearch();
            }
        });
    }

    private void BuildObjectAndSearch()
    {
        TextView departureCityTextView = (TextView)  theView.findViewById(R.id.QuickSearchDepartureCity);
        TextView destinationCityTextView = (TextView) theView.findViewById(R.id.QuickSearchDestinationCity);


        CheckBox smokers = (CheckBox)  theView.findViewById(R.id.SmokersCheckbox);
        CheckBox womenOnly = (CheckBox)  theView.findViewById(R.id.WomenOnlyCheckbox);
        CheckBox free = (CheckBox) theView.findViewById(R.id.FreeCheckbox);

        CarShare carShare = new CarShare();

        carShare.DestinationCity = destinationCityTextView.getText().toString();
        carShare.DepartureCity = departureCityTextView.getText().toString();
        carShare.SmokersAllowed = smokers.isChecked();
        carShare.WomenOnly = womenOnly.isChecked();

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
