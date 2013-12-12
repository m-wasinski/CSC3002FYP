package com.example.myapplication.Activities;

import android.annotation.TargetApi;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.view.Window;

import com.example.myapplication.Constants.Constants;
import com.example.myapplication.DomainObjects.ServiceResponse;
import com.example.myapplication.DomainObjects.User;
import com.example.myapplication.Helpers.ApplicationFileManager;
import com.example.myapplication.Helpers.ServiceHelper;
import com.example.myapplication.Interfaces.OnLoginCompleted;
import com.example.myapplication.R;
import com.google.gson.Gson;

import java.util.UUID;

public class MainActivity extends ActionBarActivity implements OnLoginCompleted {

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        Constants.RandomID = UUID.randomUUID().toString();
        Log.e("Random String", Constants.RandomID);
        setContentView(R.layout.activity_main);

        PerformStartupFileCheck();

        ApplicationFileManager fileManager = new ApplicationFileManager();

        if (fileManager.CookieExists())
        {
            ServiceHelper.AttemptAutoLogin(this);
        }
        else
        {
            try{ Thread.sleep(2000); }catch(InterruptedException e){ }
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }


    }

    private void PerformStartupFileCheck()
    {
        ApplicationFileManager fileManager = new ApplicationFileManager();

        if (!fileManager.FolderExists())
        {
            fileManager.MakeApplicationDirectory();
        }
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void OnLoginCompleted(ServiceResponse<User> serviceResponse) {
        if (serviceResponse.ServiceResponseCode == Constants.ServiceResponseSuccess)
        {
            Gson gson = new Gson();
            Intent intent = new Intent(this, HomeActivity.class);
            intent.putExtra("CurrentUser", gson.toJson(serviceResponse.Result));
            startActivity(intent);

        }
        else
        {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }

        finish();
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }



}
