package com.example.myapplication.activities.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.myapplication.R;
import com.example.myapplication.activities.base.BaseActivity;
import com.example.myapplication.adapters.RatingsAdapter;
import com.example.myapplication.constants.IntentConstants;
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.domain_objects.Rating;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.domain_objects.User;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WcfPostServiceTask;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Michal on 21/02/14.
 */
public class RatingsActivity extends BaseActivity implements WCFServiceCallback<ArrayList<Rating>, Void> {

    private User user;

    private ListView ratingsListView;

    private RatingsAdapter ratingsAdapter;

    private ProgressBar progressBar;

    private TextView noRatingsTextView;

    private String TAG = "Ratings Activity";

    private ArrayList<Rating> ratings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_ratings);

        // Initialise local variables.
        this.user = gson.fromJson(getIntent().getStringExtra(IntentConstants.USER), new TypeToken<User>(){}.getType());
        this.ratings = new ArrayList<Rating>();


        // Initialise UI elements.
        this.ratingsListView = (ListView) this.findViewById(R.id.RatingsActivityListView);
        this.noRatingsTextView = (TextView) this.findViewById(R.id.RatingsActivityNoRatingsTextView);
        this.actionBar.setTitle(this.user.getUserName() + "'s ratings");
        this.progressBar = (ProgressBar) this.findViewById(R.id.RatingsActivityProgressBar);
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.retrieveRatings();
    }

    private void retrieveRatings()
    {
        this.progressBar.setVisibility(View.VISIBLE);
        new WcfPostServiceTask<Integer>(this, getResources().getString(R.string.GetUserRatingsURL), this.user.getUserId(), new TypeToken<ServiceResponse<ArrayList<Rating>>>(){}.getType(),
                this.findNDriveManager.getAuthorisationHeaders(), this).execute();
    }

    @Override
    public void onServiceCallCompleted(final ServiceResponse<ArrayList<Rating>> serviceResponse, Void parameter) {

        this.progressBar.setVisibility(View.GONE);
        if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
        {
            Log.i(TAG, "retrieved " + serviceResponse.Result.size() + " ratings");

            this.noRatingsTextView.setVisibility(serviceResponse.Result.size() == 0 ? View.VISIBLE : View.GONE);
            this.noRatingsTextView.setText(serviceResponse.Result.size() == 0 ? this.user.getUserName() + " has no ratings." : "");
            this.ratingsAdapter = new RatingsAdapter(this, R.layout.listview_row_rating, serviceResponse.Result);
            this.ratingsListView.setAdapter(ratingsAdapter);

            this.ratingsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    showPersonsProfile(serviceResponse.Result.get(i).getFromUser());
                }
            });
        }
    }

    private void showPersonsProfile(User fromUser)
    {
        this.startActivity(new Intent(this, ProfileViewerActivity.class).putExtra(IntentConstants.USER, gson.toJson(fromUser)));
    }
}
