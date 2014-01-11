package com.example.myapplication.Activities.Base;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.myapplication.Activities.Activities.LoginActivity;
import com.example.myapplication.Constants.ServiceResponseCode;
import com.example.myapplication.Experimental.AppData;
import com.example.myapplication.R;

/**
 * Created by Michal on 05/01/14.
 */
public class BaseFragment extends Fragment {
    protected AppData appData;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        appData = ((AppData)getActivity().getApplicationContext());
        return inflater.inflate(R.layout.activity_home, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    protected void checkIfAuthorised(int serviceResponseCode) {

        if(serviceResponseCode == ServiceResponseCode.UNAUTHORISED)
        {
            Intent intent = new Intent(getActivity().getApplicationContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            getActivity().finish();
            startActivity(intent);
            Toast toast = Toast.makeText(getActivity().getApplicationContext(), "Your session has expired, you must log in again.", Toast.LENGTH_LONG);
            toast.show();
        }
    }
}
