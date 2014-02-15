package com.example.myapplication.activities.activities;

import android.annotation.TargetApi;
import android.content.Intent;
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
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.domain_objects.User;
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
        this.requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_login);
        this.progressBar = (ProgressBar) findViewById(R.id.LoginActivityProgressBar);

        this.setupEventHandlers();
    }

    private void setupEventHandlers()
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

        if(checkIfFieldsEmpty(userName, "username") || checkIfFieldsEmpty(password, "password"))
        {
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        LoginDTO loginDTO =new LoginDTO();
        loginDTO.UserName = userName.getText().toString();
        loginDTO.Password = password.getText().toString();
        loginDTO.GCMRegistrationID = findNDriveManager.getRegistrationId();


        new WCFServiceTask<LoginDTO>(this, getResources().getString(R.string.UserManualLoginURL), loginDTO, new TypeToken<ServiceResponse<User>>() {}.getType(),
                asList(new Pair(SessionConstants.REMEMBER_ME, ""+rememberMe.isChecked()), new Pair(SessionConstants.DEVICE_ID, findNDriveManager.getUniqueDeviceId()),
                        new Pair(SessionConstants.UUID, findNDriveManager.getUUID())), this).execute();
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
        this.startActivity(new Intent(this, RegistrationActivity.class));
        this.finish();
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
            case R.id.register_menu_option:
                openRegistrationActivity();
            default:
                return true;
        }
    }

    @Override
    public void onServiceCallCompleted(ServiceResponse<User> serviceResponse, String session) {
        progressBar.setVisibility(View.GONE);
        if (serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
        {
            findNDriveManager.setSessionId(session);
            findNDriveManager.login(serviceResponse.Result);
            Intent intent = new Intent(this, HomeActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        }
    }
}