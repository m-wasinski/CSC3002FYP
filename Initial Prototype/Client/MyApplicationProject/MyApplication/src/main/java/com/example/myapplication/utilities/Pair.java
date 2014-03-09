package com.example.myapplication.utilities;

/**
 * Used by the AppManager to store HTTP authorisation headers.
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
