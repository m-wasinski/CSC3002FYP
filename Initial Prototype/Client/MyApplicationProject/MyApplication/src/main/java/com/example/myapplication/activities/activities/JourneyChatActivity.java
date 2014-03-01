package com.example.myapplication.activities.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.example.myapplication.R;
import com.example.myapplication.activities.base.BaseListActivity;
import com.example.myapplication.adapters.JourneyChatAdapter;
import com.example.myapplication.constants.BroadcastTypes;
import com.example.myapplication.constants.IntentConstants;
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.constants.WcfConstants;
import com.example.myapplication.domain_objects.JourneyMessage;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.dtos.JourneyMessageMarkerDTO;
import com.example.myapplication.dtos.JourneyMessageRetrieverDTO;
import com.example.myapplication.dtos.LoadRangeDTO;
import com.example.myapplication.utilities.DateTimeHelper;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WcfPostServiceTask;
import com.example.myapplication.notification_management.NotificationProcessor;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

/**
 * The Journey Chat Activity acts as a Journey Chat room, place where all passengers participating in a journey
 * can talk with each other and exchange messages in real time.
 **/
public class JourneyChatActivity extends BaseListActivity implements AbsListView.OnScrollListener, View.OnClickListener{

    private ProgressBar progressBar;

    private int journeyId;

    private ArrayList<JourneyMessage> journeyMessages;

    private EditText messageEditText;

    private JourneyChatAdapter journeyChatAdapter;

    /* Variables used to keep track of current scroll
    position when more messages are retrieved from the server.*/
    private int currentScrollIndex;
    private int currentScrollTop;
    private int previousTotalListViewItemCount;
    private int previousFirstVisibleItem;
    private int previousVisibleItemCount;

    /*Variables used to determine whether server should be polled for more data
    and to prevent events from being fired multiple times when user scrolls slowly.*/
    private boolean requestMoreData;
    private boolean callInProgress;

    private final String TAG = "Journey Chat Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journey_chat);

        // Extract data from the bundle.
        Bundle bundle = getIntent().getExtras();

        com.example.myapplication.domain_objects.Notification notification = gson.fromJson(bundle.getString(IntentConstants.NOTIFICATION),
                new TypeToken<com.example.myapplication.domain_objects.Notification>() {}.getType());

        // Check if there is a pending notification that must be marked read.
        if(notification != null)
        {
            new NotificationProcessor().MarkDelivered(this, appManager, notification, new WCFServiceCallback<Boolean, Void>() {
                @Override
                public void onServiceCallCompleted(ServiceResponse<Boolean> serviceResponse, Void parameter) {

                }
            });
        }

        // Retrieve the id for this journey.
        JourneyMessage journeyMessage = gson.fromJson(bundle.getString(IntentConstants.PAYLOAD), new TypeToken<JourneyMessage>() {}.getType());
        journeyId =  journeyMessage != null ? journeyMessage.JourneyId : bundle.getInt(IntentConstants.JOURNEY);
        journeyMessages = new ArrayList<JourneyMessage>();
        journeyChatAdapter = new JourneyChatAdapter(this, journeyMessages, appManager.getUser().getUserId());
        setListAdapter(journeyChatAdapter);

        // Initialise UI elements.
        progressBar = (ProgressBar) findViewById(R.id.JourneyChatActivityProgressBar);
        messageEditText = (EditText) findViewById(R.id.JourneyChatActivityMessageEditText);
        Button sendButton = (Button) findViewById(R.id.JourneyChatActivitySendButton);

        // Setup event handlers.
        sendButton.setOnClickListener(this);
        getListView().setOnScrollListener(this);

