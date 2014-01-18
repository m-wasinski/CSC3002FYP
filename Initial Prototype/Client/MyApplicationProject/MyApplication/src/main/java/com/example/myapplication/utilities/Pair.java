package com.example.myapplication.utilities;

/**
 * Created by Michal on 05/01/14.
 */
public class Pair {

    private String key;
    private String value;

    public String getKey()
    {
        return this.key;
    }

    public String getValue()
    {
        return this.value;
    }

    public Pair(String key, String value)
    {
        this.key = key;
        this.value = value;
    }
}
