package com.example.myapplication.Activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.myapplication.DomainObjects.ServiceResponse;
import com.example.myapplication.DomainObjects.User;
import com.example.myapplication.Fragments.FragmentMyCarShares;
import com.example.myapplication.Helpers.UserHelper;
import com.example.myapplication.Interfaces.UserHomeActivity;
import com.example.myapplication.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

/**
 * Created by Michal on 13/11/13.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class HomeActivity extends Activity implements UserHomeActivity{

    public User CurrentUser;
    public int lol()
    {
        return CurrentUser.GetId();
    }
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        Gson gson = new Gson();

        Type userType = new TypeToken<User>() {}.getType();
        CurrentUser = gson.fromJson(getIntent().getExtras().getString("CurrentUser"), userType);

        Bundle bundle = new Bundle();
        bundle.putInt("UserId", CurrentUser.GetId());

        FragmentMyCarShares fragobj = new FragmentMyCarShares();
        fragobj.currentUserId = CurrentUser.GetId();
        fragobj.setArguments(bundle);
        setContentView(R.layout.home_activity);

        Spinner spinner = (Spinner) findViewById(R.id.app_menu_spinner);
// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.app_menu_options, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        spinner.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        ExitApp(false);
    }

    private void ExitApp(boolean forceLogout)
    {
        UserHelper.LogoutUser(this, forceLogout);
        finish();

        if(forceLogout)
        {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }

    }

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