package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

/**
 * Created by Michal on 12/11/13.
 */
public class WelcomeActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
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
}