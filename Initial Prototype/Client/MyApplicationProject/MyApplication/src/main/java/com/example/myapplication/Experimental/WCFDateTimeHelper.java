package com.example.myapplication.Experimental;

import android.util.Log;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Michal on 06/11/13.
 */

public class WCFDateTimeHelper {
    /**
     * <p>WCF services supply Dates over JSON in a strange format. This method
     * takes a WCF-formatted Date string and parses it into a JodaTime DateTime
     * object. Assumes valid input matching a format described below.</p>
     *
     * <p>WCF Dates over JSON can vary in 3 ways:</p>
     * <pre>
     * /Date(946684800000)/
     * /Date(-4094535600000+1300)/ 
     * /Date(4094535600000-0330)/</pre> 
     *
     * <p>That's milliseconds since Jan 1, 1970, plus/minus an optional timezone
     * (the milliseconds are in UTC, the timezone is applied afterwards). Note
     * that it is also possible that the first part (milliseconds) may be
     * negative - that is, a Date that occurred before 1970.</p>
     */
    public static Date parseWCFDateTimeString(String wcfDate) {
        // Strip the '/Date(' and ')/' bits off:
        wcfDate = wcfDate.replace("/Date(", "");
        wcfDate = wcfDate.replace(")/", "");
        wcfDate = wcfDate.replace("+0000", "");
        return new Date(Long.parseLong(wcfDate));
    }

    public static String ConvertToWCFDateTime(Date date) throws ParseException {

        //SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.UK);
        //simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        //Date actualDate = simpleDateFormat.parse(date.getTime());
        long epoch = date.getTime();

        return "/Date("+epoch+")/";
    }
}