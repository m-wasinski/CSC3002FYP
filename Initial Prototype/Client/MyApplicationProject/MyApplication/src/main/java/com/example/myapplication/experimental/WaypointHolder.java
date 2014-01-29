package com.example.myapplication.experimental;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.example.myapplication.R;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;

/**
 * Created by Michal on 22/01/14.
 */
public class WaypointHolder {
    public LinearLayout parentLayout;
    public LinearLayout holderLayout;
    public EditText addressEditText;
    public Marker googleMapMarker;
    public Button closeButton;
    Context context;
    public ArrayList<WaypointHolder> waypointHolders;

    public WaypointHolder(LinearLayout parentLayout, Context context, ArrayList<WaypointHolder> waypointHolders) {
        this.parentLayout = parentLayout;
        this.context = context;
        this.waypointHolders = waypointHolders;
    }

    public void initialise()
    {
        this.holderLayout = new LinearLayout(context);
        this.holderLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        this.holderLayout.setOrientation(LinearLayout.HORIZONTAL);

        Drawable image = this.context.getResources().getDrawable(R.drawable.close);
        this.closeButton = new Button(context);
        this.closeButton.setBackgroundColor(Color.parseColor("#00000000"));
        this.closeButton.setCompoundDrawablesWithIntrinsicBounds(null, null, image, null);
        this.closeButton.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        this.addressEditText = new EditText(context);
        this.addressEditText.setHint("Enter waypoint address");
        LinearLayout.LayoutParams buttonLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        buttonLayoutParams.setMargins(25, 0, 0, 0);
        this.addressEditText.setLayoutParams(buttonLayoutParams);
        this.addressEditText.setSingleLine(true);

        this.holderLayout.addView(addressEditText);
        this.holderLayout.addView(closeButton);
        this.parentLayout.addView(this.holderLayout);
        this.waypointHolders.add(this);
    }

    public void removeItself()
    {
        removeMarker();
        parentLayout.removeView(holderLayout);
        this.waypointHolders.remove(this);
    }

    public void removeMarker()
    {
        if(this.googleMapMarker != null)
        {
            this.googleMapMarker.remove();
            this.googleMapMarker = null;
        }
    }
}
