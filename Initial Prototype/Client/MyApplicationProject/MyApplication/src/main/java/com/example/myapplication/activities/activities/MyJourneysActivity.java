package com.example.myapplication.activities.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import com.example.myapplication.constants.GcmConstants;
import com.example.myapplication.constants.IntentConstants;
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.domain_objects.Journey;
import com.example.myapplication.dtos.LoadRangeDTO;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.experimental.WakeLocker;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WCFServiceTask;
import com.example.myapplication.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

/**
 * Created by Michal on 27/11/13.
 */

public class MyJourneysActivity extends BaseActivity implements WCFServiceCallback<ArrayList<Journey>, String>  {

    private EditText filterEditText;
    private ListView mainListView;
    private JourneyAdapter journeyAdapter;
    private ProgressBar progressBar;
    private Button loadMoreButton;
    private ArrayList<Journey> myJourneys;
    private int currentScrollPosition;
    private Boolean pollMoreData = false;

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myJourneys = new ArrayList<Journey>();
        setContentView(R.layout.activity_my_journeys);

        filterEditText = (EditText) findViewById(R.id.ActivityHomeFilterEditText);
        mainListView = (ListView) findViewById(R.id.MyCarSharesListView);
        journeyAdapter = new JourneyAdapter(findNDriveManager.getUser().UserId, this, R.layout.my_journeys_listview_row, myJourneys);
        journeyAdapter.getFilter().filter(filterEditText.getText().toString());
        mainListView.setAdapter(journeyAdapter);
        loadMoreButton = (Button) findViewById(R.id.ActivityMyJourneysLoadMoreButton);

        loadMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentScrollPosition = mainListView.getFirstVisiblePosition();
                pollMoreData = true;
                retrieveJourneys();
            }
        });
        mainListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int lastItem = firstVisibleItem + visibleItemCount;
                if(lastItem == totalItemCount && totalItemCount > 0) {
                    // Last item is fully visible.
                    loadMoreButton.setVisibility(View.VISIBLE);
                    mainListView.setPadding(0, 0, 0, loadMoreButton.getHeight());
                }
                else
                {
                    loadMoreButton.setVisibility(View.GONE);
                    mainListView.setPadding(0, 0, 0, 0);
                }
            }
        });
        progressBar = (ProgressBar) findViewById(R.id.ActivityMyJourneysProgressBar);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(GcmConstants.BROADCAST_ACTION_REFRESH);
        registerReceiver(GCMReceiver, intentFilter);
        filterEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                     journeyAdapter.getFilter().filter(charSequence.toString());
                     mainListView.setAdapter(journeyAdapter);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        retrieveJourneys();
    }

    @Override
    public void onServiceCallCompleted(final ServiceResponse<ArrayList<Journey>> serviceResponse, String s) {
        TextView noJourneysTextView = (TextView) findViewById(R.id.MyCarSharesNoJourneysTextView);
        progressBar.setVisibility(View.GONE);
        this.loadMoreButton.setEnabled(true);
        if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
        {
            if(serviceResponse.Result.size() == 0 && mainListView.getCount() == 0)
            {
                noJourneysTextView.setVisibility(View.VISIBLE);
                mainListView.setVisibility(View.GONE);
            }
            else
            {
                if(serviceResponse.Result.size() < findNDriveManager.getItemsPerCall())
                {
                    loadMoreButton.setText("No more data to load");
                    loadMoreButton.setEnabled(false);
                }

                noJourneysTextView.setVisibility(View.GONE);
                mainListView.setVisibility(View.VISIBLE);
                filterEditText.setEnabled(true);
                if(pollMoreData)
                {
                    myJourneys.addAll(myJourneys.size() == 0 ? 0 : myJourneys.size(), serviceResponse.Result);
                    pollMoreData = false;
                }
                else
                {
                    myJourneys.clear();
                    myJourneys.addAll(serviceResponse.Result);
                }

                journeyAdapter.notifyDataSetInvalidated();

                mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Intent intent = new Intent(getApplicationContext(), JourneyDetailsActivity.class);
                        intent.putExtra(IntentConstants.JOURNEY, gson.toJson(myJourneys.get(i)));
                        startActivity(intent);
                    }
                });
                mainListView.setSelectionFromTop(currentScrollPosition, 0);
            }


        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.other_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.action_home:
                intent = new Intent(this, HomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                break;
            case R.id.logout_menu_option:
                findNDriveManager.logout(true, false);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return super.onOptionsItemSelected(item);
    }

    private void retrieveJourneys()
    {
        loadMoreButton.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        new WCFServiceTask<LoadRangeDTO>(this, getResources().getString(R.string.GetAllJourneysURL), new LoadRangeDTO(findNDriveManager.getUser().UserId, mainListView.getCount(), findNDriveManager.getItemsPerCall(), pollMoreData),
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
            onResume();
            WakeLocker.release();
        }
    };

}