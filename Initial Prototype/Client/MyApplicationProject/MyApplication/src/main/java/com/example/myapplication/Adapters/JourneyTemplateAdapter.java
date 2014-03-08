package com.example.myapplication.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.example.myapplication.R;
import com.example.myapplication.domain_objects.JourneyTemplate;
import com.example.myapplication.utilities.DateTimeHelper;

import java.util.ArrayList;

/**
 * Created by Michal on 05/03/14.
 */
public class JourneyTemplateAdapter extends ArrayAdapter<JourneyTemplate>
{
    private Context context;
    private int layoutResourceId;
    private ArrayList<JourneyTemplate> originalTemplates;
    private ArrayList<JourneyTemplate> displayedTemplates;

    @Override
    public int getCount() {
        return displayedTemplates.size();
    }

    public JourneyTemplateAdapter(Context context, int resource, ArrayList<JourneyTemplate> templates) {
        super(context, resource, templates);
        this.layoutResourceId = resource;
        this.context = context;
        this.originalTemplates = templates;
        this.displayedTemplates = this.originalTemplates;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        final JourneyTemplateHolder holder;

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new JourneyTemplateHolder();
            holder.fromToTextView = (TextView) row.findViewById(R.id.ListViewRowJourneyTemplateAliasTextView);
            holder.dateTextView = (TextView) row.findViewById(R.id.ListViewRowJourneyTemplateCreatedDateTextView);
            row.setTag(holder);
        }
        else
        {
            holder = (JourneyTemplateHolder)row.getTag();
        }

        JourneyTemplate journeyTemplate = displayedTemplates.get(position);

        holder.fromToTextView.setText(journeyTemplate.getAlias() == null ? "" : journeyTemplate.getAlias());
        holder.dateTextView.setText(DateTimeHelper.getSimpleDate(journeyTemplate.getCreationDate()));
        return row;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint,FilterResults results) {

                displayedTemplates = (ArrayList<JourneyTemplate>) results.values; // has the filtered values
                notifyDataSetChanged();  // notifies the data with new filtered values
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();        // Holds the results of a filtering operation in values
                ArrayList<JourneyTemplate> filteredValues = new ArrayList<JourneyTemplate>();

                if (originalTemplates == null) {
                    originalTemplates = displayedTemplates; // saves the original data in mOriginalValues
                }

                if (constraint == null || constraint.length() == 0) {

                    // set the Original result to return
                    results.count = originalTemplates.size();
                    results.values = originalTemplates;
                } else {
                    constraint = constraint.toString().toLowerCase();
                    for (int i = 0; i < originalTemplates.size(); i++) {
                        String data = originalTemplates.get(i).getAlias();
                        if (data.toLowerCase().contains(constraint.toString())) {
                            filteredValues.add(originalTemplates.get(i));
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

    private class JourneyTemplateHolder
    {
        TextView fromToTextView;
        TextView dateTextView;
    }
}
