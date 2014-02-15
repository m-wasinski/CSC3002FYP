package com.example.myapplication.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.myapplication.constants.NotificationContextTypes;
import com.example.myapplication.domain_objects.Notification;
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
        NotificationsHolder holder;

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new NotificationsHolder();
            holder.contextImageView = (ImageView) row.findViewById(R.id.NotificationsActivityContextImageView);
            holder.dateTextView = (TextView) row.findViewById(R.id.NotificationsActivityDateReceivedTextView);
            holder.messageTextView = (TextView)row.findViewById(R.id.NotificationsActivityMessageContentTextView);
            holder.hasActionImageView = (ImageView) row.findViewById(R.id.NotificationListViewRightArrowImageView);
            holder.parentRelativeLayout = (RelativeLayout) row.findViewById(R.id.NotificationListViewRowParentRelativeLayout);
            holder.notificationHeaderTextView = (TextView) row.findViewById(R.id.NotificationListViewRowHeaderTextView);
            row.setTag(holder);
        }
        else
        {
            holder = (NotificationsHolder)row.getTag();
        }

        Notification notification = notifications.get(position);

        holder.parentRelativeLayout.setBackgroundColor(notification.Delivered ?  Color.parseColor("#80151515") : Color.parseColor("#80dea516"));
        holder.hasActionImageView.setVisibility(notification.NotificationPayload.isEmpty() ? View.GONE : View.VISIBLE);
        holder.dateTextView.setText(DateTimeHelper.getSimpleDate(notification.ReceivedOnDate) + " " + DateTimeHelper.getSimpleTime(notification.ReceivedOnDate));
        holder.notificationHeaderTextView.setText(notification.NotificationTitle);
        holder.messageTextView.setText(notification.NotificationMessage);

        int image;

        switch(notification.Context)
        {
            case NotificationContextTypes.Negative:
                image = R.drawable.negative;
                break;
            case NotificationContextTypes.Positive:
                image = R.drawable.positive;
                break;
             default:
                image = R.drawable.neutral;
                break;
        }

        holder.contextImageView.setImageResource(image);

        return row;
    }

    class NotificationsHolder
    {
        ImageView contextImageView;
        ImageView hasActionImageView;
        TextView messageTextView;
        TextView dateTextView;
        TextView notificationHeaderTextView;
        RelativeLayout parentRelativeLayout;
    }
}
