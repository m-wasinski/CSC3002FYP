package com.example.myapplication.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ListView;

import com.example.myapplication.Adapters.CarSharesListViewAdapter;
import com.example.myapplication.DomainObjects.CarShare;
import com.example.myapplication.Adapters.SearchResultsAdapter;
import com.example.myapplication.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Michal on 01/12/13.
 */
public class SearchResultsActivity extends Activity {

    private SearchResultsAdapter searchResultsAdapter;
    private ExpandableListView expandableListView;
    private ArrayList listHeaders;
    private HashMap<Object, List<Object>> listChildren;
    private ArrayList<CarShare> carShares;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.search_results);

        Gson gson = new Gson();

        Type type = new TypeToken<ArrayList<CarShare>>() {}.getType();
        carShares = gson.fromJson(getIntent().getExtras().getString("CarShares"), type);

        ListView listView = (ListView) findViewById(R.id.MyCarSharesListView);
        CarSharesListViewAdapter adapter = new CarSharesListViewAdapter(0, this, R.layout.my_car_shares_fragment_custom_listview_row_layout, carShares);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(view.getContext(), CarShareDetailsActivity.class);
                Gson gson = new Gson();
                intent.putExtra("CurrentCarShare", gson.toJson(carShares.get(i)));
                startActivity(intent);
            }
        });
    }
}
