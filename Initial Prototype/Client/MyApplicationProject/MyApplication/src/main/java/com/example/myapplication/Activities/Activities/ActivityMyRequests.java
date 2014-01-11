package com.example.myapplication.Activities.Activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.example.myapplication.Activities.Base.BaseActivity;
import com.example.myapplication.Activities.Base.BaseFragment;
import com.example.myapplication.Adapters.MyRequestsAdapter;
import com.example.myapplication.Constants.ServiceResponseCode;
import com.example.myapplication.DomainObjects.CarShare;
import com.example.myapplication.DomainObjects.CarShareRequest;
import com.example.myapplication.DomainObjects.ServiceResponse;
import com.example.myapplication.Interfaces.WCFServiceCallback;
import com.example.myapplication.NetworkTasks.WCFServiceTask;
import com.example.myapplication.R;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

/**
 * Created by Michal on 06/01/14.
 */
public class ActivityMyRequests extends BaseActivity implements WCFServiceCallback<ArrayList<CarShareRequest>, String> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_requests);
    }

    @Override
    public void onResume() {
        super.onResume();
        new WCFServiceTask<Integer, ArrayList<CarShareRequest>>("https://findndrive.no-ip.co.uk/Services/RequestService.svc/getrequestsforuser",
                this.appData.getUser().UserId,
                new TypeToken<ServiceResponse<ArrayList<CarShareRequest>>>() {}.getType(),
                appData.getAuthorisationHeaders(),null, this).execute();
    }


    @Override
    public void onServiceCallCompleted(final ServiceResponse<ArrayList<CarShareRequest>> serviceResponse, String parameter) {
        super.checkIfAuthorised(serviceResponse.ServiceResponseCode);
        final ListView mainListView = (ListView) findViewById(R.id.MyRequestsListView);
        TextView noRequestsTextView = (TextView) findViewById(R.id.MyRequestsNoRequestsTextView);

        if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
        {
            if(serviceResponse.Result.size() == 0)
            {
                noRequestsTextView.setVisibility(View.VISIBLE);
                mainListView.setVisibility(View.GONE);
            }
            else
            {
                noRequestsTextView.setVisibility(View.GONE);
                mainListView.setVisibility(View.VISIBLE);
            }

            ArrayList<Integer> ids = new ArrayList<Integer>();

            for (CarShareRequest request : serviceResponse.Result)
            {
                ids.add(request.CarShareId);
            }

            new WCFServiceTask<ArrayList<Integer>, ArrayList<CarShare>>("https://findndrive.no-ip.co.uk/Services/CarShareService.svc/getmultiple", ids,
                    new TypeToken<ServiceResponse<ArrayList<CarShare>>>() {}.getType(),
                    appData.getAuthorisationHeaders(),null, new WCFServiceCallback<ArrayList<CarShare>, String>() {

                @Override
                public void onServiceCallCompleted(ServiceResponse<ArrayList<CarShare>> filteredCarShares, String parameter) {
                    for(CarShareRequest request : serviceResponse.Result)
                    {
                        for(CarShare carShare : filteredCarShares.Result)
                        {
                            if(request.CarShareId == carShare.CarShareId)
                            {
                                request.CarShare = carShare;
                            }
                        }
                    }

                    MyRequestsAdapter adapter = new MyRequestsAdapter(getApplicationContext(), R.layout.fragment_my_requests_listview_row, serviceResponse.Result);
                    mainListView.setAdapter(adapter);

                }
            }).execute();
        }
    }
}
