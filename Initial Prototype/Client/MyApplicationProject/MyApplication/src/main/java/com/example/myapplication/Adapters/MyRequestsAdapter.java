package com.example.myapplication.Adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myapplication.Constants.RequestDecision;
import com.example.myapplication.DomainObjects.CarShare;
import com.example.myapplication.DomainObjects.CarShareRequest;
import com.example.myapplication.DomainObjects.ServiceResponse;
import com.example.myapplication.Experimental.AppData;
import com.example.myapplication.Experimental.DateTimeHelper;
import com.example.myapplication.Interfaces.WCFServiceCallback;
import com.example.myapplication.NetworkTasks.WCFServiceTask;
import com.example.myapplication.R;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

/**
 * Created by Michal on 06/01/14.
 */
public class MyRequestsAdapter extends ArrayAdapter<CarShareRequest> implements WCFServiceCallback<CarShare, String> {

    private Context context;
    private int layoutResourceId;
    private ArrayList<CarShareRequest> carShareRequests;
    public MyRequestsAdapter(Context context, int resource, ArrayList<CarShareRequest> carShareRequests) {
        super(context, resource, carShareRequests);
        this.layoutResourceId = resource;
        this.context = context;
        this.carShareRequests = carShareRequests;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View row = convertView;
        CarShareRequestHolder holder = null;

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new CarShareRequestHolder();
            holder.statusIcon = (ImageView) row.findViewById(R.id.MyRequestsImageStatusImageView);
            holder.carShareId = (TextView) row.findViewById(R.id.MyRequestsJourneyIdTextView);
            holder.driverUserName = (TextView)row.findViewById(R.id.MyRequestsDriverTextView);
            holder.fromTo = (TextView) row.findViewById(R.id.MyRequestsJourneyFromToTextView);
            holder.dateTime = (TextView)row.findViewById(R.id.MyRequestsDateTimeTextView);
            holder.sentOn = (TextView) row.findViewById(R.id.MyRequestsRequestSentOnTextView);
            holder.repliedOn = (TextView) row.findViewById(R.id.MyRequestsReplyReceivedOnTextView);
            holder.statusTextView = (TextView) row.findViewById(R.id.MyRequestsStatusTextView);
            row.setTag(holder);
        }
        else
        {
            holder = (CarShareRequestHolder)row.getTag();
        }

        CarShareRequest carShareRequest = carShareRequests.get(position);

        holder.carShareId.setText("Journey id: " + carShareRequest.CarShareId);
        holder.driverUserName.setText("Driver: " + carShareRequest.CarShare.Driver.FirstName + " " + carShareRequest.CarShare.Driver.LastName);
        holder.fromTo.setText("From: " + carShareRequest.CarShare.DepartureCity + " to " + carShareRequest.CarShare.DestinationCity);
        holder.sentOn.setText("Request sent on: " + DateTimeHelper.getSimpleDate(carShareRequest.SentOnDate) + " " +DateTimeHelper.getSimpleTime(carShareRequest.SentOnDate));

        String repliedOn = "Reply received on: ";
        String decision = "";
        int image = 0;

        switch(carShareRequest.Decision)
        {
            case RequestDecision.UNDECIDED:
                repliedOn = repliedOn + "N/A";
                image = R.drawable.undecided;
                decision = "Pending";
                break;
            case RequestDecision.ACCEPTED:
                repliedOn = repliedOn + DateTimeHelper.getSimpleDate(carShareRequest.DecidedOnDate) + " " + DateTimeHelper.getSimpleTime(carShareRequest.DecidedOnDate);
                image = R.drawable.accepted;
                decision = "Accepted";
                break;
            case RequestDecision.DENIED:
                repliedOn = repliedOn + DateTimeHelper.getSimpleDate(carShareRequest.DecidedOnDate) + " " + DateTimeHelper.getSimpleTime(carShareRequest.DecidedOnDate);
                image = R.drawable.denied;
                decision = "Denied";
                break;
        }

        holder.repliedOn.setText(repliedOn);
        holder.statusTextView.setText(decision);
        holder.statusIcon.setImageResource(image);

        return row;
    }

    @Override
    public void onServiceCallCompleted(ServiceResponse<CarShare> serviceResponse, String parameter) {

    }

    class CarShareRequestHolder
    {
        ImageView statusIcon;
        TextView driverUserName;
        TextView carShareId;
        TextView fromTo;
        TextView dateTime;
        TextView sentOn;
        TextView repliedOn;
        TextView statusTextView;
    }
}
