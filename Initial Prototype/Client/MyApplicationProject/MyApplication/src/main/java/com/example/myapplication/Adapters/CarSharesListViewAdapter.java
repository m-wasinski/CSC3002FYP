package com.example.myapplication.Adapters;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myapplication.DomainObjects.CarShare;
import com.example.myapplication.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by Michal on 29/11/13.
 */
public class CarSharesListViewAdapter extends ArrayAdapter<CarShare> {

    private Context context;
    private int layoutResourceId;
    private int userId;
    private ArrayList<CarShare> CarShares= null;

    public CarSharesListViewAdapter(int user, Context context, int resource, ArrayList<CarShare> carShares) {
        super(context, resource, carShares);
        this.layoutResourceId = resource;
        this.context = context;
        this.CarShares = carShares;
        userId = user;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        CarShareHolder holder = null;

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new CarShareHolder();
            holder.imgIcon = (ImageView) row.findViewById(R.id.imgIcon);
            holder.FromTo = (TextView) row.findViewById(R.id.CarShareFromToTextView);
            holder.DepartureDate = (TextView)row.findViewById(R.id.CarShareDepartureDateTextView);
            holder.DepartureTime = (TextView) row.findViewById(R.id.CarShareDepartureTimeTextView);
            holder.AvailableSeats = (TextView)row.findViewById(R.id.CarShareAvailableSeatsTextView);
            row.setTag(holder);
        }
        else
        {
            holder = (CarShareHolder)row.getTag();
        }

        CarShare carShare = CarShares.get(position);


        holder.FromTo.setText(carShare.DepartureCity + " -> " + carShare.DestinationCity);
        holder.DepartureDate.setText(carShare.DateOfDepartureAsString());
        holder.DepartureTime.setText(carShare.TimeOfDepartureAsString());
        holder.AvailableSeats.setText("Available seats: "+carShare.AvailableSeats);
        holder.imgIcon.setImageResource(R.drawable.taxi);

        if(carShare.DriverId == userId)
        {
            holder.imgIcon.setImageResource(R.drawable.steering_wheel);
        }

        return row;
    }

    static class CarShareHolder
    {
        ImageView imgIcon;
        TextView FromTo;
        TextView DepartureDate;
        TextView DepartureTime;
        TextView AvailableSeats;
    }
}
