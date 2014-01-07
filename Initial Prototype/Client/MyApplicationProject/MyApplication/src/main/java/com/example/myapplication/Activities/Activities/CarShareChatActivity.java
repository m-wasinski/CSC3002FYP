package com.example.myapplication.Activities.Activities;

import android.app.ActionBar;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.myapplication.Activities.Base.BaseActivity;
import com.example.myapplication.Activities.Fragments.MessagesFragment;
import com.example.myapplication.DomainObjects.CarShare;
import com.example.myapplication.DomainObjects.CarShareMessageDTO;
import com.example.myapplication.DomainObjects.CarShareRequest;
import com.example.myapplication.DomainObjects.ServiceResponse;
import com.example.myapplication.Interfaces.WCFServiceCallback;
import com.example.myapplication.NetworkTasks.WCFServiceTask;
import com.example.myapplication.R;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by Michal on 07/01/14.
 */
public class CarShareChatActivity extends BaseActivity implements MessagesFragment.OnFragmentInteractionListener {

    private CarShare carShare;
    private Button sendButton;
    private EditText messageEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.instant_messenger_activity);

        carShare = gson.fromJson(getIntent().getExtras().getString("CurrentCarShare"), new TypeToken<CarShare>() {}.getType());
        messageEditText = (EditText) findViewById(R.id.InstantMessengerMessageEditText);
        sendButton = (Button) findViewById(R.id.InstantMessengerSendButton);
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
        CarShareMessageDTO carShareMessageDTO = new CarShareMessageDTO();
        carShareMessageDTO.CarShareId = carShare.CarShareId;
        carShareMessageDTO.MessageBody = message;

        new WCFServiceTask<CarShareMessageDTO, CarShare>("https://findndrive.no-ip.co.uk/Services/CarShareService.svc/sendmessage", carShareMessageDTO,
                new TypeToken<ServiceResponse<CarShare>>() {}.getType(),
                appData.getAuthorisationHeaders(),null, new WCFServiceCallback<CarShare, String>() {
            @Override
            public void onServiceCallCompleted(ServiceResponse<CarShare> serviceResponse, String parameter) {

            }
        }).execute();

    }
}
