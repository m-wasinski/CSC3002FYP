package com.example.myapplication.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.Constants.Constants;
import com.example.myapplication.DomainObjects.ServiceResponse;
import com.example.myapplication.DomainObjects.User;
import com.example.myapplication.Helpers.ServiceHelper;
import com.example.myapplication.Interfaces.OnRegistrationCompleted;
import com.example.myapplication.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Michal on 12/11/13.
 */
public class RegistrationActivity extends Activity implements OnRegistrationCompleted{

    private ProgressDialog pd;
    private boolean valuesCorrect;
    private EditText userNameEditText;
    private EditText emailAddressEditText;
    private EditText passwordEditText;
    private EditText confirmedPasswordEditText;
    private TextView errorMessagesEditText;
    private Spinner genderSpinner;
    private int gender;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.registration_activity_layout);
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

        if(valuesCorrect)
        {
            gender = genderSpinner.getSelectedItemPosition();
            errorMessagesEditText.setText("");
            pd = new ProgressDialog(this);
            pd.setTitle("Registering...");
            pd.setMessage("Please wait.");
            pd.show();
            ServiceHelper.RegisterNewUser(userNameEditText.getText().toString(),
                    emailAddressEditText.getText().toString(),
                    passwordEditText.getText().toString(),
                    confirmedPasswordEditText.getText().toString(), gender, this);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
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
    public void onRegistrationCompleted(ServiceResponse<User> serviceResponse) {

        pd.dismiss();
        Toast toast;

        if (serviceResponse.ServiceResponseCode == Constants.ServiceResponseSuccess)
        {
            toast = Toast.makeText(this, "Registered successfully, please login.", Toast.LENGTH_LONG);
            toast.show();
            Intent intent = new Intent(this, LoginActivity.class);
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
}