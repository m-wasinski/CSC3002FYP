package com.example.myapplication.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.example.myapplication.Constants.Constants;
import com.example.myapplication.DomainObjects.User;
import com.example.myapplication.Helpers.ApplicationFileManager;
import com.example.myapplication.Interfaces.OnLoginCompleted;
import com.example.myapplication.R;
import com.example.myapplication.DomainObjects.ServiceResponse;
import com.example.myapplication.Helpers.UserHelper;
import com.google.gson.Gson;

import java.net.CookieManager;

public class LoginActivity extends Activity implements OnLoginCompleted {

    private ProgressDialog pd;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);



        setContentView(R.layout.login_activity);
        SetupUIEvents();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish(); // or do something else
    }

    private void SetupUIEvents()
    {
        Button loginButton = (Button) findViewById(R.id.LoginUserButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AttemptLogin();
            }
        });
    }

    private void AttemptLogin()
    {
        EditText userName = (EditText) findViewById(R.id.UserLoginUserNameEditText);
        EditText password = (EditText) findViewById(R.id.UserLoginPasswordEditText);
        CheckBox rememberMe  = (CheckBox) findViewById(R.id.RememberMeCheckBox);

        pd = new ProgressDialog(this);
        pd.setTitle("Logging in...");
        pd.setMessage("Please wait.");
        pd.show();

        UserHelper userHelper = new UserHelper();
        userHelper.LoginUser(userName.getText().toString(), password.getText().toString(), this, rememberMe.isChecked());

    }

    @Override
    public void onTaskCompleted(ServiceResponse<User> serviceResponse) {

        pd.dismiss();

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