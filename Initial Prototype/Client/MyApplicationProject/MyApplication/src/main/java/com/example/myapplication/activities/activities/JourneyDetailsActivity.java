package com.example.myapplication.activities.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
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
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WCFServiceTask;
import com.example.myapplication.utilities.Utilities;
import com.google.android.gms.maps.MapFragment;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

/**
 * Created by Michal on 06/02/14.
 */
public class JourneyDetailsActivity extends BaseMapActivity {

    private Journey journey;

    private int newMessagesCount;
    private int newRequestsCount;

    private Button showRequestsButton;
    private Button showPassengersButton;
    private Button enterChatButton;
    private Button makeChangeButton;
    private Button cancelJourneyButton;
    private Button summaryButton;
    private Button withdrawFromJourneyButton;

    private TextView headerTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_journey_details);

        // Initialise local variables.
        Bundle bundle = getIntent().getExtras();

        this.journey = gson.fromJson(bundle.getString(IntentConstants.JOURNEY), new TypeToken<Journey>() {}.getType());
        this.newMessagesCount = bundle.getInt(IntentConstants.NEW_JOURNEY_MESSAGES);
        this.newRequestsCount = bundle.getInt(IntentConstants.NEW_JOURNEY_REQUESTS);

        this.showRequestsButton = (Button) this.findViewById(R.id.MyJourneyDetailsActivityShowRequestsButton);
        this.showRequestsButton.setEnabled(this.journey.Driver.UserId == this.findNDriveManager.getUser().UserId);
        this.showRequestsButton.setVisibility(this.journey.Driver.UserId == this.findNDriveManager.getUser().UserId ? View.VISIBLE : View.GONE);

        this.showPassengersButton = (Button) this.findViewById(R.id.MyJourneyDetailsActivityShowPassengersButton);
        this.enterChatButton = (Button) this.findViewById(R.id.MyJourneyDetailsActivityEnterChatButton);

        this.makeChangeButton = (Button) this.findViewById(R.id.MyJourneyDetailsActivityMakeChangeButton);
        this.makeChangeButton.setEnabled(this.journey.Driver.UserId == this.findNDriveManager.getUser().UserId);
        this.makeChangeButton.setVisibility(this.journey.Driver.UserId == this.findNDriveManager.getUser().UserId ? View.VISIBLE : View.GONE);

        this.cancelJourneyButton = (Button) this.findViewById(R.id.MyJourneyDetailsActivityCancelJourneyButton);
        this.cancelJourneyButton.setEnabled(this.journey.Driver.UserId == this.findNDriveManager.getUser().UserId);
        this.cancelJourneyButton.setVisibility(this.journey.Driver.UserId == this.findNDriveManager.getUser().UserId ? View.VISIBLE : View.GONE);

        this.withdrawFromJourneyButton = (Button) this.findViewById(R.id.MyJourneyDetailsActivityWithdrawFromJourneyButton);
        this.withdrawFromJourneyButton.setEnabled(this.journey.Driver.UserId != this.findNDriveManager.getUser().UserId);
        this.withdrawFromJourneyButton.setVisibility(this.journey.Driver.UserId != this.findNDriveManager.getUser().UserId ? View.VISIBLE : View.GONE);

        this.summaryButton = (Button) this.findViewById(R.id.MyJourneyDetailsActivityShowSummaryButton);
        this.headerTextView = (TextView) this.findViewById(R.id.JourneyDetailsActivityHeaderTextView);
        this.headerTextView.setText(Utilities.getJourneyHeader(this.journey.GeoAddresses));

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

        // Are there any new messages/requests? if so, make sure we mark them.
        this.showRequestsButton.setCompoundDrawablesWithIntrinsicBounds(null,
                this.newRequestsCount == 0 ? getResources().getDrawable(R.drawable.home_activity_notification) :
                        getResources().getDrawable(R.drawable.home_activity_notification_new), null, null);

        this.enterChatButton.setCompoundDrawablesWithIntrinsicBounds(null,
                this.newMessagesCount == 0 ? getResources().getDrawable(R.drawable.journey_chat) :
                        getResources().getDrawable(R.drawable.journey_chat_new_message), null, null);
    }

    private void retrieveJourney()
    {
        new WCFServiceTask<Integer>(this, getResources().getString(R.string.GetSingleJourneyURL), this.journey.getJourneyId(),
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Error while retrieving current journey. Please try again later.")
                .setCancelable(false)
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        finish();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
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

        this.enterChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enterChatRoom();
            }
        });

        this.makeChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putInt(IntentConstants.JOURNEY_CREATOR_MODE, IntentConstants.JOURNEY_CREATOR_MODE_EDITING);
                bundle.putString(IntentConstants.JOURNEY ,gson.toJson(journey));

                Intent intent = new Intent(getApplicationContext(), OfferJourneyStepOneActivity.class);
                intent.putExtras(bundle);

                startActivity(intent);
            }
        });

        this.cancelJourneyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    private void enterChatRoom() {
        this.startActivity(new Intent(this, JourneyChatActivity.class).putExtra(IntentConstants.JOURNEY, this.journey.getJourneyId()));
        this.newMessagesCount = 0;
    }


    private void retrieveJourneyRequests()
    {
        new WCFServiceTask<Integer>(this, getResources().getString(R.string.GetRequestsForJourneyURL),
                this.journey.getJourneyId(),
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
            JourneyRequestAdapter adapter = new JourneyRequestAdapter(this, R.layout.listview_row_journey_request, requests);
            requestsListView.setAdapter(adapter);
            requestsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    newRequestsCount = 0;
                    showRequestDialog(requests.get(i));
                    requestsDialog.dismiss();
                }
            });
        }

        requestsDialog.show();
    }

    private void showRequestDialog(JourneyRequest journeyRequest)
    {
        startActivity(new Intent(this, JourneyRequestDialogActivity.class).putExtra(IntentConstants.JOURNEY_REQUEST, gson.toJson(journeyRequest)));
    }

    private void showPassengers()
    {
        // Show the journey requests dialog.
        final Dialog passengersDialog = new Dialog(this);
        passengersDialog.setContentView(R.layout.dialog_show_passengers);
        passengersDialog.setTitle("Passengers");
        ListView passengersListView = (ListView) passengersDialog.findViewById(R.id.AlertDialogShowPassengersListView);

        final AlertDialog.Builder passengerOptionsDialogBuilder = new AlertDialog.Builder(this);

        if(this.journey.Participants.size() > 0)
        {
            PassengersAdapter adapter = new PassengersAdapter(this, R.layout.listview_row_journey_passengers, this.journey.Participants);
            passengersListView.setAdapter(adapter);
            passengersListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l)
                {
                    passengerOptionsDialogBuilder.setTitle(journey.Participants.get(i).UserName);
                    CharSequence userOptions[] = new CharSequence[] {"Show profile", "Send friend request"};
                    passengerOptionsDialogBuilder.setItems(userOptions, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            if(which == 1)
                            {
                                showFriendRequestDialog(journey.Participants.get(i));
                            }
                        }
                    });
                    passengerOptionsDialogBuilder.show();
                }
            });
        }

        passengersDialog.show();
    }

    private void showFriendRequestDialog(final User user)
    {
        this.startActivity(new Intent(this, SendFriendRequestDialogActivity.class).putExtra(IntentConstants.USER, gson.toJson(user)));
    }
}
