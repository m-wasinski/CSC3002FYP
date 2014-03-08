package com.example.myapplication.activities.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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
 * Provides user with the ability to rate the driver.
 */
public class RateDriverActivity extends BaseActivity implements WCFServiceCallback<Boolean, Void>
{

    private Button sendFeedbackButton;

    private EditText feedbackEditText;

    private TextView messageTextView;

    private ArrayList<ButtonHolder> starButtons = new ArrayList<ButtonHolder>(5);

    private int rating = 0;

    private Journey journey;

    private ProgressBar progressBar;

    private String TAG = "Rate Driver Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_driver);

        // Initialise local variables.
        journey = gson.fromJson(getIntent().getStringExtra(IntentConstants.JOURNEY), new TypeToken<Journey>() {}.getType());

        // Initialise UI elements.
        sendFeedbackButton = (Button) findViewById(R.id.RateDriverActivityRateButton);

        feedbackEditText = (EditText) findViewById(R.id.RateDriverActivityFeedbackEditText);

        messageTextView = (TextView) findViewById(R.id.RateDriverActivityMessageTextView);

        starButtons.add(0, new ButtonHolder((Button) findViewById(R.id.RateDriverActivityStarOneButton), 1));
        starButtons.add(1, new ButtonHolder((Button) findViewById(R.id.RateDriverActivityStarTwoButton), 2));
        starButtons.add(2, new ButtonHolder((Button) findViewById(R.id.RateDriverActivityStarThreeButton), 3));
        starButtons.add(3, new ButtonHolder((Button) findViewById(R.id.RateDriverActivityStarFourButton), 4));
        starButtons.add(4, new ButtonHolder((Button) findViewById(R.id.RateDriverActivityStarFiveButton), 5));

        progressBar = (ProgressBar) findViewById(R.id.RateDriverActivityProgressBar);

        // Setup all event handlers.
        setupEventHandlers();
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

        sendFeedbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendFeedback();
            }
        });
    }

    private void setRating(int index)
    {
        rating = index;

        Log.i(TAG, "Current rating is: " + rating);

        for(int i = 0; i < starButtons.size(); i++)
        {
            starButtons.get(i).button.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(index > i ? R.drawable.rating : R.drawable.no_rating), null, null);
        }
    }

    @Override
    public void onServiceCallCompleted(ServiceResponse<Boolean> serviceResponse, Void parameter)
    {
        progressBar.setVisibility(View.GONE);
        if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
        {
            Toast.makeText(this, "Rating submitted successfully!", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void sendFeedback()
    {
        String message = feedbackEditText.getText().toString();

        feedbackEditText.setError(message.isEmpty() ? "Please enter feedback" : null);
        messageTextView.setError(rating == 0 ? "Please select rating" : null);

        if(message.isEmpty() || rating == 0)
        {
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        sendFeedbackButton.setEnabled(false);
        new WcfPostServiceTask<RatingDTO>(this, getResources().getString(R.string.RateDriverURL),
                new RatingDTO(journey.getDriver().getUserId(), rating,
                        appManager.getUser().getUserId(), message),
                new TypeToken<ServiceResponse<Boolean>>(){}.getType(), appManager.getAuthorisationHeaders(), this).execute();
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
