package com.example.myapplication.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.Toast;

import com.example.myapplication.DomainObjects.ServiceResponse;
import com.example.myapplication.DomainObjects.User;
import com.example.myapplication.Helpers.TestAuthentication;
import com.example.myapplication.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

/**
 * Created by Michal on 13/11/13.
 */
public class HomeActivity extends Activity {

    private User CurrentUser;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.home_activity);

        Gson gson = new Gson();

        Type userType = new TypeToken<User>() {}.getType();
        CurrentUser = gson.fromJson(getIntent().getExtras().getString("CurrentUser"), userType);

        Toast toast = Toast.makeText(this, CurrentUser.GetUserName(), Toast.LENGTH_LONG);
        toast.show();

        TestAuthentication test = new TestAuthentication(CurrentUser);
        test.execute();
    }

    @Override
    public void onBackPressed() {
        ShowMessageDialog();
    }

    private void ShowMessageDialog()
    {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Exit")
                .setMessage("Are you sure?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ExitApp();
                    }

                })
                .setNegativeButton("No", null)
                .show();
    }

    private void ExitApp()
    {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("Exit me", true);
        startActivity(intent);
        finish();
    }
}