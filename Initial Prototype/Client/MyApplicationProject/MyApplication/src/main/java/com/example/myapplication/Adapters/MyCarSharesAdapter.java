package com.example.myapplication.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.example.myapplication.Activities.Activities.CarShareChatActivity;
import com.example.myapplication.Activities.Activities.CarShareRequestsActivity;
import com.example.myapplication.Constants.CarShareStatus;
import com.example.myapplication.DomainObjects.CarShare;
import com.example.myapplication.Experimental.DateTimeHelper;
import com.example.myapplication.R;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * Created by Michal on 29/11/13.
 */
public class MyCarSharesAdapter extends ArrayAdapter<CarShare> {

    private Context context;
    private int layoutResourceId;
    private int userId;
    private ArrayList<CarShare> CarShares= null;
    private Mode mode;
    private final List<String> menuAsDriver = asList("Show requests", "Show details", "Show on map", "Show chat","Show passengers", "Make change", "Cancel Journey");
    private final List<String> menuAsPassenger = asList("Show details", "Show on map", "Show chat", "Show passengers", "Withdraw from journey", "Rate Driver");

    public MyCarSharesAdapter(int user, Context context, int resource, ArrayList<CarShare> carShares) {
        super(context, resource, carShares);
        this.layoutResourceId = resource;
        this.context = context;
        this.CarShares = carShares;
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
            holder.popupMenuTextView = (TextView) row.findViewById(R.id.MyCarSharesMenuTextView);
            holder.journeyId = (TextView) row.findViewById(R.id.MyCarSharesJourneyIdTextView);
            holder.unreadMessages = (TextView) row.findViewById(R.id.MyCarSharesNumberOfUnreadMessagesTextView);
            holder.newMessagesIconView = (ImageView) row.findViewById(R.id.MyCarSharesNewMessagesIcon);
            row.setTag(holder);
        }
        else
        {
            holder = (CarShareHolder)row.getTag();
        }

        final CarShare carShare = CarShares.get(position);
        int statusIconResource = 0;
        String statusText = "";

        switch(carShare.CarShareStatus)
        {
            case CarShareStatus.Upcoming:
                statusIconResource = R.drawable.upcoming;
                statusText = "Upcoming";
                break;
            case CarShareStatus.Cancelled:
                statusIconResource = R.drawable.cancelled;
                statusText = "Cancelled";
                break;
            case CarShareStatus.Past:
                statusIconResource = R.drawable.past;
                statusText = "Expired";
                break;
        }

        holder.journeyId.setText("Journey id: " + carShare.CarShareId);
        //holder.fromTo.setText(carShare.DepartureCity + " -> " + carShare.DestinationCity);
        //holder.departureDate.setText("Date: " + DateTimeHelper.getSimpleDate(carShare.DateAndTimeOfDeparture));
        holder.departureTime.setText("Time: " + DateTimeHelper.getSimpleTime(carShare.DateAndTimeOfDeparture));
        holder.availableSeats.setText("Available seats: " + carShare.AvailableSeats);
        holder.modeIcon.setImageResource(R.drawable.taxi);
        holder.statusIcon.setImageResource(statusIconResource);
        holder.statusTextView.setText(statusText);


        if(carShare.DriverId == userId)
        {
            holder.modeIcon.setImageResource(R.drawable.steering_wheel);
            holder.modeTextView.setText("Driver");
            mode = mode.Driver;
        }
        else
        {
            holder.modeTextView.setText("Passenger");
        }

        if(carShare.UnreadRequestsCount > 0)
        {
            holder.newRequestIcon.setImageResource(R.drawable.new_request);
            holder.unreadRequests.setTypeface(null, Typeface.BOLD);
            holder.fromTo.setTextColor(Color.rgb(45, 142, 28));
            holder.unreadRequests.setText("("+carShare.UnreadRequestsCount+")");
        }
        else
        {
            holder.newRequestIcon.setImageResource(R.drawable.no_new_requests);
            holder.unreadRequests.setTypeface(null, Typeface.NORMAL);
            holder.fromTo.setTextColor(Color.rgb(0, 134, 201));
        }

        final PopupMenu popupMenu = new PopupMenu(this.context, row.findViewById(R.id.MyCarSharesMenuTextView));

        if(mode == Mode.Driver)
        {
            for(int i = 0; i < menuAsDriver.size(); i++)
            {
                popupMenu.getMenu().add(Menu.NONE, i, Menu.NONE, menuAsDriver.get(i));
            }

        }
        else
        {
            for(int i = 0; i < menuAsPassenger.size(); i++)
            {
                popupMenu.getMenu().add(Menu.NONE, i, Menu.NONE, menuAsPassenger.get(i));
            }
        }

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                if(mode == mode.Driver)
                {
                    switch(menuItem.getItemId())
                    {
                        case 0:
                            startRequestsActivity(carShare);
                            break;
                        case 3:
                            startChatActivity(carShare);
                            break;
                    }
                }
                else
                {

                }

                return false;
            }
        });

        holder.popupMenuTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupMenu.show();
            }
        });
        return row;
    }

    private void startRequestsActivity(CarShare carShare)
    {
        Gson gson = new Gson();
        Intent intent = new Intent(this.context, CarShareRequestsActivity.class);
        intent.putExtra("CurrentCarShare", gson.toJson(carShare));
        this.context.startActivity(intent);
    }

    private void startChatActivity(CarShare carShare)
    {
        Gson gson = new Gson();
        Intent intent = new Intent(this.context, CarShareChatActivity.class);
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
        TextView popupMenuTextView;
        TextView unreadMessages;
    }

    enum Mode{
        Driver,
        Passenger
    }
}
