package com.example.myapplication.Activities.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.myapplication.Adapters.CarShareRequestAdapter;
import com.example.myapplication.Activities.Base.BaseActivity;
import com.example.myapplication.DomainObjects.CarShare;
import com.example.myapplication.DomainObjects.CarShareRequest;
import com.example.myapplication.DomainObjects.ServiceResponse;
import com.example.myapplication.Interfaces.WCFServiceCallback;
import com.example.myapplication.NetworkTasks.WCFServiceTask;
import com.example.myapplication.R;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by Michal on 02/01/14.
 */
public class CarShareRequestsActivity extends BaseActivity implements WCFServiceCallback<ArrayList<CarShareRequest>, String> {

    private ListView requestsListView;
    private CarShare carShare;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Type carShareType = new TypeToken<CarShare>() {}.getType();
        carShare = gson.fromJson(getIntent().getExtras().getString("CurrentCarShare"), carShareType);
        setContentView(R.layout.activity_car_share_requests);
        requestsListView = (ListView) findViewById(R.id.CarShareRequestsListView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        new WCFServiceTask<Integer, ArrayList<CarShareRequest>>("https://findndrive.no-ip.co.uk/Services/RequestService.svc/getrequests",
                this.carShare.CarShareId,
                new TypeToken<ServiceResponse<ArrayList<CarShareRequest>>>() {}.getType(),
                appData.getAuthorisationHeaders(),null, this).execute();
    }

    private void markRequestAsRead(int id)
    {
        new WCFServiceTask<Integer, CarShareRequest>("https://findndrive.no-ip.co.uk/Services/RequestService.svc/markasread",
                id,new TypeToken<ServiceResponse<CarShareRequest>>() {}.getType(),appData.getAuthorisationHeaders(),null, new WCFServiceCallback() {
            @Override
            public void onServiceCallCompleted(ServiceResponse serviceResponse, Object parameter) {
                Intent intent = new Intent(getBaseContext(), CarShareRequestDetailsActivity.class);
                intent.putExtra("CurrentCarShareRequest", gson.toJson(serviceResponse.Result));
                startActivity(intent);
            }
        }).execute();
    }

    @Override
    public void onServiceCallCompleted(final ServiceResponse<ArrayList<CarShareRequest>> serviceResponse, String parameter) {
        super.checkIfAuthorised(serviceResponse.ServiceResponseCode);
        if(serviceResponse.Result.size() > 0)
        {
            CarShareRequestAdapter adapter = new CarShareRequestAdapter(this, R.layout.car_share_request_listview_row, serviceResponse.Result);
            requestsListView.setAdapter(adapter);
            requestsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    markRequestAsRead(serviceResponse.Result.get(i).CarShareRequestId);
                }
            });
        }
    }
}
