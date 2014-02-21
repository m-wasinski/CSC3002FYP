package com.example.myapplication.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myapplication.R;
import com.example.myapplication.domain_objects.Journey;
import com.example.myapplication.domain_objects.Rating;
import com.example.myapplication.experimental.DateTimeHelper;
import com.example.myapplication.utilities.Utilities;

import java.util.ArrayList;

/**
 * Created by Michal on 21/02/14.
 */
public class RatingsAdapter  extends ArrayAdapter<Rating> {

    private Context context;
    private int layoutResourceId;
    private ArrayList<Rating> ratings;

    public RatingsAdapter(Context context, int resource, ArrayList<Rating> ratings) {
        super(context, resource, ratings);
        this.layoutResourceId = resource;
        this.context = context;
        this.ratings = ratings;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View currentRow = convertView;
        RatingsHolder holder;

        if(currentRow == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            currentRow = inflater.inflate(layoutResourceId, parent, false);
            holder = new RatingsHolder();

            holder.starRating = new ImageView[5];
            holder.starRating[0] = (ImageView) currentRow.findViewById(R.id.RatingListViewRowStarOneButton);
            holder.starRating[1]  = (ImageView) currentRow.findViewById(R.id.RatingListViewRowStarTwoButton);
            holder.starRating[2]  = (ImageView) currentRow.findViewById(R.id.RatingListViewRowStarThreeButton);
            holder.starRating[3]  = (ImageView) currentRow.findViewById(R.id.RatingListViewRowStarFourButton);
            holder.starRating[4]  = (ImageView) currentRow.findViewById(R.id.RatingListViewRowStarFiveButton);

            holder.receivedOnDateTextView = (TextView) currentRow.findViewById(R.id.RatingListViewRowLeftOnTextView);
            holder.feedbackTextView = (TextView) currentRow.findViewById(R.id.RatingListViewRowFeedbackTextView);
            holder.leftByTextView = (TextView) currentRow.findViewById(R.id.RatingListViewRowLeftByTextView);

            currentRow.setTag(holder);
        }
        else
        {
            holder = (RatingsHolder)currentRow.getTag();
        }

        Rating rating = ratings.get(position);

        for(int i = 0; i < rating.getScore(); i++)
        {
            holder.starRating[i].setImageDrawable(this.context.getResources().getDrawable(R.drawable.rating_small));
        }

        holder.feedbackTextView.setText(rating.getFeedback());
        holder.leftByTextView.setText(rating.getFromUser().getUserName());
        holder.receivedOnDateTextView.setText(DateTimeHelper.getSimpleDate(rating.getLeftOnDate()));
        return currentRow;
    }

    private class RatingsHolder
    {
        ImageView[] starRating;
        TextView receivedOnDateTextView;
        TextView feedbackTextView;
        TextView leftByTextView;
    }
}
