package com.example.myapplication.interfaces;

import java.util.Calendar;

/**
 * Created by Michal on 06/03/14.
 */
public class Interfaces
{
    public interface TemplateNameListener
    {
        void NameEntered(String name);
    }

    public interface YesNoDialogPositiveButtonListener
    {
        void positiveButtonClicked();
    }

    public interface DateSelectedListener
    {
        void dateSelected(Calendar calendar);
    }

    public interface TimeSelectedListener
    {
        void timeSelected(Calendar calendar);
    }
}
