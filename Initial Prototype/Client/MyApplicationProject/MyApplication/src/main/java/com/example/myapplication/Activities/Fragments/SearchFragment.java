package com.example.myapplication.activities.fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.myapplication.activities.activities.SearchActivity;
import com.example.myapplication.activities.base.BaseFragment;
import com.example.myapplication.activities.activities.ContactDriverActivity;
import com.example.myapplication.adapters.SearchResultsAdapter;
import com.example.myapplication.constants.SearchConstants;
import com.example.myapplication.domain_objects.Journey;
import com.example.myapplication.domain_objects.JourneyRequest;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.experimental.DateTimeHelper;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WCFServiceTask;
import com.example.myapplication.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Michal on 30/11/13.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class SearchFragment extends BaseFragment implements WCFServiceCallback<ArrayList<Journey>, String>{

    private Calendar myCalendar;
    private TextView dateTextView;
    private TextView timeTextView;
    private View view;
    private ListView searchResultsListView;
    private TextView searchLabelTextView;
    private EditText departureAndDestinationEditText;
    private Mode mode;
    private static final int GET_ADDRESS_REQUEST = 1;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        myCalendar = Calendar.getInstance();
        view = inflater.inflate(R.layout.fragment_search_journeys, container, false);
        dateTextView = (TextView) view.findViewById(R.id.SearchDateTextView);
        timeTextView = (TextView) view.findViewById(R.id.SearchTimeTextView);
        searchLabelTextView = (TextView) view.findViewById(R.id.SearchLabel);
        departureAndDestinationEditText = (EditText) view.findViewById(R.id.FragmentSearchDepartureAndDestinationTextView);
        mode = Mode.SearchPanelExpanded;

        searchLabelTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mode == Mode.SearchPanelExpanded)
                {
                    expandSearchResults();
                }
                else
                {
                    restoreSearchPane();
                }
            }
        });
        searchResultsListView = (ListView) view.findViewById(R.id.SearchCarSharesListView);

        final DatePickerDialog dateDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener(){
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                String myFormat = "MM/dd/yy"; //In which you need put here
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.UK);

                dateTextView.setText(sdf.format(myCalendar.getTime()));
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

        TextView departureCityTextView = (TextView)  view.findViewById(R.id.FragmentSearchDepartureAndDestinationTextView);
        departureCityTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                startActivityForResult(intent, GET_ADDRESS_REQUEST);
            }
        });

        setupUIEvents();
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Check which request it is that we're responding to
        if (requestCode == GET_ADDRESS_REQUEST) {
            // Make sure the request was successful
            if (resultCode == Activity.RESULT_OK) {
                this.departureAndDestinationEditText.setText("From: " + data.getExtras().getString(SearchConstants.DEPARTURE_ADDRESS)+"\n"
                        +"To: " + data.getExtras().getString(SearchConstants.DESTINATION_ADDRESS));
            }
        }
    }

    private void setupUIEvents()
    {
        Button quickSearchButton = (Button) view.findViewById(R.id.SearchButton);
        quickSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    search();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void search() throws ParseException {
        TextView departureCityTextView = (TextView)  view.findViewById(R.id.FragmentSearchDepartureAndDestinationTextView);
        //TextView destinationCityTextView = (TextView) view.findViewById(R.id.SearchDestinationCityTextView);

        /*CheckBox smokers = (CheckBox)  view.findViewById(R.id.SmokersCheckbox);
        CheckBox womenOnly = (CheckBox)  view.findViewById(R.id.SearchWomenOnlyCheckbox);
        CheckBox free = (CheckBox) view.findViewById(R.id.SearchFreeCheckbox);
        CheckBox petsAllowed = (CheckBox) view.findViewById(R.id.SearchPetsCheckBox);*/

        Journey carShare = new Journey();

        //carShare.DestinationCity = destinationCityTextView.getText().toString();
        //carShare.DepartureCity = departureCityTextView.getText().toString();
        //carShare.SmokersAllowed = smokers.isChecked();
        //carShare.WomenOnly = womenOnly.isChecked();
        //carShare.PetsAllowed = petsAllowed.isChecked();
        //carShare.Free = free.isChecked();
        carShare.DateAndTimeOfDeparture = DateTimeHelper.convertToWCFDate(myCalendar.getTime());
        //carShare.SearchByDate = dateTextView.getText().toString().length() != 0;
        //carShare.SearchByTime = timeTextView.getText().toString().length() != 0;

        new WCFServiceTask<Journey>(getActivity().getApplicationContext(),"https://findndrive.no-ip.co.uk/Services/SearchService.svc/searchcarshare",
                carShare, new TypeToken<ServiceResponse<ArrayList<Journey>>>() {}.getType(), findNDriveManager.getAuthorisationHeaders(), this).execute();
    }


    private void expandSearchResults()
    {

    }

    private void restoreSearchPane()
    {

    }

    @Override
    public void onServiceCallCompleted(final ServiceResponse<ArrayList<Journey>> carShareResults, String parameter) {
        SearchResultsAdapter adapter = new SearchResultsAdapter(getActivity(), R.layout.fragment_search_results_listview_row, carShareResults.Result);
        searchResultsListView.setAdapter(adapter);

        searchResultsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {

                new WCFServiceTask<Integer>(getActivity().getApplicationContext(), "https://findndrive.no-ip.co.uk/Services/RequestService.svc/getrequests",
                        carShareResults.Result.get(i).JourneyId,
                        new TypeToken<ServiceResponse<ArrayList<JourneyRequest>>>() {}.getType(),
                        findNDriveManager.getAuthorisationHeaders(), new WCFServiceCallback<ArrayList<JourneyRequest>, String>() {
                    @Override
                    public void onServiceCallCompleted(ServiceResponse<ArrayList<JourneyRequest>> serviceResponse, String parameter) {
                        Gson gson = new Gson();
                        Intent intent = new Intent(getActivity(), ContactDriverActivity.class);
                        intent.putExtra("CurrentCarShare", gson.toJson(carShareResults.Result.get(i)));
                        intent.putExtra("CurrentRequests", gson.toJson(serviceResponse.Result));
                        startActivity(intent);
                    }
                }).execute();
            }
        });
        expandSearchResults();
    }
}

enum Mode {
    SearchPanelExpanded,
    SearchResultsExpanded
}
