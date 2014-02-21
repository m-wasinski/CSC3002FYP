package com.example.myapplication.activities.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.myapplication.activities.base.BaseActivity;
import com.example.myapplication.adapters.JourneyAdapter;
import com.example.myapplication.constants.BroadcastTypes;
import com.example.myapplication.constants.IntentConstants;
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.domain_objects.Journey;
import com.example.myapplication.dtos.LoadRangeDTO;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.experimental.WakeLocker;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WcfPostServiceTask;
import com.example.myapplication.R;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

/**
 * Created by Michal on 27/11/13.
 */

public class MyJourneysActivity extends BaseActivity implements WCFServiceCallback<ArrayList<Journey>, String>  {

    private EditText filterEditText;

    private ListView myJourneysListView;

    private JourneyAdapter journeyAdapter;

    private ProgressBar progressBar;

    private Button loadMoreButton;

    private ArrayList<Journey> myJourneys;

    private int currentScrollPosition;

    private Boolean pollMoreData = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_my_journeys);

        // Initialise local variables.
        this.myJourneys = new ArrayList<Journey>();

        // Initialise UI elements.
        this.filterEditText = (EditText) findViewById(R.id.ActivityHomeFilterEditText);
        this.myJourneysListView = (ListView) findViewById(R.id.MyCarSharesListView);
        this.journeyAdapter = new JourneyAdapter(this, R.layout.listview_row_my_journey, myJourneys, this.findNDriveManager);
        this.journeyAdapter.getFilter().filter(filterEditText.getText().toString());
        this.myJourneysListView.setAdapter(journeyAdapter);
        this.loadMoreButton = (Button) findViewById(R.id.ActivityMyJourneysLoadMoreButton);
        this.progressBar = (ProgressBar) findViewById(R.id.ActivityMyJourneysProgressBar);

        // Setup all event handlers for UI elements.
        this.setupEventHandlers();
    }

    private void setupEventHandlers()
    {
        this.loadMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentScrollPosition = myJourneysListView.getFirstVisiblePosition();
                pollMoreData = true;
                retrieveJourneys();
            }
        });

        this.myJourneysListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int lastItem = firstVisibleItem + visibleItemCount;
                if (lastItem == totalItemCount && totalItemCount > 0 && myJourneysListView.getCount() >= findNDriveManager.getItemsPerCall()) {
                    // Last item is fully visible.
                    loadMoreButton.setVisibility(View.VISIBLE);
                    myJourneysListView.setPadding(0, 0, 0, loadMoreButton.getHeight());
                } else {
                    loadMoreButton.setVisibility(View.GONE);
                    myJourneysListView.setPadding(0, 0, 0, 0);
                }
            }
        });

        this.filterEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                journeyAdapter.getFilter().filter(charSequence.toString());
                myJourneysListView.setAdapter(journeyAdapter);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadcastTypes.BROADCAST_ACTION_REFRESH);
        registerReceiver(GCMReceiver, intentFilter);
        this.retrieveJourneys();
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(GCMReceiver);
    }

    @Override
    public void onServiceCallCompleted(final ServiceResponse<ArrayList<Journey>> serviceResponse, String s) {
        TextView noJourneysTextView = (TextView) this.findViewById(R.id.MyCarSharesNoJourneysTextView);

        this.progressBar.setVisibility(View.GONE);
        this.loadMoreButton.setEnabled(true);

        if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
        {
            if(serviceResponse.Result.size() == 0 && myJourneysListView.getCount() == 0)
            {
                noJourneysTextView.setVisibility(View.VISIBLE);
                this.myJourneysListView.setVisibility(View.GONE);
            }
            else
            {
                if(serviceResponse.Result.size() < findNDriveManager.getItemsPerCall())
                {
                    this.loadMoreButton.setText("No more data to load");
                    this.loadMoreButton.setEnabled(false);
                }

                noJourneysTextView.setVisibility(View.GONE);

                this.myJourneysListView.setVisibility(View.VISIBLE);
                this.filterEditText.setEnabled(true);

                if(this.pollMoreData)
                {
                    this.myJourneys.addAll(myJourneys.size() == 0 ? 0 : myJourneys.size(), serviceResponse.Result);
                    this.pollMoreData = false;
                }
                else
                {
                    this.myJourneys.clear();
                    this.myJourneys.addAll(serviceResponse.Result);
                }

                this.journeyAdapter.notifyDataSetInvalidated();

                this.myJourneysListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                        Bundle extras = new Bundle();
                        extras.putInt(IntentConstants.NEW_JOURNEY_MESSAGES,Integer.parseInt(((TextView)view.findViewById(R.id.MyCarSharesNumberOfUnreadMessagesTextView)).getText().toString()));
                        extras.putInt(IntentConstants.NEW_JOURNEY_REQUESTS,Integer.parseInt(((TextView)view.findViewById(R.id.MyCarSharesNumberOfUnreadRequestsTextView)).getText().toString()));
                        extras.putString(IntentConstants.JOURNEY, gson.toJson(myJourneys.get(i)));
                        Intent intent = new Intent(getApplicationContext(), JourneyDetailsActivity.class);
                        intent.putExtras(extras);
                        startActivity(intent);

                    }
                });
                this.myJourneysListView.setSelectionFromTop(currentScrollPosition, 0);
            }


        }
    }

    private void retrieveJourneys()
    {
        this.loadMoreButton.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        new WcfPostServiceTask<LoadRangeDTO>(this, getResources().getString(R.string.GetAllJourneysURL), new LoadRangeDTO(findNDriveManager.getUser().getUserId(), myJourneysListView.getCount(), findNDriveManager.getItemsPerCall(), pollMoreData),
                new TypeToken<ServiceResponse<ArrayList<Journey>>>() {}.getType(),
                findNDriveManager.getAuthorisationHeaders(), this).execute();
    }

    /**
     * Receiving push messages
     * */
    private final BroadcastReceiver GCMReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            WakeLocker.acquire(getApplicationContext());
            retrieveJourneys();
            WakeLocker.release();
        }
    };

}