        // Retrieve conversation history for this journey.
        getAllMessages();
    }

    private void sendMessage()
    {
        if(messageEditText.getText().toString().isEmpty())
        {
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        final JourneyMessage newMessage = new JourneyMessage(
                journeyId,
                appManager.getUser().getUserName(),
                appManager.getUser().getUserId(),
                messageEditText.getText().toString(),
                DateTimeHelper.convertToWCFDate(Calendar.getInstance().getTime()));

        if(newMessage.MessageBody.length() > 0 )
        {
            new WcfPostServiceTask<JourneyMessage>(this, getResources().getString(R.string.SendJourneyChatMessageURL), newMessage,
                    new TypeToken<ServiceResponse<Boolean>>() {}.getType(),
                    appManager.getAuthorisationHeaders(), new WCFServiceCallback<Boolean, Void>() {
                @Override
                public void onServiceCallCompleted(ServiceResponse<Boolean> serviceResponse, Void parameter) {
                    progressBar.setVisibility(View.GONE);
                    if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
                    {
                        messageEditText.setText("");
                        addNewMessage(newMessage);
                    }
                }
            }).execute();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();

        progressBar.setVisibility(View.VISIBLE);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadcastTypes.BROADCAST_JOURNEY_MESSAGE);
        intentFilter.setPriority(1000);
        registerReceiver(MessageReceiver, intentFilter);
        getUnreadMessages();
    }

    /*
     * This receiver listens for incoming journey chat messages.
     */
    private final BroadcastReceiver MessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Bundle bundle = intent.getExtras();

            JourneyMessage journeyMessage = gson.fromJson(bundle.getString(IntentConstants.PAYLOAD), new TypeToken<JourneyMessage>() {}.getType());

            if(journeyMessage.JourneyId == journeyId)
            {
                // If the message happens to be related to the journey of the currently opened chat room,
                // we can abort the broadcast and display the message in the listview.
                abortBroadcast();

                for(JourneyMessage message : journeyMessages)
                {
                    if(message.JourneyMessageId == journeyMessage.JourneyMessageId)
                    {
                        return;
                    }
                }

                addNewMessage(journeyMessage);
                markMessageAsRead(journeyMessage);
            }
        }
    };

    /**
     * Any new message that is received is immediately marked as read.
     * This is to prevent the app from notifying the user of a new message which they have already read.
     **/
    private void markMessageAsRead(final JourneyMessage journeyMessage)
    {
        new WcfPostServiceTask<JourneyMessageMarkerDTO>(this, getResources().getString(R.string.MarkJourneyMessageAsReadURL),
                new JourneyMessageMarkerDTO(appManager.getUser().getUserId(), journeyMessage.JourneyMessageId),
                new TypeToken<ServiceResponse<Boolean>>() {}.getType(),
                appManager.getAuthorisationHeaders(), new WCFServiceCallback<Boolean, Void>() {
            @Override
            public void onServiceCallCompleted(ServiceResponse<Boolean> serviceResponse, Void parameter) {
                if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
                {
                    Log.i(TAG, "Message no: " + journeyMessage.JourneyMessageId + " successfully marked as read.");
                }

            }
        }).execute();
    }

    /**
     * Adds a new message to the listview. This is where the message becomes visible in the conversation history.
     * This method is called after all necessary validation checks have been made.
     **/
    private void addNewMessage(JourneyMessage journeyMessage)
    {
        journeyMessages.add(journeyMessage);
        journeyChatAdapter.notifyDataSetInvalidated();
        getListView().setSelection(journeyMessages.size()-1);
        progressBar.setVisibility(View.GONE);
    }

    /**
     * Used to retrieve conversation history from the server.
     * the LoadRangeDTO object is used to reduce the number of items being returned to speed up execution of the query.
     **/
    public void getAllMessages()
    {
        callInProgress = true;
        new WcfPostServiceTask<JourneyMessageRetrieverDTO>(this, getResources().getString(R.string.RetrieveJourneyChatMessagesURL),
                new JourneyMessageRetrieverDTO(journeyId, appManager.getUser().getUserId(),
                        new LoadRangeDTO(appManager.getUser().getUserId(), getListView().getCount(),WcfConstants.MessagesPerCall)),
                new TypeToken<ServiceResponse<ArrayList<JourneyMessage>>>() {}.getType(),
                appManager.getAuthorisationHeaders(), new WCFServiceCallback<ArrayList<JourneyMessage>, Void>() {
            @Override
            public void onServiceCallCompleted(ServiceResponse<ArrayList<JourneyMessage>> serviceResponse, Void parameter) {
                if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
                {
                    Collections.reverse(serviceResponse.Result);
                    messagesRetrieved(serviceResponse.Result);
                }

            }
        }).execute();
    }

    /**
     * Get any unread messages from the server.
     * This method is called every time the activity is resumed.
     * Doing so enables user to retrieve unread messages immediately even if GCM notification is late and does not arrive on time.
     */
    public void getUnreadMessages()
    {
        new WcfPostServiceTask<JourneyMessageRetrieverDTO>(this, getResources().getString(R.string.RetrieveUnreadJourneyMessagesURL),
                new JourneyMessageRetrieverDTO(journeyId, appManager.getUser().getUserId(),
                        new LoadRangeDTO(appManager.getUser().getUserId(), 0, 0)),
                new TypeToken<ServiceResponse<ArrayList<JourneyMessage>>>() {}.getType(),
                appManager.getAuthorisationHeaders(), new WCFServiceCallback<ArrayList<JourneyMessage>, Void>() {
            @Override
            public void onServiceCallCompleted(ServiceResponse<ArrayList<JourneyMessage>> serviceResponse, Void parameter) {
                if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
                {
                    messagesRetrieved(serviceResponse.Result);
                }

            }
        }).execute();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(MessageReceiver);
    }

    /**
     * Called when messages are successfully retrieved from the server.
     * Before a message can be added to the listview and become visible,
     * it must be filtered. The listview is scanned to check if a message with this id already exists.
     * This prevents duplicate messages from being displayed on the screen.
     * An example of a scenario where it would be possible to receive the same message twice is when
     * a user relaunches this activity and unread messages are retrieved from the server manually before a late GCM notification arrives.
     **/
    private void messagesRetrieved(ArrayList<JourneyMessage> retrievedMessages) {
        progressBar.setVisibility(View.GONE);

        ArrayList<JourneyMessage> filteredMessages = new ArrayList<JourneyMessage>();

        for(JourneyMessage journeyMessage : retrievedMessages)
        {
            filteredMessages.add(journeyMessage);
        }

        for(JourneyMessage journeyMessage : journeyMessages)
        {
            for(JourneyMessage journeyMessage1 : retrievedMessages)
            {
                if(journeyMessage1.JourneyMessageId == journeyMessage.JourneyMessageId)
                {
                    filteredMessages.remove(journeyMessage1);
                }
            }
        }

        if(requestMoreData)
        {
            for(int i = 0; i < filteredMessages.size(); i++)
            {
                journeyMessages.add(i, filteredMessages.get(i));
            }
        }
        else
        {
            journeyMessages.addAll(filteredMessages);
        }

        journeyChatAdapter.notifyDataSetInvalidated();

        if(requestMoreData)
        {
            getListView().setSelectionFromTop(currentScrollIndex, currentScrollTop);
        }
        else
        {
            getListView().setSelection(journeyMessages.size());
        }

        requestMoreData = false;
        callInProgress = false;
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {

    }

    @Override
    public void onScroll(AbsListView absListView,  int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (previousTotalListViewItemCount == totalItemCount || previousVisibleItemCount == visibleItemCount || callInProgress)
        {
            return;
        }

        if(firstVisibleItem == 0)
        {
            previousTotalListViewItemCount = totalItemCount;
            previousFirstVisibleItem = firstVisibleItem;
            previousVisibleItemCount = visibleItemCount;
            currentScrollIndex = getListView().getFirstVisiblePosition();
            View v = getListView().getChildAt(0);
            currentScrollTop= (v == null) ? 0 : v.getTop();
            requestMoreData = true;
            getAllMessages();
        }
    }

    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            case R.id.JourneyChatActivitySendButton:
                sendMessage();
                break;
        }
    }
}
