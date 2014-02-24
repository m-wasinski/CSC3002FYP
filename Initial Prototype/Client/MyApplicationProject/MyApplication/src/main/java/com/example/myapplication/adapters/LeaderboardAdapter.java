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
import com.example.myapplication.domain_objects.User;
import com.example.myapplication.experimental.FindNDriveManager;
import com.example.myapplication.interfaces.WCFImageRetrieved;
import com.example.myapplication.network_tasks.WcfPictureServiceTask;

import java.util.ArrayList;

/**
 * Created by Michal on 23/02/14.
 */
public class LeaderboardAdapter extends ArrayAdapter<User> {

    private Context context;
    private int layoutResourceId;
    private ArrayList<User> users;
    private FindNDriveManager findNDriveManager;

    public LeaderboardAdapter(FindNDriveManager findNDriveManager, Context context, int resource, ArrayList<User> users) {
        super(context, resource, users);
        this.context = context;
        this.layoutResourceId = resource;
        this.findNDriveManager = findNDriveManager;
        this.users = users;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View currentRow = convertView;

        final LeaderboardHolder holder;

        if(currentRow == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            currentRow = inflater.inflate(layoutResourceId, parent, false);
            holder = new LeaderboardHolder();

            holder.starRating = new ImageView[5];
            holder.starRating[0] = (ImageView) currentRow.findViewById(R.id.LeaderboardListviewRowStarOneImageView);
            holder.starRating[1]  = (ImageView) currentRow.findViewById(R.id.LeaderboardListviewRowStarTwoImageView);
            holder.starRating[2]  = (ImageView) currentRow.findViewById(R.id.LeaderboardListviewRowStarThreeImageView);
            holder.starRating[3]  = (ImageView) currentRow.findViewById(R.id.LeaderboardListviewRowStarFourImageView);
            holder.starRating[4]  = (ImageView) currentRow.findViewById(R.id.LeaderboardListviewRowStarFiveImageView);

            holder.positionTextView = (TextView) currentRow.findViewById(R.id.LeaderboardListviewRowPositionTextView);
            holder.profileImageView = (ImageView) currentRow.findViewById(R.id.LeaderboardListviewRowProfilePictureImageView);
            holder.nameTextView = (TextView) currentRow.findViewById(R.id.LeaderboardListviewRowNameTextView);
            holder.averageScoreTextView = (TextView) currentRow.findViewById(R.id.LeaderboardListviewRowAverageScoreTextView);

            currentRow.setTag(holder);
        }
        else
        {
            holder = (LeaderboardHolder)currentRow.getTag();
        }

        User user = users.get(position);

        new WcfPictureServiceTask(this.findNDriveManager.getBitmapLruCache(), this.context.getResources().getString(R.string.GetProfilePictureURL),
                user.getProfilePictureId(), this.findNDriveManager.getAuthorisationHeaders(), new WCFImageRetrieved() {
            @Override
            public void onImageRetrieved(Bitmap bitmap) {
                if(bitmap != null)
                {
                    holder.profileImageView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, bitmap.getWidth()/2, bitmap.getHeight()/2, false));
                }
            }
        }).execute();

        for(int i = 0; i < user.getAverageRating(); i++)
        {
            holder.starRating[i].setImageDrawable(this.context.getResources().getDrawable(R.drawable.rating_small));
        }

        holder.nameTextView.setText(user.getFirstName() + " " + user.getLastName() + " (" + user.getUserName()+")");
        holder.averageScoreTextView.setText(String.valueOf(user.getAverageRating()));
        holder.positionTextView.setText("#"+String.valueOf(position+1));
        return currentRow;
    }

    @Override
    public int getCount() {
        return this.users.size();
    }

    private class LeaderboardHolder
    {
        ImageView[] starRating;
        ImageView profileImageView;
        TextView positionTextView;
        TextView nameTextView;
        TextView averageScoreTextView;
    }
}
