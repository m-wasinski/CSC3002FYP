package com.example.myapplication.activities.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
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
import com.example.myapplication.domain_objects.User;
import com.example.myapplication.dtos.LoadRangeDTO;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.factories.DialogFactory;
import com.example.myapplication.utilities.WakeLocker;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WcfPostServiceTask;
import com.example.myapplication.R;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

/**
 * Activity which displays a list of all journeys associated with the current user,
 * both as a driver and passenger.
 **/
public class MyJourneysActivity extends BaseActivity implements WCFServiceCallback<ArrayList<Journey>, String>,
        AbsListView.OnScrollListener, AdapterView.OnItemClickListener, TextWatcher {

    private static final String TAG = "My Journeys Activity";

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

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_journeys);

        user = gson.fromJson(getIntent().getStringExtra(IntentConstants.USER), new TypeToken<User>(){}.getType());

        setTitle(user != null ? user.getUserName()+"'s journeys" : "Error, could not retrieve user.");

        // Initialise local variables.
        myJourneys = new ArrayList<Journey>();

        // Initialise UI elements and setup event handlers..
        filterEditText = (EditText) findViewById(R.id.ActivityHomeFilterEditText);
        filterEditText.addTextChangedListener(this);

        //
        myJourneysListView = (ListView) findViewById(R.id.MyCarSharesListView);
        myJourneysListView.setOnScrollListener(this);

        journeyAdapter = new JourneyAdapter(this, R.layout.listview_row_my_journey, myJourneys, user);
        journeyAdapter.getFilter().filter(filterEditText.getText().toString());

        myJourneysListView.setAdapter(journeyAdapter);

        progressBar = (ProgressBar) findViewById(R.id.ActivityMyJourneysProgressBar);

        noJourneysTextView = (TextView) findViewById(R.id.MyCarSharesNoJourneysTextView);
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadcastTypes.BROADCAST_ACTION_REFRESH);
        registerReceiver(RefreshBroadcastReceiver, intentFilter);
        requestMoreData = myJourneysListView.getCount() == 0;
        retrieveJourneys();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.help:
                DialogFactory.getHelpDialog(this, "Your journeys.", getResources().getString(R.string.MyJourneysHelp));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(RefreshBroadcastReceiver);
    }

    /**
     * Called when list of journeys is retrieved from the server.
     **/
    @Override
    public void onServiceCallCompleted(final ServiceResponse<ArrayList<Journey>> serviceResponse, String s) {

        progressBar.setVisibility(View.GONE);

        if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
        {
            Log.i(TAG, "Retrieved " + serviceResponse.Result.size() + " journeys.");

            if(serviceResponse.Result.size() == 0 && myJourneysListView.getCount() == 0)
            {
                noJourneysTextView.setVisibility(View.VISIBLE);
                myJourneysListView.setVisibility(View.GONE);
            }
            else
            {
                if(!requestMoreData)
                {
                    myJourneys.clear();
                }
                else
                {
                    myJourneysListView.setSelectionFromTop(currentScrollIndex, currentScrollTop);
                }

                myJourneys.addAll(serviceResponse.Result);
                journeyAdapter.notifyDataSetChanged();
                noJourneysTextView.setVisibility(View.GONE);
                myJourneysListView.setVisibility(View.VISIBLE);
                filterEditText.setEnabled(true);
                myJourneysListView.setOnItemClickListener(this);
                requestMoreData = false;
            }
        }
    }

    /**
     * Makes a call to the web service to retrieve a list of journeys associated with the current user.
     **/
    private void retrieveJourneys()
    {
        progressBar.setVisibility(View.VISIBLE);
        new WcfPostServiceTask<LoadRangeDTO>(this, getResources().getString(R.string.GetAllJourneysURL),
                new LoadRangeDTO(user.getUserId(),
                        requestMoreData ? myJourneysListView.getCount() : 0, requestMoreData ? WcfConstants.JourneysPerCall : myJourneysListView.getCount()),
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

        if (previousTotalListViewItemCount == totalItemCount)
        {
            return;
        }

        if(firstVisibleItem + visibleItemCount >= totalItemCount)
        {
            previousTotalListViewItemCount = totalItemCount;
            requestMoreData = true;
            currentScrollIndex = myJourneysListView.getFirstVisiblePosition();
            View v = myJourneysListView.getChildAt(0);
            currentScrollTop= (v == null) ? 0 : v.getTop();

            retrieveJourneys();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
    {
        Bundle extras = new Bundle();
        extras.putInt(IntentConstants.NEW_JOURNEY_MESSAGES,Integer.parseInt(((TextView)view.findViewById(R.id.MyCarSharesNumberOfUnreadMessagesTextView)).getText().toString()));
        extras.putInt(IntentConstants.NEW_JOURNEY_REQUESTS,Integer.parseInt(((TextView)view.findViewById(R.id.MyCarSharesNumberOfUnreadRequestsTextView)).getText().toString()));
        extras.putString(IntentConstants.JOURNEY, gson.toJson(myJourneys.get(i)));
        startActivity(user.getUserId() == appManager.getUser().getUserId() ? new Intent(this, JourneyDetailsActivity.class).putExtras(extras) :
                new Intent(this, SearchResultDetailsActivity.class).putExtras(extras));
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        journeyAdapter.getFilter().filter(charSequence.toString());
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }
}