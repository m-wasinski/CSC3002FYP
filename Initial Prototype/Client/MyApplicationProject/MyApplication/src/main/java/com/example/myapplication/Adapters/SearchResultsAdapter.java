package com.example.myapplication.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.example.myapplication.Activities.Activities.CarShareRequestsActivity;
import com.example.myapplication.Constants.CarShareStatus;
import com.example.myapplication.DomainObjects.CarShare;
import com.example.myapplication.Experimental.DateTimeHelper;
import com.example.myapplication.R;
import com.google.gson.Gson;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * Created by Michal on 06/01/14.
 */
public class SearchResultsAdapter extends ArrayAdapter<CarShare> {

    private Context context;
    private int layoutResourceId;
    private ArrayList<CarShare> CarShares;
    private DecimalFormat decimalFormat;

    public SearchResultsAdapter(Context context, int resource, ArrayList<CarShare> carShares) {
        super(context, resource, carShares);
        this.layoutResourceId = resource;
        this.context = context;
        this.CarShares = carShares;
        this.decimalFormat = new DecimalFormat("0.00");
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        CarShareHolder holder;

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new CarShareHolder();
            holder.fromToTextView = (TextView) row.findViewById(R.id.FragmentSearchFromToTextView);
            holder.dateTextView = (TextView) row.findViewById(R.id.FragmentSearchDateTextView);
            holder.timeTextView = (TextView)row.findViewById(R.id.FragmentSearchTimeTextView);
            holder.seatsLeftTextView = (TextView) row.findViewById(R.id.FragmentSearchSeatsLeftTextView);
            row.setTag(holder);
        }
        else
        {
            holder = (CarShareHolder)row.getTag();
        }

        CarShare carShare = CarShares.get(position);

        holder.fromToTextView.setText(carShare.DepartureAddress.AddressLine + " -> " + carShare.DestinationAddress.AddressLine);
//        holder.journeyIdTextView.setText("Journey id: " + carShare.CarShareId);
//        holder.driverNameTextView.setText("Driver: " + carShare.Driver.FirstName + " " + carShare.Driver.LastName);
        holder.dateTextView.setText("Date: " + DateTimeHelper.getSimpleDate(carShare.DateAndTimeOfDeparture));
        holder.timeTextView.setText("Time: " + DateTimeHelper.getSimpleTime(carShare.DateAndTimeOfDeparture));
        holder.seatsLeftTextView.setText("Seats left: " + carShare.AvailableSeats);
  //      holder.feeTextView.setText(decimalFormat.format(carShare.Fee));

        /*if(carShare.AvailableSeats >= 2)
        {
            holder.qualityIcon.setImageResource(R.drawable.green_light);
        }
        else
        {
            holder.qualityIcon.setImageResource(R.drawable.yellow_light);
        }*/

        return row;
    }

    class CarShareHolder
    {
        ImageView qualityIcon;
        ImageView star1;
        ImageView star2;
        ImageView star3;
        ImageView star4;
        ImageView star5;
        TextView fromToTextView;
        TextView journeyIdTextView;
        TextView driverNameTextView;
        TextView dateTextView;
        TextView timeTextView;
        TextView seatsLeftTextView;
        TextView feeTextView;
    }
}

