package com.example.myapplication.activities.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
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
import com.example.myapplication.factories.DialogFactory;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

/**
 * This class acts as the chat window between two users.
 * It is used to send, receive and view messages as well as view previous conversation history.
 * Please note that this class only applies to the one-to-one user instant messenger feature of the app.
 **/
public class InstantMessengerActivity extends BaseListActivity  implements AbsListView.OnScrollListener, View.OnClickListener{

    private ArrayList<ChatMessage> chatMessages;

    private ChatAdapter chatAdapter;

    private EditText messageEditText;

    private int recipientId;

    private String recipientUserName;

    private ProgressBar progressBar;

    private Button sendButton;

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

    private final String TAG = "Instant Messenger Activity";

    protected void onResume() {
        super.onResume();

        progressBar.setVisibility(View.VISIBLE);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadcastTypes.BROADCAST_INSTANT_MESSENGER);
        intentFilter.setPriority(1000);
        registerReceiver(MessageReceiver, intentFilter);
        getUnreadMessages();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(MessageReceiver);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instant_messenger);

        // Extract data from the bundle.
        Bundle extras = getIntent().getExtras();

        // Check if there is a pending notification that must be marked read.
        com.example.myapplication.domain_objects.Notification notification = gson.fromJson(extras.getString(IntentConstants.NOTIFICATION),
                new TypeToken<com.example.myapplication.domain_objects.Notification>() {}.getType());

        // Go ahead and mark the notification as read if safe to do so.
        if(notification != null)
        {
            new NotificationProcessor().MarkDelivered(this, appManager, notification, new WCFServiceCallback<Boolean, Void>() {
                @Override
                public void onServiceCallCompleted(ServiceResponse<Boolean> serviceResponse, Void parameter) {
                    Log.i(TAG, "Notification successfully marked as read.");
                }
            });
        }

        // Extract information of the user we are currently chatting with.
        ChatMessage chatMessage = gson.fromJson(extras.getString(IntentConstants.PAYLOAD) ,new TypeToken<ChatMessage>() {}.getType());

        recipientId = chatMessage != null ? chatMessage.getSenderId() : extras.getInt(IntentConstants.RECIPIENT_ID);
        recipientUserName = chatMessage != null ? chatMessage.getSenderUserName() : extras.getString(IntentConstants.RECIPIENT_USERNAME);

        // Initialise UI elements.
        actionBar.setTitle("Chat with " + recipientUserName);
        progressBar = (ProgressBar) findViewById(R.id.ActivityInstantMessengerProgressBar);
        sendButton = (Button) findViewById(R.id.InstantMessengerActivityButton);
        messageEditText = (EditText) findViewById(R.id.InstantMessengerActivityMessageEditText);

        chatMessages = new ArrayList<ChatMessage>();
        chatAdapter = new ChatAdapter(this, chatMessages, appManager.getUser().getUserId());
        setListAdapter(chatAdapter);
        getAllMessages();

        sendButton.setOnClickListener(this);
        getListView().setOnScrollListener(this);
    }

    /**
     * Retrieve any unread messages from the server.
     * This method is called every time the activity is resumed.
     * Doing so enables user to retrieve unread messages immediately even if GCM notification is late and does not arrive on time.
     */
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.help:
                DialogFactory.getHelpDialog(this, "Instant Messenger", getResources().getString(R.string.InstantMessengerHelp));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Called when user presses the send message button.
     * Establishes connection with the WCF service and call the Messenger Service to send a new message.
     * A simple validation check if carried out before the message is sent to ensure users don't send blank messages.
     */
    private void sendMessage()
    {
        // Create a new message.
        final ChatMessage newMessage = new ChatMessage(appManager.getUser().getUserId(), recipientId, messageEditText.getText().toString(),
                DateTimeHelper.convertToWCFDate(Calendar.getInstance().getTime()), false, recipientUserName, appManager.getUser().getUserName());

        // If the message if blank, return.
        if(newMessage.getMessageBody().length() == 0)
        {
            return;
        }

        // All good, show the progress bar and attempt to send the message.
        this.progressBar.setVisibility(View.VISIBLE);
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

    /**
     * Adds a new message to the listview. This is where the message becomes visible in the conversation history.
     * This method is called after all necessary validation checks have been made.
     **/
    private void addNewMessage(ChatMessage m)
    {
        chatMessages.add(m);
        chatAdapter.notifyDataSetInvalidated();
        getListView().setSelection(chatMessages.size()-1);
        progressBar.setVisibility(View.GONE);
    }

    /**
     * Used to retrieve conversation history from the server.
     * the LoadRangeDTO object is used to reduce the number of items being returned to speed up execution of the query.
     **/
    private void getAllMessages()
    {
        callInProgress = true;
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

    /**
     * Called when messages are successfully retrieved from the server.
     * Before a message can be added to the listview and become visible,
     * it must be filtered. The listview is scanned to check if a message with this id already exists.
     * This prevents duplicate messages from being displayed on the screen.
     * An example of a scenario where it would be possible to receive the same message twice is when
     * a user relaunches this activity and unread messages are retrieved from the server manually before a late GCM notification arrives.
     **/
    private void messagesRetrieved(ArrayList<ChatMessage> retrievedMessages)
    {
        progressBar.setVisibility(View.GONE);

        ArrayList<ChatMessage> filteredMessages = new ArrayList<ChatMessage>();

        for(ChatMessage chatMessage : retrievedMessages)
        {
            filteredMessages.add(chatMessage);
        }

        for(ChatMessage chatMessage : chatMessages)
        {
            for(ChatMessage chatMessage1 : retrievedMessages)
            {
                if(chatMessage1.getChatMessageId() == chatMessage.getChatMessageId())
                {
                    filteredMessages.remove(chatMessage1);
                }
            }
        }

        if(requestMoreData)
        {
            for(int i = 0; i < filteredMessages.size(); i++)
            {
                chatMessages.add(i, filteredMessages.get(i));
            }
        }
        else
        {
            chatMessages.addAll(filteredMessages);
        }

        chatAdapter.notifyDataSetInvalidated();

        if(requestMoreData)
        {
            getListView().setSelectionFromTop(currentScrollIndex, currentScrollTop);
        }
        else
        {
            getListView().setSelection(chatMessages.size());
        }

        requestMoreData = false;
        callInProgress = false;
    }

    /**
     * Receiver which listens for incoming chat messages.
     **/
    private final BroadcastReceiver MessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Bundle bundle = intent.getExtras();

            ChatMessage chatMessage = gson.fromJson(bundle.getString(IntentConstants.PAYLOAD), new TypeToken<ChatMessage>() {}.getType());

            for(ChatMessage message : chatMessages)
            {
                if(message.getChatMessageId() == chatMessage.getChatMessageId())
                {
                    return;
                }
            }

            // If incoming message happens to be coming from the user we are currently chatting with,
            // we can abort the broadcast and display the message in the listview.
            if(appManager.getUser().getUserId() == chatMessage.getRecipientId() && chatMessage.getSenderId() == recipientId)
            {
                addNewMessage(chatMessage);
                markMessageAsRead(chatMessage);
                abortBroadcast();
            }
        }
    };

    /**
     * Any new message that is received is immediately marked as read.
     * This is to prevent the app from notifying the user of a new message which they have already read.
     **/
    private void markMessageAsRead(final ChatMessage chatMessage)
    {
        new WcfPostServiceTask<Integer>(this, getResources().getString(R.string.MarkMessageAsReadURL),chatMessage.getChatMessageId(),
                new TypeToken<ServiceResponse<Boolean>>() {}.getType(),
                appManager.getAuthorisationHeaders(), new WCFServiceCallback<Boolean, Void>() {
            @Override
            public void onServiceCallCompleted(ServiceResponse<Boolean> serviceResponse, Void parameter) {
                if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
                {
                    Log.i(TAG, "Message no: " + chatMessage.getChatMessageId() + " successfully marked as read.");
                }

            }
        }).execute();
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {}

    @Override
    public void onScroll(AbsListView absListView,  int firstVisibleItem, int visibleItemCount, int totalItemCount)
    {
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
            case R.id.InstantMessengerActivityButton:
                sendMessage();
                break;
        }
    }
}