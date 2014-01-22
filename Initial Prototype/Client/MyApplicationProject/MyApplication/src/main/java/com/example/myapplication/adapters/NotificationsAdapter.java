package com.example.myapplication.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myapplication.constants.NotificationContext;
import com.example.myapplication.dtos.Notification;
import com.example.myapplication.experimental.DateTimeHelper;
import com.example.myapplication.R;

import java.util.ArrayList;

/**
 * Created by Michal on 06/01/14.
 */
public class NotificationsAdapter extends ArrayAdapter<Notification> {

    private Context context;
    private int layoutResourceId;
    private ArrayList<Notification> notifications;

    public NotificationsAdapter(Context context, int resource, ArrayList<Notification> notifications) {
        super(context, resource, notifications);
        this.layoutResourceId = resource;
        this.context = context;
        this.notifications = notifications;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View row = convertView;
        NotificationsHolder holder = null;

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new NotificationsHolder();
            holder.ContextIconView = (ImageView) row.findViewById(R.id.NotificationsActivityContextImageView);
            holder.DateTextView = (TextView) row.findViewById(R.id.NotificationsActivityDateReceivedTextView);
            holder.MessageTextView = (TextView)row.findViewById(R.id.NotificationsActivityMessageContentTextView);
            row.setTag(holder);
        }
        else
        {
            holder = (NotificationsHolder)row.getTag();
        }

        Notification notification = notifications.get(position);

        holder.DateTextView.setText(DateTimeHelper.getSimpleDate(notification.ReceivedOnDate) + " " + DateTimeHelper.getSimpleTime(notification.ReceivedOnDate));
        holder.MessageTextView.setText(notification.NotificationBody);
        int image;

        switch(notification.Context)
        {
            case NotificationContext.Negative:
                image = R.drawable.denied;
                break;
            case NotificationContext.Positive:
                image = R.drawable.accepted;
                break;
             default:
                image = R.drawable.neutral;
                break;
        }

        holder.ContextIconView.setImageResource(image);

        return row;
    }

    class NotificationsHolder
    {
        ImageView ContextIconView;
        TextView MessageTextView;
        TextView DateTextView;
    }
}
