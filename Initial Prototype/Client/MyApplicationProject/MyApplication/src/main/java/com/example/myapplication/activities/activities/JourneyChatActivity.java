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
 * Created by Michal on 07/01/14.
 */
public class JourneyChatActivity extends BaseListActivity implements AbsListView.OnScrollListener{

    private ProgressBar progressBar;

    private int journeyId;

    private ArrayList<JourneyMessage> journeyMessages;

    private EditText messageEditText;

    private JourneyChatAdapter journeyChatAdapter;

    private Button sendButton;

    private int currentScrollIndex;
    private int currentScrollTop;

    private int previousTotalListViewItemCount;
    private int previousFirstVisibleItem;
    private int previousVisibleItemCount;

    private boolean requestMoreData;
    private boolean callInProgress;

    private final String TAG = "Journey Chat Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_journey_chat);

        Bundle bundle = getIntent().getExtras();

        com.example.myapplication.domain_objects.Notification notification = gson.fromJson(bundle.getString(IntentConstants.NOTIFICATION),
                new TypeToken<com.example.myapplication.domain_objects.Notification>() {}.getType());

        if(notification != null)
        {
            new NotificationProcessor().MarkDelivered(this, appManager, notification, new WCFServiceCallback<Boolean, Void>() {
                @Override
                public void onServiceCallCompleted(ServiceResponse<Boolean> serviceResponse, Void parameter) {

                }
            });
        }

        JourneyMessage journeyMessage = gson.fromJson(bundle.getString(IntentConstants.PAYLOAD), new TypeToken<JourneyMessage>() {}.getType());
        // Initialise local variables.
        this.journeyId =  journeyMessage != null ? journeyMessage.JourneyId : bundle.getInt(IntentConstants.JOURNEY);
        this.journeyMessages = new ArrayList<JourneyMessage>();
        this.journeyChatAdapter = new JourneyChatAdapter(this, this.journeyMessages, this.appManager.getUser().getUserId());
        this.setListAdapter(journeyChatAdapter);

        // Initialise UI elements.
        this.progressBar = (ProgressBar) this.findViewById(R.id.JourneyChatActivityProgressBar);
        this.messageEditText = (EditText) this.findViewById(R.id.JourneyChatActivityMessageEditText);
        this.sendButton = (Button) this.findViewById(R.id.JourneyChatActivitySendButton);

        // Setup event handlers.
        this.setupEventHandlers();

        // Retrieve all messages.
        this.retrieveAllMessages();
    }

    private void setupEventHandlers()
    {
        this.sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
        this.getListView().setOnScrollListener(this);
    }

    private void sendMessage()
    {
        if(this.messageEditText.getText().toString().isEmpty())
        {
            return;
        }

        this.progressBar.setVisibility(View.VISIBLE);

        final JourneyMessage newMessage = new JourneyMessage(
                this.journeyId,
                this.appManager.getUser().getUserName(),
                this.appManager.getUser().getUserId(),
                this.messageEditText.getText().toString(),
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

        this.progressBar.setVisibility(View.VISIBLE);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadcastTypes.BROADCAST_JOURNEY_MESSAGE);
        intentFilter.setPriority(1000);

        this.registerReceiver(MessageReceiver, intentFilter);

        this.retrieveUnreadMessages();
    }

    private final BroadcastReceiver MessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Bundle bundle = intent.getExtras();

            JourneyMessage journeyMessage = gson.fromJson(bundle.getString(IntentConstants.PAYLOAD), new TypeToken<JourneyMessage>() {}.getType());

            if(journeyMessage.JourneyId == journeyId)
            {
                this.abortBroadcast();

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

    private void markMessageAsRead(final JourneyMessage journeyMessage)
    {
        new WcfPostServiceTask<JourneyMessageMarkerDTO>(this, getResources().getString(R.string.MarkJourneyMessageAsReadURL),
                new JourneyMessageMarkerDTO(this.appManager.getUser().getUserId(), journeyMessage.JourneyMessageId),
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

    private void addNewMessage(JourneyMessage journeyMessage)
    {
        this.journeyMessages.add(journeyMessage);
        this.journeyChatAdapter.notifyDataSetInvalidated();
        this.getListView().setSelection(this.journeyMessages.size()-1);
        this.progressBar.setVisibility(View.GONE);
    }

    public void retrieveAllMessages()
    {
        this.callInProgress = true;
        new WcfPostServiceTask<JourneyMessageRetrieverDTO>(this, getResources().getString(R.string.RetrieveJourneyChatMessagesURL),
                new JourneyMessageRetrieverDTO(this.journeyId, this.appManager.getUser().getUserId(),
                        new LoadRangeDTO(appManager.getUser().getUserId(), this.getListView().getCount(),WcfConstants.MessagesPerCall)),
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

    public void retrieveUnreadMessages()
    {
        new WcfPostServiceTask<JourneyMessageRetrieverDTO>(this, getResources().getString(R.string.RetrieveUnreadJourneyMessagesURL),
                new JourneyMessageRetrieverDTO(this.journeyId, this.appManager.getUser().getUserId(),
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

    private void messagesRetrieved(ArrayList<JourneyMessage> retrievedMessages) {
        this.progressBar.setVisibility(View.GONE);

        ArrayList<JourneyMessage> filteredMessages = new ArrayList<JourneyMessage>();

        for(JourneyMessage journeyMessage : retrievedMessages)
        {
            filteredMessages.add(journeyMessage);
        }

        for(JourneyMessage journeyMessage : this.journeyMessages)
        {
            for(JourneyMessage journeyMessage1 : retrievedMessages)
            {
                if(journeyMessage1.JourneyMessageId == journeyMessage.JourneyMessageId)
                {
                    filteredMessages.remove(journeyMessage1);
                }
            }
        }

        if(this.requestMoreData)
        {
            for(int i = 0; i < filteredMessages.size(); i++)
            {
                this.journeyMessages.add(i, filteredMessages.get(i));
            }
        }
        else
        {
            this.journeyMessages.addAll(filteredMessages);
        }

        this.journeyChatAdapter.notifyDataSetInvalidated();

        if(this.requestMoreData)
        {
            this.getListView().setSelectionFromTop(currentScrollIndex, currentScrollTop);
        }
        else
        {
            this.getListView().setSelection(journeyMessages.size());
        }

        this.requestMoreData = false;
        this.callInProgress = false;
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {

    }

    @Override
    public void onScroll(AbsListView absListView,  int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (this.previousTotalListViewItemCount == totalItemCount || this.previousVisibleItemCount == visibleItemCount || this.callInProgress)
        {
            return;
        }

        if(firstVisibleItem == 0)
        {
            Log.i(TAG, "ListView at top!");
            this.previousTotalListViewItemCount = totalItemCount;
            this.previousFirstVisibleItem = firstVisibleItem;
            this.previousVisibleItemCount = visibleItemCount;
            this.currentScrollIndex = this.getListView().getFirstVisiblePosition();
            View v = this.getListView().getChildAt(0);
            this.currentScrollTop= (v == null) ? 0 : v.getTop();
            this.requestMoreData = true;
            this.retrieveAllMessages();
        }
    }
}
