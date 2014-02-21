package com.example.myapplication.utilities;

import android.widget.EditText;
import android.widget.TextView;

import java.util.regex.Pattern;

/**
 * Created by Michal on 12/12/13.
 */
public class Validators {
    public static Boolean validateEmailAddress(EditText editText)
    {
        Pattern rfc2822 = Pattern.compile(
                "^[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$");

        if (!rfc2822.matcher(editText.getText().toString()).matches()) {
            editText.setError("Invalid email address.");
            return false;
        }
        else
        {
            editText.setError(null);
            return true;
        }
    }

    public static Boolean validateEmailAddress(TextView textView)
    {
        Pattern rfc2822 = Pattern.compile(
                "^[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$");

        if (!rfc2822.matcher(textView.getText().toString()).matches()) {
            textView.setError("Invalid email address.");
            return false;
        }
        else
        {
            textView.setError(null);
            return true;
        }
    }
}
