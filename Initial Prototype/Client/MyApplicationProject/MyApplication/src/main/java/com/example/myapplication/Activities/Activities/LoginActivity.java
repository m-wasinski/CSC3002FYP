package com.example.myapplication.Activities.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import com.example.myapplication.Activities.Base.BaseActivity;
import com.example.myapplication.Constants.Constants;
import com.example.myapplication.Constants.ServiceResponseCode;
import com.example.myapplication.DTOs.LoginDTO;
import com.example.myapplication.DomainObjects.ServiceResponse;
import com.example.myapplication.DomainObjects.User;
import com.example.myapplication.Experimental.AppData;
import com.example.myapplication.Helpers.Pair;
import com.example.myapplication.Interfaces.WCFServiceCallback;
import com.example.myapplication.NetworkTasks.WCFServiceTask;
import com.example.myapplication.R;
import com.google.gson.reflect.TypeToken;

import static java.util.Arrays.asList;

public class LoginActivity extends BaseActivity implements WCFServiceCallback<User,String> {

    private ProgressDialog progressDialog;
    private AppData appData;

    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState);
        appData = ((AppData)getApplication());
        setContentView(R.layout.login_activity_layout);
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
        EditText userName = (EditText) findViewById(R.id.UserLoginUserNameEditText);
        EditText password = (EditText) findViewById(R.id.UserLoginPasswordTextField);
        CheckBox rememberMe  = (CheckBox) findViewById(R.id.RememberMeCheckBox);

        if(checkIfFieldsEmpty(userName, "username") || checkIfFieldsEmpty(password, "password"))
        {
            return;
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Logging in...");
        progressDialog.setMessage("Please wait.");
        progressDialog.show();


        LoginDTO loginDTO =new LoginDTO();
        loginDTO.UserName = userName.getText().toString();
        loginDTO.Password = password.getText().toString();
        loginDTO.GCMRegistrationID = appData.getRegistrationId();


        new WCFServiceTask<LoginDTO, User>("https://findndrive.no-ip.co.uk/Services/UserService.svc/manuallogin", loginDTO, new TypeToken<ServiceResponse<User>>() {}.getType(),
                asList(new Pair(Constants.REMEMBER_ME, ""+rememberMe.isChecked()), new Pair(Constants.DEVICE_ID, appData.getUniqueDeviceId()), new Pair(Constants.UUID, appData.getUUID())),
                Constants.SESSION_ID, this).execute();
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
        progressDialog.dismiss();

        if (serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
        {
            storeSessionId(session);
            appData.setUser(serviceResponse.Result);
            Intent intent = new Intent(this, ActivityHome.class);
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
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.GLOBAL_APP_DATA, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.PROPERTY_SESSION_ID, sessionId);
        editor.commit();
        appData.setSessionId(sessionId);
    }
}