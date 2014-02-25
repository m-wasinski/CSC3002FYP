package com.example.myapplication.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myapplication.R;
import com.example.myapplication.app_management.AppManager;
import com.example.myapplication.domain_objects.User;
import com.example.myapplication.interfaces.WCFImageRetrieved;
import com.example.myapplication.network_tasks.WcfPictureServiceTask;

import java.util.ArrayList;

/**
 * Created by Michal on 07/02/14.
 */
public class PassengersAdapter extends ArrayAdapter<User> {

    private ArrayList<User> passengers;
    private Context context;
    private int resourceId;
    private AppManager appManager;

    public PassengersAdapter(AppManager appManager, Context context, int resourceId, ArrayList<User> passengers) {
        super(context, resourceId, passengers);

        this.context = context;
        this.passengers = passengers;
        this.resourceId = resourceId;
        this.appManager = appManager;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View currentRow = convertView;
        final PassengerHolder passengerHolder;

        if(currentRow == null)
        {
            LayoutInflater inflater = ((Activity)this.context).getLayoutInflater();
            currentRow = inflater.inflate(resourceId, parent, false);
            passengerHolder = new PassengerHolder();
            passengerHolder.profilePicture = (ImageView) currentRow.findViewById(R.id.AlertDialogShowPassengersPassengerImageView);
            passengerHolder.nameTextView = (TextView) currentRow.findViewById(R.id.AlertDialogShowPassengersPassengerTextView);
            currentRow.setTag(passengerHolder);
        }
        else
        {
            passengerHolder = (PassengerHolder)currentRow.getTag();
        }

        User passenger = this.passengers.get(position);

        passengerHolder.nameTextView.setText(passenger.getFirstName() + " " + passenger.getLastName() + " ("+passenger.getUserName()+")");

        new WcfPictureServiceTask(this.appManager.getBitmapLruCache(), this.context.getResources().getString(R.string.GetProfilePictureURL),
                passenger.getProfilePictureId(), this.appManager.getAuthorisationHeaders(), new WCFImageRetrieved() {
            @Override
            public void onImageRetrieved(Bitmap bitmap) {
                if(bitmap != null)
                {
                    passengerHolder.profilePicture.setImageBitmap(Bitmap.createScaledBitmap(bitmap, bitmap.getWidth()/8, bitmap.getHeight()/8, false));
                }
            }
        }).execute();

        return currentRow;
    }

    class PassengerHolder
    {
        ImageView profilePicture;
        TextView nameTextView;
    }
}
