package com.example.myapplication.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.domain_objects.User;
import com.example.myapplication.R;
import com.example.myapplication.dtos.ChatMessageRetrieverDTO;
import com.example.myapplication.experimental.AppData;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WCFServiceTask;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

/**
 * Created by Michal on 04/01/14.
 */
public class FriendsAdapter extends ArrayAdapter<User> {

    private ArrayList<User> travelBuddies;
    private Context context;
    private int layoutResourceId;
    private ArrayList<User> friends;
    private AppData appData;

    public FriendsAdapter(Context context, int resource, ArrayList<User> travelBuddies, ArrayList<User> friends, AppData appData) {
        super(context, resource, travelBuddies);
        this.travelBuddies = travelBuddies;
        this.context = context;
        this.layoutResourceId = resource;
        this.friends = friends;
        this.appData = appData;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        final TravelBuddyHolder holder;

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new TravelBuddyHolder();
            holder.profilePicture = (ImageView) row.findViewById(R.id.TravelBuddyListRowImage);
            holder.userNameTextView = (TextView) row.findViewById(R.id.TravelBuddyListRowNameTextView);
            holder.unreadMessagesCountTextView = (TextView) row.findViewById(R.id.FriendsListRowUnreadMessagesTextView);
            row.setTag(holder);
        }
        else
        {
            holder = (TravelBuddyHolder)row.getTag();
        }

        User travelBuddy = travelBuddies.get(position);

        holder.userNameTextView.setText(travelBuddy.UserName);

        new WCFServiceTask<ChatMessageRetrieverDTO>(getContext().getResources().getString(R.string.GetUnreadMessagesCountForFriendURL),
                new ChatMessageRetrieverDTO(friends.get(position).UserId, appData.getUser().UserId),
                new TypeToken<ServiceResponse<Integer>>() {}.getType(), appData.getAuthorisationHeaders(), new WCFServiceCallback<Integer, Void>() {
            @Override
            public void onServiceCallCompleted(ServiceResponse<Integer> serviceResponse, Void parameter) {
                holder.unreadMessagesCountTextView.setText(serviceResponse.Result + " New messages");
            }
        }).execute();

        return row;
    }

    class TravelBuddyHolder
    {
        ImageView profilePicture;
        TextView userNameTextView;
        TextView unreadMessagesCountTextView;
    }
}
