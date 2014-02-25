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

import com.example.myapplication.activities.base.BaseListActivity;
import com.example.myapplication.adapters.ChatAdapter;
import com.example.myapplication.constants.BroadcastTypes;
import com.example.myapplication.constants.IntentConstants;
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.constants.WcfConstants;
import com.example.myapplication.domain_objects.ChatMessage;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.dtos.ChatMessageRetrieverDTO;
import com.example.myapplication.R;
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
 * Created by Michal on 04/01/14.
 */
public class InstantMessengerActivity extends BaseListActivity  implements AbsListView.OnScrollListener{
    /** Called when the activity is first created. */

    private ArrayList<ChatMessage> chatMessages;

    private ChatAdapter chatAdapter;

    private EditText messageEditText;

    private int recipientId;

    private String recipientUserName;

    private ProgressBar progressBar;

    private Button sendButton;

    private int currentScrollIndex;
    private int currentScrollTop;

    private int previousTotalListViewItemCount;
    private int previousFirstVisibleItem;
    private int previousVisibleItemCount;

    private boolean requestMoreData;
    private boolean callInProgress;

    private final String TAG = "Instant Messenger Activity";

    protected void onResume() {
        super.onResume();

        this.progressBar.setVisibility(View.VISIBLE);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadcastTypes.BROADCAST_INSTANT_MESSENGER);
        intentFilter.setPriority(1000);
        registerReceiver(MessageReceiver, intentFilter);
        this.getUnreadMessages();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(MessageReceiver);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_instant_messenger);

        // Extract data from the bundle.
        Bundle extras = getIntent().getExtras();

        com.example.myapplication.domain_objects.Notification notification = gson.fromJson(extras.getString(IntentConstants.NOTIFICATION),
                new TypeToken<com.example.myapplication.domain_objects.Notification>() {}.getType());

        if(notification != null)
        {
            new NotificationProcessor().MarkDelivered(this, appManager, notification, new WCFServiceCallback<Boolean, Void>() {
                @Override
                public void onServiceCallCompleted(ServiceResponse<Boolean> serviceResponse, Void parameter) {

                }
            });
        }

        ChatMessage chatMessage = this.gson.fromJson(extras.getString(IntentConstants.PAYLOAD) ,new TypeToken<ChatMessage>() {}.getType());

        this.recipientId = chatMessage != null ? chatMessage.SenderId : extras.getInt(IntentConstants.RECIPIENT_ID);
        this.recipientUserName = chatMessage != null ? chatMessage.SenderUserName : extras.getString(IntentConstants.RECIPIENT_USERNAME);

        // Initialise UI elements.
        this.actionBar.setTitle("Chat with " + this.recipientUserName);
        this.progressBar = (ProgressBar) this.findViewById(R.id.ActivityInstantMessengerProgressBar);
        this.sendButton = (Button) this.findViewById(R.id.InstantMessengerActivityButton);
        this.messageEditText = (EditText) this.findViewById(R.id.InstantMessengerActivityMessageEditText);

        // Setup event handlers.
        this.setupEventHandlers();

        this.chatMessages = new ArrayList<ChatMessage>();
        this.chatAdapter = new ChatAdapter(this, chatMessages, appManager.getUser().getUserId());
        this.setListAdapter(chatAdapter);
        this.retrieveMessages();

        // For debugging purposes.
        Log.d(TAG, this.recipientUserName + " " + this.recipientId);
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

    private void getUnreadMessages() {
        new WcfPostServiceTask<ChatMessageRetrieverDTO>(this, getResources().getString(R.string.GetUnreadMessages),
                new ChatMessageRetrieverDTO(recipientId, appManager.getUser().getUserId(),
                        new LoadRangeDTO(appManager.getUser().getUserId(), 0,0)),
                new TypeToken<ServiceResponse<ArrayList<ChatMessage>>>() {}.getType(),
                appManager.getAuthorisationHeaders(), new WCFServiceCallback<ArrayList<ChatMessage>, Void>() {
            @Override
            public void onServiceCallCompleted(ServiceResponse<ArrayList<ChatMessage>> serviceResponse, Void parameter) {
                if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
                {
                    Log.i(TAG, "Successfully retrieved: "+ serviceResponse.Result.size() + " unread messages.");
                    messagesRetrieved(serviceResponse.Result);
                }

            }
        }).execute();
    }

    private void sendMessage()
    {
        this.progressBar.setVisibility(View.VISIBLE);
        final ChatMessage newMessage = new ChatMessage(appManager.getUser().getUserId(), recipientId, messageEditText.getText().toString(),
                DateTimeHelper.convertToWCFDate(Calendar.getInstance().getTime()), false, recipientUserName, appManager.getUser().getUserName());

        if(newMessage.MessageBody.length() > 0 )
        {
            this.sendButton.setEnabled(false);

            new WcfPostServiceTask<ChatMessage>(this, getResources().getString(R.string.SendMessageURL), newMessage,
                    new TypeToken<ServiceResponse<Boolean>>() {}.getType(),
                    appManager.getAuthorisationHeaders(), new WCFServiceCallback<Boolean, Void>() {
                @Override
                public void onServiceCallCompleted(ServiceResponse<Boolean> serviceResponse, Void parameter) {
                    if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
                    {
                        messageEditText.setText("");
                        addNewMessage(newMessage);
                        sendButton.setEnabled(true);
                    }
                }
            }).execute();
        }
    }

    private void addNewMessage(ChatMessage m)
    {
        chatMessages.add(m);
        chatAdapter.notifyDataSetInvalidated();
        getListView().setSelection(chatMessages.size()-1);
        this.progressBar.setVisibility(View.GONE);
    }

    private void retrieveMessages()
    {
        this.callInProgress = true;
        new WcfPostServiceTask<ChatMessageRetrieverDTO>(this, getResources().getString(R.string.GetMessagesURL), new ChatMessageRetrieverDTO(appManager.getUser().getUserId(), recipientId,
                new LoadRangeDTO(appManager.getUser().getUserId(), this.getListView().getCount(),WcfConstants.MessagesPerCall)),
                new TypeToken<ServiceResponse<ArrayList<ChatMessage>>>() {}.getType(),
                appManager.getAuthorisationHeaders(), new WCFServiceCallback<ArrayList<ChatMessage>, Void>() {
            @Override
            public void onServiceCallCompleted(ServiceResponse<ArrayList<ChatMessage>> serviceResponse, Void parameter) {
                if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
                {
                    Collections.reverse(serviceResponse.Result);
                    Log.i(TAG, "Successfully retrieved: "+ serviceResponse.Result.size() + " messages.");
                    messagesRetrieved(serviceResponse.Result);
                }

            }
        }).execute();
    }

    private void messagesRetrieved(ArrayList<ChatMessage> retrievedMessages)
    {
        this.progressBar.setVisibility(View.GONE);

        ArrayList<ChatMessage> filteredMessages = new ArrayList<ChatMessage>();

        for(ChatMessage chatMessage : retrievedMessages)
        {
            filteredMessages.add(chatMessage);
        }

        for(ChatMessage chatMessage : this.chatMessages)
        {
            for(ChatMessage chatMessage1 : retrievedMessages)
            {
                if(chatMessage1.ChatMessageId == chatMessage.ChatMessageId)
                {
                    filteredMessages.remove(chatMessage1);
                }
            }
        }

        if(this.requestMoreData)
        {
            for(int i = 0; i < filteredMessages.size(); i++)
            {
                this.chatMessages.add(i, filteredMessages.get(i));
            }
        }
        else
        {
            this.chatMessages.addAll(filteredMessages);
        }

        this.chatAdapter.notifyDataSetInvalidated();

        if(this.requestMoreData)
        {
            this.getListView().setSelectionFromTop(currentScrollIndex, currentScrollTop);
        }
        else
        {
            this.getListView().setSelection(chatMessages.size());
        }

        this.requestMoreData = false;
        this.callInProgress = false;
    }

    private final BroadcastReceiver MessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Bundle bundle = intent.getExtras();

            ChatMessage chatMessage = gson.fromJson(bundle.getString(IntentConstants.PAYLOAD), new TypeToken<ChatMessage>() {}.getType());

            for(ChatMessage message : chatMessages)
            {
                if(message.ChatMessageId == chatMessage.ChatMessageId)
                {
                    return;
                }
            }

            if(appManager.getUser().getUserId() == chatMessage.RecipientId && chatMessage.SenderId == recipientId)
            {
                addNewMessage(chatMessage);
                markMessageAsRead(chatMessage);
                this.abortBroadcast();
            }
        }
    };

    private void markMessageAsRead(final ChatMessage chatMessage)
    {
        new WcfPostServiceTask<Integer>(this, getResources().getString(R.string.MarkMessageAsReadURL),chatMessage.ChatMessageId,
                new TypeToken<ServiceResponse<Boolean>>() {}.getType(),
                appManager.getAuthorisationHeaders(), new WCFServiceCallback<Boolean, Void>() {
            @Override
            public void onServiceCallCompleted(ServiceResponse<Boolean> serviceResponse, Void parameter) {
                if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
                {
                    Log.i(TAG, "Message no: " + chatMessage.ChatMessageId + " successfully marked as read.");
                }

            }
        }).execute();
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {}

    @Override
    public void onScroll(AbsListView absListView,  int firstVisibleItem, int visibleItemCount, int totalItemCount)
    {
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
            this.retrieveMessages();
        }
    }
}