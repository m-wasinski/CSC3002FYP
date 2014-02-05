package com.example.myapplication.utilities;

import com.example.myapplication.domain_objects.GeoAddress;

import java.util.ArrayList;

/**
 * Created by Michal on 01/12/13.
 */
public class Helpers {
    public static String translateBoolean(boolean b)
    {
        return b ? "yes" : "no";
    }

    public static String translateGender(int i)
    {
        return i == 1 ? "Male" : "Female";
    }

    public static String getJourneyHeader(ArrayList<GeoAddress> addresses)
    {
        String header = "From: " + addresses.get(0).AddressLine;

        if(addresses.size() > 2)
        {
            header = header + "\nvia: ";

            for(int i = 1; i < addresses.size()-1; i++)
            {
                header = header + addresses.get(i).AddressLine;

                if(i < addresses.size()-2)
                {
                    header = header + ", ";
                }
            }
        }

        header = header + "\nTo: " + addresses.get(addresses.size()-1).AddressLine;

        return header;
    }
}
