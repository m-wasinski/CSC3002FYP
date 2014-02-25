package com.example.myapplication.activities.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.activities.base.BaseActivity;
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.constants.SessionConstants;
import com.example.myapplication.dtos.RegisterDTO;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.domain_objects.User;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WcfPostServiceTask;
import com.example.myapplication.utilities.Pair;
import com.example.myapplication.utilities.Validators;
import com.google.gson.reflect.TypeToken;

import static java.util.Arrays.asList;

/**
 * Created by Michal on 12/11/13.
 */
public class RegistrationActivity extends BaseActivity implements WCFServiceCallback<User, String>{

    private boolean valuesCorrect;

    private EditText userNameEditText;
    private EditText emailAddressEditText;
    private EditText passwordEditText;
    private EditText confirmedPasswordEditText;

    private Button registerButton;

    private ProgressBar progressBar;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_registration);


        // Initialise UI elements.
        this.progressBar = (ProgressBar) this.findViewById(R.id.RegistrationActivityProgressBar);
        this.userNameEditText = (EditText) this.findViewById(R.id.UserNameTextField);
        this.emailAddressEditText = (EditText) this.findViewById(R.id.EmailTextField);
        this.passwordEditText = (EditText) this.findViewById(R.id.RegistrationPasswordTextField);
        this.confirmedPasswordEditText = (EditText) this.findViewById(R.id.RegistrationConfirmPasswordTextField);
        this.registerButton = (Button) this.findViewById(R.id.RegisterNewUserButton);

        // Setup all event handlers.
        this.setupEventHandlers();
    }

    void setupEventHandlers()
    {
        this.registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AttemptToRegister();
            }
        });

        this.emailAddressEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus && !emailAddressEditText.getText().toString().isEmpty())
                    Validators.validateEmailAddress(emailAddressEditText);
            }});

        this.userNameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus && !userNameEditText.getText().toString().isEmpty())
                    Validators.validateUserName(userNameEditText);
            }});
    }

    private void AttemptToRegister() {

        boolean detailsCorrect;

        detailsCorrect = Validators.validateUserName(this.userNameEditText);
        detailsCorrect = Validators.validateEmailAddress(this.emailAddressEditText);
        detailsCorrect = Validators.validatePasswords(this.passwordEditText, this.confirmedPasswordEditText);

        if(detailsCorrect)
        {
            progressBar.setVisibility(View.VISIBLE);

            new WcfPostServiceTask<RegisterDTO>(this, getResources().getString(R.string.UserRegisterURL),
                    new RegisterDTO(passwordEditText.getText().toString(), confirmedPasswordEditText.getText().toString(),
                            new User(userNameEditText.getText().toString(),emailAddressEditText.getText().toString(), this.appManager.getRegistrationId())),
                    new TypeToken<ServiceResponse<User>>() {}.getType(),
                    asList(new Pair(SessionConstants.REMEMBER_ME, ""+false),
                           new Pair(SessionConstants.DEVICE_ID, appManager.getUniqueDeviceId()),
                           new Pair(SessionConstants.UUID, appManager.getUUID())), this).execute();

        }
    }

    @Override
    public void onServiceCallCompleted(ServiceResponse<User> serviceResponse, String session) {

        progressBar.setVisibility(View.GONE);

        if (serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
        {
            appManager.setSessionId(session);
            appManager.setUser(serviceResponse.Result);
            Toast.makeText(this, "Registered successfully!", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, HomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
            this.finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.register, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.login_menu_option:
                openLoginActivity();
            default:
                return true;
        }
    }

    private void openLoginActivity()
    {
        this.startActivity(new Intent(this, LoginActivity.class));
        this.finish();
    }
}