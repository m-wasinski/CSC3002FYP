package com.example.myapplication.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.myapplication.constants.RequestDecision;
import com.example.myapplication.domain_objects.JourneyRequest;
import com.example.myapplication.experimental.DateTimeHelper;
import com.example.myapplication.R;
import com.example.myapplication.experimental.FindNDriveManager;
import com.example.myapplication.interfaces.WCFImageRetrieved;
import com.example.myapplication.network_tasks.WcfPictureServiceTask;

import java.util.ArrayList;

/**
 * Created by Michal on 02/01/14.
 */
public class JourneyRequestAdapter extends ArrayAdapter<JourneyRequest> implements Filterable {

    private ArrayList<JourneyRequest> journeyRequests;
    private Context context;
    private int resourceId;
    private FindNDriveManager findNDriveManager;

    public JourneyRequestAdapter(FindNDriveManager findNDriveManager, Context context, int resource, ArrayList<JourneyRequest> requests) {
        super(context, resource, requests);
        this.journeyRequests = requests;
        this.context = context;
        this.resourceId = resource;
        this.findNDriveManager = findNDriveManager;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View currentRow = convertView;
        final JourneyRequestHolder journeyRequestHolder;

        if(currentRow == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            currentRow = inflater.inflate(resourceId, parent, false);
            journeyRequestHolder = new JourneyRequestHolder();
            journeyRequestHolder.profilePicture = (ImageView) currentRow.findViewById(R.id.JourneyRequestListViewRowProfileIcon);
            journeyRequestHolder.nameTextView = (TextView) currentRow.findViewById(R.id.JourneyRequestListViewRowNameTextView);
            journeyRequestHolder.parentLayout = (LinearLayout) currentRow.findViewById(R.id.JourneyRequestListViewRowParentLayout);
            journeyRequestHolder.statusTextView = (TextView) currentRow.findViewById(R.id.JourneyRequestListViewRowStatusTextView);
            journeyRequestHolder.receivedOnTextView = (TextView) currentRow.findViewById(R.id.JourneyRequestListViewRowReceivedOnTextView);
            journeyRequestHolder.decidedOnTextView = (TextView) currentRow.findViewById(R.id.JourneyRequestListViewRowDecidedOnTextView);
            currentRow.setTag(journeyRequestHolder);
        }
        else
        {
            journeyRequestHolder = (JourneyRequestHolder)currentRow.getTag();
        }

        final JourneyRequest request = journeyRequests.get(position);

        String repliedOnDate = request.Decision == RequestDecision.UNDECIDED ?
                "N/A" : DateTimeHelper.getSimpleDate(request.DecidedOnDate) +  " " + DateTimeHelper.getSimpleTime(request.DecidedOnDate);

        String decisionStatus="";

        journeyRequestHolder.parentLayout.setBackgroundColor(request.Read ? Color.rgb(255, 255, 255) : Color.rgb(112, 146, 190));

        switch(request.Decision)
        {
            case RequestDecision.UNDECIDED:
                decisionStatus = "Awaiting decision";
                break;
            case RequestDecision.DENIED:
                decisionStatus = "Denied";
                break;
            case RequestDecision.ACCEPTED:
                decisionStatus = "Accepted";
                break;
        }

        journeyRequestHolder.nameTextView.setText(request.User.getFirstName() + " " + request.User.getLastName()  + " ("+request.User.getUserName()+")");
        journeyRequestHolder.receivedOnTextView.setText(DateTimeHelper.getSimpleDate(request.SentOnDate) +  " " + DateTimeHelper.getSimpleTime(request.SentOnDate));
        journeyRequestHolder.decidedOnTextView.setText(repliedOnDate);
        journeyRequestHolder.statusTextView.setText(decisionStatus);

        new WcfPictureServiceTask(this.findNDriveManager.getBitmapLruCache(), this.context.getResources().getString(R.string.GetProfilePictureURL),
                request.UserId, this.findNDriveManager.getAuthorisationHeaders(), new WCFImageRetrieved() {
            @Override
            public void onImageRetrieved(Bitmap bitmap) {
                if(bitmap != null)
                {
                    journeyRequestHolder.profilePicture.setImageBitmap(Bitmap.createScaledBitmap(bitmap, bitmap.getWidth()/8, bitmap.getHeight()/8, false));
                }
            }
        }).execute();

        return currentRow;
    }

    class JourneyRequestHolder
    {
        ImageView profilePicture;
        TextView nameTextView;
        TextView statusTextView;
        LinearLayout parentLayout;
        TextView receivedOnTextView;
        TextView decidedOnTextView;
    }
}
