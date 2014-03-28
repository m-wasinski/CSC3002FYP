package com.example.myapplication.utilities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.example.myapplication.domain_objects.GeoAddress;

import java.util.ArrayList;

/**
 * Contains a set of helper methods used at various places in the program.
 */
public class Utilities {

    public static String translateBoolean(boolean b)
    {
        return b ? "yes" : "no";
    }

    public static String translateGender(int i)
    {
        if(i == 1)
        {
            return "Male";
        }

        if(i == 2)
        {
            return "Female";
        }

        return "Private";
    }

    /**
     * Retrieves a formatted journey header for all journey locations.
     * @param addresses - list of all addresses in a journey.
     * @return
     */
    public static String getJourneyHeader(ArrayList<GeoAddress> addresses)
    {
        String header = "From: " + addresses.get(0).getAddressLine();

        if(addresses.size() > 2)
        {
            header = header + "\nvia: ";

            for(int i = 1; i < addresses.size()-1; i++)
            {
                header = header + addresses.get(i).getAddressLine();

                if(i < addresses.size()-2)
                {
                    header = header + ", ";
                }
            }
        }

        header = header + "\nTo: " + addresses.get(addresses.size()-1).getAddressLine();

        return header;
    }

    /**
     * Checks whether network connection is currently available.
     * Network tasks use call this in their pre-execute method to determine
     * if network connection is available prior to making a web call.
     * @param context - Context from currently active activity.
     * @return
     */
    public static Boolean isNetworkAvailable(Context context)  {

        try{
            ConnectivityManager connectivityManager = (ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            NetworkInfo mobileInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            if (wifiInfo.isConnected() || mobileInfo.isConnected()) {
                return true;
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }
}
