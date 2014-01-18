package com.example.myapplication.activities.base;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Window;

import com.example.myapplication.experimental.AppData;

/**
 * Created by Michal on 05/01/14.
 */
public class BaseActionBarActivity extends ActionBarActivity {

    protected final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    protected final String TAG = this.getClass().getSimpleName();
    protected AppData appData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        appData = ((AppData)getApplication());
    }
}
