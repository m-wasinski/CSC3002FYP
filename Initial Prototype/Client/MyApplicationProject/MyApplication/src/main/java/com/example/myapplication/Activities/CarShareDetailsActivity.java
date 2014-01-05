package com.example.myapplication.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.example.myapplication.DomainObjects.CarShare;
import com.example.myapplication.Experimental.AppData;
import com.example.myapplication.Helpers.Helpers;
import com.example.myapplication.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

/**
 * Created by Michal on 01/12/13.
 */
public class CarShareDetailsActivity extends Activity {

    private CarShare carShare;
    private Button contactDriverButton;
    private AppData appData;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        Gson gson = new Gson();
        Type carShareType = new TypeToken<CarShare>() {}.getType();
        carShare = gson.fromJson(getIntent().getExtras().getString("CurrentCarShare"), carShareType);
        setContentView(R.layout.car_share_details);

        fillDetails();

        contactDriverButton = (Button) findViewById(R.id.CarShareDetailsContactDriverButton);
        contactDriverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Gson gson = new Gson();
                Intent intent = new Intent(getBaseContext(), ContactDriverActivity.class);
                intent.putExtra("CurrentCarShare", gson.toJson(carShare));
                startActivity(intent);
            }
        });
    }

    private void fillDetails()
    {
        TextView departureCity = (TextView) findViewById(R.id.CarShareDetailsDepartureCity);
        departureCity.setText(carShare.DepartureCity);

        TextView destinationCity = (TextView) findViewById(R.id.CarShareDetailsDestinationCity);
        destinationCity.setText(carShare.DestinationCity);

        TextView date = (TextView) findViewById(R.id.CarShareDetailsDepartureDate);
        date.setText(carShare.DateOfDepartureAsString());

        TextView time = (TextView) findViewById(R.id.CarShareDetailsDepartureTime);
        time.setText(carShare.TimeOfDepartureAsString());

        TextView smokers = (TextView) findViewById(R.id.CarShareDetailsSmokers);
        smokers.setText(Helpers.TranslateBoolean(carShare.SmokersAllowed));

        TextView womenOnly = (TextView) findViewById(R.id.CarShareDetailsWomenOnly);
        womenOnly.setText(Helpers.TranslateBoolean(carShare.WomenOnly));

        TextView availableSeats = (TextView) findViewById(R.id.CarShareDetailsAvailableSeats);
        availableSeats.setText(""+carShare.AvailableSeats);

        TextView description = (TextView) findViewById(R.id.CarShareDetailsDescription);
        description.setText(carShare.Description);

        TextView fee = (TextView) findViewById(R.id.CarShareDetailsFee);
        fee.setText("£"+carShare.Fee);

        TextView pets = (TextView) findViewById(R.id.CarShareDetailsPets);
        pets.setText(Helpers.TranslateBoolean(carShare.PetsAllowed));
    }
}
