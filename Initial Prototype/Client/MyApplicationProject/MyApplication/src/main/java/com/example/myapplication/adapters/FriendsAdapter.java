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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.myapplication.app_management.AppManager;
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.constants.StatusConstants;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.domain_objects.User;
import com.example.myapplication.R;
import com.example.myapplication.dtos.ChatMessageRetrieverDTO;
import com.example.myapplication.interfaces.WCFImageRetrieved;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WcfPictureServiceTask;
import com.example.myapplication.network_tasks.WcfPostServiceTask;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

/**
 * Created by Michal on 04/01/14.
 */
public class FriendsAdapter extends ArrayAdapter<User> {

    private Context context;
    private int layoutResourceId;
    private ArrayList<User> friends;
    private AppManager appManager;

    public FriendsAdapter(Context context, int resource, AppManager appManager, ArrayList<User> friends) {
        super(context, resource, friends);
        this.context = context;
        this.layoutResourceId = resource;
        this.appManager = appManager;
        this.friends = friends;
    }

    @Override
    public int getCount() {
        return this.friends.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View row = convertView;

        final FriendHolder holder;

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new FriendHolder();
            holder.profilePicture = (ImageView) row.findViewById(R.id.FriendListViewRowImage);
            holder.userNameTextView = (TextView) row.findViewById(R.id.TravelBuddyListRowNameTextView);
            holder.unreadMessagesCountTextView = (TextView) row.findViewById(R.id.FriendsListRowUnreadMessagesTextView);
            holder.currentOnlineStatus = (ImageView) row.findViewById(R.id.FriendListViewRowStatusIcon);
            holder.parentLayout = (LinearLayout) row.findViewById(R.id.FriendListViewRowParentLayout);
            row.setTag(holder);
        }
        else
        {
            holder = (FriendHolder)row.getTag();
        }

        User friend = this.friends.get(position);

        holder.userNameTextView.setText(friend.getUserName());
        holder.currentOnlineStatus.setImageResource(friend.getStatus() == StatusConstants.Online ? R.drawable.available : R.drawable.unavailable);
        new WcfPostServiceTask<ChatMessageRetrieverDTO>(this.context, getContext().getResources().getString(R.string.GetUnreadMessagesCountForFriendURL),
                new ChatMessageRetrieverDTO(friends.get(position).getUserId(), appManager.getUser().getUserId(), null),
                new TypeToken<ServiceResponse<Integer>>() {}.getType(), appManager.getAuthorisationHeaders(), new WCFServiceCallback<Integer, Void>() {
            @Override
            public void onServiceCallCompleted(ServiceResponse<Integer> serviceResponse, Void parameter) {
                if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
                {
                    holder.unreadMessagesCountTextView.setText(String.valueOf(serviceResponse.Result));
                    holder.unreadMessagesCountTextView.setVisibility(serviceResponse.Result > 0 ? View.VISIBLE : View.GONE);
                    holder.parentLayout.setBackgroundColor(serviceResponse.Result > 0 ? Color.parseColor("#80dea516") : Color.parseColor("#80151515"));
                }
            }
        }).execute();

        new WcfPictureServiceTask(this.appManager.getBitmapLruCache(), this.context.getResources().getString(R.string.GetProfilePictureURL),
                friend.getUserId(), this.appManager.getAuthorisationHeaders(), new WCFImageRetrieved() {
            @Override
            public void onImageRetrieved(Bitmap bitmap) {
                if(bitmap != null)
                {
                    holder.profilePicture.setImageBitmap(Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), false));
                }
            }
        }).execute();

        return row;
    }

    class FriendHolder
    {
        ImageView profilePicture;
        ImageView currentOnlineStatus;
        TextView userNameTextView;
        TextView unreadMessagesCountTextView;
        LinearLayout parentLayout;
    }
}
