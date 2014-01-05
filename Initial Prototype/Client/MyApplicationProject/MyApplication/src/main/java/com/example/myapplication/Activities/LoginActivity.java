package com.example.myapplication.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.example.myapplication.Constants.Constants;
import com.example.myapplication.DomainObjects.User;
import com.example.myapplication.Experimental.AppData;
import com.example.myapplication.Interfaces.loginActivityInterface;
import com.example.myapplication.NetworkTasks.ManualLoginTask;
import com.example.myapplication.R;
import com.example.myapplication.DomainObjects.ServiceResponse;

public class LoginActivity extends Activity implements loginActivityInterface {

    private ProgressDialog pd;
    private AppData appData;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appData = ((AppData)getApplication());
        setContentView(R.layout.login_activity_layout);
        setupUIEvents();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    private void setupUIEvents()
    {
        Button loginButton = (Button) findViewById(R.id.LoginUserButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        Button registerButton = (Button) findViewById(R.id.LoginActivityRegisterButton);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openRegistrationActivity();
            }
        });
    }

    private void attemptLogin()
    {
        EditText userName = (EditText) findViewById(R.id.UserLoginUserNameEditText);
        EditText password = (EditText) findViewById(R.id.UserLoginPasswordTextField);
        CheckBox rememberMe  = (CheckBox) findViewById(R.id.RememberMeCheckBox);

        pd = new ProgressDialog(this);
        pd.setTitle("Logging in...");
        pd.setMessage("Please wait.");
        pd.show();

        ManualLoginTask userLoginTask = new ManualLoginTask(userName.getText().toString(), password.getText().toString(), rememberMe.isChecked(), this, appData);
        userLoginTask.execute();
    }

    private void openRegistrationActivity()
    {
        Intent intent = new Intent(this, RegistrationActivity.class);
        startActivity(intent);
        finish();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.login, menu);

        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.RegisterMenuOption:
                openRegistrationActivity();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void manualLoginCompleted(ServiceResponse<User> serviceResponse) {
        pd.dismiss();

        if (serviceResponse.ServiceResponseCode == Constants.SERVICE_RESPONSE_SUCCESS)
        {
            appData.setUser(serviceResponse.Result);
            Intent intent = new Intent(this, HomeActivity.class);
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