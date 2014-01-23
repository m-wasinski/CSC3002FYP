package com.example.myapplication.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myapplication.dtos.Journey;
import com.example.myapplication.experimental.DateTimeHelper;
import com.example.myapplication.R;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by Michal on 06/01/14.
 */
public class SearchResultsAdapter extends ArrayAdapter<Journey> {

    private Context context;
    private int layoutResourceId;
    private ArrayList<Journey> CarShares;
    private DecimalFormat decimalFormat;

    public SearchResultsAdapter(Context context, int resource, ArrayList<Journey> carShares) {
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

        Journey carShare = CarShares.get(position);

        holder.fromToTextView.setText(carShare.GeoAddresses.get(0).AddressLine + " -> " + carShare.GeoAddresses.get(carShare.GeoAddresses.size()-1).AddressLine);
//        holder.journeyIdTextView.setText("Journey id: " + carShare.JourneyId);
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

