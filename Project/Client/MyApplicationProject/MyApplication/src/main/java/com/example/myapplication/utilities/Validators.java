package com.example.myapplication.utilities;

import android.widget.EditText;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A set of static method to validate user's input.
 */
public class Validators {

    /**
     * Validates the EmailAddress provided by the user using a regex pattern.
     * @param editText - EditText containing the email address.
     * @return
     */
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

    /**
     * Validates the EmailAddress provided by the user using a regex pattern.
     * @param textView - TextView containing the emaill address.
     * @return
     */
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

    /**
     * Validates passwords provided by the user to ensure
     * that both are of correct length and that both match.
     * @param password - EditText containing first password.
     * @param confirmedPassword - EditText containing confirmed password.
     * @return
     */
    public static boolean validatePasswords(EditText password, EditText confirmedPassword)
    {
        if(!password.getText().toString().equals(confirmedPassword.getText().toString()))
        {
            password.setError("Both passwords must match.");
            password.setError("Both passwords must match.");
            return false;
        }

        if(password.getText().toString().length() < 6)
        {
            password.setError("Password must be at least 6 characters long.");
            return false;
        }

        if(confirmedPassword.getText().toString().length() < 6)
        {
            confirmedPassword.setError("Password must be at least 6 characters long.");
            return false;
        }

        password.setError(null);
        confirmedPassword.setError(null);

        return true;
    }

    /**
     * Validates the username provided by the user to ensure it's at least 4 characters long.
     * @param userNameEditText - EditText containing the username.
     * @return
     */
    public static boolean validateUserName(EditText userNameEditText){
        Pattern pattern = Pattern.compile("[~#@*+%{}<>\\[\\]|\"\\ ^/[/\\\\]]");
        Matcher matcher = pattern.matcher(userNameEditText.getText().toString());

        if(userNameEditText.getText().toString().length() < 4 || matcher.find())
        {
            userNameEditText.setError("Username must be at least 4 characters long, " +
                    "and cannot contain the following characters: ~, #, @, *, +, %, {, }, <, >, [, ], |, “, ”, \\, /, _, ^");
            return false;
        }

        userNameEditText.setError(null);
        return true;
    }
}
