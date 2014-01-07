package com.example.myapplication.Activities.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.example.myapplication.DomainObjects.CarShare;
import com.example.myapplication.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

/**
 * Created by Michal on 02/01/14.
 */
public class ManageAsDriverActivity extends Activity {

    private Button showDetailsButton;
    private Button showOnMapButton;
    private Button makeAChangeButton;
    private Button showPassengersButton;
    private Button cancelJourneyButton;
    private Button showRequestsButton;
    private CarShare carShare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_as_driver);

        Gson gson = new Gson();
        Type carShareType = new TypeToken<CarShare>() {}.getType();
        carShare = gson.fromJson(getIntent().getExtras().getString("CurrentCarShare"), carShareType);

        showDetailsButton = (Button) findViewById(R.id.ManageAsDriverShowDetailsButton);
        showOnMapButton = (Button) findViewById(R.id.ManageAsDriverShowOnMapButton);
        makeAChangeButton = (Button) findViewById(R.id.ManageAsDriverMakeChangeButton);
        showPassengersButton = (Button) findViewById(R.id.ManageAsDriverShowPassengersButton);
        cancelJourneyButton = (Button) findViewById(R.id.ManageAsDriverCancelJourneyButton);
        showRequestsButton = (Button) findViewById(R.id.ManageAsDriverShowRequestsButton);

        setupUIEvents();
    }

    private void setupUIEvents()
    {
        showRequestsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Gson gson = new Gson();
                Intent intent = new Intent(getBaseContext(), CarShareRequestsActivity.class);
                intent.putExtra("CurrentCarShare", gson.toJson(carShare));
                startActivity(intent);
            }
        });
    }
}
