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
        this.requestMoreData = this.leaderboardListView.getCount() == 0;
        this.retrieveLeaderboard();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_leaderboard);

        // Initialise local variables.
        this.leaderboard = new ArrayList<User>();
        this.leaderboardAdapter = new LeaderboardAdapter(this.appManager, this, R.layout.listview_row_leaderboard, this.leaderboard);

        // Initialise UI elements.
        this.leaderboardListView = (ListView) this.findViewById(R.id.LeaderboardActivityListView);
        this.progressBar = (ProgressBar) this.findViewById(R.id.LeaderboardActivityProgressBar);
        this.leaderboardListView.setAdapter(this.leaderboardAdapter);
    }

    private void retrieveLeaderboard()
    {
        this.progressBar.setVisibility(View.VISIBLE);
        new WcfPostServiceTask<LoadRangeDTO>(this, this.getResources().getString(R.string.GetLeaderboardURL),
                new LoadRangeDTO(this.appManager.getUser().getUserId(), this.requestMoreData ? leaderboardListView.getCount() : 0,
                        this.requestMoreData ? WcfConstants.LeaderboardPerCall : leaderboardListView.getCount()),
                new TypeToken<ServiceResponse<ArrayList<User>>>(){}.getType(),this.appManager.getAuthorisationHeaders(), this).execute();
    }

    @Override
    public void onServiceCallCompleted(final ServiceResponse<ArrayList<User>> serviceResponse, Void parameter) {
        this.progressBar.setVisibility(View.GONE);
        if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
        {
            Log.i("Leaderboard Activity", "Successfully retrieved leaderboard consisting of: " + serviceResponse.Result.size() + " users.");

            if(!this.requestMoreData)
            {
                this.leaderboard.clear();
            }
            else
            {
                this.leaderboardListView.setSelectionFromTop(currentScrollIndex, currentScrollTop);
            }

            this.leaderboard.addAll(serviceResponse.Result);
            this.leaderboardAdapter.notifyDataSetInvalidated();
            this.requestMoreData = false;
            this.leaderboardListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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

        if (this.previousTotalListViewItemCount == totalItemCount)
        {
            return;
        }

        if(firstVisibleItem + visibleItemCount >= totalItemCount)
        {
            this.previousTotalListViewItemCount = totalItemCount;
            this.requestMoreData = true;
            this.currentScrollIndex = this.leaderboardListView.getFirstVisiblePosition();
            View v = this.leaderboardListView.getChildAt(0);
            this.currentScrollTop= (v == null) ? 0 : v.getTop();

            this.retrieveLeaderboard();
        }
    }
}
