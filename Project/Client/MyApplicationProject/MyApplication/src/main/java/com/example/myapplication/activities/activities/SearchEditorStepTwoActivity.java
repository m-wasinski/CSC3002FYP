package com.example.myapplication.activities.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.activities.base.BaseActivity;
import com.example.myapplication.adapters.SearchResultsAdapter;
import com.example.myapplication.constants.IntentConstants;
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.domain_objects.Journey;
import com.example.myapplication.domain_objects.JourneyTemplate;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.factories.DialogFactory;
import com.example.myapplication.factories.ServiceTaskFactory;
import com.example.myapplication.interfaces.Interfaces;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WcfPostServiceTask;
import com.example.myapplication.utilities.CustomDateTimePicker;
import com.example.myapplication.utilities.DateTimeHelper;
import com.example.myapplication.utilities.Utilities;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * Allows the user to specify the more advanced criteria, it is be used for the following purposes:
 * - Performing a new journey search.
 * - Editing existing template.
 * - Creating a new template.
 */
public class SearchEditorStepTwoActivity extends BaseActivity implements View.OnClickListener,
        WCFServiceCallback<ArrayList<Journey>, Void>, Interfaces.TemplateNameListener, Interfaces.YesNoDialogPositiveButtonListener {

    private JourneyTemplate journeyTemplate;
    private String[] vehicleOptions;

    private TableRow flexibleDateTableRow;
    private TableRow flexibleTimeTableRow;

    private TextView dateTextView;
    private TextView timeTextView;
    private TextView smokersTextView;
    private TextView petsTextView;
    private TextView vehicleTypeTextView;
    private TextView feeTextView;

    private EditText flexibleDateEditText;
    private EditText flexibleTimeEditText;

    private Button subtractFlexibleDateButton;
    private Button subtractFlexibleTimeButton;

    private Button searchButton;

    private Button saveTemplateButton;
    private Button updateTemplateButton;

    private int mode;

    private Calendar calendar;

    private ProgressBar progressBar;

    private WcfPostServiceTask<JourneyTemplate> searchServiceTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_editor_step_two);

        Bundle bundle = getIntent().getExtras();

        calendar = DateTimeHelper.getCalendar();

        mode = bundle.getInt(IntentConstants.SEARCH_MODE);
        journeyTemplate = gson.fromJson(bundle.getString(IntentConstants.JOURNEY_TEMPLATE), new TypeToken<JourneyTemplate>(){}.getType());

        if((mode == IntentConstants.EDITING_TEMPLATE || mode == IntentConstants.SEARCH_MODE_FROM_TEMPLATE) && journeyTemplate.getDateAndTimeOfDeparture() != null)
        {
            calendar.setTime(DateTimeHelper.parseWCFDate(journeyTemplate.getDateAndTimeOfDeparture()));
        }

        TextView fromToTextView = (TextView) findViewById(R.id.SearchStepTwoActivityFromToTextView);
        fromToTextView.setText(Utilities.getJourneyHeader(journeyTemplate.getGeoAddresses()));
        searchButton = (Button) findViewById(R.id.SearchJourneysActivityStepTwoSearchButton);
        searchButton.setOnClickListener(this);
        saveTemplateButton = (Button) findViewById(R.id.SearchJourneysActivityStepTwoSaveTemplateButton);
        saveTemplateButton.setOnClickListener(this);
        updateTemplateButton = (Button) findViewById(R.id.SearchJourneysActivityStepTwoUpdateTemplateButton);
        updateTemplateButton.setOnClickListener(this);

        switch (mode)
        {
            case IntentConstants.SEARCH_MODE_FROM_TEMPLATE:
                setTitle(journeyTemplate.getAlias());
                searchButton.setVisibility(View.VISIBLE);
                saveTemplateButton.setVisibility(View.GONE);
                updateTemplateButton.setVisibility(View.GONE);
                break;
            case IntentConstants.SEARCH_MODE_NEW:
                searchButton.setText("Search for journeys");
                setTitle("New search");
                updateTemplateButton.setVisibility(View.GONE);
                saveTemplateButton.setVisibility(View.GONE);
                break;
            case IntentConstants.CREATING_NEW_TEMPLATE:
                setTitle("New template");
                searchButton.setVisibility(View.GONE);
                updateTemplateButton.setVisibility(View.GONE);
                break;
            case IntentConstants.EDITING_TEMPLATE:
                searchButton.setVisibility(View.GONE);
                saveTemplateButton.setVisibility(View.GONE);
                break;
        }

        vehicleOptions = concatenate(new String[] {"I don't mind"}, getResources().getStringArray(R.array.vehicle_types));

        flexibleDateTableRow = (TableRow) findViewById(R.id.SearchMoreOptionsFlexibleDateTableRow);
        flexibleDateTableRow.setVisibility(journeyTemplate.isSearchByDate() ? View.VISIBLE : View.GONE);

        flexibleTimeTableRow = (TableRow) findViewById(R.id.SearchMoreOptionsFlexibleTimeTableRow);
        flexibleTimeTableRow.setVisibility(journeyTemplate.isSearchByTime() ? View.VISIBLE : View.GONE);

        findViewById(R.id.SearchMoreOptionsActivityDepartureDateTableRow).setOnClickListener(this);
        findViewById(R.id.SearchMoreOptionsActivityDepartureTimeTableRow).setOnClickListener(this);
        findViewById(R.id.SearchMoreOptionsActivitySmokersTableRow).setOnClickListener(this);
        findViewById(R.id.SearchMoreOptionsActivityPetsTableRow).setOnClickListener(this);
        findViewById(R.id.SearchMoreOptionsActivityVehicleTypeTableRow).setOnClickListener(this);
        findViewById(R.id.SearchMoreOptionsActivityFeeTimeTableRow).setOnClickListener(this);

        String idontMind = "I don't mind";
        String no = "No";

        dateTextView = (TextView) findViewById(R.id.SearchMoreOptionsActivityDepartureDateTextView);
        dateTextView.setText(!journeyTemplate.isSearchByDate() ? idontMind : DateTimeHelper.getSimpleDate(journeyTemplate.getDateAndTimeOfDeparture()));

        timeTextView = (TextView) findViewById(R.id.SearchMoreOptionsActivityDepartureTimeTextView);
        timeTextView.setText(!journeyTemplate.isSearchByTime() ? idontMind : DateTimeHelper.getSimpleTime(journeyTemplate.getDateAndTimeOfDeparture()));

        smokersTextView  = (TextView) findViewById(R.id.SearchMoreOptionsActivitySmokersTextView);

        smokersTextView.setText(journeyTemplate.getSmokers() ? idontMind : no);

        petsTextView = (TextView) findViewById(R.id.SearchMoreOptionsActivityPetsTextView);
        petsTextView.setText(journeyTemplate.getPets() ? idontMind : no);

        vehicleTypeTextView = (TextView) findViewById(R.id.SearchMoreOptionsActivityVehicleTypeTextView);
        vehicleTypeTextView.setText(vehicleOptions[journeyTemplate.getVehicleType()+1]);

        Button addFlexibleDateButton = (Button) findViewById(R.id.SearchMoreOptionsPlusDaysButton);
        addFlexibleDateButton.setOnClickListener(this);

        feeTextView = (TextView) findViewById(R.id.SearchMoreOptionsActivityFeeTimeTextView);
        feeTextView.setText(new DecimalFormat("0.00").format(journeyTemplate.getFee()));

        subtractFlexibleDateButton = (Button) findViewById(R.id.SearchMoreOptionsMinusDaysButton);
        subtractFlexibleDateButton.setOnClickListener(this);
        subtractFlexibleDateButton.setEnabled(journeyTemplate.getDateAllowance() > 0);

        Button addFlexibleTimeButton = (Button) findViewById(R.id.SearchMoreOptionsPlusHoursButton);
        addFlexibleTimeButton.setOnClickListener(this);


        subtractFlexibleTimeButton = (Button) findViewById(R.id.SearchMoreOptionsMinusHoursButton);
        subtractFlexibleTimeButton.setOnClickListener(this);
        subtractFlexibleTimeButton.setEnabled(journeyTemplate.getTimeAllowance() > 0);

        flexibleDateEditText = (EditText) findViewById(R.id.SearchMoreOptionsFlexibleDateEditText);
        flexibleDateEditText.setText(String.valueOf(journeyTemplate.getDateAllowance()));

        flexibleTimeEditText = (EditText) findViewById(R.id.SearchMoreOptionsFlexibleTimeEditText);
        flexibleTimeEditText.setText(String.valueOf(journeyTemplate.getTimeAllowance()));

        progressBar = (ProgressBar) findViewById(R.id.SearchJourneysActivityStepTwoProgressBar);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(searchServiceTask != null)
        {
            searchServiceTask.cancel(true);
        }
    }

    @Override
    public void onClick(View view) {

        switch (view.getId())
        {
            case R.id.SearchMoreOptionsActivityDepartureDateTableRow:
                getDate(dateTextView);
                break;
            case R.id.SearchMoreOptionsActivityDepartureTimeTableRow:
                getTime(timeTextView);
                break;
            case R.id.SearchMoreOptionsActivitySmokersTableRow:
                showOptionsDialog(this, "Smokers", null, new OnValueSelected() {
                    @Override
                    public void valueSelected(int which, String choice) {
                        smokersTextView.setText(choice);
                        journeyTemplate.setSmokers(which == 0);
                    }
                });
                break;
            case R.id.SearchMoreOptionsActivityPetsTableRow:
                showOptionsDialog(this, "Pets", null, new OnValueSelected() {
                    @Override
                    public void valueSelected(int which, String choice) {
                        petsTextView.setText(choice);
                        journeyTemplate.setPets(which == 0);
                    }
                });
                break;
            case R.id.SearchMoreOptionsActivityVehicleTypeTableRow:
                showOptionsDialog(this, "Vehicle Type", vehicleOptions, new OnValueSelected() {
                    @Override
                    public void valueSelected(int which, String choice) {
                        vehicleTypeTextView.setText(choice);
                        journeyTemplate.setVehicleType(which - 1);
                    }
                });
                break;
            case R.id.SearchMoreOptionsActivityFeeTimeTableRow:
                showFeeSpecifyDialog(new OnFeeSelected() {
                    @Override
                    public void feeSelected(double fee) {
                        feeTextView.setText(new DecimalFormat("0.00").format(fee));
                        journeyTemplate.setFee(fee);
                    }
                });
                break;
            case R.id.SearchMoreOptionsPlusDaysButton:
                journeyTemplate.setDateAllowance(journeyTemplate.getDateAllowance()+1);
                flexibleDateEditText.setText(String.valueOf(journeyTemplate.getDateAllowance()));
                subtractFlexibleDateButton.setEnabled(journeyTemplate.getDateAllowance() > 0);
                break;
            case R.id.SearchMoreOptionsMinusDaysButton:
                journeyTemplate.setDateAllowance(journeyTemplate.getDateAllowance() > 0 ? journeyTemplate.getDateAllowance()-1 : 0);
                flexibleDateEditText.setText(String.valueOf(journeyTemplate.getDateAllowance()));
                subtractFlexibleDateButton.setEnabled(journeyTemplate.getDateAllowance() > 0);
                break;
            case R.id.SearchMoreOptionsPlusHoursButton:
                journeyTemplate.setTimeAllowance(journeyTemplate.getTimeAllowance() + 1);
                flexibleTimeEditText.setText(String.valueOf(journeyTemplate.getTimeAllowance()));
                subtractFlexibleTimeButton.setEnabled(journeyTemplate.getTimeAllowance() > 0);
                break;
            case R.id.SearchMoreOptionsMinusHoursButton:
                journeyTemplate.setTimeAllowance(journeyTemplate.getTimeAllowance() > 0 ? journeyTemplate.getTimeAllowance() - 1 : 0);
                flexibleTimeEditText.setText(String.valueOf(journeyTemplate.getTimeAllowance()));
                subtractFlexibleTimeButton.setEnabled(journeyTemplate.getTimeAllowance() > 0);
                break;
            case R.id.SearchJourneysActivityStepTwoSearchButton:
                searchForJourneys();
                break;
            case R.id.SearchJourneysActivityStepTwoSaveTemplateButton:
                createNewTemplate();
                break;
            case R.id.SearchJourneysActivityStepTwoUpdateTemplateButton:
                updateTemplate();
                break;
        }
    }

    private void updateTemplate()
    {
        progressBar.setVisibility(View.VISIBLE);
        updateTemplateButton.setEnabled(false);
        new WcfPostServiceTask<JourneyTemplate>(this, getResources().getString(R.string.UpdateJourneyTemplateURL),
                journeyTemplate, new TypeToken<ServiceResponse<Void>>() {}.getType(), appManager.getAuthorisationHeaders(), new WCFServiceCallback<Void, Void>() {
            @Override
            public void onServiceCallCompleted(ServiceResponse<Void> serviceResponse, Void parameter) {
                progressBar.setVisibility(View.GONE);
                if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
                {
                    startActivity(new Intent(getApplicationContext(), HomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                    Toast.makeText(SearchEditorStepTwoActivity.this, "Template was updated successfully.", Toast.LENGTH_LONG).show();
                }
                else
                {
                    updateTemplateButton.setEnabled(true);
                }
            }
        }).execute();
    }

    private void getTemplateName()
    {
        DialogFactory.getJourneyTemplateNameDialog(this, this);
    }


    private void getDate(final TextView textView)
    {
        new CustomDateTimePicker().showDatePickerDialog(this, calendar, new Interfaces.DateSelectedListener() {
            @Override
            public void dateSelected(Calendar c) {
                if (c != null) {
                    calendar.set(Calendar.YEAR, c.get(Calendar.YEAR));
                    calendar.set(Calendar.MONTH, c.get(Calendar.MONTH));
                    calendar.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH));

                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMMM-yyyy", Locale.UK);

                    textView.setText(simpleDateFormat.format(calendar.getTime()));
                    journeyTemplate.setDateAndTimeOfDeparture(DateTimeHelper.convertToWCFDate(calendar.getTime()));
                    journeyTemplate.setSearchByDate(true);
                    flexibleDateTableRow.setVisibility(View.VISIBLE);
                } else {
                    textView.setText("I don't mind");
                    flexibleDateTableRow.setVisibility(View.GONE);
                    journeyTemplate.setSearchByTime(false);
                    journeyTemplate.setDateAllowance(0);
                }
            }
        }, true, true);
    }

    private void getTime(final TextView textView)
    {
        new CustomDateTimePicker().showTimePickerDialog(this, calendar, new Interfaces.TimeSelectedListener() {
            @Override
            public void timeSelected(Calendar c) {
                if (c != null) {
                    calendar.set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY));
                    calendar.set(Calendar.MINUTE, c.get(Calendar.MINUTE));
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.UK);
                    textView.setText(sdf.format(calendar.getTime()));
                    journeyTemplate.setDateAndTimeOfDeparture(DateTimeHelper.convertToWCFDate(calendar.getTime()));
                    journeyTemplate.setSearchByTime(true);
                    flexibleTimeTableRow.setVisibility(View.VISIBLE);
                } else {
                    textView.setText("I don't mind");
                    journeyTemplate.setSearchByTime(false);
                    journeyTemplate.setTimeAllowance(0);
                    flexibleTimeTableRow.setVisibility(View.GONE);
                }
            }
        }, true);
    }

    private void showFeeSpecifyDialog(final OnFeeSelected onFeeSelected)
    {
        final Dialog feeDialog = new Dialog(this);
        feeDialog.setContentView(R.layout.dialog_specify_fee);
        feeDialog.setTitle("Select fee");
        final EditText feeEditText = (EditText) feeDialog.findViewById(R.id.FeeSpecifyDialogFeeEditText);
        Button okButton = (Button) feeDialog.findViewById(R.id.FeeSpecifyDialogOkButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFeeSelected.feeSelected(parseFee(feeEditText));
                feeDialog.dismiss();
            }
        });

        Button freeButton = (Button) feeDialog.findViewById(R.id.FeeSpecifyDialogFreeButton);
        freeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFeeSelected.feeSelected(0);
                feeDialog.dismiss();
            }
        });

        feeDialog.show();
    }

    private double parseFee(EditText editText)
    {
        double fee;

        try
        {
            fee = Double.parseDouble(editText.getText().toString());

        }
        catch(NumberFormatException e)
        {
            fee = 0;
        }

        return fee;
    }

    private void showOptionsDialog(final Context context, String title, final String[] vehicles, final OnValueSelected onValueSelectted)
    {
        final String[] options = {"I don't mind", "No"};
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setItems(vehicles == null ? options : vehicles, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onValueSelectted.valueSelected(which, vehicles == null ? options[which] : vehicles[which]);
            }
        });

        builder.show();
    }

    @Override
    public void NameEntered(String name) {
        journeyTemplate.setAlias(name);
        createNewTemplate();
    }

    /**
     * Called after user confirms that they wish to create a new template.
     **/
    @Override
    public void positiveButtonClicked() {
        getTemplateName();
    }

    private interface OnValueSelected
    {
        void valueSelected(int which, String choice);
    }

    private interface OnFeeSelected
    {
        void feeSelected(double fee);
    }

    /**
     * Concatenates two arrays, used for joining the 'I don't mind' option with array of vehicles stored in resources.
     *
     * @param A
     * @param B
     * @param <T>
     * @return
     */
    private <T> T[] concatenate (T[] A, T[] B) {
        int aLen = A.length;
        int bLen = B.length;

        @SuppressWarnings("unchecked")
        T[] C = (T[]) Array.newInstance(A.getClass().getComponentType(), aLen+bLen);
        System.arraycopy(A, 0, C, 0, aLen);
        System.arraycopy(B, 0, C, aLen, bLen);

        return C;
    }

    /**
     * Calls the web service to perform the search for journeys.
     * Search criteria on which the search is performed is specified in the journeyTemplate object.
     */
    private void searchForJourneys()
    {
        progressBar.setVisibility(View.VISIBLE);
        searchButton.setEnabled(false);

        // All good, call the webservice to begin the search.
        searchServiceTask = ServiceTaskFactory.getJourneySearch(this, appManager.getAuthorisationHeaders(), journeyTemplate, this);
        searchServiceTask.execute();
    }

    /**
     * Callback from the service task called once search results are retrieved from the web service.
     *
     * @param serviceResponse - contains arraylist of journeys in the Result property.
     * @param parameter
     */
    @Override
    public void onServiceCallCompleted(ServiceResponse<ArrayList<Journey>> serviceResponse, Void parameter) {
        progressBar.setVisibility(View.GONE);
        searchButton.setEnabled(true);

        if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
        {
            if(serviceResponse.Result.size() > 0)
            {
                showSearchResultsDialog(serviceResponse.Result);
            }
            else
            {
                if(mode == IntentConstants.SEARCH_MODE_NEW)
                {
                    DialogFactory.getYesNoDialog(this, "Create a template?",
                            "No journeys matching your criteria were found. Would you like to create a new template and be notified when a journey like this becomes available?", this);
                }
                else
                {
                    Toast.makeText(SearchEditorStepTwoActivity.this, "No journeys were found.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    /**
     * Displays the dialog with search results.
     **/
    private void showSearchResultsDialog(final ArrayList<Journey> searchResults)
    {
        Dialog searchResultsDialog = new Dialog(this, R.style.Theme_CustomDialog);
        searchResultsDialog.setCanceledOnTouchOutside(true);
        searchResultsDialog.setContentView(R.layout.dialog_search_results);
        ListView resultsListView = (ListView) searchResultsDialog.findViewById(R.id.SearchResultsDialogResultsListView);
        SearchResultsAdapter adapter = new SearchResultsAdapter(this, R.layout.listview_row_search_result, searchResults);
        resultsListView.setAdapter(adapter);

        resultsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                showJourneyDetails(searchResults.get(i));
            }
        });

        searchResultsDialog.show();
    }

    /**
     * Once user clicks on on the the search results, they are transferred to the SearchResultDetailsActivity.
     * @param journey
     */
    private void showJourneyDetails(Journey journey)
    {
        Bundle bundle = new Bundle();
        bundle.putString(IntentConstants.JOURNEY, gson.toJson(journey));
        startActivity(new Intent(this, SearchResultDetailsActivity.class).putExtras(bundle));
    }

    /**
     *  Call the web service to create a new template from the current journey template object.
     *  Template is saved database and then used by the web service to suggest journeys to the user.
     **/
    private void createNewTemplate()
    {
        saveTemplateButton.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        new WcfPostServiceTask<JourneyTemplate>(this, getResources().getString(R.string.CreateNewJourneyTemplateURL),
                journeyTemplate, new TypeToken<ServiceResponse<Void>>() {}.getType(), appManager.getAuthorisationHeaders(), new WCFServiceCallback<Void, Void>() {
            @Override
            public void onServiceCallCompleted(ServiceResponse<Void> serviceResponse, Void parameter) {
                progressBar.setVisibility(View.GONE);
                if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
                {
                    startActivity(new Intent(getApplicationContext(), HomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                    Toast.makeText(SearchEditorStepTwoActivity.this, "New template was created successfully.", Toast.LENGTH_LONG).show();
                }
                else
                {
                    saveTemplateButton.setEnabled(true);
                }
            }
        }).execute();
    }
}
