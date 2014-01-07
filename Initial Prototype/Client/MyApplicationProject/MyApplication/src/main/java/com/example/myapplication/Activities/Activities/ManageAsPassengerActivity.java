package com.example.myapplication.Activities.Activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;

import com.example.myapplication.R;

/**
 * Created by Michal on 02/01/14.
 */
public class ManageAsPassengerActivity extends Activity {

    private Button showDetailsButton;
    private Button showOnMapButton;
    private Button showPassengersButton;
    private Button withdrawFromJourneyButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_as_passenger);

        showDetailsButton = (Button) findViewById(R.id.ManageAsDriverShowDetailsButton);
        showOnMapButton = (Button) findViewById(R.id.ManageAsDriverShowOnMapButton);
        showPassengersButton = (Button) findViewById(R.id.ManageAsDriverShowPassengersButton);
        withdrawFromJourneyButton = (Button) findViewById(R.id.ManageAsDriverCancelJourneyButton);
    }
}
