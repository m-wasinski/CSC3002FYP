package com.example.myapplication.Fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.myapplication.Activities.CarShareDetailsActivity;
import com.example.myapplication.Activities.HomeActivity;
import com.example.myapplication.Constants.Constants;
import com.example.myapplication.DomainObjects.CarShare;
import com.example.myapplication.DomainObjects.ServiceResponse;
import com.example.myapplication.Experimental.MyCarSharesAdapter;
import com.example.myapplication.Helpers.ServiceHelper;
import com.example.myapplication.Interfaces.OnCarSharesRetrieved;
import com.example.myapplication.R;
import com.google.gson.Gson;

import java.util.ArrayList;

/**
 * Created by Michal on 27/11/13.
 */

public class FragmentMyCarShares extends android.support.v4.app.Fragment implements OnCarSharesRetrieved{

    public int currentUserId;
    private ListView mainListView;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        //Bundle bundle = this.getArguments();
        //if(bundle != null){
        //    currentUserId = bundle.getInt("UserId");
        //
        //
        // }
        //currentUserId = ((HomeActivity) getActivity()).lol();
        currentUserId = 2;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle bundle = this.getArguments();
        if(bundle != null){
            currentUserId = bundle.getInt("UserId");
        }
        Toast toast = Toast.makeText(getActivity(), ""+currentUserId, Toast.LENGTH_LONG);
        toast.show();
        ServiceHelper.RetrieveMyCarShares(currentUserId, this);

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.my_car_shares_fragment, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        ServiceHelper.RetrieveMyCarShares(currentUserId, this);
    }

    @Override
    public void onCarSharesRetrieved(ServiceResponse<ArrayList<CarShare>> serviceResponse) {
        mainListView = (ListView) getView().findViewById(R.id.MyCarSharesListView);
        if(serviceResponse.ServiceResponseCode == Constants.ServiceResponseSuccess)
        {
            Log.e("Current UserId", "" +currentUserId);
            final CarShare carShares[] = serviceResponse.Result.toArray(new CarShare[serviceResponse.Result.size()]);
            MyCarSharesAdapter adapter = new MyCarSharesAdapter(currentUserId, this.getActivity(), R.layout.my_car_shares_fragment_custom_listview_row_layout, carShares);
            mainListView.setAdapter(adapter);

            mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Intent intent = new Intent(view.getContext(), CarShareDetailsActivity.class);
                    Gson gson = new Gson();
                    intent.putExtra("CurrentCarShare", gson.toJson(carShares[i]));
                    startActivity(intent);
                }
            });


        }
        else
        {

        }
    }
}