package com.example.myapplication.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myapplication.activities.activities.JourneyChatActivity;
import com.example.myapplication.activities.activities.JourneyRequestsActivity;
import com.example.myapplication.constants.JourneyStatus;
import com.example.myapplication.dtos.Journey;
import com.example.myapplication.experimental.DateTimeHelper;
import com.example.myapplication.R;
import com.google.gson.Gson;

import java.util.ArrayList;

/**
 * Created by Michal on 29/11/13.
 */
public class JourneyAdapter extends ArrayAdapter<Journey> {

    private Context context;
    private int layoutResourceId;
    private int userId;
    private ArrayList<Journey> originalCarShares;
    private ArrayList<Journey> displayedCarShares;
    private Mode mode;

    @Override
    public int getCount() {
        return displayedCarShares.size();
    }

    public JourneyAdapter(int user, Context context, int resource, ArrayList<Journey> carShares) {
        super(context, resource, carShares);
        this.layoutResourceId = resource;
        this.context = context;
        this.originalCarShares = carShares;
        this.displayedCarShares = this.originalCarShares;
        userId = user;
        mode = mode.Passenger;
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
            holder.modeIcon = (ImageView) row.findViewById(R.id.imgIcon);
            holder.fromTo = (TextView) row.findViewById(R.id.MyCarSharesFromToTextView);
            holder.departureDate = (TextView)row.findViewById(R.id.MyCarSharesDepartureDateTextView);
            holder.departureTime = (TextView) row.findViewById(R.id.MyCarSharesDepartureTimeTextView);
            holder.availableSeats = (TextView)row.findViewById(R.id.MyCarSharesAvailableSeatsTextView);
            holder.newRequestIcon = (ImageView) row.findViewById(R.id.MyCarSharesNewRequestIcon);
            holder.unreadRequests = (TextView) row.findViewById(R.id.MyCarSharesNumberOfUnreadRequestsTextView);
            holder.modeTextView = (TextView) row.findViewById(R.id.MyCarSharesModeTextView);
            holder.statusTextView = (TextView) row.findViewById(R.id.MyCarSharesStatusTextView);
            holder.statusIcon = (ImageView) row.findViewById(R.id.MyCarSharesStatusIcon);
            holder.journeyId = (TextView) row.findViewById(R.id.MyCarSharesJourneyIdTextView);
            holder.unreadMessages = (TextView) row.findViewById(R.id.MyCarSharesNumberOfUnreadMessagesTextView);
            holder.newMessagesIconView = (ImageView) row.findViewById(R.id.MyCarSharesNewMessagesIcon);
            holder.creationDateTextView = (TextView) row.findViewById(R.id.InstantMessengerRowDateTextView);
            row.setTag(holder);
        }
        else
        {
            holder = (CarShareHolder)row.getTag();
        }

        final Journey journey = displayedCarShares.get(position);
        int statusIconResource = 0;
        String statusText = "";

        switch(journey.CarShareStatus)
        {
            case JourneyStatus.Upcoming:
                statusIconResource = R.drawable.upcoming;
                statusText = "Upcoming";
                break;
            case JourneyStatus.Cancelled:
                statusIconResource = R.drawable.cancelled;
                statusText = "Cancelled";
                break;
            case JourneyStatus.Past:
                statusIconResource = R.drawable.past;
                statusText = "Expired";
                break;
        }

        holder.journeyId.setText("Journey id: " + journey.JourneyId);
        holder.fromTo.setText(journey.DepartureAddress.AddressLine + " -> " + journey.DestinationAddress.AddressLine);
        holder.departureDate.setText("Date: " + DateTimeHelper.getSimpleDate(journey.DateAndTimeOfDeparture));
        holder.departureTime.setText("Time: " + DateTimeHelper.getSimpleTime(journey.DateAndTimeOfDeparture));
        holder.availableSeats.setText("Available seats: " + journey.AvailableSeats);
        holder.modeIcon.setImageResource(R.drawable.taxi);
        holder.statusIcon.setImageResource(statusIconResource);
        holder.statusTextView.setText(statusText);
        holder.creationDateTextView.setText(DateTimeHelper.getSimpleDate(journey.CreationDate));

        if(journey.DriverId == userId)
        {
            holder.modeIcon.setImageResource(R.drawable.steering_wheel);
            holder.modeTextView.setText("Driver");
            mode = mode.Driver;
        }
        else
        {
            holder.modeTextView.setText("Passenger");
        }

        if(journey.UnreadRequestsCount > 0)
        {
            holder.newRequestIcon.setImageResource(R.drawable.new_request);
            holder.unreadRequests.setTypeface(null, Typeface.BOLD);
            //holder.fromTo.setTextColor(Color.rgb(45, 142, 28));
            holder.unreadRequests.setText("("+journey.UnreadRequestsCount+")");
        }
        else
        {
            holder.newRequestIcon.setImageResource(R.drawable.no_new_requests);
            holder.unreadRequests.setTypeface(null, Typeface.NORMAL);
            //holder.fromTo.setTextColor(Color.rgb(0, 134, 201));
        }

        return row;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint,FilterResults results) {

                displayedCarShares = (ArrayList<Journey>) results.values; // has the filtered values
                notifyDataSetChanged();  // notifies the data with new filtered values
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();        // Holds the results of a filtering operation in values
                ArrayList<Journey> filteredValues = new ArrayList<Journey>();

                if (originalCarShares == null) {
                    originalCarShares = displayedCarShares; // saves the original data in mOriginalValues
                }

                /********
                 *
                 *  If constraint(CharSequence that is received) is null returns the mOriginalValues(Original) values
                 *  else does the Filtering and returns FilteredArrList(Filtered)
                 *
                 ********/
                if (constraint == null || constraint.length() == 0) {

                    // set the Original result to return
                    results.count = originalCarShares.size();
                    results.values = originalCarShares;
                } else {
                    constraint = constraint.toString().toLowerCase();
                    for (int i = 0; i < originalCarShares.size(); i++) {
                        String data = (originalCarShares.get(i).DepartureAddress.AddressLine + " " + originalCarShares.get(i).DestinationAddress.AddressLine).replace("->", "");
                        if (data.toLowerCase().contains(constraint.toString())) {
                            filteredValues.add(originalCarShares.get(i));
                        }
                    }
                    // set the Filtered result to return
                    results.count = filteredValues.size();
                    results.values = filteredValues;
                }
                return results;
            }
        };
        return filter;
    }

    private void startRequestsActivity(Journey carShare)
    {
        Gson gson = new Gson();
        Intent intent = new Intent(this.context, JourneyRequestsActivity.class);
        intent.putExtra("CurrentCarShare", gson.toJson(carShare));
        this.context.startActivity(intent);
    }

    private void startChatActivity(Journey carShare)
    {
        Gson gson = new Gson();
        Intent intent = new Intent(this.context, JourneyChatActivity.class);
        intent.putExtra("CurrentCarShare", gson.toJson(carShare));
        this.context.startActivity(intent);
    }
    class CarShareHolder
    {
        ImageView modeIcon;
        ImageView statusIcon;
        ImageView newRequestIcon;
        ImageView newMessagesIconView;
        TextView journeyId;
        TextView statusTextView;
        TextView fromTo;
        TextView departureDate;
        TextView departureTime;
        TextView availableSeats;
        TextView unreadRequests;
        TextView modeTextView;
        TextView unreadMessages;
        TextView creationDateTextView;
    }

    enum Mode{
        Driver,
        Passenger
    }
}
