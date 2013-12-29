package com.example.myapplication.Fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.myapplication.Activities.CarShareDetailsActivity;
import com.example.myapplication.Activities.HomeActivity;
import com.example.myapplication.Adapters.CarSharesListViewAdapter;
import com.example.myapplication.Constants.Constants;
import com.example.myapplication.DomainObjects.CarShare;
import com.example.myapplication.DomainObjects.ServiceResponse;
import com.example.myapplication.DomainObjects.User;
import com.example.myapplication.Helpers.ServiceHelper;
import com.example.myapplication.Interfaces.OnCarSharesRetrieved;
import com.example.myapplication.R;
import com.google.gson.Gson;

import java.util.ArrayList;

/**
 * Created by Michal on 27/11/13.
 */

public class MyCarSharesFragment extends android.support.v4.app.Fragment implements OnCarSharesRetrieved{

    private User currentUser;
    private ListView mainListView;
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        currentUser = ((HomeActivity) getActivity()).GetCurrentUser();
        ServiceHelper.RetrieveMyCarShares(currentUser.UserId, this);

        return inflater.inflate(R.layout.my_car_shares_fragment, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        ServiceHelper.RetrieveMyCarShares(currentUser.UserId, this);
    }

    @Override
    public void onCarSharesRetrieved(final ServiceResponse<ArrayList<CarShare>> serviceResponse) {
        mainListView = (ListView) getView().findViewById(R.id.MyCarSharesListView);
        if(serviceResponse.ServiceResponseCode == Constants.ServiceResponseSuccess)
        {
            CarSharesListViewAdapter adapter = new CarSharesListViewAdapter(currentUser.UserId, this.getActivity(), R.layout.my_car_shares_fragment_custom_listview_row_layout, serviceResponse.Result);
            mainListView.setAdapter(adapter);

            mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Intent intent = new Intent(view.getContext(), CarShareDetailsActivity.class);
                    Gson gson = new Gson();
                    intent.putExtra("CurrentCarShare", gson.toJson(serviceResponse.Result.get(i)));
                    startActivity(intent);
                }
            });
        }
    }
}