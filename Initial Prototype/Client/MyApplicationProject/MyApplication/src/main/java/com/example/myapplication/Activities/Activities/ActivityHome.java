package com.example.myapplication.Activities.Activities;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.example.myapplication.Activities.Base.BaseActivity;
import com.example.myapplication.Adapters.MyCarSharesAdapter;
import com.example.myapplication.Constants.ServiceResponseCode;
import com.example.myapplication.DomainObjects.CarShare;
import com.example.myapplication.DomainObjects.ServiceResponse;
import com.example.myapplication.Interfaces.WCFServiceCallback;
import com.example.myapplication.NetworkTasks.WCFServiceTask;
import com.example.myapplication.R;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

/**
 * Created by Michal on 27/11/13.
 */

public class ActivityHome extends BaseActivity implements WCFServiceCallback<ArrayList<CarShare>, String>  {

    private Boolean forceLogout;
    private EditText filterEditText;
    private ListView mainListView;
    private MyCarSharesAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        actionBar.setTitle(appData.getUser().UserName);
        filterEditText = (EditText) findViewById(R.id.ActivityHomeFilterEditText);
        mainListView = (ListView) findViewById(R.id.MyCarSharesListView);
        filterEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                     adapter.getFilter().filter(charSequence.toString());
                     mainListView.setAdapter(adapter);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        new WCFServiceTask<Integer, ArrayList<CarShare>>("https://findndrive.no-ip.co.uk/Services/CarShareService.svc/getall", appData.getUser().UserId,
                new TypeToken<ServiceResponse<ArrayList<CarShare>>>() {}.getType(),
                appData.getAuthorisationHeaders(),null, this).execute();
    }

    @Override
    public void onServiceCallCompleted(final ServiceResponse<ArrayList<CarShare>> serviceResponse, String s) {
        super.checkIfAuthorised(serviceResponse.ServiceResponseCode);
        TextView noJourneysTextView = (TextView) findViewById(R.id.MyCarSharesNoJourneysTextView);

        if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
        {
            if(serviceResponse.Result.size() == 0)
            {
                noJourneysTextView.setVisibility(View.VISIBLE);
                mainListView.setVisibility(View.GONE);
            }
            else
            {
                noJourneysTextView.setVisibility(View.GONE);
                mainListView.setVisibility(View.VISIBLE);
                filterEditText.setEnabled(true);
            }

            adapter = new MyCarSharesAdapter(appData.getUser().UserId, this, R.layout.fragment_my_car_shares_listview_row, serviceResponse.Result);
            adapter.getFilter().filter(filterEditText.getText().toString());
            mainListView.setAdapter(adapter);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ExitApp(false);
    }

    private void ExitApp(boolean b)
    {
        forceLogout = b;
        new WCFServiceTask<Boolean, Boolean>("https://findndrive.no-ip.co.uk/Services/UserService.svc/logout",
                forceLogout, new TypeToken<ServiceResponse<Boolean>>(){}.getType(), appData.getAuthorisationHeaders(), null, new WCFServiceCallback<Boolean, Void>() {
            @Override
            public void onServiceCallCompleted(ServiceResponse<Boolean> serviceResponse, Void parameter) {
                if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
                {
                    finish();

                    if(forceLogout)
                    {
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                }
            }
        }).execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.LogoutMenuOption:
                ExitApp(true);
                break;
            case R.id.action_add_new_car_share:
                intent = new Intent(this, PostNewCarShareActivity.class);
                startActivity(intent);
                break;
            case R.id.travel_buddies_menu_option:
                intent = new Intent(this, TravelBuddyListActivity.class);
                startActivity(intent);
                break;
            case R.id.action_search:
                intent = new Intent(this, ActivitySearch.class);
                startActivity(intent);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }
}