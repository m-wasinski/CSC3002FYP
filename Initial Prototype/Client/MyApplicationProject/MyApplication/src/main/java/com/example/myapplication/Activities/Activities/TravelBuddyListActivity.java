package com.example.myapplication.Activities.Activities;

import android.os.Bundle;
import android.widget.ListView;

import com.example.myapplication.Activities.Base.BaseActivity;
import com.example.myapplication.Adapters.TravelBuddiesAdapter;
import com.example.myapplication.DomainObjects.ServiceResponse;
import com.example.myapplication.DomainObjects.User;
import com.example.myapplication.Interfaces.WCFServiceCallback;
import com.example.myapplication.NetworkTasks.WCFServiceTask;
import com.example.myapplication.R;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

/**
 * Created by Michal on 04/01/14.
 */
public class TravelBuddyListActivity extends BaseActivity implements WCFServiceCallback<ArrayList<User>, String> {

    private ListView travelBuddiesListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.travel_buddy_list_activity);

        new WCFServiceTask<Integer, ArrayList<User>>("https://findndrive.no-ip.co.uk/Services/UserService.svc/gettravelbuddies", appData.getUser().UserId,
                new TypeToken<ServiceResponse<ArrayList<User>>>() {}.getType(), appData.getAuthorisationHeaders(), null, this).execute();

        travelBuddiesListView = (ListView) findViewById(R.id.TravelBuddyListActivityListView);
    }

    @Override
    public void onServiceCallCompleted(ServiceResponse<ArrayList<User>> serviceResponse, String parameter) {
        super.checkIfAuthorised(serviceResponse.ServiceResponseCode);
        TravelBuddiesAdapter travelBuddiesAdapter = new TravelBuddiesAdapter(this,  R.layout.travel_buddy_list_row, serviceResponse.Result);
        travelBuddiesListView.setAdapter(travelBuddiesAdapter);
    }
}
