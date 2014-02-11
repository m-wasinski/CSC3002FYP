package com.example.myapplication.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.example.myapplication.R;
import com.example.myapplication.domain_objects.ChatMessage;
import com.example.myapplication.experimental.DateTimeHelper;

import java.util.ArrayList;
/**
 * Created by Michal on 18/01/14.
 */
public class ChatAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<ChatMessage> mMessages;
    private int userId;
    public ChatAdapter(Context context, ArrayList<ChatMessage> messages, int userId) {
        super();
        this.mContext = context;
        this.mMessages = messages;
        this.userId = userId;
    }
    @Override
    public int getCount() {
        return mMessages.size();
    }
    @Override
    public Object getItem(int position) {
        return mMessages.get(position);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ChatMessage message = (ChatMessage) this.getItem(position);

        ViewHolder holder;
        if(convertView == null)
        {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.listview_row_chat_bubble, parent, false);
            holder.message = (TextView) convertView.findViewById(R.id.InstantMessengerRowMessageTextView);
            holder.date = (TextView) convertView.findViewById(R.id.InstantMessengerRowDateTextView);
            convertView.setTag(holder);
        }
        else
            holder = (ViewHolder) convertView.getTag();

        holder.message.setText(message.MessageBody);
        holder.date.setText(DateTimeHelper.getSimpleDate(message.SentOnDate) + " " + DateTimeHelper.getSimpleTime(message.SentOnDate));
        LayoutParams layoutParams = (LayoutParams) holder.message.getLayoutParams();
        LayoutParams layoutParams1 = (LayoutParams) holder.date.getLayoutParams();

        //Check whether message is mine to show green background and align to right
        if(message.SenderId == this.userId)
        {
            holder.message.setTextColor(Color.parseColor("#32B4E4"));
            layoutParams.gravity = Gravity.RIGHT;
            layoutParams1.gravity = Gravity.RIGHT;
        }
        else
        {
            holder.message.setTextColor(Color.parseColor("#FFFFFF"));
            layoutParams.gravity = Gravity.LEFT;
            layoutParams1.gravity = Gravity.LEFT;
        }
        holder.message.setLayoutParams(layoutParams);
        holder.date.setLayoutParams(layoutParams1);
       // holder.message.setTextColor(R.color.textColor);

        return convertView;
    }
    private static class ViewHolder
    {
        TextView message;
        TextView date;
    }

    @Override
    public long getItemId(int position) {
        //Unimplemented, because we aren't using Sqlite.
        return 0;
    }

}

