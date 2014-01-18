package com.example.myapplication.experimental;

/**
 * Created by Michal on 08/12/13.
 */
public class SpinnerNavigationItem {
    private String title;
    private int icon;

    public SpinnerNavigationItem(String title, int icon){
        this.title = title;
        this.icon = icon;
    }

    public String getTitle(){
        return this.title;
    }

    public int getIcon(){
        return this.icon;
    }
}
