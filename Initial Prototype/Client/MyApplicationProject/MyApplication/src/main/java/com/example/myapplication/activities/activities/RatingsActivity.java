package com.example.myapplication.activities.activities;

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
import com.example.myapplication.domain_objects.Notification;
import com.example.myapplication.domain_objects.Rating;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.domain_objects.User;
import com.example.myapplication.utilities.DialogCreator;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WcfPostServiceTask;
import com.example.myapplication.notification_management.NotificationProcessor;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

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
        Bundle bundle = getIntent().getExtras();

        this.user = gson.fromJson(bundle.getString(IntentConstants.USER), new TypeToken<User>(){}.getType());
        this.ratings = new ArrayList<Rating>();

        if(bundle != null)
        {
            Notification notification =  gson.fromJson(bundle.getString(IntentConstants.NOTIFICATION),  new TypeToken<Notification>() {}.getType());

            if(notification != null)
            {
                new NotificationProcessor().MarkDelivered(this, this.appManager, notification, new WCFServiceCallback<Boolean, Void>() {
                    @Override
                    public void onServiceCallCompleted(ServiceResponse<Boolean> serviceResponse, Void parameter) {
                        Log.i(this.getClass().getSimpleName(), "Notification successfully marked as delivered");
                    }
                });
            }
        }

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
                this.appManager.getAuthorisationHeaders(), this).execute();
    }

    @Override
    public void onServiceCallCompleted(final ServiceResponse<ArrayList<Rating>> serviceResponse, Void parameter) {

        this.progressBar.setVisibility(View.GONE);
        if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
        {
            Log.i(TAG, "retrieved " + serviceResponse.Result.size() + " ratings");

            this.noRatingsTextView.setVisibility(serviceResponse.Result.size() == 0 ? View.VISIBLE : View.GONE);
            this.noRatingsTextView.setText(serviceResponse.Result.size() == 0 ? this.user.getUserName() + " has no ratings." : "");
            this.ratingsAdapter = new RatingsAdapter(this, R.layout.listview_row_rating, serviceResponse.Result, this.appManager);
            this.ratingsListView.setAdapter(ratingsAdapter);

            this.ratingsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    showPersonDialog(serviceResponse.Result.get(i).getFromUser());
                }
            });
        }
    }

    private void showPersonDialog(User user)
    {
        DialogCreator.ShowProfileOptionsDialog(this, user);
    }
}
