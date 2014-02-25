package com.example.myapplication.activities.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.activities.base.BaseActivity;
import com.example.myapplication.constants.IntentConstants;
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.domain_objects.Journey;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.dtos.RatingDTO;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WcfPostServiceTask;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

/**
 * Created by Michal on 21/02/14.
 */
public class RateDriverActivity extends BaseActivity implements WCFServiceCallback<Boolean, Void>
{

    private Button sendFeedbackButton;

    private EditText feedbackEditText;

    private TextView messageTextView;

    private ArrayList<ButtonHolder> starButtons = new ArrayList<ButtonHolder>(5);

    private int rating = 0;

    private Journey journey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_rate_driver);

        // Initialise local variables.
        this.journey = gson.fromJson(getIntent().getStringExtra(IntentConstants.JOURNEY), new TypeToken<Journey>() {}.getType());

        // Initialise UI elements.
        this.sendFeedbackButton = (Button) this.findViewById(R.id.RateDriverActivityRateButton);

        this.feedbackEditText = (EditText) this.findViewById(R.id.RateDriverActivityFeedbackEditText);

        this.messageTextView = (TextView) this.findViewById(R.id.RateDriverActivityMessageTextView);

        this.starButtons.add(0, new ButtonHolder((Button) this.findViewById(R.id.RateDriverActivityStarOneButton), 1));
        this.starButtons.add(1, new ButtonHolder((Button) this.findViewById(R.id.RateDriverActivityStarTwoButton), 2));
        this.starButtons.add(2, new ButtonHolder((Button) this.findViewById(R.id.RateDriverActivityStarThreeButton), 3));
        this.starButtons.add(3, new ButtonHolder((Button) this.findViewById(R.id.RateDriverActivityStarFourButton), 4));
        this.starButtons.add(4, new ButtonHolder((Button) this.findViewById(R.id.RateDriverActivityStarFiveButton), 5));

        // Setup all event handlers.
        this.setupEventHandlers();
    }

    private void setupEventHandlers()
    {
        for(final ButtonHolder buttonHolder : starButtons)
        {
            buttonHolder.button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setRating(buttonHolder.rating);
                }
            });
        }

        this.sendFeedbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendFeedback();
            }
        });
    }

    private void setRating(int index)
    {
        this.rating = index;

        Log.i("Rating Driver: ", "Current rating is: " + this.rating);

        for(int i = 0; i < this.starButtons.size(); i++)
        {
            this.starButtons.get(i).button.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(index > i ? R.drawable.rating : R.drawable.no_rating), null, null);
        }
    }

    @Override
    public void onServiceCallCompleted(ServiceResponse<Boolean> serviceResponse, Void parameter)
    {
        if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
        {
            Toast.makeText(this, "Rating submitted successfully!", Toast.LENGTH_LONG).show();
            this.finish();
        }

    }

    private void sendFeedback()
    {
        String message = this.feedbackEditText.getText().toString();

        this.feedbackEditText.setError(message.isEmpty() ? "Please enter feedback" : null);
        this.messageTextView.setError(this.rating == 0 ? "Please select rating" : null);

        if(message.isEmpty() || this.rating == 0)
        {
            return;
        }

        new WcfPostServiceTask<RatingDTO>(this, getResources().getString(R.string.RateDriverURL),
                new RatingDTO(this.journey.Driver.getUserId(), this.rating,
                        this.appManager.getUser().getUserId(), message),
                new TypeToken<ServiceResponse<Boolean>>(){}.getType(), this.appManager.getAuthorisationHeaders(), this).execute();
    }

    private class ButtonHolder
    {
        Button button;
        int rating;

        private ButtonHolder(Button button, int rating) {
            this.button = button;
            this.rating = rating;
        }
    }
}
