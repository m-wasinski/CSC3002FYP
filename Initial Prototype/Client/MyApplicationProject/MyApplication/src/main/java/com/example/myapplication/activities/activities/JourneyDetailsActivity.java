package com.example.myapplication.activities.activities;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.activities.base.BaseMapActivity;
import com.example.myapplication.adapters.JourneyRequestAdapter;
import com.example.myapplication.constants.IntentConstants;
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.domain_objects.Journey;
import com.example.myapplication.domain_objects.JourneyRequest;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WCFServiceTask;
import com.google.android.gms.maps.MapFragment;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

/**
 * Created by Michal on 06/02/14.
 */
public class JourneyDetailsActivity extends BaseMapActivity {

    private Journey journey;

    private Button showRequestsButton;
    private Button showPassengersButton;
    private Button enterChatButton;
    private Button makeChangeButton;
    private Button cancelJourneyButton;
    private Button summaryButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        actionBar.hide();
        setContentView(R.layout.activity_journey_details);

        // Initialise local private variables.
        this.journey = gson.fromJson(getIntent().getExtras().getString(IntentConstants.JOURNEY), new TypeToken<Journey>() {}.getType());

        this.showRequestsButton = (Button) this.findViewById(R.id.MyJourneyDetailsActivityShowRequestsButton);
        this.showPassengersButton = (Button) this.findViewById(R.id.MyJourneyDetailsActivityShowPassengersButton);
        this.enterChatButton = (Button) this.findViewById(R.id.MyJourneyDetailsActivityEnterChatButton);
        this.makeChangeButton = (Button) this.findViewById(R.id.MyJourneyDetailsActivityMakeChangeButton);
        this.cancelJourneyButton = (Button) this.findViewById(R.id.MyJourneyDetailsActivityCancelJourneyButton);
        this.summaryButton = (Button) this.findViewById(R.id.MyJourneyDetailsActivityShowSummaryButton);

        // Setup event handlers.
        this.setupEventHandlers();

        try {
            // Loading map
            this.initialiseMap();
        } catch (Exception e) {
            e.printStackTrace();
        }

        super.drawDrivingDirectionsOnMap(googleMap, journey.GeoAddresses);
    }

    private void initialiseMap() {

        if (this.googleMap == null) {
            this.googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.FragmentJourneyDetailsMap)).getMap();

            if (this.googleMap == null) {
                Toast.makeText(this,
                        "Unable to initialise Google Maps, please check your network connection.", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    private void setupEventHandlers()
    {
        this.showRequestsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                retrieveJourneyRequests();
            }
        });

        this.showPassengersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPassengers();
            }
        });
    }

    private void retrieveJourneyRequests()
    {
        new WCFServiceTask<Integer>(this, getResources().getString(R.string.GetRequestsForJourneyURL),
                this.journey.JourneyId,
                new TypeToken<ServiceResponse<ArrayList<JourneyRequest>>>() {}.getType(),
                findNDriveManager.getAuthorisationHeaders(), new WCFServiceCallback<ArrayList<JourneyRequest>, Void>() {
            @Override
            public void onServiceCallCompleted(ServiceResponse<ArrayList<JourneyRequest>> serviceResponse, Void parameter) {
                if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
                {
                    showRequests(serviceResponse.Result);
                }
            }
        }).execute();
    }

    private void showRequests(final ArrayList<JourneyRequest> requests)
    {
        // Show the journey requests dialog.
        final Dialog requestsDialog = new Dialog(this);
        requestsDialog.setContentView(R.layout.activity_journey_requests);
        requestsDialog.setTitle("Requests");
        ListView requestsListView = (ListView) requestsDialog.findViewById(R.id.JourneyRequestsListView);

        if(requests.size() > 0)
        {
            JourneyRequestAdapter adapter = new JourneyRequestAdapter(this, R.layout.journey_request_listview_row, requests);
            requestsListView.setAdapter(adapter);
            requestsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    markRequestAsRead(requests.get(i).JourneyRequestId);
                }
            });
        }

        requestsDialog.show();
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
                    intent.putExtra(IntentConstants.JOURNEY_REQUEST, gson.toJson(serviceResponse.Result));
                    startActivity(intent);
                }
            }
        }).execute();
    }

    private void showPassengers()
    {

    }
}
