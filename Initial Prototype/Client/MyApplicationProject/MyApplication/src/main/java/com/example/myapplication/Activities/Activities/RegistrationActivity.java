package com.example.myapplication.activities.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.activities.base.BaseActivity;
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.constants.SessionConstants;
import com.example.myapplication.constants.SharedPreferencesConstants;
import com.example.myapplication.dtos.RegisterDTO;
import com.example.myapplication.dtos.ServiceResponse;
import com.example.myapplication.dtos.User;
import com.example.myapplication.utilities.Pair;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WCFServiceTask;
import com.example.myapplication.R;
import com.google.gson.reflect.TypeToken;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private TextView errorMessagesEditText;
    private ProgressBar progressBar;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration_activity);
        progressBar = (ProgressBar) findViewById(R.id.RegistrationActivityProgressBar);
        userNameEditText = (EditText) findViewById(R.id.UserNameTextField);
        userNameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus)
                    validateUserName();
            }});

        emailAddressEditText = (EditText) findViewById(R.id.EmailTextField);
        emailAddressEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus)
                    validateEmail();
            }});

        passwordEditText = (EditText) findViewById(R.id.RegistrationPasswordTextField);
        passwordEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus)
                    validatePasswords();
            }});


        confirmedPasswordEditText = (EditText) findViewById(R.id.RegistrationConfirmPasswordTextField);
        confirmedPasswordEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus)
                    validatePasswords();
            }});

        errorMessagesEditText = (TextView) findViewById(R.id.RegisterActivityErrorMessages);

        SetupUIEvents();

    }

    void SetupUIEvents()
    {
        Button registerButton = (Button) findViewById(R.id.RegisterNewUserButton);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AttemptToRegister();
            }
        });
    }

    private void AttemptToRegister() {

        validateUserName();
        validateEmail();
        validatePasswords();

        if(valuesCorrect)
        {
            errorMessagesEditText.setText("");
            progressBar.setVisibility(View.VISIBLE);
            RegisterDTO registerDTO = new RegisterDTO();
            registerDTO.Password = passwordEditText.getText().toString();
            registerDTO.ConfirmedPassword = confirmedPasswordEditText.getText().toString();
            registerDTO.User = new User();
            registerDTO.User.UserName = userNameEditText.getText().toString();
            registerDTO.User.EmailAddress = emailAddressEditText.getText().toString();
            registerDTO.User.GCMRegistrationID = appData.getRegistrationId();

            new WCFServiceTask<RegisterDTO>(getResources().getString(R.string.UserRegisterURL),
                    registerDTO,
                    new TypeToken<ServiceResponse<User>>() {}.getType(),
                    asList(new Pair(SessionConstants.REMEMBER_ME, ""+false),
                           new Pair(SessionConstants.DEVICE_ID, appData.getUniqueDeviceId()),
                           new Pair(SessionConstants.UUID, appData.getUUID())), this).execute();

        }
    }

    private void validateUserName(){
        String userName = userNameEditText.getText().toString();
        Pattern pattern = Pattern.compile("[~#@*+%{}<>\\[\\]|\"\\ ^/[/\\\\]]");
        Matcher matcher = pattern.matcher(userName);

        if(userName.length() < 4 || matcher.find())
        {
            userNameEditText.setError("Username must be at least 4 characters long, and cannot contain the following characters: ~, #, @, *, +, %, {, }, <, >, [, ], |, “, ”, \\, /, _, ^");
            valuesCorrect = false;
            return;
        }
        else
            userNameEditText.setError(null);

        valuesCorrect = true;
    }

    private void validateEmail(){
        String emailAddress = emailAddressEditText.getText().toString();

        Pattern rfc2822 = Pattern.compile(
                "^[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$");

        if (!rfc2822.matcher(emailAddress).matches()) {
            emailAddressEditText.setError("Invalid email address.");
            valuesCorrect = false;
            return;
        }
        else
            emailAddressEditText.setError(null);

        valuesCorrect = true;
    }

    private void validatePasswords()
    {
        String password = passwordEditText.getText().toString();
        String confirmedPassword = confirmedPasswordEditText.getText().toString();

        if(!password.equals(confirmedPassword))
        {
            passwordEditText.setError("Both passwords must match.");
            confirmedPasswordEditText.setError("Both passwords must match.");
            valuesCorrect = false;
            return;
        }

        if(password.length() < 6 || confirmedPassword.length() < 6)
        {
            passwordEditText.setError("Password must be at least 6 characters long.");
            confirmedPasswordEditText.setError("Password must be at least 6 characters long.");
            valuesCorrect = false;
            return;
        }

        passwordEditText.setError(null);
        confirmedPasswordEditText.setError(null);

        valuesCorrect = true;
    }

    @Override
    public void onServiceCallCompleted(ServiceResponse<User> serviceResponse, String session) {
        Toast toast;
        progressBar.setVisibility(View.VISIBLE);
        if (serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
        {
            storeSessionId(session);
            appData.setUser(serviceResponse.Result);
            toast = Toast.makeText(this, "Registered successfully!", Toast.LENGTH_LONG);
            toast.show();
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
            finish();
        }
        else
        {
            for(String error : serviceResponse.ErrorMessages)
            {
                errorMessagesEditText.append(error+"\n");
            }
        }
    }

    private void storeSessionId(String sessionId)
    {
        SharedPreferences sharedPreferences = getSharedPreferences(SharedPreferencesConstants.GLOBAL_APP_DATA, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPreferencesConstants.PROPERTY_SESSION_ID, sessionId);
        editor.commit();
        appData.setSessionId(sessionId);
    }
}