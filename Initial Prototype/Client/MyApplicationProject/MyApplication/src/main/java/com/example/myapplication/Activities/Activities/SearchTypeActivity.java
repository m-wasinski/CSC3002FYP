package com.example.myapplication.activities.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.myapplication.R;
import com.example.myapplication.activities.base.BaseActivity;
import com.example.myapplication.constants.IntentConstants;

/**
 * Created by Michal on 05/03/14.
 */
public class SearchTypeActivity extends BaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_step_one);

        findViewById(R.id.SearchStepOneActivityNewSearchLayout).setOnClickListener(this);
        findViewById(R.id.SearchStepOneActivityTemplateLayout).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        Bundle bundle = new Bundle();
        switch (view.getId())
        {
            case R.id.SearchStepOneActivityNewSearchLayout:
                bundle.putInt(IntentConstants.SEARCH_MODE, IntentConstants.SEARCH_MODE_NEW);
                startActivity(new Intent(this, SearchEditorStepOneActivity.class).putExtras(bundle));
                break;
            case R.id.SearchStepOneActivityTemplateLayout:
                bundle.putInt(IntentConstants.SEARCH_MODE, IntentConstants.SEARCH_MODE_FROM_TEMPLATE);
                startActivity(new Intent(this, MyJourneyTemplatesActivity.class).putExtras(bundle));
                break;
        }
    }
}
