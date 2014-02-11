package com.example.myapplication.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myapplication.constants.JourneyStatus;
import com.example.myapplication.domain_objects.Journey;
import com.example.myapplication.experimental.DateTimeHelper;
import com.example.myapplication.R;
import com.example.myapplication.utilities.Utilities;

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
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        JourneyHolder holder = null;

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new JourneyHolder();
            holder.fromTo = (TextView) row.findViewById(R.id.MyCarSharesFromToTextView);
            holder.departureDate = (TextView)row.findViewById(R.id.MyCarSharesDepartureDateTextView);
            holder.departureTime = (TextView) row.findViewById(R.id.MyCarSharesDepartureTimeTextView);
            holder.availableSeats = (TextView)row.findViewById(R.id.MyCarSharesAvailableSeatsTextView);
            holder.newRequestIcon = (ImageView) row.findViewById(R.id.MyCarSharesNewRequestIcon);
            holder.unreadRequests = (TextView) row.findViewById(R.id.MyCarSharesNumberOfUnreadRequestsTextView);
            holder.modeTextView = (TextView) row.findViewById(R.id.MyCarSharesModeTextView);
            holder.statusTextView = (TextView) row.findViewById(R.id.MyCarSharesStatusTextView);
            holder.journeyId = (TextView) row.findViewById(R.id.MyCarSharesJourneyIdTextView);
            holder.unreadMessages = (TextView) row.findViewById(R.id.MyCarSharesNumberOfUnreadMessagesTextView);
            holder.newMessagesIconView = (ImageView) row.findViewById(R.id.MyCarSharesNewMessagesIcon);
            holder.creationDateTextView = (TextView) row.findViewById(R.id.InstantMessengerRowDateTextView);
            row.setTag(holder);
        }
        else
        {
            holder = (JourneyHolder)row.getTag();
        }

        final Journey journey = displayedCarShares.get(position);
        String statusText = "";

        switch(journey.JourneyStatus)
        {
            case JourneyStatus.Upcoming:
                statusText = "Upcoming";
                break;
            case JourneyStatus.Cancelled:
                statusText = "Cancelled";
                break;
            case JourneyStatus.Past:
                statusText = "Expired";
                break;
        }

        holder.journeyId.setText(String.valueOf(journey.JourneyId));
        holder.fromTo.setText(Utilities.getJourneyHeader(journey.GeoAddresses));
        holder.departureDate.setText(DateTimeHelper.getSimpleDate(journey.DateAndTimeOfDeparture));
        holder.departureTime.setText(DateTimeHelper.getSimpleTime(journey.DateAndTimeOfDeparture));
        holder.availableSeats.setText(String.valueOf(journey.AvailableSeats));
        holder.statusTextView.setText(statusText);
        holder.creationDateTextView.setText(DateTimeHelper.getSimpleDate(journey.CreationDate));
        holder.modeTextView.setText(journey.DriverId == userId ? "Driver" : "Passenger");
        holder.newRequestIcon.setImageResource(journey.UnreadRequestsCount > 0 ? R.drawable.new_notification_myjourney : R.drawable.notification_myjourney);
        holder.unreadRequests.setTypeface(null, journey.UnreadRequestsCount > 0 ? (Typeface.BOLD) : (Typeface.NORMAL));
        holder.unreadRequests.setText(""+journey.UnreadRequestsCount);

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

                if (constraint == null || constraint.length() == 0) {

                    // set the Original result to return
                    results.count = originalCarShares.size();
                    results.values = originalCarShares;
                } else {
                    constraint = constraint.toString().toLowerCase();
                    for (int i = 0; i < originalCarShares.size(); i++) {
                        String data = (originalCarShares.get(i).GeoAddresses.get(0).AddressLine + " " + originalCarShares.get(i).GeoAddresses.get(originalCarShares.get(i).GeoAddresses.size()-1).AddressLine).replace("->", "");
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

    class JourneyHolder
    {
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
}
