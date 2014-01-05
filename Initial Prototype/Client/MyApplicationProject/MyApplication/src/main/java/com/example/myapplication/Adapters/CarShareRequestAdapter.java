package com.example.myapplication.Adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.myapplication.Constants.Constants;
import com.example.myapplication.DomainObjects.CarShareRequest;
import com.example.myapplication.R;

import java.util.ArrayList;

/**
 * Created by Michal on 02/01/14.
 */
public class CarShareRequestAdapter extends ArrayAdapter<CarShareRequest> {

    private ArrayList<CarShareRequest> carShareRequests;
    private Context context;
    private int layoutResourceId;

    public CarShareRequestAdapter(Context context, int resource, ArrayList<CarShareRequest> requests) {
        super(context, resource, requests);
        this.carShareRequests = requests;
        this.context = context;
        this.layoutResourceId = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        CarShareRequestHolder holder = null;

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new CarShareRequestHolder();
            holder.profilePicture = (ImageView) row.findViewById(R.id.CarShareRequestAdapterIcon);
            holder.nameTextView = (TextView) row.findViewById(R.id.CarShareRequestAdapterNameTextView);
            holder.parentLayout = (LinearLayout) row.findViewById(R.id.CarShareRequestAdapterParentLayout);
            holder.newTextView =  (TextView) row.findViewById(R.id.CarShareRequestAdapterNewTextView);
            holder.statusTextView = (TextView) row.findViewById(R.id.CarShareRequestAdapterStatusTextView);
            row.setTag(holder);
        }
        else
        {
            holder = (CarShareRequestHolder)row.getTag();
        }

        CarShareRequest request = carShareRequests.get(position);


        holder.nameTextView.setText("Request From: " + request.User.UserName);

        String decisionStatus = "Accepted";

        if(carShareRequests.get(position).Decision == Constants.DENIED)
        {
            decisionStatus = "Denied";
        }

        holder.profilePicture.setImageResource(R.drawable.user_man);

        if(!request.Read)
        {
            holder.parentLayout.setBackgroundColor(Color.rgb(112, 146, 190));
            holder.nameTextView.setTypeface(null, Typeface.BOLD);
            holder.statusTextView.setTypeface(null, Typeface.BOLD);
            holder.newTextView.setVisibility(View.VISIBLE);
            decisionStatus = "Unread";
        }

        holder.statusTextView.setText("Status: " + decisionStatus);
        return row;
    }

    static class CarShareRequestHolder
    {
        ImageView profilePicture;
        TextView nameTextView;
        TextView newTextView;
        TextView statusTextView;
        LinearLayout parentLayout;
    }
}
