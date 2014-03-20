package com.example.myapplication.activities.activities;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.activities.base.BaseMapActivity;
import com.example.myapplication.adapters.JourneyRequestAdapter;
import com.example.myapplication.adapters.PassengersAdapter;
import com.example.myapplication.constants.IntentConstants;
import com.example.myapplication.constants.JourneyStatus;
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.domain_objects.Journey;
import com.example.myapplication.domain_objects.JourneyRequest;
import com.example.myapplication.domain_objects.Notification;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.domain_objects.User;
import com.example.myapplication.dtos.JourneyUserDTO;
import com.example.myapplication.factories.DialogFactory;
import com.example.myapplication.interfaces.Interfaces;
import com.example.myapplication.interfaces.OnDrivingDirectionsRetrrievedListener;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WcfPostServiceTask;
import com.example.myapplication.notification_management.NotificationProcessor;
import com.example.myapplication.utilities.Utilities;
import com.google.android.gms.maps.MapFragment;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

/**
 * This activity is used to display the details of the journey being passed in.
 * It also contains a control panel for the user to perform various operations on the journey such as cancelling, making changes, viewing passengers etc.
 * The options that are visible to the user depend on whether they are the passengers or the driver of this journey.
 * Passengers for example, cannot cancel the journey while drivers can or
 * Passengers can withdraw themselves from a journey while drivers cannot.
 **/
public class JourneyManagementActivity extends BaseMapActivity implements View.OnClickListener, OnDrivingDirectionsRetrrievedListener{

    private Journey journey;

    private int newMessagesCount;
    private int newRequestsCount;

    private Button showRequestsButton;
    private Button enterChatButton;
    private Button makeChangeButton;
    private Button cancelJourneyButton;
    private ProgressBar progressBar;

    TextView statusTextView;

    private final String TAG = "Journey Management Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journey_management);

        // Extract data from the bundle.
        Bundle extras = getIntent().getExtras();

        // Check to see if there is a pending notification to be marked as delivered inside the bundle.
        Notification notification = gson.fromJson(extras.getString(IntentConstants.NOTIFICATION), new TypeToken<Notification>() {}.getType());

        // If there is, go ahead and mark it as delivered.
        if(notification != null)
        {
            new NotificationProcessor().MarkDelivered(this, appManager, notification, new WCFServiceCallback<Void, Void>() {
                @Override
                public void onServiceCallCompleted(ServiceResponse<Void> serviceResponse, Void parameter) {
                    Log.i(TAG, "Notification successfully marked as delivered");
                }
            });
        }

        journey = gson.fromJson(extras.getString(IntentConstants.JOURNEY), new TypeToken<Journey>() {}.getType());

        newMessagesCount = extras.getInt(IntentConstants.NEW_JOURNEY_MESSAGES);
        newRequestsCount = extras.getInt(IntentConstants.NEW_JOURNEY_REQUESTS);

        // Initialise all UI elements and setup the event handlers.

        // The journey requests button.
        showRequestsButton = (Button) findViewById(R.id.MyJourneyDetailsActivityShowRequestsButton);
        showRequestsButton.setEnabled(journey.getDriver().getUserId() == appManager.getUser().getUserId());
        showRequestsButton.setOnClickListener(this);
        showRequestsButton.setVisibility(journey.getDriver().getUserId() == appManager.getUser().getUserId() ? View.VISIBLE : View.GONE);

        // The journey passengers button.
        Button showPassengersButton = (Button) findViewById(R.id.MyJourneyDetailsActivityShowPassengersButton);
        showPassengersButton.setOnClickListener(this);

        // The journey chat room button.
        enterChatButton = (Button) findViewById(R.id.MyJourneyDetailsActivityEnterChatButton);
        enterChatButton.setOnClickListener(this);

        // The make change to a journey button.
        makeChangeButton = (Button) findViewById(R.id.MyJourneyDetailsActivityMakeChangeButton);
        makeChangeButton.setEnabled(journey.getDriver().getUserId() == appManager.getUser().getUserId() && journey.getJourneyStatus() == JourneyStatus.OK);
        makeChangeButton.setVisibility(journey.getDriver().getUserId() == appManager.getUser().getUserId() ? View.VISIBLE : View.GONE);
        makeChangeButton.setOnClickListener(this);

        // The cancel journey button.
        cancelJourneyButton = (Button) findViewById(R.id.MyJourneyDetailsActivityCancelJourneyButton);
        cancelJourneyButton.setEnabled(journey.getDriver().getUserId() == appManager.getUser().getUserId() && journey.getJourneyStatus() == JourneyStatus.OK);
        cancelJourneyButton.setVisibility(journey.getDriver().getUserId() == appManager.getUser().getUserId() ? View.VISIBLE : View.GONE);
        cancelJourneyButton.setOnClickListener(this);

