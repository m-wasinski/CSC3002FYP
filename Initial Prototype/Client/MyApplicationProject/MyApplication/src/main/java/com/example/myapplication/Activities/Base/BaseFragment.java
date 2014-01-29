package com.example.myapplication.activities.base;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.myapplication.activities.activities.LoginActivity;
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.experimental.FindNDriveManager;
import com.example.myapplication.R;

/**
 * Created by Michal on 05/01/14.
 */
public class BaseFragment extends Fragment {
    protected FindNDriveManager findNDriveManager;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        findNDriveManager = ((FindNDriveManager)getActivity().getApplicationContext());
        return inflater.inflate(R.layout.activity_my_journeys, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
