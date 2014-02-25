package com.example.myapplication.activities.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.myapplication.activities.base.BaseActivity;
import com.example.myapplication.adapters.JourneyAdapter;
import com.example.myapplication.constants.BroadcastTypes;
import com.example.myapplication.constants.IntentConstants;
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.constants.WcfConstants;
import com.example.myapplication.domain_objects.Journey;
import com.example.myapplication.dtos.LoadRangeDTO;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.utilities.WakeLocker;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WcfPostServiceTask;
import com.example.myapplication.R;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

/**
 * Created by Michal on 27/11/13.
 */

public class MyJourneysActivity extends BaseActivity implements WCFServiceCallback<ArrayList<Journey>, String>,
        AbsListView.OnScrollListener, AdapterView.OnItemClickListener, TextWatcher {

    private EditText filterEditText;

    private ListView myJourneysListView;

    private JourneyAdapter journeyAdapter;

    private ProgressBar progressBar;

    private ArrayList<Journey> myJourneys;

    private int currentScrollIndex;

    private int currentScrollTop;

    private TextView noJourneysTextView;

    private boolean requestMoreData;

    private int previousTotalListViewItemCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_my_journeys);

        // Initialise local variables.
        this.myJourneys = new ArrayList<Journey>();

        // Initialise UI elements.
        this.filterEditText = (EditText) this.findViewById(R.id.ActivityHomeFilterEditText);
        this.myJourneysListView = (ListView) this.findViewById(R.id.MyCarSharesListView);
        this.myJourneysListView.setScrollingCacheEnabled(false);
        this.journeyAdapter = new JourneyAdapter(this, R.layout.listview_row_my_journey, myJourneys, this.appManager);
        this.journeyAdapter.getFilter().filter(filterEditText.getText().toString());
        this.myJourneysListView.setAdapter(journeyAdapter);
        this.progressBar = (ProgressBar) this.findViewById(R.id.ActivityMyJourneysProgressBar);
        this.noJourneysTextView = (TextView) this.findViewById(R.id.MyCarSharesNoJourneysTextView);

        // Setup all event handlers for UI elements.
        this.setupEventHandlers();
    }

    private void setupEventHandlers()
    {
        this.myJourneysListView.setOnScrollListener(this);
        this.filterEditText.addTextChangedListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadcastTypes.BROADCAST_ACTION_REFRESH);
        registerReceiver(RefreshBroadcastReceiver, intentFilter);
        this.requestMoreData = this.myJourneysListView.getCount() == 0;
        this.retrieveJourneys();
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(RefreshBroadcastReceiver);
    }

    /*
     * Called when list of journeys is retrieved from the server.
     */
    @Override
    public void onServiceCallCompleted(final ServiceResponse<ArrayList<Journey>> serviceResponse, String s) {

        this.progressBar.setVisibility(View.GONE);

        if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
        {
            Log.i("My Journeys Activity:", "Retrieved " + serviceResponse.Result.size() + " journeys.");

            if(serviceResponse.Result.size() == 0 && this.myJourneysListView.getCount() == 0)
            {
                this.noJourneysTextView.setVisibility(View.VISIBLE);
                this.myJourneysListView.setVisibility(View.GONE);
            }
            else
            {
                if(!this.requestMoreData)
                {
                    this.myJourneys.clear();
                }
                else
                {
                    this.myJourneysListView.setSelectionFromTop(currentScrollIndex, currentScrollTop);
                }

                this.myJourneys.addAll(serviceResponse.Result);
                this.journeyAdapter.notifyDataSetInvalidated();
                this.noJourneysTextView.setVisibility(View.GONE);
                this.myJourneysListView.setVisibility(View.VISIBLE);
                this.filterEditText.setEnabled(true);
                this.myJourneysListView.setOnItemClickListener(this);
                this.requestMoreData = false;
            }
        }
    }

    private void retrieveJourneys()
    {
        this.progressBar.setVisibility(View.VISIBLE);
        new WcfPostServiceTask<LoadRangeDTO>(this, getResources().getString(R.string.GetAllJourneysURL),
                new LoadRangeDTO(appManager.getUser().getUserId(),
                        this.requestMoreData ? myJourneysListView.getCount() : 0, this.requestMoreData ? WcfConstants.JourneysPerCall : myJourneysListView.getCount()),
                new TypeToken<ServiceResponse<ArrayList<Journey>>>() {}.getType(),
                appManager.getAuthorisationHeaders(), this).execute();
    }

    /**
     * Receiver which listens for incoming refresh requests.
     * */
    private final BroadcastReceiver RefreshBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            WakeLocker.acquire(getApplicationContext());
            requestMoreData = false;
            retrieveJourneys();
            WakeLocker.release();
        }
    };

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
    {
        if (totalItemCount == 0)
        {
            return;
        }

        if (this.previousTotalListViewItemCount == totalItemCount)
        {
            return;
        }

        if(firstVisibleItem + visibleItemCount >= totalItemCount)
        {
            this.previousTotalListViewItemCount = totalItemCount;
            this.requestMoreData = true;
            this.currentScrollIndex = this.myJourneysListView.getFirstVisiblePosition();
            View v = this.myJourneysListView.getChildAt(0);
            this.currentScrollTop= (v == null) ? 0 : v.getTop();

            this.retrieveJourneys();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
    {
        Bundle extras = new Bundle();
        extras.putInt(IntentConstants.NEW_JOURNEY_MESSAGES,Integer.parseInt(((TextView)view.findViewById(R.id.MyCarSharesNumberOfUnreadMessagesTextView)).getText().toString()));
        extras.putInt(IntentConstants.NEW_JOURNEY_REQUESTS,Integer.parseInt(((TextView)view.findViewById(R.id.MyCarSharesNumberOfUnreadRequestsTextView)).getText().toString()));
        extras.putString(IntentConstants.JOURNEY, gson.toJson(this.myJourneys.get(i)));
        this.startActivity(new Intent(this, JourneyDetailsActivity.class).putExtras(extras));
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        this.journeyAdapter.getFilter().filter(charSequence.toString());
        this.myJourneysListView.setAdapter(journeyAdapter);
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }
}