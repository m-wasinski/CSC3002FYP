package com.example.myapplication.activities.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.example.myapplication.R;
import com.example.myapplication.activities.base.BaseActivity;
import com.example.myapplication.adapters.LeaderboardAdapter;
import com.example.myapplication.constants.IntentConstants;
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.constants.WcfConstants;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.domain_objects.User;
import com.example.myapplication.dtos.LoadRangeDTO;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WcfPostServiceTask;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

/**
 * Displays the system-wide leaderboard consisting of all users currently registered in the system.
 * Leaderboard is sorted in descending order putting users with highest scores on top.
 **/
public class LeaderboardActivity extends BaseActivity implements WCFServiceCallback<ArrayList<User>, Void>,
        AbsListView.OnScrollListener, AdapterView.OnItemClickListener{

    private ListView leaderboardListView;

    private LeaderboardAdapter leaderboardAdapter;

    private ArrayList<User> leaderboard;

    private ProgressBar progressBar;

    private int currentScrollIndex;

    private int currentScrollTop;

    private boolean requestMoreData;

    private int previousTotalListViewItemCount;

    private final String TAG = "Leaderboard Activity";

    @Override
    protected void onResume() {
        super.onResume();
        requestMoreData = leaderboardListView.getCount() == 0;
        retrieveLeaderboard();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        // Initialise local variables.
        leaderboard = new ArrayList<User>();
        leaderboardAdapter = new LeaderboardAdapter(appManager, this, R.layout.listview_row_leaderboard, leaderboard);

        // Initialise UI elements.
        leaderboardListView = (ListView) findViewById(R.id.LeaderboardActivityListView);
        leaderboardListView.setOnItemClickListener(this);
        progressBar = (ProgressBar) findViewById(R.id.LeaderboardActivityProgressBar);
        leaderboardListView.setAdapter(leaderboardAdapter);
    }

    /**
     * Calls the web service to retrieve current leaderboard.
     */
    private void retrieveLeaderboard()
    {
        progressBar.setVisibility(View.VISIBLE);
        new WcfPostServiceTask<LoadRangeDTO>(this, getResources().getString(R.string.GetLeaderboardURL),
                new LoadRangeDTO(appManager.getUser().getUserId(), requestMoreData ? leaderboardListView.getCount() : 0,
                        requestMoreData ? WcfConstants.LeaderboardPerCall : leaderboardListView.getCount()),
                new TypeToken<ServiceResponse<ArrayList<User>>>(){}.getType(),appManager.getAuthorisationHeaders(), this).execute();
    }

    /**
     * Called after the most up-to-date leaderboard has been retrieved from the server.
     *
     * @param serviceResponse - the Result property containing the actual arraylist of user objects sorted in descending order.
     * @param parameter
     */
    @Override
    public void onServiceCallCompleted(final ServiceResponse<ArrayList<User>> serviceResponse, Void parameter) {
        progressBar.setVisibility(View.GONE);
        if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
        {
            Log.i(TAG, "Successfully retrieved leaderboard consisting of: " + serviceResponse.Result.size() + " users.");

            if(!requestMoreData)
            {
                leaderboard.clear();
            }
            else
            {
                leaderboardListView.setSelectionFromTop(currentScrollIndex, currentScrollTop);
            }

            leaderboard.addAll(serviceResponse.Result);
            leaderboardAdapter.notifyDataSetInvalidated();
            requestMoreData = false;
        }
    }

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
            currentScrollIndex = leaderboardListView.getFirstVisiblePosition();
            View v = leaderboardListView.getChildAt(0);
            currentScrollTop= (v == null) ? 0 : v.getTop();

            retrieveLeaderboard();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Bundle bundle = new Bundle();
        bundle.putInt(IntentConstants.PROFILE_VIEWER_MODE, IntentConstants.PROFILE_VIEWER_VIEWING);
        bundle.putInt(IntentConstants.USER, leaderboard.get(i).getUserId());
        startActivity(new Intent(this, ProfileViewerActivity.class).putExtras(bundle));
    }
}
