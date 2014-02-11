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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.example.myapplication.activities.base.BaseListActivity;
import com.example.myapplication.adapters.ChatAdapter;
import com.example.myapplication.constants.GcmConstants;
import com.example.myapplication.constants.IntentConstants;
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.domain_objects.ChatMessage;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.dtos.ChatMessageRetrieverDTO;
import com.example.myapplication.R;
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

    private ArrayList<ChatMessage> messages;

    private ChatAdapter adapter;

    private EditText messageEditText;

    private int recipientId;

    private String recipientUserName;

    private ProgressBar progressBar;

    @Override
    protected void onResume() {
        super.onResume();
        this.progressBar.setVisibility(View.VISIBLE);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(GcmConstants.BROADCAST_INSTANT_MESSENGER);
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
        setContentView(R.layout.activity_instant_messenger);
        Bundle extras = getIntent().getExtras();
        this.recipientId = extras.getInt(IntentConstants.RECIPIENT_ID);
        this.recipientUserName = extras.getString(IntentConstants.RECIPIENT_USERNAME);
        this.actionBar.setTitle(this.recipientUserName);
        this.progressBar = (ProgressBar) this.findViewById(R.id.ActivityInstantMessengerProgressBar);
        Button sendButton = (Button) findViewById(R.id.InstantMessengerActivityButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
        messageEditText = (EditText) this.findViewById(R.id.text);
        messages = new ArrayList<ChatMessage>();

        adapter = new ChatAdapter(this, messages, findNDriveManager.getUser().UserId);
        setListAdapter(adapter);

        this.retrieveMessages();
    }

    private void getUnreadMessages() {
        new WCFServiceTask<ChatMessageRetrieverDTO>(this, getResources().getString(R.string.GetUnreadMessages), new ChatMessageRetrieverDTO(recipientId, findNDriveManager.getUser().UserId),
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
            new WCFServiceTask<ChatMessage>(this, getResources().getString(R.string.SendMessageURL), newMessage,
                    new TypeToken<ServiceResponse<Boolean>>() {}.getType(),
                    findNDriveManager.getAuthorisationHeaders(), new WCFServiceCallback<Boolean, Void>() {
                @Override
                public void onServiceCallCompleted(ServiceResponse<Boolean> serviceResponse, Void parameter) {
                    if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
                    {
                        messageEditText.setText("");
                        addNewMessage(newMessage);
                    }
                }
            }).execute();
        }
    }

    private void addNewMessage(ChatMessage m)
    {
        messages.add(m);
        adapter.notifyDataSetInvalidated();
        getListView().setSelection(messages.size()-1);
        this.progressBar.setVisibility(View.GONE);
    }

    private void retrieveMessages()
    {
        new WCFServiceTask<ChatMessageRetrieverDTO>(this, getResources().getString(R.string.GetMessagesURL), new ChatMessageRetrieverDTO(findNDriveManager.getUser().UserId, recipientId),
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

    private void messagesRetrieved(ArrayList<ChatMessage> retrievedMessages)
    {
        this.progressBar.setVisibility(View.GONE);
        ArrayList<ChatMessage> filtersMessages = retrievedMessages;

        for(ChatMessage chatMessage : this.messages)
        {
            for(ChatMessage chatMessage1 : retrievedMessages)
            {
                if(chatMessage1.ChatMessageId == chatMessage.ChatMessageId)
                {
                    filtersMessages.remove(chatMessage1);
                }
            }
        }
        this.messages.addAll(filtersMessages);
        adapter.notifyDataSetInvalidated();
        getListView().setSelection(messages.size()-1);
    }

    private final BroadcastReceiver GCMReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            ChatMessage chatMessage = gson.fromJson(intent.getExtras().getString(IntentConstants.MESSAGE), new TypeToken<ChatMessage>() {}.getType());

            for(ChatMessage message : messages)
            {
                if(message.ChatMessageId == chatMessage.ChatMessageId)
                {
                    return;
                }
            }
            if(findNDriveManager.getUser().UserId == chatMessage.RecipientId && chatMessage.SenderId == recipientId)
            {
                addNewMessage(chatMessage);
            }
            else
            {
                ohShitThisIsNotMyMessage(chatMessage);
            }

            this.abortBroadcast();
        }
    };

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