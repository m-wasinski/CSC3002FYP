package com.example.myapplication.Activities.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.Activities.Activities.ContactDriverActivity;
import com.example.myapplication.Activities.Base.BaseFragment;
import com.example.myapplication.DomainObjects.CarShare;
import com.example.myapplication.DomainObjects.CarShareRequest;
import com.example.myapplication.DomainObjects.ServiceResponse;
import com.example.myapplication.Experimental.AppData;
import com.example.myapplication.Interfaces.FragmentClosed;
import com.example.myapplication.Interfaces.WCFServiceCallback;
import com.example.myapplication.NetworkTasks.WCFServiceTask;
import com.example.myapplication.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

/**
 * Created by Michal on 10/01/14.
 */
public class JourneyDetailsFragment extends BaseFragment {

    //Member variables
    private Button contactDriverButton;
    private CarShare carShare;

    private ImageButton minimiseButton;
    private ImageButton closeButton;
    private LinearLayout contentLayout;

    private TextView journeyHeader;

    FragmentClosed listener;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_journey_information, container, false);
        carShare = new Gson().fromJson(getArguments().getString("CurrentCarShare"), new TypeToken<CarShare>() {
        }.getType());
        appData = ((AppData)getActivity().getApplicationContext());
        journeyHeader = (TextView) view.findViewById(R.id.FragmentJourneyInformationFromToTextView);
        journeyHeader.setText(carShare.DepartureAddress.AddressLine + " -> " + carShare.DestinationAddress.AddressLine);
        contentLayout = (LinearLayout) view.findViewById(R.id.FragmentJourneyInformationContentLayout);

        closeButton = (ImageButton) view.findViewById(R.id.FragmentJourneyInformationCloseButton);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        minimiseButton = (ImageButton) view.findViewById(R.id.FragmentJourneyInformationMinimizeButton);
        minimiseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                contentLayout.setVisibility(contentLayout.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            }
        });

        contactDriverButton = (Button) view.findViewById(R.id.FragmentJourneyInformationContactDriverButton);
        contactDriverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new WCFServiceTask<Integer, CarShareRequest>("https://findndrive.no-ip.co.uk/Services/RequestService.svc/getrequests",
                        carShare.CarShareId,
                        new TypeToken<ServiceResponse<ArrayList<CarShareRequest>>>() {}.getType(),
                        appData.getAuthorisationHeaders(),null, new WCFServiceCallback<ArrayList<CarShareRequest>, String>() {
                    @Override
                    public void onServiceCallCompleted(ServiceResponse<ArrayList<CarShareRequest>> serviceResponse, String parameter) {
                        Gson gson = new Gson();
                        Intent intent = new Intent(getActivity(), ContactDriverActivity.class);
                        intent.putExtra("CurrentCarShare", gson.toJson(carShare));
                        intent.putExtra("CurrentRequests", gson.toJson(serviceResponse.Result));
                        startActivity(intent);
                    }
                }).execute();
            }
        });
        return view;
    }

    public void setOnCloseListener(FragmentClosed fragmentClosedInterface)
    {
        listener = fragmentClosedInterface;
    }

    private void dismiss()
    {
        getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
        listener.onFragmentClosed();
    }


}
