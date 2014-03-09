package com.example.myapplication.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.myapplication.R;
import com.example.myapplication.domain_objects.JourneyMessage;
import com.example.myapplication.utilities.DateTimeHelper;

import java.util.ArrayList;

/**
 * Created by Michal on 11/02/14.
 */
public class JourneyChatAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<JourneyMessage> journeyMessages;
    private int userId;

    public JourneyChatAdapter(Context context, ArrayList<JourneyMessage> messages, int userId) {
        super();
        this.context = context;
        this.journeyMessages = messages;
        this.userId = userId;
    }

    @Override
    public int getCount() {
        return this.journeyMessages.size();
    }

    @Override
    public Object getItem(int i) {
        return journeyMessages.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        JourneyMessage message = (JourneyMessage) this.getItem(i);

        ViewHolder holder;

        if(view == null)
        {
            holder = new ViewHolder();
            view = LayoutInflater.from(this.context).inflate(R.layout.listview_row_journey_message, viewGroup, false);
            holder.message = (TextView) view.findViewById(R.id.ListviewRowJourneyMessageMessageTextView);
            holder.date = (TextView) view.findViewById(R.id.ListviewRowJourneyMessageSentOnTextView);
            holder.sender  = (TextView) view.findViewById(R.id.ListviewRowJourneyMessageSenderTextView);
            view.setTag(holder);
        }
        else
            holder = (ViewHolder) view.getTag();

        holder.message.setText(message.getMessageBody());
        holder.date.setText(DateTimeHelper.getSimpleDate(message.getSentOnDate()) + " " + DateTimeHelper.getSimpleTime(message.getSentOnDate()));
        holder.sender.setText(message.getSenderId() == this.userId ? "You: " : message.getSenderUsername()+": ");

        return view;
    }

    private static class ViewHolder
    {
        TextView message;
        TextView date;
        TextView sender;
    }
}
