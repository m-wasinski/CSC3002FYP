package com.example.myapplication.Experimental;

/**
 * Created by Michal on 16/12/13.
 */

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.myapplication.Fragments.FragmentMyCarShares;
import com.example.myapplication.Fragments.FragmentSearch;

public class TabsPagerAdapter extends FragmentPagerAdapter {

    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int index) {

        switch (index) {
            case 0:
                // Top Rated fragment activity
                return new FragmentMyCarShares();
            case 1:
                // Games fragment activity
                return new FragmentSearch();
            case 2:
                // Movies fragment activity
                return new FragmentMyCarShares();
        }

        return null;
    }

    @Override
    public int getCount() {
        // get item count - equal to number of tabs
        return 3;
    }

}