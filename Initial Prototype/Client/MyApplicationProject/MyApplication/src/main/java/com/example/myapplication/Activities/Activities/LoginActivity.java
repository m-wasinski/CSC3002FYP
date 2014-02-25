package com.example.myapplication.activities.activities;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
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

public class LoginActivity extends BaseActivity implements WCFServiceCallback<User,String> {

    private ProgressBar progressBar;
    private LinearLayout keepMeLoggedInLinearLayout;
    private Button loginButton;
    private Button registerButton;
    private CheckBox rememberMeCheckbox;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_login);

        // Initialise local variables.
        this.progressBar = (ProgressBar) this.findViewById(R.id.LoginActivityProgressBar);
        this.keepMeLoggedInLinearLayout = (LinearLayout) this.findViewById(R.id.LoginActivityKeepMeLoggedInLinearLayout);
        this.loginButton = (Button) this.findViewById(R.id.LoginUserButton);
        this.registerButton = (Button) this.findViewById(R.id.LoginActivityRegisterButton);
        this.rememberMeCheckbox = (CheckBox) this.findViewById(R.id.RememberMeCheckBox);

        this.setupEventHandlers();
    }

    private void setupEventHandlers()
    {
        this.loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        this.registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openRegistrationActivity();
            }
        });

        this.keepMeLoggedInLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rememberMeCheckbox.setChecked(!rememberMeCheckbox.isChecked());
            }
        });
    }

    private void attemptLogin()
    {
        EditText userName = (EditText) findViewById(R.id.UserLoginUserNameEditText);
        EditText password = (EditText) findViewById(R.id.UserLoginPasswordTextField);

        if(validateFields(userName, "username") || validateFields(password, "password"))
        {
            return;
        }

        this.progressBar.setVisibility(View.VISIBLE);

        new WcfPostServiceTask<LoginDTO>(this, getResources().getString(R.string.UserManualLoginURL),
                new LoginDTO(userName.getText().toString(), password.getText().toString(), appManager.getRegistrationId()), new TypeToken<ServiceResponse<User>>() {}.getType(),
                asList(new Pair(SessionConstants.REMEMBER_ME, ""+this.rememberMeCheckbox.isChecked()), new Pair(SessionConstants.DEVICE_ID, appManager.getUniqueDeviceId()),
                        new Pair(SessionConstants.UUID, appManager.getUUID())), this).execute();
    }

    private boolean validateFields(EditText editText, String value)
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
        getMenuInflater().inflate(R.menu.login, menu);
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
            appManager.setSessionId(session);
            appManager.login(serviceResponse.Result);
            Intent intent = new Intent(this, HomeActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        }
    }
}