package com.example.myapplication.activities.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.activities.base.BaseMapActivity;
import com.example.myapplication.adapters.JourneyRequestAdapter;
import com.example.myapplication.adapters.PassengersAdapter;
import com.example.myapplication.constants.IntentConstants;
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.domain_objects.Journey;
import com.example.myapplication.domain_objects.JourneyRequest;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.domain_objects.User;
import com.example.myapplication.dtos.LoadRangeDTO;
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.retrieveJourney();
    }

    private void retrieveJourney()
    {
        new WCFServiceTask<Integer>(this, getResources().getString(R.string.GetSingleJourneyURL), this.journey.JourneyId,
                new TypeToken<ServiceResponse<Journey>>() {}.getType(),
                findNDriveManager.getAuthorisationHeaders(), new WCFServiceCallback<Journey, Void>() {
            @Override
            public void onServiceCallCompleted(ServiceResponse<Journey> serviceResponse, Void parameter) {
                if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
                {
                    if(serviceResponse.Result != null)
                    {
                        journeyRetrieved(serviceResponse.Result);
                    }
                    else
                    {
                        errorCouldNotRetrieveJourney();
                    }
                }
            }
        }).execute();
    }

    private void errorCouldNotRetrieveJourney()
    {

    }

    private void journeyRetrieved(Journey journey)
    {
        this.journey = journey;
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
                    markRequestAsRead(requests.get(i).JourneyRequestId, requestsDialog);
                }
            });
        }

        requestsDialog.show();
    }

    private void markRequestAsRead(int id, final Dialog requestDialog)
    {
        new WCFServiceTask<Integer>(this, getResources().getString(R.string.MarkRequestAsReadURL),
                id,new TypeToken<ServiceResponse<JourneyRequest>>() {}.getType(), findNDriveManager.getAuthorisationHeaders(), new WCFServiceCallback() {
            @Override
            public void onServiceCallCompleted(ServiceResponse serviceResponse, Object parameter) {
                if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
                {
                    requestDialog.dismiss();
                    Intent intent = new Intent(getBaseContext(), JourneyRequestDetailsActivity.class);
                    intent.putExtra(IntentConstants.JOURNEY_REQUEST, gson.toJson(serviceResponse.Result));
                    startActivity(intent);
                }
            }
        }).execute();
    }

    private void showPassengers()
    {
        // Show the journey requests dialog.
        final Dialog passengersDialog = new Dialog(this);
        passengersDialog.setContentView(R.layout.alert_dialog_show_passengers);
        passengersDialog.setTitle("Passengers");
        ListView passengersListView = (ListView) passengersDialog.findViewById(R.id.AlertDialogShowPassengersListView);

        final AlertDialog.Builder passengerOptionsDialogBuilder = new AlertDialog.Builder(this);

        if(this.journey.Participants.size() > 0)
        {
            PassengersAdapter adapter = new PassengersAdapter(this, R.layout.alert_dialog_show_passengers_listview_row, this.journey.Participants);
            passengersListView.setAdapter(adapter);
            passengersListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
                {
                    passengerOptionsDialogBuilder.setTitle(journey.Participants.get(i).UserName);
                    CharSequence userOptions[] = new CharSequence[] {"Show profile", "Send friend request"};
                    passengerOptionsDialogBuilder.setItems(userOptions, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {

                        }
                    });
                    passengerOptionsDialogBuilder.show();
                }
            });
        }

        passengersDialog.show();
    }
}
