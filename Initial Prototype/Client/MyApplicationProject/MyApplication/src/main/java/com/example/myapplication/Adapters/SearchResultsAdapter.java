package com.example.myapplication.Adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Michal on 01/12/13.
 */
public class SearchResultsAdapter extends BaseExpandableListAdapter {

    private Context _context;
    private ArrayList<Object> _listDataHeaders; // header titles
    // child data in format of header title, child title
    private HashMap<Object, List<Object>> _listDataChild;


    public SearchResultsAdapter(Context context,ArrayList listDataHeaders,HashMap<Object, List<Object>> listChildData) {
        this._context = context;
        this._listDataHeaders = listDataHeaders;
        this._listDataChild = listChildData;
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return this._listDataChild.get(this._listDataHeaders.get(groupPosition))
                .get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        final String childText = (String) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(com.example.myapplication.R.layout.search_results_listview_details, null);
        }

        TextView txtListChild = (TextView) convertView
                .findViewById(com.example.myapplication.R.id.TestTextView);

        txtListChild.setText(childText);
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this._listDataChild.get(this._listDataHeaders.get(groupPosition))
                .size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this._listDataHeaders.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this._listDataHeaders.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {

        String carShareHeader[] = ((String) getGroup(groupPosition)).split(",[ ]*");

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(com.example.myapplication.R.layout.search_results_listview_header, null);
        }

        TextView citites = (TextView) convertView.findViewById(R.id.SearchResultsCitiesTextView);

        TextView date = (TextView) convertView.findViewById(R.id.SearchResultsDateTextView);

        TextView availableSeats = (TextView) convertView.findViewById(R.id.SearchResultsAvailableSeatsTextView);

        TextView time = (TextView) convertView.findViewById(R.id.SearchResultsTimeTextView);

        citites.setText(carShareHeader[0]);

        date.setText(carShareHeader[1]);

        availableSeats.setText(carShareHeader[2]);

        time.setText(carShareHeader[3]);

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
