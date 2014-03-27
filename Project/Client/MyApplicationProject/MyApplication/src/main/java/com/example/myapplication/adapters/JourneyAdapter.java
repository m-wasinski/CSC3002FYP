package com.example.myapplication.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myapplication.R;
import com.example.myapplication.constants.JourneyStatus;
import com.example.myapplication.domain_objects.Journey;
import com.example.myapplication.domain_objects.User;
import com.example.myapplication.utilities.DateTimeHelper;
import com.example.myapplication.utilities.Utilities;

import java.util.ArrayList;

/**
 * Created by Michal on 29/11/13.
 */
public class JourneyAdapter extends ArrayAdapter<Journey> {

    private Context context;
    private int layoutResourceId;
    private ArrayList<Journey> originalJourneys;
    private ArrayList<Journey> displayedJourneys;
    private User user;

    @Override
    public int getCount() {
        return displayedJourneys.size();
    }

    public JourneyAdapter(Context context, int resource, ArrayList<Journey> journeys, User user) {
        super(context, resource, journeys);
        this.layoutResourceId = resource;
        this.context = context;
        this.originalJourneys = journeys;
        this.displayedJourneys = this.originalJourneys;
        this.user = user;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        final JourneyHolder holder;

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

        final Journey journey = displayedJourneys.get(position);

        String statusText = "";

        switch(journey.getJourneyStatus())
        {
            case JourneyStatus.OK:
                statusText = "OK";
                break;
            case JourneyStatus.Cancelled:
                statusText = "Cancelled";
                break;
            case JourneyStatus.Expired:
                statusText = "Expired";
                break;
        }

        holder.journeyId.setText(String.valueOf(journey.getJourneyId()));
        holder.fromTo.setText(Utilities.getJourneyHeader(journey.getGeoAddresses()));
        holder.departureDate.setText(DateTimeHelper.getSimpleDate(journey.getDateAndTimeOfDeparture()));
        holder.departureTime.setText(DateTimeHelper.getSimpleTime(journey.getDateAndTimeOfDeparture()));
        holder.availableSeats.setText(String.valueOf(journey.getAvailableSeats()));
        holder.statusTextView.setText(statusText);
        holder.creationDateTextView.setText(DateTimeHelper.getSimpleDate(journey.getCreationDate()));
        holder.modeTextView.setText(journey.getDriver().getUserId() == user.getUserId() ? "Driver" : "Passenger");
        holder.newRequestIcon.setImageResource(journey.getDecidedRequestsCount() > 0 ? R.drawable.new_notification_myjourney : R.drawable.notification_myjourney);
        holder.unreadRequests.setText(""+journey.getDecidedRequestsCount());
        holder.unreadMessages.setText(String.valueOf(journey.getUnreadMessagesCount()));
        holder.newMessagesIconView.setImageResource(journey.getUnreadMessagesCount() > 0 ? R.drawable.new_journey_message : R.drawable.envelope_blue);

        return row;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint,FilterResults results) {

                displayedJourneys = (ArrayList<Journey>) results.values; // has the filtered values
                notifyDataSetChanged();  // notifies the data with new filtered values
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();        // Holds the results of a filtering operation in values
                ArrayList<Journey> filteredValues = new ArrayList<Journey>();

                if (originalJourneys == null) {
                    originalJourneys = displayedJourneys; // saves the original data in mOriginalValues
                }

                if (constraint == null || constraint.length() == 0) {

                    // set the Original result to return
                    results.count = originalJourneys.size();
                    results.values = originalJourneys;
                } else {
                    constraint = constraint.toString().toLowerCase();
                    for (int i = 0; i < originalJourneys.size(); i++) {
                        String data = Utilities.getJourneyHeader(originalJourneys.get(i).getGeoAddresses());
                        if (data.toLowerCase().contains(constraint.toString())) {
                            filteredValues.add(originalJourneys.get(i));
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
