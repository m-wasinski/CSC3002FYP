package com.example.myapplication.experimental;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Michal on 06/11/13.
 */

public class DateTimeHelper {
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
    public static Date parseWCFDate(String wcfDate) {
        wcfDate = wcfDate.replace("/Date(", "");
        wcfDate = wcfDate.replace(")/", "");
        if(wcfDate.contains("+"))
        {
            wcfDate = wcfDate.replace(wcfDate.substring(wcfDate.indexOf("+")-1, wcfDate.length()-1), "");
        }

        return new Date(Long.parseLong(wcfDate));
    }

    public static String convertToWCFDate(Date date) {
        long epoch = date.getTime();
        return "/Date("+epoch+")/";
    }

    public static String getSimpleDate(String wcfDate)
    {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(DateTimeHelper.parseWCFDate(wcfDate));
        SimpleDateFormat simpleDateFormat  = new SimpleDateFormat("dd-MMMM-yyyy", Locale.UK);
        simpleDateFormat.setTimeZone( TimeZone.getTimeZone("GMT"));
        return simpleDateFormat.format(calendar.getTime());
    }

    public static String getSimpleTime(String wcfDate)
    {
        Calendar _calendar = new GregorianCalendar();
        _calendar.setTime(DateTimeHelper.parseWCFDate(wcfDate));
        SimpleDateFormat simpleDateFormat  = new SimpleDateFormat("HH:mm");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return simpleDateFormat.format(_calendar.getTime());
    }
}