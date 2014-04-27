package com.example.myapplication.activities.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.example.myapplication.R;
import com.example.myapplication.activities.base.BaseActivity;
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.constants.SessionConstants;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.domain_objects.User;
import com.example.myapplication.dtos.LoginDTO;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WcfPostServiceTask;
import com.example.myapplication.utilities.Pair;
import com.google.gson.reflect.TypeToken;

import static java.util.Arrays.asList;

/**
 * Provides users with the ability to log into the application using their username and password.
 **/
public class LoginActivity extends BaseActivity implements WCFServiceCallback<User,String>, View.OnClickListener {

    private ProgressBar progressBar;
    private Button loginButton;
    private CheckBox rememberMeCheckbox;

    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialise UI elements and setup event handlers.
        progressBar = (ProgressBar) findViewById(R.id.LoginActivityProgressBar);

        LinearLayout keepMeLoggedInLinearLayout = (LinearLayout) findViewById(R.id.LoginActivityKeepMeLoggedInLinearLayout);
        keepMeLoggedInLinearLayout.setOnClickListener(this);

        loginButton = (Button) findViewById(R.id.LoginActivityLoginUserButton);
        loginButton.setOnClickListener(this);

        Button registerButton = (Button) findViewById(R.id.LoginActivityRegisterButton);
        registerButton.setOnClickListener(this);

        rememberMeCheckbox = (CheckBox) findViewById(R.id.LoginActivityRememberMeCheckBox);
    }

    /**
     * Validates the username and password fields and triggers call to the web service to perform user log in.
     */
    private void attemptLogin()
    {
        EditText userName = (EditText) findViewById(R.id.LoginActivityUserNameEditText);
        EditText password = (EditText) findViewById(R.id.LoginActivityPasswordTextField);

        if(validateFields(userName, "username") || validateFields(password, "password"))
        {
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        loginButton.setEnabled(false);

        new WcfPostServiceTask<LoginDTO>(this, getResources().getString(R.string.UserManualLoginURL),
                new LoginDTO(userName.getText().toString(), password.getText().toString(), getAppManager().getRegistrationId()), new TypeToken<ServiceResponse<User>>() {}.getType(),
                asList(new Pair(SessionConstants.REMEMBER_ME, ""+rememberMeCheckbox.isChecked()), new Pair(SessionConstants.DEVICE_ID, getAppManager().getUniqueDeviceId()),
                        new Pair(SessionConstants.UUID, getAppManager().getUUID())), this).execute();
    }

    private boolean validateFields(EditText editText, String value)
    {
        editText.setError(editText.getText().toString().isEmpty() ? value + " cannot be empty!" : null);
        return editText.getText().toString().isEmpty();
    }

    private void openRegistrationActivity()
    {
        startActivity(new Intent(this, RegistrationActivity.class));
        finish();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.register_menu_option:
                openRegistrationActivity();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Called after user login has been processed by the web service.
     *
     * @param serviceResponse
     * @param session
     */
    @Override
    public void onServiceCallCompleted(ServiceResponse<User> serviceResponse, String session) {
        progressBar.setVisibility(View.GONE);

        if (serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
        {
            getAppManager().setSessionId(session);
            getAppManager().setUser(serviceResponse.Result);
            startActivity(new Intent(this, HomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
            finish();
        }
        else
        {
            loginButton.setEnabled(true);
        }
    }

    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            case R.id.LoginActivityLoginUserButton:
                attemptLogin();
                break;
            case R.id.LoginActivityRegisterButton:
                openRegistrationActivity();
                break;
            case R.id.LoginActivityKeepMeLoggedInLinearLayout:
                rememberMeCheckbox.setChecked(!rememberMeCheckbox.isChecked());
                break;
        }
    }
}