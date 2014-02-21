package com.example.myapplication.activities.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
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
        this.errorMessagesEditText = (TextView) findViewById(R.id.RegisterActivityErrorMessages);
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

        this.confirmedPasswordEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus && !confirmedPasswordEditText.getText().toString().isEmpty())
                    validatePasswords();
            }});

        this.emailAddressEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus && !emailAddressEditText.getText().toString().isEmpty())
                    validateEmail();
            }});

        this.passwordEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus && !passwordEditText.getText().toString().isEmpty())
                    validatePasswords();
            }});

        this.userNameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus && !userNameEditText.getText().toString().isEmpty())
                    validateUserName();
            }});
    }

    private void AttemptToRegister() {

        this.validateUserName();
        this.validateEmail();
        this.validatePasswords();

        if(valuesCorrect)
        {
            errorMessagesEditText.setText("");
            progressBar.setVisibility(View.VISIBLE);

            new WcfPostServiceTask<RegisterDTO>(this, getResources().getString(R.string.UserRegisterURL),
                    new RegisterDTO(passwordEditText.getText().toString(), confirmedPasswordEditText.getText().toString(),
                            new User(userNameEditText.getText().toString(),emailAddressEditText.getText().toString(), this.findNDriveManager.getRegistrationId())),
                    new TypeToken<ServiceResponse<User>>() {}.getType(),
                    asList(new Pair(SessionConstants.REMEMBER_ME, ""+false),
                           new Pair(SessionConstants.DEVICE_ID, findNDriveManager.getUniqueDeviceId()),
                           new Pair(SessionConstants.UUID, findNDriveManager.getUUID())), this).execute();

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
            findNDriveManager.setSessionId(session);
            findNDriveManager.setUser(serviceResponse.Result);
            toast = Toast.makeText(this, "Registered successfully!", Toast.LENGTH_LONG);
            toast.show();
            startActivity(new Intent(this, HomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.register, menu);
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