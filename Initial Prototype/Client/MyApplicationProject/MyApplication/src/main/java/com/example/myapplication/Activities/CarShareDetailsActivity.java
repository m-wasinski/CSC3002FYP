package com.example.myapplication.Activities;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.myapplication.DomainObjects.CarShare;
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

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);

        Gson gson = new Gson();

        Type userType = new TypeToken<CarShare>() {}.getType();
        carShare = gson.fromJson(getIntent().getExtras().getString("CurrentCarShare"), userType);
        setContentView(R.layout.car_share_details);

        FillDetails();
    }

    private void FillDetails()
    {
        TextView departureCity = (TextView) findViewById(R.id.CarShareDetailsDepartureCity);
        departureCity.setText(carShare.DepartureCity);

        TextView destinationCity = (TextView) findViewById(R.id.CarShareDetailsDestinationCity);
        destinationCity.setText(carShare.DestinationCity);

        TextView date = (TextView) findViewById(R.id.CarShareDetailsDepartureDate);
        date.setText(carShare.DateOfDeparture);

        TextView time = (TextView) findViewById(R.id.CarShareDetailsDepartureTime);
        time.setText(carShare.DateOfDepartureAsString());

        TextView smokers = (TextView) findViewById(R.id.CarShareDetailsSmokers);
        smokers.setText(Helpers.TranslateBoolean(carShare.SmokersAllowed));

        TextView womenOnly = (TextView) findViewById(R.id.CarShareDetailsWomenOnly);
        womenOnly.setText(Helpers.TranslateBoolean(carShare.WomenOnly));

        TextView availableSeats = (TextView) findViewById(R.id.CarShareDetailsAvailableSeats);
        availableSeats.setText(""+carShare.AvailableSeats);

        TextView description = (TextView) findViewById(R.id.CarShareDetailsDescription);
        description.setText(carShare.Description);
    }
}
