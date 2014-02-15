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

import com.example.myapplication.activities.base.BaseListActivity;
import com.example.myapplication.adapters.ChatAdapter;
import com.example.myapplication.constants.BroadcastTypes;
import com.example.myapplication.constants.IntentConstants;
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.domain_objects.ChatMessage;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.dtos.ChatMessageRetrieverDTO;
import com.example.myapplication.R;
import com.example.myapplication.dtos.LoadRangeDTO;
import com.example.myapplication.experimental.DateTimeHelper;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WCFServiceTask;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Michal on 04/01/14.
 */
public class InstantMessengerActivity extends BaseListActivity {
    /** Called when the activity is first created. */

    private ArrayList<ChatMessage> chatMessages;

    private ChatAdapter chatAdapter;

    private EditText messageEditText;

    private int recipientId;

    private String recipientUserName;

    private ProgressBar progressBar;

    private Button sendButton;

    private Button loadMoreButton;

    private Boolean loadMoreData = false;

    private int currentScrollPosition;

    private final String TAG = this.getClass().getSimpleName();

    protected void onResume() {
        super.onResume();
        this.progressBar.setVisibility(View.VISIBLE);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadcastTypes.BROADCAST_INSTANT_MESSENGER);
        intentFilter.setPriority(1000);
        registerReceiver(GCMReceiver, intentFilter);
        this.getUnreadMessages();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(GCMReceiver);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_instant_messenger);

        // Extract data from the bundle.
        Bundle extras = getIntent().getExtras();

        // Initialise UI elements.
        this.recipientId = extras.getInt(IntentConstants.RECIPIENT_ID);
        this.recipientUserName = extras.getString(IntentConstants.RECIPIENT_USERNAME);
        this.actionBar.setTitle("Chat with " + this.recipientUserName);
        this.progressBar = (ProgressBar) this.findViewById(R.id.ActivityInstantMessengerProgressBar);
        this.sendButton = (Button) this.findViewById(R.id.InstantMessengerActivityButton);
        this.messageEditText = (EditText) this.findViewById(R.id.InstantMessengerActivityMessageEditText);
        this.loadMoreButton = (Button) this.findViewById(R.id.InstantMessengerActivityLoadMoreButton);

        // Setup event handlers.
        this.setupEventHandlers();

        this.chatMessages = new ArrayList<ChatMessage>();
        this.chatAdapter = new ChatAdapter(this, chatMessages, findNDriveManager.getUser().UserId);
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

        this.loadMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadMoreButton.setEnabled(false);
                progressBar.setVisibility(View.VISIBLE);
                currentScrollPosition = getListView().getLastVisiblePosition();
                loadMoreData = true;
                retrieveMessages();
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

    private void getUnreadMessages() {
        new WCFServiceTask<ChatMessageRetrieverDTO>(this, getResources().getString(R.string.GetUnreadMessages),
                new ChatMessageRetrieverDTO(recipientId, findNDriveManager.getUser().UserId,
                        new LoadRangeDTO(findNDriveManager.getUser().UserId, getListView().getCount(), findNDriveManager.getItemsPerCall(), loadMoreData)),
                new TypeToken<ServiceResponse<ArrayList<ChatMessage>>>() {}.getType(),
                findNDriveManager.getAuthorisationHeaders(), new WCFServiceCallback<ArrayList<ChatMessage>, Void>() {
            @Override
            public void onServiceCallCompleted(ServiceResponse<ArrayList<ChatMessage>> serviceResponse, Void parameter) {
                if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
                {
                    messagesRetrieved(serviceResponse.Result);
                }

            }
        }).execute();
    }

    private void sendMessage()
    {
        this.progressBar.setVisibility(View.VISIBLE);
        final ChatMessage newMessage = new ChatMessage(findNDriveManager.getUser().UserId, recipientId, messageEditText.getText().toString(),
                DateTimeHelper.convertToWCFDate(Calendar.getInstance().getTime()), false, recipientUserName, findNDriveManager.getUser().UserName);

        if(newMessage.MessageBody.length() > 0 )
        {
            this.sendButton.setEnabled(false);

            new WCFServiceTask<ChatMessage>(this, getResources().getString(R.string.SendMessageURL), newMessage,
                    new TypeToken<ServiceResponse<Boolean>>() {}.getType(),
                    findNDriveManager.getAuthorisationHeaders(), new WCFServiceCallback<Boolean, Void>() {
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
        new WCFServiceTask<ChatMessageRetrieverDTO>(this, getResources().getString(R.string.GetMessagesURL), new ChatMessageRetrieverDTO(findNDriveManager.getUser().UserId, recipientId,
                new LoadRangeDTO(findNDriveManager.getUser().UserId, getListView().getCount(), findNDriveManager.getItemsPerCall(), loadMoreData)),
                new TypeToken<ServiceResponse<ArrayList<ChatMessage>>>() {}.getType(),
                findNDriveManager.getAuthorisationHeaders(), new WCFServiceCallback<ArrayList<ChatMessage>, Void>() {
            @Override
            public void onServiceCallCompleted(ServiceResponse<ArrayList<ChatMessage>> serviceResponse, Void parameter) {
                if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
                {
                    loadMoreButton.setText(serviceResponse.Result.size() < findNDriveManager.getItemsPerCall() ? "No more data to load" : "Show more");
                    loadMoreButton.setEnabled(!(serviceResponse.Result.size() < findNDriveManager.getItemsPerCall()));
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

        if(this.loadMoreData)
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
        this.getListView().setSelection(this.loadMoreData ? this.currentScrollPosition : this.getListView().getLastVisiblePosition());
        this.loadMoreData = false;
    }

    private final BroadcastReceiver GCMReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            ChatMessage chatMessage = gson.fromJson(intent.getExtras().getString(IntentConstants.PAYLOAD), new TypeToken<ChatMessage>() {}.getType());

            for(ChatMessage message : chatMessages)
            {
                if(message.ChatMessageId == chatMessage.ChatMessageId)
                {
                    return;
                }
            }
            if(findNDriveManager.getUser().UserId == chatMessage.RecipientId && chatMessage.SenderId == recipientId)
            {
                addNewMessage(chatMessage);
                markMessageAsRead(chatMessage);
            }
            else
            {
                ohShitThisIsNotMyMessage(chatMessage);
            }

            this.abortBroadcast();
        }
    };

    private void markMessageAsRead(final ChatMessage chatMessage)
    {
        new WCFServiceTask<Integer>(this, getResources().getString(R.string.MarkMessageAsReadURL),chatMessage.ChatMessageId,
                new TypeToken<ServiceResponse<Boolean>>() {}.getType(),
                findNDriveManager.getAuthorisationHeaders(), new WCFServiceCallback<Boolean, Void>() {
            @Override
            public void onServiceCallCompleted(ServiceResponse<Boolean> serviceResponse, Void parameter) {
                if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
                {
                    Log.i(TAG, "Message no: " + chatMessage.ChatMessageId + " successfully marked as read.");
                }

            }
        }).execute();
    }

    private void ohShitThisIsNotMyMessage(ChatMessage chatMessage)
    {
        NotificationManager mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        Bundle extras = new Bundle();
        extras.putString(IntentConstants.RECIPIENT_USERNAME, chatMessage.SenderUserName);
        extras.putInt(IntentConstants.RECIPIENT_ID, chatMessage.SenderId);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, InstantMessengerActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        .putExtras(extras), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.logo)
                        .setContentTitle("Message from " + chatMessage.SenderUserName)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(chatMessage.MessageBody))
                        .setContentText(chatMessage.MessageBody);

        mBuilder.setContentIntent(contentIntent);
        Notification notification = mBuilder.build();
        notification.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;

        mNotificationManager.notify(0, notification);
    }
}