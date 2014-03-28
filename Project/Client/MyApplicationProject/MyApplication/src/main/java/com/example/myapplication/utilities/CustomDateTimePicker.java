package com.example.myapplication.utilities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.TimePicker;

import com.example.myapplication.interfaces.Interfaces;

import java.util.Calendar;

/**
 * Used to solve a common problem in Android where touching the screen anywhere outside the date or time picker sets the date or time despite not clicking the done button.
 * The date and time dialogs are simply encapsulated in this class and the setDate and setTime variables are used to determine whether
 * the user really wanted to set the date or time or whether they simply dismissed the dialog.
 */
public class CustomDateTimePicker {

    private boolean setDate;
    private boolean setTime;

    /**
     * Shows the date picker dialog.
     * @param context - Context from currently visible activity.
     * @param calendar - Calendar object passed from the activity.
     * @param listener - Callback to be invoked after date is selected.
     * @param resettable - Indicates whether date can be set back to null.
     * @param setMinDate - Indicates whether a smallest minimum date of today must be applied.
     */
    public void showDatePickerDialog(Context context, final Calendar calendar, final Interfaces.DateSelectedListener listener, boolean resettable, boolean setMinDate)
    {
        final DatePickerDialog dateDialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener(){
            public void onDateSet(android.widget.DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                if(setDate)
                {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, monthOfYear);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    listener.dateSelected(calendar);
                }

            }
        } ,calendar
                .get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        if(setMinDate)
        {
            dateDialog.getDatePicker().setMinDate(Calendar.getInstance().getTimeInMillis()- 1000);
        }

        dateDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                setDate = false;
                dateDialog.dismiss();
            }
        });

        dateDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                setDate = false;
                dateDialog.dismiss();
            }
        });

        dateDialog.setButton(DatePickerDialog.BUTTON_POSITIVE, "Done", new DatePickerDialog.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // user set new date
                setDate = true;
            }
        });

        if(resettable)
        {
            dateDialog.setButton(DatePickerDialog.BUTTON_NEGATIVE, "I don't mind", new DatePickerDialog.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    setDate = false;
                    listener.dateSelected(null);
                }
            });
        }


        dateDialog.show();
    }

    /**
     * Shows the time picker dialog.
     * @param context - Context from currently visible activity.
     * @param calendar - Calendar object passed from the activity.
     * @param listener - Callback to be invoked after time is selected.
     * @param resettable - Indicates whether time can be set back to null.
     */
    public void showTimePickerDialog(Context context, final Calendar calendar, final Interfaces.TimeSelectedListener listener, boolean resettable)
    {
        final TimePickerDialog timeDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int i, int i2) {

                if(setTime)
                {
                    calendar.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
                    calendar.set(Calendar.MINUTE, timePicker.getCurrentMinute());
                    listener.timeSelected(calendar);
                }

            }
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);

        timeDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                setDate = false;
                timeDialog.dismiss();
            }
        });

        timeDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                setTime = false;
                timeDialog.dismiss();
            }
        });

        timeDialog.setButton(DatePickerDialog.BUTTON_POSITIVE, "Done", new DatePickerDialog.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // user set new date
                setTime = true;
            }
        });

        if(resettable)
        {
            timeDialog.setButton(DatePickerDialog.BUTTON_NEGATIVE, "I don't mind", new DatePickerDialog.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    setTime = false;
                    listener.timeSelected(null);
                }
            });
        }


        timeDialog.show();
    }
}
