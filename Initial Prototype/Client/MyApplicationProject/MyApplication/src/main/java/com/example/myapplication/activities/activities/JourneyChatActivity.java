package com.example.myapplication.activities.activities;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
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
import com.example.myapplication.domain_objects.JourneyMessage;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.dtos.JourneyMessageMarkerDTO;
import com.example.myapplication.dtos.JourneyMessageRetrieverDTO;
import com.example.myapplication.dtos.LoadRangeDTO;
import com.example.myapplication.experimental.DateTimeHelper;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WCFServiceTask;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Michal on 07/01/14.
 */
public class JourneyChatActivity extends BaseListActivity{

    private ProgressBar progressBar;

    private int journeyId;

    private ArrayList<JourneyMessage> journeyMessages;

    private EditText messageEditText;

    private JourneyChatAdapter journeyChatAdapter;

    private Button sendButton;
    private Button loadMoreButton;

    private int currentScrollPosition;

    private Boolean loadMoreData = false;

    private final String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_journey_chat);

        // Initialise local variables.
        this.journeyId =  getIntent().getExtras().getInt(IntentConstants.JOURNEY);
        this.journeyMessages = new ArrayList<JourneyMessage>();
        this.journeyChatAdapter = new JourneyChatAdapter(this, this.journeyMessages, this.findNDriveManager.getUser().UserId);
        this.setListAdapter(journeyChatAdapter);

        // Initialise UI elements.
        this.progressBar = (ProgressBar) this.findViewById(R.id.JourneyChatActivityProgressBar);
        this.messageEditText = (EditText) this.findViewById(R.id.JourneyChatActivityMessageEditText);
        this.sendButton = (Button) this.findViewById(R.id.JourneyChatActivitySendButton);
        this.loadMoreButton = (Button) this.findViewById(R.id.JourneyChatActivityLoadMoreButton);

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

        this.loadMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadMoreButton.setEnabled(false);
                progressBar.setVisibility(View.VISIBLE);
                currentScrollPosition = getListView().getLastVisiblePosition();
                loadMoreData = true;
                retrieveAllMessages();
            }
        });

        this.getListView().setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
            }

            @Override
            public void onScroll(AbsListView absListView,  int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                loadMoreButton.setVisibility(firstVisibleItem == 0 ? View.VISIBLE : View.GONE);
                getListView().setPadding(0, loadMoreButton.getVisibility() == View.VISIBLE ? loadMoreButton.getHeight() : 0, 0, 0);
            }
        });
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
                this.findNDriveManager.getUser().UserName,
                this.findNDriveManager.getUser().UserId,
                this.messageEditText.getText().toString(),
                DateTimeHelper.convertToWCFDate(Calendar.getInstance().getTime()));

        if(newMessage.MessageBody.length() > 0 )
        {
            new WCFServiceTask<JourneyMessage>(this, getResources().getString(R.string.SendJourneyChatMessageURL), newMessage,
                    new TypeToken<ServiceResponse<Boolean>>() {}.getType(),
                    findNDriveManager.getAuthorisationHeaders(), new WCFServiceCallback<Boolean, Void>() {
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

        this.registerReceiver(GCMReceiver, intentFilter);

        this.retrieveUnreadMessages();
    }

    private final BroadcastReceiver GCMReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            JourneyMessage journeyMessage = gson.fromJson(intent.getExtras().getString(IntentConstants.PAYLOAD), new TypeToken<JourneyMessage>() {}.getType());

            for(JourneyMessage message : journeyMessages)
            {
                if(message.JourneyMessageId == journeyMessage.JourneyMessageId)
                {
                    return;
                }
            }

            if(journeyMessage.JourneyId == journeyId)
            {
                addNewMessage(journeyMessage);
                markMessageAsRead(journeyMessage);
            }
            else
            {
                ohShitThisIsNotMyMessage(journeyMessage);
            }

            this.abortBroadcast();
        }
    };

    private void markMessageAsRead(final JourneyMessage journeyMessage)
    {
        new WCFServiceTask<JourneyMessageMarkerDTO>(this, getResources().getString(R.string.MarkJourneyMessageAsReadURL),
                new JourneyMessageMarkerDTO(this.findNDriveManager.getUser().UserId, journeyMessage.JourneyMessageId),
                new TypeToken<ServiceResponse<Boolean>>() {}.getType(),
                findNDriveManager.getAuthorisationHeaders(), new WCFServiceCallback<Boolean, Void>() {
            @Override
            public void onServiceCallCompleted(ServiceResponse<Boolean> serviceResponse, Void parameter) {
                if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
                {
                    Log.i(TAG, "Message no: " + journeyMessage.JourneyMessageId + " successfully marked as read.");
                }

            }
        }).execute();
    }

    private void ohShitThisIsNotMyMessage(JourneyMessage journeyMessage)
    {
        NotificationManager mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, JourneyChatActivity.class)
                        .putExtra(IntentConstants.JOURNEY, journeyMessage.JourneyId)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP), PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.logo)
                        .setContentTitle("New journey chat message")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText("Click here to see it."))
                        .setContentText("Click here to see it.");

        mBuilder.setContentIntent(contentIntent);
        Notification notification = mBuilder.build();
        notification.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;

        mNotificationManager.notify(0, notification);
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
        new WCFServiceTask<JourneyMessageRetrieverDTO>(this, getResources().getString(R.string.RetrieveJourneyChatMessagesURL),
                new JourneyMessageRetrieverDTO(this.journeyId, this.findNDriveManager.getUser().UserId,
                        new LoadRangeDTO(findNDriveManager.getUser().UserId, getListView().getCount(), findNDriveManager.getItemsPerCall(), loadMoreData)),
                new TypeToken<ServiceResponse<ArrayList<JourneyMessage>>>() {}.getType(),
                findNDriveManager.getAuthorisationHeaders(), new WCFServiceCallback<ArrayList<JourneyMessage>, Void>() {
            @Override
            public void onServiceCallCompleted(ServiceResponse<ArrayList<JourneyMessage>> serviceResponse, Void parameter) {
                if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
                {
                    loadMoreButton.setText(serviceResponse.Result.size() < findNDriveManager.getItemsPerCall() ? "No more data to load" : "Show more");
                    loadMoreButton.setEnabled(!(serviceResponse.Result.size() < findNDriveManager.getItemsPerCall()));
                    messagesRetrieved(serviceResponse.Result);
                }

            }
        }).execute();
    }

    public void retrieveUnreadMessages()
    {
        new WCFServiceTask<JourneyMessageRetrieverDTO>(this, getResources().getString(R.string.RetrieveUnreadJourneyMessagesURL),
                new JourneyMessageRetrieverDTO(this.journeyId, this.findNDriveManager.getUser().UserId,
                        new LoadRangeDTO(findNDriveManager.getUser().UserId, getListView().getCount(), findNDriveManager.getItemsPerCall(), loadMoreData)),
                new TypeToken<ServiceResponse<ArrayList<JourneyMessage>>>() {}.getType(),
                findNDriveManager.getAuthorisationHeaders(), new WCFServiceCallback<ArrayList<JourneyMessage>, Void>() {
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
        unregisterReceiver(GCMReceiver);
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

        if(this.loadMoreData)
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
        this.getListView().setSelection(this.loadMoreData ? this.currentScrollPosition : this.getListView().getLastVisiblePosition());
        this.loadMoreData = false;
    }
}
