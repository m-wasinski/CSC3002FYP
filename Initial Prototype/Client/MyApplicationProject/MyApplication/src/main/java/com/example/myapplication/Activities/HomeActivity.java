package com.example.myapplication.Activities;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import com.example.myapplication.DomainObjects.ServiceResponse;
import com.example.myapplication.DomainObjects.User;
import com.example.myapplication.Experimental.SpinnerNavigationItem;
import com.example.myapplication.Experimental.TitleNavigationAdapter;
import com.example.myapplication.Fragments.FragmentMyCarShares;
import com.example.myapplication.Helpers.ServiceHelper;
import com.example.myapplication.Interfaces.UserHomeActivity;
import com.example.myapplication.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by Michal on 13/11/13.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class HomeActivity extends Activity implements UserHomeActivity{


    // action bar
    private ActionBar actionBar;

    // Title navigation Spinner data
    private ArrayList<SpinnerNavigationItem> navSpinner;

    // Navigation adapter
    private TitleNavigationAdapter adapter;

    public User CurrentUser;
    public int lol()
    {
        return CurrentUser.GetId();
    }



    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);



        Gson gson = new Gson();

        Type userType = new TypeToken<User>() {}.getType();
        CurrentUser = gson.fromJson(getIntent().getExtras().getString("CurrentUser"), userType);

        Bundle bundle = new Bundle();
        bundle.putInt("UserId", CurrentUser.GetId());

        FragmentMyCarShares fragobj = new FragmentMyCarShares();
        fragobj.currentUserId = CurrentUser.GetId();
        fragobj.setArguments(bundle);

        actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        // Spinner title navigation data
        navSpinner = new ArrayList<SpinnerNavigationItem>();

        navSpinner.add(new SpinnerNavigationItem("Home", R.drawable.steering_wheel));
        navSpinner.add(new SpinnerNavigationItem("Post New Car Share", R.drawable.steering_wheel));
        navSpinner.add(new SpinnerNavigationItem("Profile", R.drawable.steering_wheel));
        navSpinner.add(new SpinnerNavigationItem("Friends", R.drawable.steering_wheel));
        actionBar.setSelectedNavigationItem(0);
        // title drop down adapter
        adapter = new TitleNavigationAdapter(getApplicationContext(), navSpinner);

        // assigning the spinner navigation
        actionBar.setListNavigationCallbacks(adapter, new ActionBar.OnNavigationListener() {
            @Override
            public boolean onNavigationItemSelected(int i, long l) {
                Log.e("Selected", ""+i);
                switch(i){
                    case 1:
                        Intent intent = new Intent(getBaseContext(), PostNewCarShareActivity.class);
                        Gson g = new Gson();
                        intent.putExtra("CurrentUser", g.toJson(CurrentUser));
                        startActivity(intent);
                }

                return false;
            }
        });

        setContentView(R.layout.home_activity);

// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.app_menu_options, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears

    }

    @Override
    protected void onResume() {
        super.onResume();
        actionBar.setSelectedNavigationItem(0);
    }

    @Override
    public void onBackPressed() {
        ExitApp(false);
    }

    private void ExitApp(boolean forceLogout)
    {
        ServiceHelper.LogoutUser(this, forceLogout);
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