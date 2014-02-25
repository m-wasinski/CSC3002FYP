package com.example.myapplication.activities.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.example.myapplication.R;
import com.example.myapplication.activities.base.BaseActivity;
import com.example.myapplication.adapters.LeaderboardAdapter;
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.domain_objects.User;
import com.example.myapplication.utilities.DialogCreator;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WcfPostServiceTask;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

/**
 * Created by Michal on 23/02/14.
 */
public class LeaderboardActivity extends BaseActivity implements WCFServiceCallback<ArrayList<User>, Void>{

    private ListView mainListView;

    private ProgressBar progressBar;

    @Override
    protected void onResume() {
        super.onResume();
        retrieveLeaderboard();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_leaderboard);

        // Initialise UI elements.
        this.mainListView = (ListView) this.findViewById(R.id.LeaderboardActivityListView);
        this.progressBar = (ProgressBar) this.findViewById(R.id.LeaderboardActivityProgressBar);
    }

    private void retrieveLeaderboard()
    {
        this.progressBar.setVisibility(View.VISIBLE);
        new WcfPostServiceTask<Void>(this, this.getResources().getString(R.string.GetLeaderboardURL), null,
                new TypeToken<ServiceResponse<ArrayList<User>>>(){}.getType(),this.appManager.getAuthorisationHeaders(), this).execute();
    }

    @Override
    public void onServiceCallCompleted(final ServiceResponse<ArrayList<User>> serviceResponse, Void parameter) {
        this.progressBar.setVisibility(View.GONE);
        if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
        {
            LeaderboardAdapter leaderboardAdapter = new LeaderboardAdapter(this.appManager, this, R.layout.listview_row_leaderboard, serviceResponse.Result);
            this.mainListView.setAdapter(leaderboardAdapter);
            this.mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
}
