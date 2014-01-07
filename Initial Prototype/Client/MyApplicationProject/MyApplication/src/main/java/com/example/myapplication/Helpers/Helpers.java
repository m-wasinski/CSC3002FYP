package com.example.myapplication.Helpers;

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
