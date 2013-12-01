package com.example.myapplication.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.myapplication.Constants.Constants;
import com.example.myapplication.DomainObjects.ServiceResponse;
import com.example.myapplication.DomainObjects.User;
import com.example.myapplication.Helpers.UserHelper;
import com.example.myapplication.Interfaces.OnRegistrationCompleted;
import com.example.myapplication.R;

/**
 * Created by Michal on 12/11/13.
 */
public class RegistrationActivity extends Activity implements OnRegistrationCompleted{

    private ProgressDialog pd;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.registration_activity);
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
        EditText userName = (EditText) findViewById(R.id.UserNameTextField);
        EditText emailAddress = (EditText) findViewById(R.id.EmailTextField);
        EditText password = (EditText) findViewById(R.id.RegistrationPasswordTextField);
        EditText confirmedPassword = (EditText) findViewById(R.id.RegistrationConfirmPasswordTextField);

        pd = new ProgressDialog(this);
        pd.setTitle("Registering...");
        pd.setMessage("Please wait.");
        pd.show();

        UserHelper.RegisterNewUser(userName.getText().toString(),
                                   emailAddress.getText().toString(),
                                   password.getText().toString(),
                                   confirmedPassword.getText().toString(), this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish(); // or do something else
    }

    @Override
    public void onRegistrationCompleted(ServiceResponse<User> serviceResponse) {
        pd.dismiss();

        if (serviceResponse.ServiceResponseCode == Constants.ServiceResponseSuccess)
        {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
        else
        {
            Toast toast = Toast.makeText(this, "Incorrect username or password.", Toast.LENGTH_LONG);
            toast.show();
        }
    }
}