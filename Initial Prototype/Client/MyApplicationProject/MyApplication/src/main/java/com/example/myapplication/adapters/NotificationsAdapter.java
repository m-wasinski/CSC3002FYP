package com.example.myapplication.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.myapplication.app_management.AppManager;
import com.example.myapplication.domain_objects.Notification;
import com.example.myapplication.utilities.DateTimeHelper;
import com.example.myapplication.R;
import com.example.myapplication.interfaces.WCFImageRetrieved;
import com.example.myapplication.network_tasks.WcfPictureServiceTask;

import java.util.ArrayList;

/**
 * Created by Michal on 06/01/14.
 */
public class NotificationsAdapter extends ArrayAdapter<Notification> {

    private Context context;
    private int layoutResourceId;
    private ArrayList<Notification> notifications;
    private AppManager appManager;

    public NotificationsAdapter(AppManager appManager, Context context, int resource, ArrayList<Notification> notifications) {
        super(context, resource, notifications);
        this.appManager = appManager;
        this.layoutResourceId = resource;
        this.context = context;
        this.notifications = notifications;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View row = convertView;
        final NotificationsHolder holder;

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

        holder.parentRelativeLayout.setBackgroundColor(notification.getDelivered() ?  Color.parseColor("#80151515") : Color.parseColor("#80dea516"));
        holder.hasActionImageView.setVisibility(notification.getTargetObjectId() == -1 ? View.GONE : View.VISIBLE);
        holder.dateTextView.setText(DateTimeHelper.getSimpleDate(notification.getReceivedOnDate()) + " " + DateTimeHelper.getSimpleTime(notification.getReceivedOnDate()));
        holder.notificationHeaderTextView.setText(notification.getNotificationTitle());
        holder.messageTextView.setText(notification.getNotificationMessage());

        if(notification.getProfilePictureId() != -1)
        {
            new WcfPictureServiceTask(this.appManager.getBitmapLruCache(), this.context.getResources().getString(R.string.GetProfilePictureURL),
                    notification.getProfilePictureId(), this.appManager.getAuthorisationHeaders(), new WCFImageRetrieved() {
                @Override
                public void onImageRetrieved(Bitmap bitmap) {
                    if(bitmap != null)
                    {
                        holder.contextImageView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, bitmap.getWidth()/2, bitmap.getHeight()/2, false));
                    }
                }
            }).execute();
        }

        return row;
    }

    private class NotificationsHolder
    {
        ImageView contextImageView;
        ImageView hasActionImageView;
        TextView messageTextView;
        TextView dateTextView;
        TextView notificationHeaderTextView;
        RelativeLayout parentRelativeLayout;
    }
}
