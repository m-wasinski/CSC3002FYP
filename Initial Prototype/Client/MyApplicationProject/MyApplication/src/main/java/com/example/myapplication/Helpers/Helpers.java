package com.example.myapplication.Helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.example.myapplication.Activities.HomeActivity;
import com.example.myapplication.Constants.Constants;

/**
 * Created by Michal on 01/12/13.
 */
public class Helpers {
    public static String TranslateBoolean(boolean b)
    {
        return b ? "yes" : "no";
    }

    public static String TranslateGender(int i)
    {
        return i == 1 ? "Male" : "Female";
    }
}
