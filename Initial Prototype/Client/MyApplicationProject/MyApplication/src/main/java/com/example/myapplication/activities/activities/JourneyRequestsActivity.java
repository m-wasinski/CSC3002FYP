package com.example.myapplication.activities.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.myapplication.adapters.JourneyRequestAdapter;
import com.example.myapplication.activities.base.BaseActivity;
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.dtos.Journey;
import com.example.myapplication.dtos.JourneyRequest;
import com.example.myapplication.dtos.ServiceResponse;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WCFServiceTask;
import com.example.myapplication.R;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

/**
 * Created by Michal on 02/01/14.
 */
public class JourneyRequestsActivity extends BaseActivity implements WCFServiceCallback<ArrayList<JourneyRequest>, String> {

    private ListView requestsListView;
    private Journey journey;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        journey = gson.fromJson(getIntent().getExtras().getString("CurrentCarShare"), new TypeToken<Journey>() {}.getType());
        setContentView(R.layout.activity_journey_requests);
        requestsListView = (ListView) findViewById(R.id.CarShareRequestsListView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        new WCFServiceTask<Integer>(this, getResources().getString(R.string.GetRequestsForJourneyURL),
                this.journey.JourneyId,
                new TypeToken<ServiceResponse<ArrayList<JourneyRequest>>>() {}.getType(),
                findNDriveManager.getAuthorisationHeaders(), this).execute();
    }

    private void markRequestAsRead(int id)
    {
        new WCFServiceTask<Integer>(this, getResources().getString(R.string.MarkRequestAsReadURL),
                id,new TypeToken<ServiceResponse<JourneyRequest>>() {}.getType(), findNDriveManager.getAuthorisationHeaders(), new WCFServiceCallback() {
            @Override
            public void onServiceCallCompleted(ServiceResponse serviceResponse, Object parameter) {
                if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
                {
                    Intent intent = new Intent(getBaseContext(), JourneyRequestDetailsActivity.class);
                    intent.putExtra("CurrentCarShareRequest", gson.toJson(serviceResponse.Result));
                    startActivity(intent);
                }
            }
        }).execute();
    }

    @Override
    public void onServiceCallCompleted(final ServiceResponse<ArrayList<JourneyRequest>> serviceResponse, String parameter) {
        if(serviceResponse.Result.size() > 0)
        {
            JourneyRequestAdapter adapter = new JourneyRequestAdapter(this, R.layout.journey_request_listview_row, serviceResponse.Result);
            requestsListView.setAdapter(adapter);
            requestsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    markRequestAsRead(serviceResponse.Result.get(i).JourneyRequestId);
                }
            });
        }
    }
}
