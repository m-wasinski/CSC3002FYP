package com.example.myapplication.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myapplication.R;
import com.example.myapplication.domain_objects.User;

import java.util.ArrayList;

/**
 * Created by Michal on 07/02/14.
 */
public class PassengersAdapter extends ArrayAdapter<User> {

    private ArrayList<User> passengers;
    private Context context;
    private int resourceId;

    public PassengersAdapter(Context context, int resourceId, ArrayList<User> passengers) {
        super(context, resourceId, passengers);

        this.context = context;
        this.passengers = passengers;
        this.resourceId = resourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View currentRow = convertView;
        PassengerHolder passengerHolder = new PassengerHolder();

        if(currentRow == null)
        {
            LayoutInflater inflater = ((Activity)this.context).getLayoutInflater();
            currentRow = inflater.inflate(resourceId, parent, false);
            passengerHolder.profilePicture = (ImageView) currentRow.findViewById(R.id.AlertDialogShowPassengersPassengerImageView);
            passengerHolder.nameTextView = (TextView) currentRow.findViewById(R.id.AlertDialogShowPassengersPassengerTextView);
            currentRow.setTag(passengerHolder);
        }
        else
        {
            passengerHolder = (PassengerHolder)currentRow.getTag();
        }

        User passenger = this.passengers.get(position);

        passengerHolder.profilePicture.setImageResource(R.drawable.user_man);
        passengerHolder.nameTextView.setText(passenger.getFirstName() + " " + passenger.getLastName() + " ("+passenger.getUserName()+")");

        return currentRow;
    }

    class PassengerHolder
    {
        ImageView profilePicture;
        TextView nameTextView;
    }
}
