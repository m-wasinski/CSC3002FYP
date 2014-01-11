package com.example.myapplication.Activities.Activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.Activities.Base.BaseActivity;
import com.example.myapplication.Constants.Constants;
import com.example.myapplication.Constants.ServiceResponseCode;
import com.example.myapplication.DTOs.RegisterDTO;
import com.example.myapplication.DomainObjects.CarShare;
import com.example.myapplication.DomainObjects.ServiceResponse;
import com.example.myapplication.DomainObjects.User;
import com.example.myapplication.Helpers.Pair;
import com.example.myapplication.Interfaces.WCFServiceCallback;
import com.example.myapplication.NetworkTasks.WCFServiceTask;
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
    private Spinner genderSpinner;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.registration_activity);
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

        genderSpinner = (Spinner) findViewById(R.id.RegisterActivityGender);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.gender, R.layout.support_simple_spinner_dropdown_item);
        genderSpinner.setAdapter(adapter);

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
        validateGender();

        if(valuesCorrect)
        {
            int gender = genderSpinner.getSelectedItemPosition();
            errorMessagesEditText.setText("");
            progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Registering...");
            progressDialog.setMessage("Please wait.");
            progressDialog.show();

            RegisterDTO registerDTO = new RegisterDTO();
            registerDTO.Password = passwordEditText.getText().toString();
            registerDTO.ConfirmedPassword = confirmedPasswordEditText.getText().toString();
            registerDTO.User = new User();
            registerDTO.User.UserName = userNameEditText.getText().toString();
            registerDTO.User.EmailAddress = emailAddressEditText.getText().toString();
            registerDTO.User.GCMRegistrationID = appData.getRegistrationId();
            registerDTO.User.Gender = gender;

            new WCFServiceTask<RegisterDTO, User>("https://findndrive.no-ip.co.uk/Services/UserService.svc/register",
                    registerDTO,
                    new TypeToken<ServiceResponse<User>>() {}.getType(),
                    asList(new Pair(Constants.REMEMBER_ME, ""+false),
                           new Pair(Constants.DEVICE_ID, appData.getUniqueDeviceId()),
                           new Pair(Constants.UUID, appData.getUUID())), Constants.SESSION_ID, this).execute();

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

    private void validateGender()
    {
        if(genderSpinner.getSelectedItemPosition() == 0)
        {
            Toast toast = Toast.makeText(this, "Please select gender.", Toast.LENGTH_LONG);
            toast.show();
            valuesCorrect = false;
            return;
        }

        valuesCorrect = true;
    }

    @Override
    public void onServiceCallCompleted(ServiceResponse<User> serviceResponse, String session) {
        progressDialog.dismiss();
        Toast toast;

        if (serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
        {
            storeSessionId(session);
            appData.setUser(serviceResponse.Result);
            toast = Toast.makeText(this, "Registered successfully!", Toast.LENGTH_LONG);
            toast.show();
            Intent intent = new Intent(this, ActivityHome.class);
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
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.GLOBAL_APP_DATA, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.PROPERTY_SESSION_ID, sessionId);
        editor.commit();
        appData.setSessionId(sessionId);
    }
}