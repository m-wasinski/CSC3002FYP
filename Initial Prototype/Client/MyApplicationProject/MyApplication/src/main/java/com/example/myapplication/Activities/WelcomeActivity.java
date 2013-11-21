package com.example.myapplication.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.example.myapplication.Constants.Constants;
import com.example.myapplication.DomainObjects.ServiceResponse;
import com.example.myapplication.DomainObjects.User;
import com.example.myapplication.Helpers.ApplicationFileManager;
import com.example.myapplication.Helpers.UserHelper;
import com.example.myapplication.Interfaces.OnLoginCompleted;
import com.example.myapplication.R;
import com.google.gson.Gson;

/**
 * Created by Michal on 12/11/13.
 */
public class WelcomeActivity extends Activity implements OnLoginCompleted{
    public void onCreate(Bundle savedInstanceState) {
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        ApplicationFileManager fileManager = new ApplicationFileManager();
        Log.e("Cookie State", "" + fileManager.CookieExists());
        Log.e("Cookie Value", "" + fileManager.GetTokenValue());
        if (fileManager.CookieExists())
        {

            UserHelper userHelper = new UserHelper();
            userHelper.AttemptAutoLogin(this);
        }
        setContentView(R.layout.welcome_activity);
        super.onCreate(savedInstanceState);

        SetupUIEvents();
    }

    void SetupUIEvents(){
        Button loginButton = (Button) findViewById(R.id.LoginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoadLoginScreen();
            }
        });

        Button registerButton = (Button) findViewById(R.id.RegisterButton);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoadRegistrationScreen();
            }
        });
    }

    void LoadLoginScreen(){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    void LoadRegistrationScreen(){
        Intent intent = new Intent(this, RegistrationActivity.class);
        startActivity(intent);
    }

    @Override
    public void onTaskCompleted(ServiceResponse<User> serviceResponse) {
        if (serviceResponse.ServiceResponseCode == Constants.ServiceResponseSuccess)
        {
            Gson gson = new Gson();
            Intent intent = new Intent(this, HomeActivity.class);
            intent.putExtra("CurrentUser", gson.toJson(serviceResponse.Result));
            startActivity(intent);
            this.finish();
        }
        else
        {
            Toast toast = Toast.makeText(this, "Incorrect username or password.", Toast.LENGTH_LONG);
            toast.show();
        }
    }
}