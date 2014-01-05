package com.example.myapplication.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.myapplication.DomainObjects.CarShareRequest;
import com.example.myapplication.DomainObjects.User;
import com.example.myapplication.R;

import java.util.ArrayList;

/**
 * Created by Michal on 04/01/14.
 */
public class TravelBuddiesAdapter extends ArrayAdapter<User> {

    private ArrayList<User> travelBuddies;
    private Context context;
    private int layoutResourceId;

    public TravelBuddiesAdapter(Context context, int resource, ArrayList<User> travelBuddies) {
        super(context, resource, travelBuddies);
        this.travelBuddies = travelBuddies;
        this.context = context;
        this.layoutResourceId = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        TravelBuddyHolder holder = null;

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new TravelBuddyHolder();
            holder.profilePicture = (ImageView) row.findViewById(R.id.TravelBuddyListRowImage);
            holder.userNameTextView = (TextView) row.findViewById(R.id.TravelBuddyListRowNameTextView);
            row.setTag(holder);
        }
        else
        {
            holder = (TravelBuddyHolder)row.getTag();
        }

        User travelBuddy = travelBuddies.get(position);

        holder.userNameTextView.setText(travelBuddy.UserName);

        return row;
    }

    class TravelBuddyHolder
    {
        ImageView profilePicture;
        TextView userNameTextView;
    }
}
