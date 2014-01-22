package com.example.myapplication.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myapplication.constants.StatusConstants;
import com.example.myapplication.dtos.ServiceResponse;
import com.example.myapplication.dtos.User;
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

    private Context context;
    private int layoutResourceId;
    private ArrayList<User> friends;
    private AppData appData;

    public FriendsAdapter(Context context, int resource, AppData appData, ArrayList<User> friends) {
        super(context, resource, appData.getFriends());
        this.context = context;
        this.layoutResourceId = resource;
        this.appData = appData;
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
            row.setTag(holder);
        }
        else
        {
            holder = (FriendHolder)row.getTag();
        }

        User friend = this.friends.get(position);

        holder.userNameTextView.setText(friend.UserName);
        holder.currentOnlineStatus.setImageResource(friend.Status == StatusConstants.Online ? R.drawable.available : R.drawable.unavailable);
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

    class FriendHolder
    {
        ImageView profilePicture;
        ImageView currentOnlineStatus;
        TextView userNameTextView;
        TextView unreadMessagesCountTextView;
    }
}
