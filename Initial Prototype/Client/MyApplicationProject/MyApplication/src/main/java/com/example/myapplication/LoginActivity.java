package com.example.myapplication;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity implements OnLoginCompleted {

    private ProgressDialog pd;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.login_activity);
        SetupUIEvents();
    }

    void SetupUIEvents()
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

        pd = new ProgressDialog(this);
        pd.setTitle("Logging in...");
        pd.setMessage("Please wait.");
        pd.show();

        UserHelper userHelper = new UserHelper();
        userHelper.LoginUser(userName.getText().toString(), password.getText().toString(), this);

    }

    @Override
    public void onTaskCompleted(ServiceResponse<User> serviceResponse) {

        pd.dismiss();

        Toast toast;

        if (ServiceResponseCodes.Success == serviceResponse.ServiceResponseCode)
        {
             toast = Toast.makeText(this, "Login successfull!", Toast.LENGTH_LONG);

        }
        else
        {
             toast = Toast.makeText(this, "Incorrent username or password", Toast.LENGTH_LONG);
        }

        toast.show();
    }
}