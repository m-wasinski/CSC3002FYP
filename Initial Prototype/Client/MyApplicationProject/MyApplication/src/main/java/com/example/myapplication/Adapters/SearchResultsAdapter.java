package com.example.myapplication.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.myapplication.domain_objects.Journey;
import com.example.myapplication.experimental.DateTimeHelper;
import com.example.myapplication.R;
import com.example.myapplication.utilities.Helpers;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by Michal on 06/01/14.
 */
public class SearchResultsAdapter extends ArrayAdapter<Journey> {

    private Context context;
    private int layoutResourceId;
    private ArrayList<Journey> journeys;
    private DecimalFormat decimalFormat;

    public SearchResultsAdapter(Context context, int resource, ArrayList<Journey> journeys) {
        super(context, resource, journeys);
        this.layoutResourceId = resource;
        this.context = context;
        this.journeys = journeys;
        this.decimalFormat = new DecimalFormat("0.00");
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View currentRow = convertView;
        SearchResultHolder holder;

        if(currentRow == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            currentRow = inflater.inflate(layoutResourceId, parent, false);
            holder = new SearchResultHolder();
            holder.fromToTextView = (TextView) currentRow.findViewById(R.id.FragmentSearchFromToTextView);
            holder.dateTextView = (TextView) currentRow.findViewById(R.id.FragmentSearchDateTextView);
            holder.timeTextView = (TextView)currentRow.findViewById(R.id.FragmentSearchTimeTextView);
            holder.seatsLeftTextView = (TextView) currentRow.findViewById(R.id.FragmentSearchSeatsLeftTextView);
            holder.feeTextView = (TextView) currentRow.findViewById(R.id.FragmentSearchFeeTextView);
            currentRow.setTag(holder);
        }
        else
        {
            holder = (SearchResultHolder)currentRow.getTag();
        }

        Journey journey = journeys.get(position);

        holder.fromToTextView.setText(Helpers.getJourneyHeader(journey.GeoAddresses));
        holder.dateTextView.setText("Date: " + DateTimeHelper.getSimpleDate(journey.DateAndTimeOfDeparture));
        holder.timeTextView.setText("Time: " + DateTimeHelper.getSimpleTime(journey.DateAndTimeOfDeparture));
        holder.seatsLeftTextView.setText("Seats left: " + journey.AvailableSeats);
        holder.feeTextView.setText("Fee: £"+decimalFormat.format(journey.Fee));
        return currentRow;
    }

    class SearchResultHolder
    {
        TextView fromToTextView;
        TextView dateTextView;
        TextView timeTextView;
        TextView seatsLeftTextView;
        TextView feeTextView;
    }
}