        // The withdraw from journey button.
        Button withdrawFromJourneyButton = (Button) findViewById(R.id.MyJourneyDetailsActivityWithdrawFromJourneyButton);
        withdrawFromJourneyButton.setEnabled(journey.getDriver().getUserId() != appManager.getUser().getUserId() && journey.getJourneyStatus() == JourneyStatus.OK);
        withdrawFromJourneyButton.setVisibility(journey.getDriver().getUserId() != appManager.getUser().getUserId() ? View.VISIBLE : View.GONE);
        withdrawFromJourneyButton.setOnClickListener(this);

        // The rate driver button.
        Button rateDriverButton = (Button) findViewById(R.id.MyJourneyDetailsActivityRateDriverButton);
        rateDriverButton.setEnabled(!(journey.getDriver().getUserId() == appManager.getUser().getUserId()));
        rateDriverButton.setVisibility(!(journey.getDriver().getUserId() == appManager.getUser().getUserId()) ? View.VISIBLE : View.GONE);
        rateDriverButton.setOnClickListener(this);

        // The journey summary button.
        Button summaryButton = (Button) findViewById(R.id.MyJourneyDetailsActivityShowSummaryButton);
        summaryButton.setOnClickListener(this);

        progressBar = (ProgressBar) findViewById(R.id.JourneyDetailsActivityProgressBar);
        TextView headerTextView = (TextView) findViewById(R.id.JourneyDetailsActivityHeaderTextView);
        headerTextView.setText(Utilities.getJourneyHeader(journey.getGeoAddresses()));

        statusTextView = (TextView) findViewById(R.id.JourneyDetailsActivityStatusTextView);

        String statusText = "";

        switch(journey.getJourneyStatus())
        {
            case JourneyStatus.OK:
                statusText = "OK";
                break;
            case JourneyStatus.Cancelled:
                statusText = "Cancelled";
                break;
            case JourneyStatus.Expired:
                statusText = "Expired";
                break;
        }

        statusTextView.setText(statusText);

        try {
            // Loading map
            initialiseMap();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.help:
                DialogFactory.getHelpDialog(this, "Journey details.", getResources().getString(R.string.JourneyDetailsHelp));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.VISIBLE);
        retrieveJourney();

        // Are there any new messages/requests? if so, make sure we mark them.
        showRequestsButton.setCompoundDrawablesWithIntrinsicBounds(null,
                newRequestsCount == 0 ? getResources().getDrawable(R.drawable.home_activity_notification) :
                        getResources().getDrawable(R.drawable.home_activity_notification_new), null, null);

