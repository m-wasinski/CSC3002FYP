package com.example.myapplication.Fragments;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.myapplication.Activities.ManageAsDriverActivity;
import com.example.myapplication.Activities.ManageAsPassengerActivity;
import com.example.myapplication.Adapters.CarShareAdapter;
import com.example.myapplication.Constants.Constants;
import com.example.myapplication.DomainObjects.CarShare;
import com.example.myapplication.DomainObjects.ServiceResponse;
import com.example.myapplication.DomainObjects.User;
import com.example.myapplication.Experimental.AppData;
import com.example.myapplication.Interfaces.OnCarSharesRetrieved;
import com.example.myapplication.NetworkTasks.MyCarSharesRetriever;
import com.example.myapplication.R;
import com.google.gson.Gson;

import java.util.ArrayList;

/**
 * Created by Michal on 27/11/13.
 */

public class MyCarSharesFragment extends android.support.v4.app.Fragment implements OnCarSharesRetrieved{

    private User user;
    private ListView mainListView;
    private AppData appData;
    private MyCarSharesRetriever myCarSharesRetriever;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        appData = ((AppData)getActivity().getApplicationContext());
        user = appData.getUser();

        return inflater.inflate(R.layout.my_car_shares_fragment, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        myCarSharesRetriever = new MyCarSharesRetriever(user.UserId, this);
        myCarSharesRetriever.execute();
    }

    @Override
    public void onCarSharesRetrieved(final ServiceResponse<ArrayList<CarShare>> serviceResponse) {
        mainListView = (ListView) getView().findViewById(R.id.MyCarSharesListView);
        if(serviceResponse.ServiceResponseCode == Constants.SERVICE_RESPONSE_SUCCESS)
        {
            CarShareAdapter adapter = new CarShareAdapter(user.UserId, this.getActivity(), R.layout.my_car_shares_fragment_custom_listview_row_layout, serviceResponse.Result);
            mainListView.setAdapter(adapter);

            mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    if(serviceResponse.Result.get(i).Driver.UserId == appData.getUser().UserId)
                    {
                        Gson gson = new Gson();
                        Intent intent = new Intent(getActivity(), ManageAsDriverActivity.class);
                        intent.putExtra("CurrentCarShare", gson.toJson(serviceResponse.Result.get(i)));
                        startActivity(intent);
                    }
                    else
                    {
                        Intent intent = new Intent(getActivity(), ManageAsPassengerActivity.class);
                        startActivity(intent);                    }
                }
            });
        }
    }
}