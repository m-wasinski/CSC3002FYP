package com.example.myapplication.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.myapplication.constants.RequestDecision;
import com.example.myapplication.domain_objects.JourneyRequest;
import com.example.myapplication.experimental.DateTimeHelper;
import com.example.myapplication.R;

import java.util.ArrayList;

/**
 * Created by Michal on 02/01/14.
 */
public class JourneyRequestAdapter extends ArrayAdapter<JourneyRequest> implements Filterable {

    private ArrayList<JourneyRequest> journeyRequests;
    private Context context;
    private int resourceId;

    public JourneyRequestAdapter(Context context, int resource, ArrayList<JourneyRequest> requests) {
        super(context, resource, requests);
        this.journeyRequests = requests;
        this.context = context;
        this.resourceId = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View currentRow = convertView;
        JourneyRequestHolder holder = new JourneyRequestHolder();

        if(currentRow == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            currentRow = inflater.inflate(resourceId, parent, false);
            holder.profilePicture = (ImageView) currentRow.findViewById(R.id.CarShareRequestAdapterIcon);
            holder.nameTextView = (TextView) currentRow.findViewById(R.id.CarShareRequestAdapterNameTextView);
            holder.parentLayout = (LinearLayout) currentRow.findViewById(R.id.CarShareRequestAdapterParentLayout);
            holder.newTextView =  (TextView) currentRow.findViewById(R.id.CarShareRequestAdapterNewTextView);
            holder.statusTextView = (TextView) currentRow.findViewById(R.id.CarShareRequestAdapterStatusTextView);
            holder.receivedOnTextView = (TextView) currentRow.findViewById(R.id.CarShareRequestAdapterReceivedOnTextView);
            holder.decidedOnTextView = (TextView) currentRow.findViewById(R.id.CarShareRequestAdapterDecidedOnTextView);
            currentRow.setTag(holder);
        }
        else
        {
            holder = (JourneyRequestHolder)currentRow.getTag();
        }

        JourneyRequest request = journeyRequests.get(position);
        String repliedOnDate = DateTimeHelper.getSimpleDate(request.DecidedOnDate) +  " " + DateTimeHelper.getSimpleTime(request.DecidedOnDate);
        String decisionStatus="";

        if(!request.Read)
        {
            holder.parentLayout.setBackgroundColor(Color.rgb(112, 146, 190));
            holder.nameTextView.setTypeface(null, Typeface.BOLD);
            holder.statusTextView.setTypeface(null, Typeface.BOLD);
            holder.receivedOnTextView.setTypeface(null, Typeface.BOLD);
            holder.decidedOnTextView.setTypeface(null, Typeface.BOLD);
            holder.newTextView.setVisibility(View.VISIBLE);
            decisionStatus = "Unread";
            repliedOnDate = "N/A";
        }
        else
        {
            holder.parentLayout.setBackgroundColor(Color.rgb(255, 255, 255));
            holder.nameTextView.setTypeface(null, Typeface.NORMAL);
            holder.statusTextView.setTypeface(null, Typeface.NORMAL);
            holder.receivedOnTextView.setTypeface(null, Typeface.NORMAL);
            holder.decidedOnTextView.setTypeface(null, Typeface.NORMAL);
            holder.newTextView.setVisibility(View.GONE);

            switch(request.Decision)
            {
                case RequestDecision.UNDECIDED:
                    decisionStatus = "Awaiting decision";
                    break;
                case RequestDecision.DENIED:
                    decisionStatus = "Denied";
                    break;
                case RequestDecision.ACCEPTED:
                    decisionStatus = "Accepted";
                    break;
            }
        }

        holder.nameTextView.setText("From: " + request.User.FirstName + " " + request.User.LastName  + " ("+request.User.UserName+")");
        holder.receivedOnTextView.setText("Received: " + DateTimeHelper.getSimpleDate(request.SentOnDate) +  " " + DateTimeHelper.getSimpleTime(request.SentOnDate));
        holder.decidedOnTextView.setText("Replied: " + repliedOnDate);
        holder.profilePicture.setImageResource(R.drawable.user_man);
        holder.statusTextView.setText("Status: " + decisionStatus);

        return currentRow;
    }

    class JourneyRequestHolder
    {
        ImageView profilePicture;
        TextView nameTextView;
        TextView newTextView;
        TextView statusTextView;
        LinearLayout parentLayout;
        TextView receivedOnTextView;
        TextView decidedOnTextView;
    }
}
