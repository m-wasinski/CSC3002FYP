package com.example.myapplication.utilities;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * A set of utilities used to help converting date object sent from the WCF web service
 * which is sent in the following format /Date(123456...)/
 **/
public class DateTimeHelper {

    /**
     * Parses the WCF date received in the form of a String and returns a proper Java object.
     */
    public static Date parseWCFDate(String wcfDate) {
        wcfDate = wcfDate.replace("/Date(", "");
        wcfDate = wcfDate.replace(")/", "");

        if(wcfDate.contains("+"))
        {
            wcfDate = wcfDate.replace(wcfDate.substring(wcfDate.indexOf("+")-1, wcfDate.length()-1), "");
        }

        long dateLong = Long.parseLong(wcfDate);
        return new Date(dateLong);
    }

    /**
     * Converts Java Date object into a long and adds the necessary tags required
     * by the WCF web service to accept it as a valid date object.
     *
     * @param date
     * @return
     */
    public static String convertToWCFDate(Date date) {
        return "/Date("+date.getTime()+")/";
    }

    /**
     * Extracts the date parts from the WCF date String.
     * @param wcfDate
     * @return
     */
    public static String getSimpleDate(String wcfDate)
    {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(DateTimeHelper.parseWCFDate(wcfDate));
        SimpleDateFormat simpleDateFormat  = new SimpleDateFormat("dd-MMMM-yyyy", Locale.UK);
        simpleDateFormat.setTimeZone( TimeZone.getTimeZone("UTC"));
        return simpleDateFormat.format(calendar.getTime());
    }

    /**
     * Extracts the time part from the WCF date String.
     * @param wcfDate
     * @return
     */
    public static String getSimpleTime(String wcfDate)
    {
        Calendar _calendar = new GregorianCalendar();
        _calendar.setTime(DateTimeHelper.parseWCFDate(wcfDate));
        SimpleDateFormat simpleDateFormat  = new SimpleDateFormat("HH:mm");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return simpleDateFormat.format(_calendar.getTime());
    }

    /**
     * Returns Java calendar instance with timezone already defined.
     * The UTC timezone is used universally in the app to avoid time-zone differences.
     * @return
     */
    public static Calendar getCalendar()
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));

        return calendar;
    }
}