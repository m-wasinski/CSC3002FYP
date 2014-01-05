package com.example.myapplication.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.example.myapplication.Adapters.CarShareAdapter;
import com.example.myapplication.Adapters.CarShareRequestAdapter;
import com.example.myapplication.DomainObjects.CarShare;
import com.example.myapplication.DomainObjects.CarShareRequest;
import com.example.myapplication.DomainObjects.ServiceResponse;
import com.example.myapplication.Interfaces.CarShareRequestRetrieverInterface;
import com.example.myapplication.NetworkTasks.CarShareRequestsRetriever;
import com.example.myapplication.NetworkTasks.MarkRequestAsReadTask;
import com.example.myapplication.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by Michal on 02/01/14.
 */
public class CarShareRequestsActivity extends Activity implements CarShareRequestRetrieverInterface {

    private ListView requestsListView;
    private CarShare carShare;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Gson gson = new Gson();
        Type carShareType = new TypeToken<CarShare>() {}.getType();
        carShare = gson.fromJson(getIntent().getExtras().getString("CurrentCarShare"), carShareType);

        setContentView(R.layout.car_share_requests);

        requestsListView = (ListView) findViewById(R.id.CarShareRequestsListView);

        CarShareRequestsRetriever carShareRequestsRetriever = new CarShareRequestsRetriever(carShare.CarShareId, this);
        carShareRequestsRetriever.execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        CarShareRequestsRetriever carShareRequestsRetriever = new CarShareRequestsRetriever(carShare.CarShareId, this);
        carShareRequestsRetriever.execute();
    }

    @Override
    public void carShareRequestsRetrieved(final ServiceResponse<ArrayList<CarShareRequest>> serviceResponse) {

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

    private void markRequestAsRead(int id)
    {
        MarkRequestAsReadTask markRequestAsReadTask = new MarkRequestAsReadTask(id, this);
        markRequestAsReadTask.execute();
    }

    @Override
    public void requestMarkedAsRead(ServiceResponse<CarShareRequest> carShareRequest) {
        Gson gson = new Gson();
        Intent intent = new Intent(getBaseContext(), CarShareRequestDetailsActivity.class);
        intent.putExtra("CurrentCarShareRequest", gson.toJson(carShareRequest.Result));
        startActivity(intent);
    }
}
