package com.example.myapplication.activities.activities;

import android.app.ActionBar;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.myapplication.activities.base.BaseActivity;
import com.example.myapplication.activities.fragments.MessagesFragment;
import com.example.myapplication.dtos.Journey;
import com.example.myapplication.dtos.JourneyMessageDTO;
import com.example.myapplication.dtos.ServiceResponse;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WCFServiceTask;
import com.example.myapplication.R;
import com.google.gson.reflect.TypeToken;

/**
 * Created by Michal on 07/01/14.
 */
public class JourneyChatActivity extends BaseActivity implements MessagesFragment.OnFragmentInteractionListener {

    private Journey carShare;
    private Button sendButton;
    private EditText messageEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.instant_messenger_activity);

        carShare = gson.fromJson(getIntent().getExtras().getString("CurrentCarShare"), new TypeToken<Journey>() {}.getType());
        //messageEditText = (EditText) findViewById(R.id.InstantMessengerMessageEditText);
        //sendButton = (Button) findViewById(R.id.InstantMessengerSendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                send(messageEditText.getText().toString());
            }
        });
        ActionBar actionBar = getActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public String getProfileEmail() {
        return null;
    }

    private void send(String message)
    {
        JourneyMessageDTO journeyMessageDTO = new JourneyMessageDTO();
        journeyMessageDTO.JourneyId = carShare.JourneyId;
        journeyMessageDTO.MessageBody = message;

        new WCFServiceTask<JourneyMessageDTO>("https://findndrive.no-ip.co.uk/Services/CarShareService.svc/sendmessage", journeyMessageDTO,
                new TypeToken<ServiceResponse<Journey>>() {}.getType(),
                appData.getAuthorisationHeaders(), new WCFServiceCallback<Journey, String>() {
            @Override
            public void onServiceCallCompleted(ServiceResponse<Journey> serviceResponse, String parameter) {

            }
        }).execute();

    }
}
