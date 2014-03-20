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
import com.example.myapplication.domain_objects.Notification;
import com.example.myapplication.domain_objects.Rating;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.domain_objects.User;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WcfPostServiceTask;
import com.example.myapplication.notification_management.NotificationProcessor;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

/**
 * Displays a list of all ratings that have been left for the designated user.
 *
 */
public class RatingsActivity extends BaseActivity implements WCFServiceCallback<ArrayList<Rating>, Void> {

    private User user;

    private ListView ratingsListView;

    private ProgressBar progressBar;

    private TextView noRatingsTextView;

    private String TAG = "Ratings Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ratings);

        // Initialise local variables.
        Bundle bundle = getIntent().getExtras();

        user = gson.fromJson(bundle.getString(IntentConstants.USER), new TypeToken<User>(){}.getType());

        Notification notification =  gson.fromJson(bundle.getString(IntentConstants.NOTIFICATION),  new TypeToken<Notification>() {}.getType());

        if(notification != null)
        {
            new NotificationProcessor().MarkDelivered(this, appManager, notification, new WCFServiceCallback<Void, Void>() {
                @Override
                public void onServiceCallCompleted(ServiceResponse<Void> serviceResponse, Void parameter) {
                    Log.i(TAG, "Notification successfully marked as delivered");
                }
            });
        }
        // Initialise UI elements.
        ratingsListView = (ListView) findViewById(R.id.RatingsActivityListView);
        noRatingsTextView = (TextView) findViewById(R.id.RatingsActivityNoRatingsTextView);
        actionBar.setTitle(user.getUserName() + "'s ratings");
        progressBar = (ProgressBar) findViewById(R.id.RatingsActivityProgressBar);
    }

    @Override
    protected void onResume() {
        super.onResume();
        retrieveRatings();
    }

    private void retrieveRatings()
    {
        progressBar.setVisibility(View.VISIBLE);
        new WcfPostServiceTask<Integer>(this, getResources().getString(R.string.GetUserRatingsURL), user.getUserId(), new TypeToken<ServiceResponse<ArrayList<Rating>>>(){}.getType(),
                appManager.getAuthorisationHeaders(), this).execute();
    }

    @Override
    public void onServiceCallCompleted(final ServiceResponse<ArrayList<Rating>> serviceResponse, Void parameter) {

        progressBar.setVisibility(View.GONE);
        if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
        {
            Log.i(TAG, "retrieved " + serviceResponse.Result.size() + " ratings");

            noRatingsTextView.setVisibility(serviceResponse.Result.size() == 0 ? View.VISIBLE : View.GONE);
            noRatingsTextView.setText(serviceResponse.Result.size() == 0 ? user.getUserName() + " has no ratings." : "");
            RatingsAdapter ratingsAdapter = new RatingsAdapter(this, R.layout.listview_row_rating, serviceResponse.Result, appManager);
            ratingsListView.setAdapter(ratingsAdapter);

            ratingsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    showPersonDialog(serviceResponse.Result.get(i).getFromUser());
                }
            });
        }
    }

    private void showPersonDialog(User user)
    {
        Bundle bundle = new Bundle();
        bundle.putInt(IntentConstants.PROFILE_VIEWER_MODE, IntentConstants.PROFILE_VIEWER_VIEWING);
        bundle.putInt(IntentConstants.USER, user.getUserId());
        startActivity(new Intent(this, ProfileViewerActivity.class).putExtras(bundle));
    }
}
