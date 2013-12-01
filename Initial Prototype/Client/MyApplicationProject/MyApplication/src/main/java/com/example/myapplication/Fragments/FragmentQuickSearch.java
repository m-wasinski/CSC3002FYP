package com.example.myapplication.Fragments;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.myapplication.Helpers.UserHelper;
import com.example.myapplication.R;

/**
 * Created by Michal on 30/11/13.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class FragmentQuickSearch extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.quick_search_fragment, container, false);
    }
}
