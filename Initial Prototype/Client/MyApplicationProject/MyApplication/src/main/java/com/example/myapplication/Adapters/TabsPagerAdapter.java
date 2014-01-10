package com.example.myapplication.Adapters;

/**
 * Created by Michal on 16/12/13.
 */

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.myapplication.Activities.Activities.SearchMapActivity;
import com.example.myapplication.Activities.Fragments.MyCarSharesFragment;
import com.example.myapplication.Activities.Fragments.MyRequestsFragment;
import com.example.myapplication.Activities.Fragments.SearchFragment;

public class TabsPagerAdapter extends FragmentPagerAdapter {

    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int index) {

        switch (index) {
            case 0:
                return new MyCarSharesFragment();
            case 1:
                return new SearchMapActivity();
            case 2:
                return new MyRequestsFragment();
        }

        return null;
    }

    @Override
    public int getCount() {
        // get item count - equal to number of tabs
        return 3;
    }

}