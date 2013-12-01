package com.example.myapplication.Fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.myapplication.Activities.HomeActivity;
import com.example.myapplication.Constants.Constants;
import com.example.myapplication.DomainObjects.CarShare;
import com.example.myapplication.DomainObjects.ServiceResponse;
import com.example.myapplication.Experimental.MyCarSharesAdapter;
import com.example.myapplication.Helpers.UserHelper;
import com.example.myapplication.Interfaces.OnCarSharesRetrieved;
import com.example.myapplication.R;

import java.util.ArrayList;

/**
 * Created by Michal on 27/11/13.
 */

public class FragmentMyCarShares extends Fragment implements OnCarSharesRetrieved{

    public int currentUserId;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        //Bundle bundle = this.getArguments();
        //if(bundle != null){
        //    currentUserId = bundle.getInt("UserId");
        //
        //
        // }
        currentUserId = ((HomeActivity) this.getActivity()).lol();
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
        UserHelper.RetrieveMyCarShares(currentUserId, this);

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_car_shares, container, false);
    }

    @Override
    public void onCarSharesRetrieved(ServiceResponse<ArrayList<CarShare>> serviceResponse) {
        ListView listView = (ListView) getView().findViewById(R.id.MyCarSharesListView);

        if(serviceResponse.ServiceResponseCode == Constants.ServiceResponseSuccess)
        {
            Log.e("Current UserId", "" +currentUserId);
            CarShare carShares[] = serviceResponse.Result.toArray(new CarShare[serviceResponse.Result.size()]);
            MyCarSharesAdapter adapter = new MyCarSharesAdapter(currentUserId, this.getActivity(), R.layout.my_car_shares_custom_listview_row, carShares);
            listView.setAdapter(adapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Toast toast = Toast.makeText(getActivity(), "Item clicked!", Toast.LENGTH_LONG);
                    toast.show();
                }
            });


        }
        else
        {

        }
    }
}