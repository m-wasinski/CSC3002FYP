package com.example.myapplication.activities.activities;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.activities.base.BaseActivity;
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.constants.SessionConstants;
import com.example.myapplication.constants.SharedPreferencesConstants;
import com.example.myapplication.dtos.ServiceResponse;
import com.example.myapplication.dtos.User;
import com.example.myapplication.dtos.LoginDTO;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WCFServiceTask;
import com.example.myapplication.utilities.Pair;
import com.google.gson.reflect.TypeToken;

import static java.util.Arrays.asList;

public class LoginActivity extends BaseActivity implements WCFServiceCallback<User,String> {

    private ProgressBar progressBar;
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        progressBar = (ProgressBar) findViewById(R.id.LoginActivityProgressBar);
        setupUIEvents();
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
        progressBar.setVisibility(View.VISIBLE);
        EditText userName = (EditText) findViewById(R.id.UserLoginUserNameEditText);
        EditText password = (EditText) findViewById(R.id.UserLoginPasswordTextField);
        CheckBox rememberMe  = (CheckBox) findViewById(R.id.RememberMeCheckBox);

        if(checkIfFieldsEmpty(userName, "username") || checkIfFieldsEmpty(password, "password"))
        {
            return;
        }

        LoginDTO loginDTO =new LoginDTO();
        loginDTO.UserName = userName.getText().toString();
        loginDTO.Password = password.getText().toString();
        loginDTO.GCMRegistrationID = appData.getRegistrationId();


        new WCFServiceTask<LoginDTO>(getResources().getString(R.string.UserManualLoginURL), loginDTO, new TypeToken<ServiceResponse<User>>() {}.getType(),
                asList(new Pair(SessionConstants.REMEMBER_ME, ""+rememberMe.isChecked()), new Pair(SessionConstants.DEVICE_ID, appData.getUniqueDeviceId()),
                        new Pair(SessionConstants.UUID, appData.getUUID())), this).execute();
    }

    private boolean checkIfFieldsEmpty(EditText editText, String value)
    {
        if(editText.getText().toString().isEmpty())
        {
            editText.setError(value + " cannot be empty!");
        }
        else
        {
            editText.setError(null);
        }

        return editText.getText().toString().isEmpty();
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
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.register_menu_option:
                openRegistrationActivity();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onServiceCallCompleted(ServiceResponse<User> serviceResponse, String session) {
        progressBar.setVisibility(View.GONE);
        if (serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
        {
            storeSessionId(session);
            appData.setUser(serviceResponse.Result);
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
            finish();
        }

        if (serviceResponse.ServiceResponseCode == ServiceResponseCode.UNAUTHORISED)
        {
            Toast toast = Toast.makeText(this, "Incorrect username or password.", Toast.LENGTH_LONG);
            toast.show();
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