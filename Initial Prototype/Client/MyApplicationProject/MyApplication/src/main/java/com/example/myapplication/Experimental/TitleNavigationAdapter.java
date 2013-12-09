package com.example.myapplication.Experimental;

/**
 * Created by Michal on 08/12/13.
 */
import java.util.ArrayList;
import android.app.Activity;
import android.content.Context;
import android.support.v7.appcompat.R;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TitleNavigationAdapter extends BaseAdapter {

    private ImageView imgIcon;
    private TextView txtTitle;
    private ArrayList<SpinnerNavigationItem> spinnerNavItem;
    private Context context;

    public TitleNavigationAdapter(Context context,
                                  ArrayList<SpinnerNavigationItem> spinnerNavItem) {
        this.spinnerNavItem = spinnerNavItem;
        this.context = context;
    }

    @Override
    public int getCount() {
        return spinnerNavItem.size();
    }

    @Override
    public Object getItem(int index) {
        return spinnerNavItem.get(index);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater)
                    context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(com.example.myapplication.R.layout.list_item_title_navigation, null);
        }

        imgIcon = (ImageView) convertView.findViewById(com.example.myapplication.R.id.SpinnerNavigationIcon);
        txtTitle = (TextView) convertView.findViewById(com.example.myapplication.R.id.SpinnerNavigationTitle);

        imgIcon.setImageResource(spinnerNavItem.get(position).getIcon());
        imgIcon.setVisibility(View.GONE);
        txtTitle.setText(spinnerNavItem.get(position).getTitle());
        return convertView;
    }


    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater)
                    context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(com.example.myapplication.R.layout.list_item_title_navigation, null);
        }

        imgIcon = (ImageView) convertView.findViewById(com.example.myapplication.R.id.SpinnerNavigationIcon);
        txtTitle = (TextView) convertView.findViewById(com.example.myapplication.R.id.SpinnerNavigationTitle);

        imgIcon.setImageResource(spinnerNavItem.get(position).getIcon());
        txtTitle.setText(spinnerNavItem.get(position).getTitle());
        return convertView;
    }

}