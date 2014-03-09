package com.example.myapplication.activities.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.activities.base.BaseActivity;
import com.example.myapplication.adapters.JourneyTemplateAdapter;
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
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

/**
 * Displays list of all journey templates created by the currently logged in user.
 **/
public class MyJourneyTemplatesActivity extends BaseActivity implements WCFServiceCallback<ArrayList<JourneyTemplate>, Void>,
        AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, View.OnClickListener, Interfaces.TemplateNameListener, TextWatcher {

    private JourneyTemplateAdapter journeyTemplateAdapter;

    private ArrayList<JourneyTemplate> journeyTemplates;

    private ProgressBar progressBar;

    private WcfPostServiceTask<JourneyTemplate> searchServiceTask;

    private int mode;

    private EditText searchEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_journey_templates);

        journeyTemplates = new ArrayList<JourneyTemplate>();

        mode = getIntent().getExtras().getInt(IntentConstants.SEARCH_MODE);

        ListView templatesListView = (ListView) findViewById(R.id.MyJourneyTemplatesActivityListView);

        journeyTemplateAdapter = new JourneyTemplateAdapter(this, R.layout.listview_row_journey_template, journeyTemplates);

        templatesListView.setAdapter(journeyTemplateAdapter);
        templatesListView.setOnItemClickListener(this);
        templatesListView.setOnItemLongClickListener(this);

        progressBar = (ProgressBar) findViewById(R.id.MyJourneyTemplatesActivityProgressBar);

        Button createTemplateButton = (Button) findViewById(R.id.MyJourneyTemplatesActivityCreateNewButton);
        createTemplateButton.setOnClickListener(this);

        searchEditText = (EditText) findViewById(R.id.MyJourneyTemplatesActivityFilterEditText);
        searchEditText.addTextChangedListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        retrieveTemplates();
    }

    /**
     * Calls the web service to retrieve the list of template for this user.
     */
    private void retrieveTemplates()
    {
        progressBar.setVisibility(View.VISIBLE);
        new WcfPostServiceTask<Integer>(this, getResources().getString(R.string.GetJourneyTemplatesURL),
                appManager.getUser().getUserId(), new TypeToken<ServiceResponse<ArrayList<JourneyTemplate>>>() {}.getType(),
                appManager.getAuthorisationHeaders(), this).execute();
    }

    /**
     * Called after this user's list of templates has been successfully retrieved from the server.
     *
     * @param serviceResponse
     * @param parameter
     */
    @Override
    public void onServiceCallCompleted(ServiceResponse<ArrayList<JourneyTemplate>> serviceResponse, Void parameter) {
        progressBar.setVisibility(View.GONE);

        if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
        {
            searchEditText.setEnabled(serviceResponse.Result.size() != 0);
            findViewById(R.id.MyJourneyTemplatesActivityNoTemplatesTextView).setVisibility(serviceResponse.Result.size() == 0 ? View.VISIBLE : View.GONE);
            journeyTemplates.clear();
            journeyTemplates.addAll(serviceResponse.Result);
            journeyTemplateAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Bundle bundle = new Bundle();
        bundle.putInt(IntentConstants.SEARCH_MODE, mode);
        bundle.putString(IntentConstants.JOURNEY_TEMPLATE, gson.toJson(journeyTemplates.get(i)));
        startActivity(new Intent(this, SearchEditorStepOneActivity.class).putExtras(bundle));
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {
        final String items[] = {"Quick search", "Delete"};
        AlertDialog.Builder ab=new AlertDialog.Builder(this);
        ab.setTitle(journeyTemplates.get(i).getAlias());

        ab.setItems(items, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface d, int choice) {
               switch (choice)
               {
                   case 0:
                       searchForJourneys(i);
                       break;
                   case 1:
                       DialogFactory.getYesNoDialog(MyJourneyTemplatesActivity.this, journeyTemplates.get(i).getAlias(),
                               "Are you sure you want to delete this template? You will not be notified anymore when a journey matching this template is offered.", new Interfaces.YesNoDialogPositiveButtonListener() {
                           @Override
                           public void positiveButtonClicked() {
                               deleteTemplate(i);
                           }
                       });
                       break;
               }
            }
        });
        ab.show();
        return true;
    }

    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            case R.id.MyJourneyTemplatesActivityCreateNewButton:
                DialogFactory.getJourneyTemplateNameDialog(this, this);
                break;
        }
    }

    @Override
    public void NameEntered(String name)
    {
        JourneyTemplate journeyTemplate = new JourneyTemplate();
        journeyTemplate.setAlias(name);

        Bundle bundle = new Bundle();
        bundle.putInt(IntentConstants.SEARCH_MODE, IntentConstants.CREATING_NEW_TEMPLATE);
        bundle.putString(IntentConstants.JOURNEY_TEMPLATE, gson.toJson(journeyTemplate));

        startActivity(new Intent(this, SearchEditorStepOneActivity.class).putExtras(bundle));
    }

    private void deleteTemplate(int index)
    {
        progressBar.setVisibility(View.VISIBLE);
        new WcfPostServiceTask<Integer>(this, getResources().getString(R.string.DeleteJourneyTemplateURL),
                journeyTemplates.get(index).getJourneyTemplateId(),
                new TypeToken<ServiceResponse<Boolean>>() {}.getType(), appManager.getAuthorisationHeaders(), new WCFServiceCallback<Boolean, Void>() {
            @Override
            public void onServiceCallCompleted(ServiceResponse<Boolean> serviceResponse, Void parameter) {
                progressBar.setVisibility(View.GONE);
                if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
                {
                    Toast.makeText(MyJourneyTemplatesActivity.this, "Template was deleted successfully.", Toast.LENGTH_LONG).show();
                    retrieveTemplates();
                }
            }
        }).execute();
    }

    private void searchForJourneys(int i)
    {
        progressBar.setVisibility(View.VISIBLE);
        searchServiceTask = ServiceTaskFactory.getJourneySearch(this, appManager.getAuthorisationHeaders(), journeyTemplates.get(i), new WCFServiceCallback<ArrayList<Journey>, Void>() {
            @Override
            public void onServiceCallCompleted(ServiceResponse<ArrayList<Journey>> serviceResponse, Void parameter) {
                progressBar.setVisibility(View.GONE);
                searchServiceTask = null;
                if (serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS) {
                    if (serviceResponse.Result.size() > 0) {
                        DialogFactory.getJourneySearchResultsDialog(MyJourneyTemplatesActivity.this, serviceResponse.Result);
                    } else {
                        Toast.makeText(MyJourneyTemplatesActivity.this, "No journeys were found.", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
        searchServiceTask.execute();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(searchServiceTask != null)
        {
            progressBar.setVisibility(View.GONE);
            searchServiceTask.cancel(true);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        journeyTemplateAdapter.getFilter().filter(charSequence.toString());
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }
}
