package com.example.myapplication.activities.activities;

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
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.constants.WcfConstants;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.domain_objects.User;
import com.example.myapplication.dtos.LoadRangeDTO;
import com.example.myapplication.utilities.DialogCreator;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WcfPostServiceTask;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

/**
 * Created by Michal on 23/02/14.
 */
public class LeaderboardActivity extends BaseActivity implements WCFServiceCallback<ArrayList<User>, Void>, AbsListView.OnScrollListener{

    private ListView leaderboardListView;

    private LeaderboardAdapter leaderboardAdapter;

    private ArrayList<User> leaderboard;

    private ProgressBar progressBar;

    private int currentScrollIndex;

    private int currentScrollTop;

    private boolean requestMoreData;

    private int previousTotalListViewItemCount;

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
        progressBar = (ProgressBar) findViewById(R.id.LeaderboardActivityProgressBar);
        leaderboardListView.setAdapter(leaderboardAdapter);
    }

    private void retrieveLeaderboard()
    {
        progressBar.setVisibility(View.VISIBLE);
        new WcfPostServiceTask<LoadRangeDTO>(this, getResources().getString(R.string.GetLeaderboardURL),
                new LoadRangeDTO(appManager.getUser().getUserId(), requestMoreData ? leaderboardListView.getCount() : 0,
                        requestMoreData ? WcfConstants.LeaderboardPerCall : leaderboardListView.getCount()),
                new TypeToken<ServiceResponse<ArrayList<User>>>(){}.getType(),appManager.getAuthorisationHeaders(), this).execute();
    }

    @Override
    public void onServiceCallCompleted(final ServiceResponse<ArrayList<User>> serviceResponse, Void parameter) {
        progressBar.setVisibility(View.GONE);
        if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
        {
            Log.i("Leaderboard Activity", "Successfully retrieved leaderboard consisting of: " + serviceResponse.Result.size() + " users.");

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
            leaderboardListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    showPersonMenu(serviceResponse.Result.get(i));
                }
            });
        }
    }

    private void showPersonMenu(User user)
    {
        DialogCreator.ShowProfileOptionsDialog(this, user);
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
}
