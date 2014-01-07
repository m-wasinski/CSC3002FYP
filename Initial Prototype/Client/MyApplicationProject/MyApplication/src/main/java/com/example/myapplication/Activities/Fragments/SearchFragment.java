package com.example.myapplication.Activities.Fragments;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
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
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.myapplication.Activities.Activities.MapActivity;
import com.example.myapplication.Activities.Base.BaseFragment;
import com.example.myapplication.Activities.Activities.ContactDriverActivity;
import com.example.myapplication.Adapters.MyCarSharesAdapter;
import com.example.myapplication.Adapters.SearchResultsAdapter;
import com.example.myapplication.Constants.Constants;
import com.example.myapplication.DomainObjects.CarShare;
import com.example.myapplication.DomainObjects.CarShareRequest;
import com.example.myapplication.DomainObjects.ServiceResponse;
import com.example.myapplication.Experimental.DateTimeHelper;
import com.example.myapplication.Interfaces.WCFServiceCallback;
import com.example.myapplication.NetworkTasks.WCFServiceTask;
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
public class SearchFragment extends BaseFragment implements WCFServiceCallback<ArrayList<CarShare>, String>{

    private Calendar myCalendar;
    private TextView dateTextView;
    private TextView timeTextView;
    private View view;
    private ListView searchResultsListView;
    private TextView searchLabelTextView;
    private Mode mode;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        myCalendar = Calendar.getInstance();
        view = inflater.inflate(R.layout.fragment_search_car_shares, container, false);
        dateTextView = (TextView) view.findViewById(R.id.SearchDateTextView);
        timeTextView = (TextView) view.findViewById(R.id.SearchTimeTextView);
        searchLabelTextView = (TextView) view.findViewById(R.id.SearchLabel);
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

        TextView departureCityTextView = (TextView)  view.findViewById(R.id.SearchDepartureCityTextView);
        departureCityTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), MapActivity.class);
                startActivity(intent);
            }
        });

        setupUIEvents();
        return view;
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
        TextView departureCityTextView = (TextView)  view.findViewById(R.id.SearchDepartureCityTextView);
        TextView destinationCityTextView = (TextView) view.findViewById(R.id.SearchDestinationCityTextView);

        /*CheckBox smokers = (CheckBox)  view.findViewById(R.id.SmokersCheckbox);
        CheckBox womenOnly = (CheckBox)  view.findViewById(R.id.SearchWomenOnlyCheckbox);
        CheckBox free = (CheckBox) view.findViewById(R.id.SearchFreeCheckbox);
        CheckBox petsAllowed = (CheckBox) view.findViewById(R.id.SearchPetsCheckBox);*/

        CarShare carShare = new CarShare();

        carShare.DestinationCity = destinationCityTextView.getText().toString();
        carShare.DepartureCity = departureCityTextView.getText().toString();
        //carShare.SmokersAllowed = smokers.isChecked();
        //carShare.WomenOnly = womenOnly.isChecked();
        //carShare.PetsAllowed = petsAllowed.isChecked();
        //carShare.Free = free.isChecked();
        carShare.DateAndTimeOfDeparture = DateTimeHelper.convertToWCFDate(myCalendar.getTime());
        carShare.SearchByDate = dateTextView.getText().toString().length() != 0;
        carShare.SearchByTime = timeTextView.getText().toString().length() != 0;

        new WCFServiceTask<CarShare, ArrayList<CarShare>>("https://findndrive.no-ip.co.uk/Services/SearchService.svc/searchcarshare",
                carShare, new TypeToken<ServiceResponse<ArrayList<CarShare>>>() {}.getType(), appData.getAuthorisationHeaders(), null, this).execute();
    }


    private void expandSearchResults()
    {

        final LinearLayout parentPane   = (LinearLayout) view.findViewById(R.id.SearchParentLinearLayout);
        float ws = parentPane.getWeightSum();
        searchLabelTextView.setText("Search");
        ObjectAnimator shrink  = ObjectAnimator.ofFloat(parentPane, "weightSum", ws, view.getHeight() /searchLabelTextView.getHeight());
        shrink.setDuration(Constants.MEDIUM_ANIMATION_SPEED);
        shrink.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                parentPane.requestLayout();
            }
        });
        mode = Mode.SearchResultsExpanded;
        shrink.start();
    }

    private void restoreSearchPane()
    {
        final LinearLayout parentPane   = (LinearLayout) view.findViewById(R.id.SearchParentLinearLayout);
        float ws = parentPane.getWeightSum();

        ObjectAnimator shrink  = ObjectAnimator.ofFloat(parentPane, "weightSum", ws, 2.0f);
        shrink.setDuration(Constants.MEDIUM_ANIMATION_SPEED);
        shrink.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                parentPane.requestLayout();
            }
        });
        searchLabelTextView.setText("Minimize");
        mode = Mode.SearchPanelExpanded;
        shrink.start();
    }

    @Override
    public void onServiceCallCompleted(final ServiceResponse<ArrayList<CarShare>> carShareResults, String parameter) {
        SearchResultsAdapter adapter = new SearchResultsAdapter(getActivity(), R.layout.fragment_search_results_listview_row, carShareResults.Result);
        searchResultsListView.setAdapter(adapter);

        searchResultsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {

                new WCFServiceTask<Integer, CarShareRequest>("https://findndrive.no-ip.co.uk/Services/RequestService.svc/getrequests",
                        carShareResults.Result.get(i).CarShareId,
                        new TypeToken<ServiceResponse<ArrayList<CarShareRequest>>>() {}.getType(),
                        appData.getAuthorisationHeaders(),null, new WCFServiceCallback<ArrayList<CarShareRequest>, String>() {
                    @Override
                    public void onServiceCallCompleted(ServiceResponse<ArrayList<CarShareRequest>> serviceResponse, String parameter) {
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
