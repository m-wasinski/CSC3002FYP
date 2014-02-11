package com.example.myapplication.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
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
            holder.profilePicture = (ImageView) currentRow.findViewById(R.id.JourneyRequestListViewRowProfileIcon);
            holder.nameTextView = (TextView) currentRow.findViewById(R.id.JourneyRequestListViewRowNameTextView);
            holder.parentLayout = (LinearLayout) currentRow.findViewById(R.id.JourneyRequestListViewRowParentLayout);
            holder.statusTextView = (TextView) currentRow.findViewById(R.id.JourneyRequestListViewRowStatusTextView);
            holder.receivedOnTextView = (TextView) currentRow.findViewById(R.id.JourneyRequestListViewRowReceivedOnTextView);
            holder.decidedOnTextView = (TextView) currentRow.findViewById(R.id.JourneyRequestListViewRowDecidedOnTextView);
            currentRow.setTag(holder);
        }
        else
        {
            holder = (JourneyRequestHolder)currentRow.getTag();
        }

        final JourneyRequest request = journeyRequests.get(position);

        String repliedOnDate = request.Decision == RequestDecision.UNDECIDED ?
                "N/A" : DateTimeHelper.getSimpleDate(request.DecidedOnDate) +  " " + DateTimeHelper.getSimpleTime(request.DecidedOnDate);

        String decisionStatus="";

        holder.parentLayout.setBackgroundColor(request.Read ? Color.rgb(255, 255, 255) : Color.rgb(112, 146, 190));

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

        holder.nameTextView.setText(request.User.FirstName + " " + request.User.LastName  + " ("+request.User.UserName+")");
        holder.receivedOnTextView.setText(DateTimeHelper.getSimpleDate(request.SentOnDate) +  " " + DateTimeHelper.getSimpleTime(request.SentOnDate));
        holder.decidedOnTextView.setText(repliedOnDate);
        holder.profilePicture.setImageResource(R.drawable.user_man);
        holder.statusTextView.setText(decisionStatus);


        return currentRow;
    }

    class JourneyRequestHolder
    {
        ImageView profilePicture;
        TextView nameTextView;
        TextView statusTextView;
        LinearLayout parentLayout;
        TextView receivedOnTextView;
        TextView decidedOnTextView;
    }
}
