package com.example.myapplication.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import com.example.myapplication.DomainObjects.ServiceResponse;
import com.example.myapplication.DomainObjects.User;
import com.example.myapplication.Helpers.TestAuthentication;
import com.example.myapplication.Helpers.UserHelper;
import com.example.myapplication.Interfaces.UserHomeActivity;
import com.example.myapplication.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

/**
 * Created by Michal on 13/11/13.
 */
public class HomeActivity extends Activity implements UserHomeActivity{

    private User CurrentUser;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.home_activity);

        Gson gson = new Gson();

        Type userType = new TypeToken<User>() {}.getType();
        CurrentUser = gson.fromJson(getIntent().getExtras().getString("CurrentUser"), userType);

        Toast toast = Toast.makeText(this, CurrentUser.GetUserName(), Toast.LENGTH_LONG);
        toast.show();
    }

    @Override
    public void onBackPressed() {
        ExitApp(false);
    }

    private void ExitApp(boolean forceLogout)
    {
        UserHelper userHelper = new UserHelper();
        userHelper.LogoutUser(this, forceLogout);
        finish();

        if(forceLogout)
        {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }

    }

    @Override
    public void OnLogoutCompleted(ServiceResponse<Boolean> serviceResponse) {

    }

    @Override

    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.home, menu);

        return true;

    }

    @Override

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.LogoutMenuOption:
                ExitApp(true);
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}