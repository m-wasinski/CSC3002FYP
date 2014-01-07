package com.example.myapplication.Activities.Fragments;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.example.myapplication.Activities.Activities.ManageAsDriverActivity;
import com.example.myapplication.Activities.Activities.ManageAsPassengerActivity;
import com.example.myapplication.Adapters.MyCarSharesAdapter;
import com.example.myapplication.Activities.Base.BaseFragment;
import com.example.myapplication.Constants.ServiceResponseCode;
import com.example.myapplication.DomainObjects.CarShare;
import com.example.myapplication.DomainObjects.ServiceResponse;
import com.example.myapplication.DomainObjects.User;
import com.example.myapplication.Experimental.AppData;
import com.example.myapplication.Interfaces.WCFServiceCallback;
import com.example.myapplication.NetworkTasks.WCFServiceTask;
import com.example.myapplication.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by Michal on 27/11/13.
 */

public class MyCarSharesFragment extends BaseFragment implements WCFServiceCallback<ArrayList<CarShare>, String>  {

    private User user;
    private AppData appData;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        appData = ((AppData)getActivity().getApplicationContext());
        user = appData.getUser();

        return inflater.inflate(R.layout.fragment_my_car_shares, container, false);


    }

    @Override
    public void onResume() {
        super.onResume();
        new WCFServiceTask<Integer, ArrayList<CarShare>>("https://findndrive.no-ip.co.uk/Services/CarShareService.svc/getall", appData.getUser().UserId,
                new TypeToken<ServiceResponse<ArrayList<CarShare>>>() {}.getType(),
                appData.getAuthorisationHeaders(),null, this).execute();
    }

    @Override
    public void onServiceCallCompleted(final ServiceResponse<ArrayList<CarShare>> serviceResponse, String s) {
        super.checkIfAuthorised(serviceResponse.ServiceResponseCode);
        ListView mainListView = (ListView) getView().findViewById(R.id.MyCarSharesListView);
        TextView noJourneysTextView = (TextView) getActivity().findViewById(R.id.MyCarSharesNoJourneysTextView);
        if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
        {
            if(serviceResponse.Result.size() == 0)
            {
                noJourneysTextView.setVisibility(View.VISIBLE);
                mainListView.setVisibility(View.GONE);
            }
            else
            {
                noJourneysTextView.setVisibility(View.GONE);
                mainListView.setVisibility(View.VISIBLE);
            }

            MyCarSharesAdapter adapter = new MyCarSharesAdapter(user.UserId, this.getActivity(), R.layout.fragment_my_car_shares_listview_row, serviceResponse.Result);
            mainListView.setAdapter(adapter);

            mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    if (serviceResponse.Result.get(i).Driver.UserId == appData.getUser().UserId) {
                        Gson gson = new Gson();
                        Intent intent = new Intent(getActivity(), ManageAsDriverActivity.class);
                        intent.putExtra("CurrentCarShare", gson.toJson(serviceResponse.Result.get(i)));
                        //startActivity(intent);
                    } else {
                        Intent intent = new Intent(getActivity(), ManageAsPassengerActivity.class);
                        //startActivity(intent);
                    }
                }
            });
        }
    }
}