        enterChatButton.setCompoundDrawablesWithIntrinsicBounds(null,
                newMessagesCount == 0 ? getResources().getDrawable(R.drawable.journey_chat) :
                        getResources().getDrawable(R.drawable.journey_chat_new_message), null, null);
    }


    /**
     * Used to retrieve the most up-to-date information about the current journey.
     **/
    private void retrieveJourney()
    {
        new WcfPostServiceTask<Integer>(this, getResources().getString(R.string.GetSingleJourneyURL), journey.getJourneyId(),
                new TypeToken<ServiceResponse<Journey>>() {}.getType(),
                appManager.getAuthorisationHeaders(), new WCFServiceCallback<Journey, Void>() {
            @Override
            public void onServiceCallCompleted(ServiceResponse<Journey> serviceResponse, Void parameter) {
                if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
                {
                    journeyRetrieved(serviceResponse.Result);
                }
            }
        }).execute();
    }

    /**
     * Called after most-up-to-date journey object is retrieved from the web service.
     * Overwrites the global journey object with the latest version retrieved from the web service.
     * @param journey - most-up-to-date journey object.
     */
    private void journeyRetrieved(Journey journey)
    {
        this.journey = journey;
        super.drawDrivingDirectionsOnMap(googleMap, journey.getGeoAddresses(), this);
    }

    /**
     * Initialises Gooogle Map.
     */
    private void initialiseMap() {

        if (googleMap == null) {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.FragmentJourneyDetailsMap)).getMap();

            if (googleMap == null) {
                Toast.makeText(this,
                        "Unable to initialise Google Maps, please check your network connection.", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    /**
     * Asks the user to confirm their decision regarding leaving current journey.
     */
    private void showWithdrawQuestionDialog()
    {
        DialogFactory.getYesNoDialog(this, "Leave journey?",
                "Are you sure you want to withdraw yourself from this journey? You will be removed from the list of passengers.",
                new Interfaces.YesNoDialogPositiveButtonListener() {
            @Override
            public void positiveButtonClicked() {
                withdrawFromJourney();
            }
        });
    }

    /**
     * Displays dialog allowing driver to confirm their decision regarding cancelling this journey.
     */
    private void showCancelJourneyDialog()
    {
        DialogFactory.getYesNoDialog(this, "Cancel journey?", "Are you sure you want to cancel this journey?", new Interfaces.YesNoDialogPositiveButtonListener() {
            @Override
            public void positiveButtonClicked() {
                cancelJourney();
            }
        });
    }

    /**
     * Starts the journey chat room activity.
     */
    private void enterChatRoom() {
        Bundle bundle = new Bundle();
        bundle.putInt(IntentConstants.JOURNEY, journey.getJourneyId());
        startActivity(new Intent(this, JourneyChatActivity.class).putExtras(bundle));
        newMessagesCount = 0;
    }

    /**
     * Retrieves list of all journey requests for this journey from the web service.
     */
    private void retrieveJourneyRequests()
    {
        progressBar.setVisibility(View.VISIBLE);
        new WcfPostServiceTask<Integer>(this, getResources().getString(R.string.GetRequestsForJourneyURL),
                journey.getJourneyId(),
                new TypeToken<ServiceResponse<ArrayList<JourneyRequest>>>() {}.getType(),
                appManager.getAuthorisationHeaders(), new WCFServiceCallback<ArrayList<JourneyRequest>, Void>() {
            @Override
            public void onServiceCallCompleted(ServiceResponse<ArrayList<JourneyRequest>> serviceResponse, Void parameter) {
                progressBar.setVisibility(View.GONE);
                if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
                {
                    showRequests(serviceResponse.Result);
                }
            }
        }).execute();
    }

    /**
     * Displays the list of all the requests for this journey in a separate dialog.
     *
     * @param requests - Arraylist of Journey Requests to be displayed.
     */
    private void showRequests(final ArrayList<JourneyRequest> requests)
    {
        // Show the journey requests dialog.
        final Dialog requestsDialog = new Dialog(this);
        requestsDialog.setContentView(R.layout.dialog_show_journey_requests);
        requestsDialog.setTitle("Requests");
        ListView requestsListView = (ListView) requestsDialog.findViewById(R.id.JourneyActivityRequestsListView);
        requestsListView.setVisibility(requests.size() == 0 ? View.GONE : View.VISIBLE);
        TextView noRequestsTextView = (TextView) requestsDialog.findViewById(R.id.JourneyActivityRequestsNoRequestsTextView);
        noRequestsTextView.setVisibility(requests.size() == 0 ? View.VISIBLE : View.GONE);

        if(requests.size() > 0)
        {
            JourneyRequestAdapter adapter = new JourneyRequestAdapter(appManager, this, R.layout.listview_row_journey_request, requests);
            requestsListView.setAdapter(adapter);
            requestsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    newRequestsCount = 0;
                    requestsDialog.dismiss();
                    Bundle bundle = new Bundle();
                    bundle.putString(IntentConstants.JOURNEY_REQUEST, gson.toJson(requests.get(i)));
                    startActivity(new Intent(JourneyManagementActivity.this, JourneyRequestDialogActivity.class).putExtras(bundle));
                }
            });
        }

        requestsDialog.show();
    }

    /**
     * Responsible for displaying a dialog window with the list of passengers participating in this journey.
     *
     * @param passengers - Arraylist of Users.
     */
    private void showPassengers(final ArrayList<User> passengers)
    {
        // Show the journey requests dialog.
        final Dialog passengersDialog = new Dialog(this);
        passengersDialog.setContentView(R.layout.dialog_show_passengers);
        passengersDialog.setTitle("Passengers");
        ListView passengersListView = (ListView) passengersDialog.findViewById(R.id.AlertDialogShowPassengersListView);
        passengersListView.setVisibility(passengers.size() == 0 ? View.GONE : View.VISIBLE);
        TextView noPassengers = (TextView) passengersDialog.findViewById(R.id.AlertDialogShowPassengersNoPassengersTextView);
        noPassengers.setVisibility(passengers.size() == 0 ? View.VISIBLE : View.GONE);

        if(passengers.size() > 0)
        {
            PassengersAdapter adapter = new PassengersAdapter(appManager, this, R.layout.listview_row_journey_passengers, passengers);
            passengersListView.setAdapter(adapter);
            passengersListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l)
                {
                    Bundle bundle = new Bundle();
                    bundle.putInt(IntentConstants.PROFILE_VIEWER_MODE, IntentConstants.PROFILE_VIEWER_VIEWING);
                    bundle.putInt(IntentConstants.USER, passengers.get(i).getUserId());
                    startActivity(new Intent(JourneyManagementActivity.this, ProfileViewerActivity.class).putExtras(bundle));
                }
            });
        }

        passengersDialog.show();
    }

    /**
     * Starts a new ServiceTask whose responsibility is to remove the current user from the list of passengers.
     **/
    private void withdrawFromJourney()
    {
        progressBar.setVisibility(View.VISIBLE);
        new WcfPostServiceTask<JourneyUserDTO>(this, getResources().getString(R.string.WithdrawFromJourneyURL),
                new JourneyUserDTO(journey.getJourneyId(), appManager.getUser().getUserId()),
                new TypeToken<ServiceResponse<Void>>() {}.getType(),
                appManager.getAuthorisationHeaders(), new WCFServiceCallback<Void, Void>() {
            @Override
            public void onServiceCallCompleted(ServiceResponse<Void> serviceResponse, Void parameter) {
                progressBar.setVisibility(View.GONE);
                if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
                {
                    Toast.makeText(JourneyManagementActivity.this, "You have been successfully withdrawn from this journey.", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }).execute();
    }

    /**
     * Calls the web service to cancel the current journey.
     **/
    private void cancelJourney()
    {
        progressBar.setVisibility(View.VISIBLE);
        new WcfPostServiceTask<JourneyUserDTO>(this, getResources().getString(R.string.CancelJourneyURL),
                new JourneyUserDTO(journey.getJourneyId(), appManager.getUser().getUserId()),
                new TypeToken<ServiceResponse<Journey>>() {}.getType(),
                appManager.getAuthorisationHeaders(), new WCFServiceCallback<Journey, Void>() {
            @Override
            public void onServiceCallCompleted(ServiceResponse<Journey> serviceResponse, Void parameter) {
                progressBar.setVisibility(View.GONE);
                if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
                {
                    successfullyCancelledJourney(serviceResponse.Result);
                }
            }
        }).execute();
    }

    /**
     * Called upon successful journey cancellation.
     **/
    private void successfullyCancelledJourney(Journey journey)
    {
        progressBar.setVisibility(View.GONE);
        this.journey = journey;
        makeChangeButton.setEnabled(false);
        cancelJourneyButton.setEnabled(false);
        Toast.makeText(this, "This journey has been cancelled successfully.", Toast.LENGTH_LONG).show();
        statusTextView.setText("Cancelled");
    }

    /**
     * Calls the web service to retrieve the list of passengers participating in this journey.
     **/
    private void getPassengers()
    {
        progressBar.setVisibility(View.VISIBLE);
        new WcfPostServiceTask<Integer>(this, getResources().getString(R.string.GetPassengersURL),
                journey.getJourneyId(),
                new TypeToken<ServiceResponse<ArrayList<User>>>() {}.getType(),
                appManager.getAuthorisationHeaders(), new WCFServiceCallback<ArrayList<User>, Void>() {
            @Override
            public void onServiceCallCompleted(final ServiceResponse<ArrayList<User>> serviceResponse, Void parameter) {
                progressBar.setVisibility(View.GONE);
                if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
                {
                    showPassengers(serviceResponse.Result);
                }
            }
        }).execute();
    }

    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            case R.id.MyJourneyDetailsActivityShowRequestsButton:
                retrieveJourneyRequests();
                break;
            case R.id.MyJourneyDetailsActivityShowPassengersButton:
                getPassengers();
                break;
            case R.id.MyJourneyDetailsActivityEnterChatButton:
                enterChatRoom();
                break;
            case R.id.MyJourneyDetailsActivityMakeChangeButton:
                startEditor();
                break;
            case R.id.MyJourneyDetailsActivityWithdrawFromJourneyButton:
                showWithdrawQuestionDialog();
                break;
            case R.id.MyJourneyDetailsActivityCancelJourneyButton:
                showCancelJourneyDialog();
                break;
            case R.id.MyJourneyDetailsActivityRateDriverButton:
                startActivity(new Intent(this, RateDriverActivity.class).putExtra(IntentConstants.JOURNEY, gson.toJson(journey)));
                break;
            case R.id.MyJourneyDetailsActivityShowSummaryButton:
                startActivity(new Intent(this, JourneySummaryActivity.class).putExtra(IntentConstants.JOURNEY, gson.toJson(journey)));
                break;
        }
    }

    /**
     * Starts the first step of the journey editor giving driver the ability to make changes to the current journey.
     **/
    private void startEditor()
    {
        Bundle bundle = new Bundle();
        bundle.putInt(IntentConstants.JOURNEY_CREATOR_MODE, IntentConstants.JOURNEY_CREATOR_MODE_EDITING);
        bundle.putString(IntentConstants.JOURNEY ,gson.toJson(journey));
        startActivity(new Intent(this, OfferJourneyStepOneActivity.class).putExtras(bundle));
    }

    @Override
    public void onDrivingDirectionsRetrieved() {
        progressBar.setVisibility(View.GONE);
    }
}
