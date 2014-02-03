package com.example.myapplication.activities.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.myapplication.activities.activities.ContactDriverActivity;
import com.example.myapplication.activities.base.BaseFragment;
import com.example.myapplication.constants.IntentConstants;
import com.example.myapplication.domain_objects.Journey;
import com.example.myapplication.domain_objects.JourneyRequest;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.experimental.FindNDriveManager;
import com.example.myapplication.interfaces.FragmentClosed;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WCFServiceTask;
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
    private Journey carShare;

    private ImageButton minimiseButton;
    private ImageButton closeButton;
    private LinearLayout contentLayout;

    private TextView journeyHeader;

    FragmentClosed listener;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_journey_information, container, false);
        carShare = new Gson().fromJson(getArguments().getString(IntentConstants.JOURNEY), new TypeToken<Journey>() {
        }.getType());
        findNDriveManager = ((FindNDriveManager)getActivity().getApplicationContext());
        journeyHeader = (TextView) view.findViewById(R.id.FragmentJourneyInformationFromToTextView);
        journeyHeader.setText(carShare.GeoAddresses.get(0).AddressLine + " -> " + carShare.GeoAddresses.get(carShare.GeoAddresses.size()-1).AddressLine);
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
                new WCFServiceTask<Integer>(getActivity().getApplicationContext(), getResources().getString(R.string.GetRequestsForJourneyURL),
                        carShare.JourneyId,
                        new TypeToken<ServiceResponse<ArrayList<JourneyRequest>>>() {}.getType(),
                        findNDriveManager.getAuthorisationHeaders(), new WCFServiceCallback<ArrayList<JourneyRequest>, String>() {
                    @Override
                    public void onServiceCallCompleted(ServiceResponse<ArrayList<JourneyRequest>> serviceResponse, String parameter) {
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
        listener.onFragmentClosed();
    }


}
