package com.example.myapplication.Activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.example.myapplication.Adapters.TravelBuddiesAdapter;
import com.example.myapplication.DomainObjects.ServiceResponse;
import com.example.myapplication.DomainObjects.User;
import com.example.myapplication.Experimental.AppData;
import com.example.myapplication.Interfaces.TravelBuddiesRetrievedInterface;
import com.example.myapplication.NetworkTasks.GetTravelBuddiesTask;
import com.example.myapplication.R;

import java.util.ArrayList;

/**
 * Created by Michal on 04/01/14.
 */
public class TravelBuddyListActivity extends Activity implements TravelBuddiesRetrievedInterface {

    private AppData appData;
    private ListView travelBuddiesListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.travel_buddy_list_activity);
        appData = ((AppData)getApplication());
        GetTravelBuddiesTask getTravelBuddiesTask = new GetTravelBuddiesTask(appData.getUser().UserId, this);
        getTravelBuddiesTask.execute();
        travelBuddiesListView = (ListView) findViewById(R.id.TravelBuddyListActivityListView);
    }

    @Override
    public void travelBuddiesRetrieved(ServiceResponse<ArrayList<User>> travelBuddies) {
        TravelBuddiesAdapter travelBuddiesAdapter = new TravelBuddiesAdapter(this,  R.layout.travel_buddy_list_row, travelBuddies.Result);
        travelBuddiesListView.setAdapter(travelBuddiesAdapter);
    }
}
