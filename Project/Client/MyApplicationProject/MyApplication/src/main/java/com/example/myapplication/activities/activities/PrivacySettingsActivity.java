package com.example.myapplication.activities.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.activities.base.BaseActivity;
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.domain_objects.User;
import com.example.myapplication.dtos.PrivacySettingsUpdaterDTO;
import com.example.myapplication.factories.ServiceTaskFactory;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WcfPostServiceTask;

/**
 * Created by Michal on 06/03/14.
 */
public class PrivacySettingsActivity extends BaseActivity implements View.OnClickListener, WCFServiceCallback<User,Void> {

    private TextView emailTextView;
    private TextView genderTextView;
    private TextView dateOfBirthTextView;
    private TextView phoneNumberTextVierw;
    private TextView ratingTextView;
    private TextView journeysTextView;

    private Button saveChangesButton;

    private String[] privacyLevels = {"Private", "Friends Only", "Everyone"};

    private ProgressBar progressBar;

    private WcfPostServiceTask<PrivacySettingsUpdaterDTO> privacySettingsUpdater;

    private int emailPrivacyLevel;
    private int genderPrivacyLevel;
    private int dateOfBirthPrivacyLevel;
    private int phoneNumberPrivacyLevel;
    private int ratingPrivacyLevel;
    private int journeysPrivacyLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_settings);

        // Initialise UI elements and assign event handlers.
        findViewById(R.id.PrivacySettingsActivityEmailAddressTableRow).setOnClickListener(this);
        findViewById(R.id.PrivacySettingsActivityGenderTableRow).setOnClickListener(this);
        findViewById(R.id.PrivacySettingsActivityDateOfBirthTableRow).setOnClickListener(this);
        findViewById(R.id.PrivacySettingsActivityPhoneNumberTableRow).setOnClickListener(this);
        findViewById(R.id.PrivacySettingsActivityRatingTableRow).setOnClickListener(this);
        findViewById(R.id.PrivacySettingsActivityJourneysTableRow).setOnClickListener(this);

        // TextViews
        emailTextView = (TextView) findViewById(R.id.PrivacySettingsActivityEmailAddressTextView);
        genderTextView = (TextView) findViewById(R.id.PrivacySettingsActivityGenderTextView);
        dateOfBirthTextView = (TextView) findViewById(R.id.PrivacySettingsActivityDateOfBirthTextView);
        phoneNumberTextVierw = (TextView) findViewById(R.id.PrivacySettingsActivityPhoneNumberTextView);
        ratingTextView = (TextView) findViewById(R.id.PrivacySettingsActivityRatingsTextView);
        journeysTextView = (TextView) findViewById(R.id.PrivacySettingsActivityJourneysTextView);

        // The save changes button.
        saveChangesButton = (Button) findViewById(R.id.PrivacySettingsActivitySaveChangesButton);
        saveChangesButton.setOnClickListener(this);

        progressBar = (ProgressBar) findViewById(R.id.PrivacySettingsActivityProgressBar);

        loadPrivacySettings();
    }

    private void loadPrivacySettings()
    {
        emailTextView.setText(privacyLevels[getAppManager().getUser().getPrivacySettings().getEmailPrivacyLevel()]);
        emailPrivacyLevel = getAppManager().getUser().getPrivacySettings().getEmailPrivacyLevel();
        genderTextView.setText(privacyLevels[getAppManager().getUser().getPrivacySettings().getGenderPrivacyLevel()]);
        genderPrivacyLevel = getAppManager().getUser().getPrivacySettings().getGenderPrivacyLevel();
        dateOfBirthTextView.setText(privacyLevels[getAppManager().getUser().getPrivacySettings().getDateOfBirthPrivacyLevel()]);
        dateOfBirthPrivacyLevel = getAppManager().getUser().getPrivacySettings().getDateOfBirthPrivacyLevel();
        phoneNumberTextVierw.setText(privacyLevels[getAppManager().getUser().getPrivacySettings().getPhoneNumberPrivacyLevel()]);
        phoneNumberPrivacyLevel = getAppManager().getUser().getPrivacySettings().getPhoneNumberPrivacyLevel();
        ratingTextView.setText(privacyLevels[getAppManager().getUser().getPrivacySettings().getRatingPrivacyLevel()]);
        ratingPrivacyLevel = getAppManager().getUser().getPrivacySettings().getRatingPrivacyLevel();
        journeysTextView.setText(privacyLevels[getAppManager().getUser().getPrivacySettings().getJourneysPrivacyLevel()]);
        journeysPrivacyLevel = getAppManager().getUser().getPrivacySettings().getJourneysPrivacyLevel();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.PrivacySettingsActivityEmailAddressTableRow:
                getPrivacyLevel(new privacyLevelChangeListener() {
                    @Override
                    public void privacyLevelChanged(int level) {
                        emailPrivacyLevel = level;
                        emailTextView.setText(privacyLevels[level]);
                    }
                });
                break;
            case R.id.PrivacySettingsActivityGenderTableRow:
                getPrivacyLevel(new privacyLevelChangeListener() {
                    @Override
                    public void privacyLevelChanged(int level) {
                        genderPrivacyLevel = level;
                        genderTextView.setText(privacyLevels[level]);
                    }
                });
                break;
            case R.id.PrivacySettingsActivityDateOfBirthTableRow:
                getPrivacyLevel(new privacyLevelChangeListener() {
                    @Override
                    public void privacyLevelChanged(int level) {
                        dateOfBirthPrivacyLevel = level;
                        dateOfBirthTextView.setText(privacyLevels[level]);
                    }
                });
                break;
            case R.id.PrivacySettingsActivityPhoneNumberTableRow:
                getPrivacyLevel(new privacyLevelChangeListener() {
                    @Override
                    public void privacyLevelChanged(int level) {
                        phoneNumberPrivacyLevel = level;
                        phoneNumberTextVierw.setText(privacyLevels[level]);
                    }
                });
                break;
            case R.id.PrivacySettingsActivityRatingTableRow:
                getPrivacyLevel(new privacyLevelChangeListener() {
                    @Override
                    public void privacyLevelChanged(int level) {
                        ratingPrivacyLevel = level;
                        ratingTextView.setText(privacyLevels[level]);
                    }
                });
                break;
            case R.id.PrivacySettingsActivityJourneysTableRow:
                getPrivacyLevel(new privacyLevelChangeListener() {
                    @Override
                    public void privacyLevelChanged(int level) {
                        journeysPrivacyLevel = level;
                        journeysTextView.setText(privacyLevels[level]);
                    }
                });
                break;
            case R.id.PrivacySettingsActivitySaveChangesButton:
                saveChanges();
                break;
        }
    }

    private void saveChanges()
    {
        saveChangesButton.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        privacySettingsUpdater = ServiceTaskFactory.getPrivacySettingsUpdater(this, getAppManager().getAuthorisationHeaders(),
                new PrivacySettingsUpdaterDTO(getAppManager().getUser().getUserId(), journeysPrivacyLevel, ratingPrivacyLevel, phoneNumberPrivacyLevel, dateOfBirthPrivacyLevel, emailPrivacyLevel, genderPrivacyLevel), this);
        privacySettingsUpdater.execute();
    }

    private void getPrivacyLevel(final privacyLevelChangeListener listener)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(privacyLevels, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.privacyLevelChanged(which);
            }
        });

        builder.show();
    }

    @Override
    public void onServiceCallCompleted(ServiceResponse<User> serviceResponse, Void parameter) {
        progressBar.setVisibility(View.GONE);
        saveChangesButton.setEnabled(true);

        if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
        {
            Toast.makeText(this, "Changes to your privacy settings were saved successfully.", Toast.LENGTH_LONG).show();
            getAppManager().setUser(serviceResponse.Result);
            loadPrivacySettings();
        }

    }

    private interface privacyLevelChangeListener
    {
        void privacyLevelChanged(int level);
    }
}
