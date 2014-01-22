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
import android.widget.Button;
import android.widget.EditText;

import com.example.myapplication.activities.base.BaseListActivity;
import com.example.myapplication.adapters.ChatAdapter;
import com.example.myapplication.constants.GcmConstants;
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.dtos.ChatMessage;
import com.example.myapplication.dtos.ServiceResponse;
import com.example.myapplication.dtos.ChatMessageRetrieverDTO;
import com.example.myapplication.R;
import com.example.myapplication.experimental.DateTimeHelper;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WCFServiceTask;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

/**
 * Created by Michal on 04/01/14.
 */
public class InstantMessengerActivity extends BaseListActivity {
    /** Called when the activity is first created. */

    private ArrayList<ChatMessage> messages;
    private ChatAdapter adapter;
    private EditText text;
    private Button sendButton;
    private int recipientId;
    private String recipientUserName;

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(GcmConstants.PROPERTY_FORWARD_MESSAGE);
        registerReceiver(GCMReceiver, intentFilter);
        appData.setCurrentlyVisibleActivity(this.getClass().getSimpleName());
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(GCMReceiver);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.instant_messenger_activity);
        Bundle extras = getIntent().getExtras();
        this.recipientId = extras.getInt("RecipientId");
        this.recipientUserName = extras.getString("RecipientUsername");
        this.actionBar.setTitle(this.recipientUserName);
        sendButton = (Button) findViewById(R.id.InstantMessengerActivityButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
        text = (EditText) this.findViewById(R.id.text);
        messages = new ArrayList<ChatMessage>();

        adapter = new ChatAdapter(this, messages, appData.getUser().UserId);
        setListAdapter(adapter);
        retrieveMessages();
    }
    private void sendMessage()
    {
        addNewMessage(new ChatMessage(appData.getUser().UserId, recipientId, text.getText().toString(),
                DateTimeHelper.convertToWCFDate(Calendar.getInstance().getTime()), false, recipientUserName, appData.getUser().UserName));
        String newMessage = text.getText().toString();
        if(newMessage.length() > 0)
        {
            new WCFServiceTask<ChatMessage>(getResources().getString(R.string.SendMessageURL), new ChatMessage(appData.getUser().UserId, recipientId, newMessage,
                    DateTimeHelper.convertToWCFDate(Calendar.getInstance().getTime()), false, recipientUserName, appData.getUser().UserName),
                    new TypeToken<ServiceResponse<Boolean>>() {}.getType(),
                    appData.getAuthorisationHeaders(), new WCFServiceCallback<Boolean, Void>() {
                @Override
                public void onServiceCallCompleted(ServiceResponse<Boolean> serviceResponse, Void parameter) {
                    if(serviceResponse.ServiceResponseCode == ServiceResponseCode.UNAUTHORISED)
                    {

                    }
                }
            }).execute();
        }
    }

    private void markMessagesAsRead(ArrayList<ChatMessage> unreadMessages)
    {
        new WCFServiceTask<ArrayList<ChatMessage>>(getResources().getString(R.string.MarkMessagesAsReadURL), unreadMessages,
                new TypeToken<ServiceResponse<Boolean>>() {}.getType(),
                appData.getAuthorisationHeaders(), new WCFServiceCallback<Boolean, Void>() {
            @Override
            public void onServiceCallCompleted(ServiceResponse<Boolean> serviceResponse, Void parameter) {
                checkIfAuthorised(serviceResponse.ServiceResponseCode);
            }
        }).execute();
    }

    private void addNewMessage(ChatMessage m)
    {
        messages.add(m);
        adapter.notifyDataSetInvalidated();
        getListView().setSelection(messages.size()-1);
    }

    private void retrieveMessages()
    {
        new WCFServiceTask<ChatMessageRetrieverDTO>(getResources().getString(R.string.GetMessagesURL), new ChatMessageRetrieverDTO(appData.getUser().UserId, recipientId),
                new TypeToken<ServiceResponse<ArrayList<ChatMessage>>>() {}.getType(),
                appData.getAuthorisationHeaders(), new WCFServiceCallback<ArrayList<ChatMessage>, Void>() {
            @Override
            public void onServiceCallCompleted(ServiceResponse<ArrayList<ChatMessage>> serviceResponse, Void parameter) {
                if(serviceResponse.ServiceResponseCode == ServiceResponseCode.UNAUTHORISED)
                {

                }
                if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
                {
                    messages.addAll(serviceResponse.Result);
                    adapter.notifyDataSetInvalidated();
                    getListView().setSelection(messages.size()-1);

                    ArrayList<ChatMessage> unreadMessages = new ArrayList<ChatMessage>();

                    for(ChatMessage chatMessage : serviceResponse.Result)
                    {
                        if(!chatMessage.Read)
                        {
                            unreadMessages.add(chatMessage);
                        }
                    }
                    markMessagesAsRead(unreadMessages);

                }

            }
        }).execute();
    }

    private final BroadcastReceiver GCMReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ChatMessage chatMessage = gson.fromJson(intent.getExtras().getString("message"), new TypeToken<ChatMessage>() {}.getType());

            for(ChatMessage message : messages)
            {
                if(message.ChatMessageId == chatMessage.ChatMessageId)
                {
                    return;
                }
            }
            if(appData.getUser().UserId == chatMessage.RecipientId && chatMessage.SenderId == recipientId)
            {
                markMessagesAsRead(new ArrayList<ChatMessage>(Arrays.asList(chatMessage)));
                addNewMessage(chatMessage);
            }
            else
            {
                ohShitThisIsNotMyMessage(chatMessage);
            }
        }
    };

    private void ohShitThisIsNotMyMessage(ChatMessage chatMessage)
    {
        NotificationManager mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        int id =  (int) System.currentTimeMillis();

        Bundle extras = new Bundle();
        extras.putString("RecipientUsername", chatMessage.SenderUserName);
        extras.putInt("RecipientId", chatMessage.SenderId);

        PendingIntent contentIntent = PendingIntent.getActivity(this, id,
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

        mNotificationManager.notify(id, notification);
    }

    private void checkIfStillLoggedIn(int serviceResponseCode)
    {
       super.checkIfAuthorised(serviceResponseCode);
    }
}