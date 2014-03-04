package com.example.myapplication.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.myapplication.R;
import com.example.myapplication.app_management.AppManager;
import com.example.myapplication.constants.StatusConstants;
import com.example.myapplication.domain_objects.User;
import com.example.myapplication.interfaces.WCFImageRetrieved;
import com.example.myapplication.network_tasks.WcfPictureServiceTask;

import java.util.ArrayList;

/**
 * Created by Michal on 04/01/14.
 */
public class FriendsAdapter extends ArrayAdapter<User> {

    private Context context;
    private int layoutResourceId;
    private ArrayList<User> displayedFriendsList;
    private ArrayList<User> originalFriendsList;
    private AppManager appManager;

    public FriendsAdapter(Context context, int resource, AppManager appManager, ArrayList<User> friends) {
        super(context, resource, friends);
        this.context = context;
        layoutResourceId = resource;
        this.appManager = appManager;
        originalFriendsList = friends;
        displayedFriendsList = originalFriendsList;
    }

    @Override
    public int getCount() {
        return displayedFriendsList.size();
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

        User friend = displayedFriendsList.get(position);

        holder.userNameTextView.setText(friend.getUserName());
        holder.currentOnlineStatus.setImageResource(friend.getStatus() == StatusConstants.Online ? R.drawable.available : R.drawable.unavailable);
        holder.unreadMessagesCountTextView.setText(String.valueOf(friend.getUnreadMessagesCount()));
        holder.unreadMessagesCountTextView.setVisibility(friend.getUnreadMessagesCount() > 0 ? View.VISIBLE : View.GONE);
        holder.parentLayout.setBackgroundColor(friend.getUnreadMessagesCount() > 0 ? Color.parseColor("#80dea516") : Color.parseColor("#80151515"));

        /*new WcfPostServiceTask<ChatMessageRetrieverDTO>(context, getContext().getResources().getString(R.string.GetUnreadMessagesCountForFriendURL),
                new ChatMessageRetrieverDTO(displayedFriendsList.get(position).getUserId(), appManager.getUser().getUserId(), null),
                new TypeToken<ServiceResponse<Integer>>() {}.getType(), appManager.getAuthorisationHeaders(), new WCFServiceCallback<Integer, Void>() {
            @Override
            public void onServiceCallCompleted(ServiceResponse<Integer> serviceResponse, Void parameter) {
                if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
                {

                }
            }
        }).execute();*/

        if(holder.profilePicture.getDrawable() == null)
        {
            new WcfPictureServiceTask(appManager.getBitmapLruCache(), context.getResources().getString(R.string.GetProfilePictureURL),
                    friend.getUserId(), appManager.getAuthorisationHeaders(), new WCFImageRetrieved() {
                @Override
                public void onImageRetrieved(Bitmap bitmap) {
                    if(bitmap != null)
                    {
                        holder.profilePicture.setImageBitmap(Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), false));
                    }
                }
            }).execute();
        }

        return row;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint,FilterResults results) {

                displayedFriendsList = (ArrayList<User>) results.values; // has the filtered values
                notifyDataSetChanged();  // notifies the data with new filtered values
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();        // Holds the results of a filtering operation in values
                ArrayList<User> filteredValues = new ArrayList<User>();

                if (originalFriendsList == null) {
                    originalFriendsList = displayedFriendsList; // saves the original data in mOriginalValues
                }

                if (constraint == null || constraint.length() == 0) {

                    // set the Original result to return
                    results.count = originalFriendsList.size();
                    results.values = originalFriendsList;
                } else {
                    constraint = constraint.toString().toLowerCase();
                    for (int i = 0; i < originalFriendsList.size(); i++) {
                        String data = originalFriendsList.get(i).getUserName();
                        if (data.toLowerCase().contains(constraint.toString())) {
                            filteredValues.add(originalFriendsList.get(i));
                        }
                    }
                    // set the Filtered result to return
                    results.count = filteredValues.size();
                    results.values = filteredValues;
                }
                return results;
            }
        };
        return filter;
    }

    private class FriendHolder
    {
        ImageView profilePicture;
        ImageView currentOnlineStatus;
        TextView userNameTextView;
        TextView unreadMessagesCountTextView;
        LinearLayout parentLayout;
    }
}
