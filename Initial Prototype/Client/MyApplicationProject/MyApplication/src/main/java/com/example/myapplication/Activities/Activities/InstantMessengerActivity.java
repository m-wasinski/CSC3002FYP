package com.example.myapplication.activities.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.myapplication.activities.base.BaseListActivity;
import com.example.myapplication.adapters.ChatAdapter;
import com.example.myapplication.constants.GcmConstants;
import com.example.myapplication.domain_objects.ChatMessage;
import com.example.myapplication.domain_objects.JourneyRequest;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.domain_objects.User;
import com.example.myapplication.dtos.ChatMessageRetrieverDTO;
import com.example.myapplication.experimental.AppData;
import com.example.myapplication.activities.fragments.MessagesFragment;
import com.example.myapplication.R;
import com.example.myapplication.experimental.DateTimeHelper;
import com.example.myapplication.experimental.WakeLocker;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WCFServiceTask;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

/**
 * Created by Michal on 04/01/14.
 */
public class InstantMessengerActivity extends BaseListActivity {
    /** Called when the activity is first created. */

    ArrayList<ChatMessage> messages;
    ChatAdapter adapter;
    EditText text;
    private Button sendButton;
    private User recipient;

    @Override
    protected void onResume() {
        super.onResume();
        markMessagesAsRead();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.instant_messenger_activity);
        this.recipient = gson.fromJson(getIntent().getExtras().getString("Recipient"), new TypeToken<User>() {}.getType());
        sendButton = (Button) findViewById(R.id.InstantMessengerActivityButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
        text = (EditText) this.findViewById(R.id.text);
        messages = new ArrayList<ChatMessage>();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(GcmConstants.PROPERTY_FORWARD_MESSAGE);
        registerReceiver(GCMReceiver, intentFilter);
        adapter = new ChatAdapter(this, messages, appData.getUser().UserId);
        setListAdapter(adapter);
        retrieveMessages();
    }
    private void sendMessage()
    {
        addNewMessage(new ChatMessage(appData.getUser().UserId, recipient.UserId, text.getText().toString(),
                DateTimeHelper.convertToWCFDate(Calendar.getInstance().getTime()), false, recipient.UserName, appData.getUser().UserName));
        String newMessage = text.getText().toString();
        if(newMessage.length() > 0)
        {
            new WCFServiceTask<ChatMessage>(getResources().getString(R.string.SendMessageURL), new ChatMessage(appData.getUser().UserId, recipient.UserId, newMessage,
                    DateTimeHelper.convertToWCFDate(Calendar.getInstance().getTime()), false, recipient.UserName, appData.getUser().UserName),
                    new TypeToken<ServiceResponse<Boolean>>() {}.getType(),
                    appData.getAuthorisationHeaders(), new WCFServiceCallback<Boolean, Void>() {
                @Override
                public void onServiceCallCompleted(ServiceResponse<Boolean> serviceResponse, Void parameter) {
                }
            }).execute();
        }
    }

    private void markMessagesAsRead()
    {
        new WCFServiceTask<ChatMessageRetrieverDTO>(getResources().getString(R.string.MarkMessagesAsReadURL), new ChatMessageRetrieverDTO(recipient.UserId, appData.getUser().UserId),
                new TypeToken<ServiceResponse<Boolean>>() {}.getType(),
                appData.getAuthorisationHeaders(), new WCFServiceCallback<Boolean, Void>() {
            @Override
            public void onServiceCallCompleted(ServiceResponse<Boolean> serviceResponse, Void parameter) {
            }
        }).execute();
    }

    private void addNewMessage(ChatMessage m)
    {
        messages.add(m);
        adapter.notifyDataSetInvalidated();
        getListView().setSelection(messages.size()-1);
        markMessagesAsRead();
    }

    private void retrieveMessages()
    {
        new WCFServiceTask<ChatMessageRetrieverDTO>(getResources().getString(R.string.GetMessagesURL), new ChatMessageRetrieverDTO(appData.getUser().UserId, recipient.UserId),
                new TypeToken<ServiceResponse<ArrayList<ChatMessage>>>() {}.getType(),
                appData.getAuthorisationHeaders(), new WCFServiceCallback<ArrayList<ChatMessage>, Void>() {
            @Override
            public void onServiceCallCompleted(ServiceResponse<ArrayList<ChatMessage>> serviceResponse, Void parameter) {
                messages.addAll(serviceResponse.Result);
                adapter.notifyDataSetInvalidated();
                getListView().setSelection(messages.size()-1);
            }
        }).execute();
    }

    private final BroadcastReceiver GCMReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ChatMessage chatMessage = gson.fromJson(intent.getExtras().getString("message"), new TypeToken<ChatMessage>() {}.getType());
            Log.e(""+appData.getUser().UserId,""+chatMessage.RecipientId);
            if(appData.getUser().UserId == chatMessage.RecipientId)
            {
                addNewMessage(chatMessage);
            }
        }
    };
}