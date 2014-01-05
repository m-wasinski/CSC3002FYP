package com.example.myapplication.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.Constants.Constants;
import com.example.myapplication.DomainObjects.ServiceResponse;
import com.example.myapplication.DomainObjects.User;
import com.example.myapplication.Helpers.ServiceHelpers;
import com.example.myapplication.Interfaces.OnRegistrationCompleted;
import com.example.myapplication.R;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Michal on 12/11/13.
 */
public class RegistrationActivity extends Activity implements OnRegistrationCompleted{

    private ProgressDialog pd;
    private boolean valuesCorrect;
    private EditText userNameEditText;
    private EditText emailAddressEditText;
    private EditText passwordEditText;
    private EditText confirmedPasswordEditText;
    private TextView errorMessagesEditText;
    private Spinner genderSpinner;
    private int gender;

    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    /**
     * Substitute you own sender ID here. This is the project number you got
     * from the API Console, as described in "Getting Started."
     */
    String SENDER_ID = "505647745249";

    /**
     * Tag used on log messages.
     */
    static final String TAG = "GCMDemo";

    TextView mDisplay;
    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    SharedPreferences prefs;
    Context context;

    String regid;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.registration_activity_layout);
        userNameEditText = (EditText) findViewById(R.id.UserNameTextField);
        userNameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus)
                    validateUserName();
            }});

        emailAddressEditText = (EditText) findViewById(R.id.EmailTextField);
        emailAddressEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus)
                    validateEmail();
            }});

        passwordEditText = (EditText) findViewById(R.id.RegistrationPasswordTextField);
        passwordEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus)
                    validatePasswords();
            }});


        confirmedPasswordEditText = (EditText) findViewById(R.id.RegistrationConfirmPasswordTextField);
        confirmedPasswordEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus)
                    validatePasswords();
            }});

        errorMessagesEditText = (TextView) findViewById(R.id.RegisterActivityErrorMessages);

        genderSpinner = (Spinner) findViewById(R.id.RegisterActivityGender);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.gender, R.layout.support_simple_spinner_dropdown_item);
        genderSpinner.setAdapter(adapter);

        SetupUIEvents();

    }

    void SetupUIEvents()
    {
        Button registerButton = (Button) findViewById(R.id.RegisterNewUserButton);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AttemptToRegister();
            }
        });
    }

    private void AttemptToRegister() {

        validateUserName();
        validateEmail();
        validatePasswords();

        if(valuesCorrect)
        {
            gender = genderSpinner.getSelectedItemPosition();
            errorMessagesEditText.setText("");
            pd = new ProgressDialog(this);
            pd.setTitle("Registering...");
            pd.setMessage("Please wait.");
            pd.show();
            ServiceHelpers.RegisterNewUser(userNameEditText.getText().toString(),
                    emailAddressEditText.getText().toString(),
                    passwordEditText.getText().toString(),
                    confirmedPasswordEditText.getText().toString(), gender, this);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    private void validateUserName(){
        String userName = userNameEditText.getText().toString();
        Pattern pattern = Pattern.compile("[~#@*+%{}<>\\[\\]|\"\\ ^/[/\\\\]]");
        Matcher matcher = pattern.matcher(userName);

        if(userName.length() < 4 || matcher.find())
        {
            userNameEditText.setError("Username must be at least 4 characters long, and cannot contain the following characters: ~, #, @, *, +, %, {, }, <, >, [, ], |, “, ”, \\, /, _, ^");
            valuesCorrect = false;
            return;
        }
        else
            userNameEditText.setError(null);

        valuesCorrect = true;
    }

    private void validateEmail(){
        String emailAddress = emailAddressEditText.getText().toString();

        Pattern rfc2822 = Pattern.compile(
                "^[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$");

        if (!rfc2822.matcher(emailAddress).matches()) {
            emailAddressEditText.setError("Invalid email address.");
            valuesCorrect = false;
            return;
        }
        else
            emailAddressEditText.setError(null);

        valuesCorrect = true;
    }

    private void validatePasswords()
    {
        String password = passwordEditText.getText().toString();
        String confirmedPassword = confirmedPasswordEditText.getText().toString();

        if(!password.equals(confirmedPassword))
        {
            passwordEditText.setError("Both passwords must match.");
            confirmedPasswordEditText.setError("Both passwords must match.");
            valuesCorrect = false;
            return;
        }

        if(password.length() < 6 || confirmedPassword.length() < 6)
        {
            passwordEditText.setError("Password must be at least 6 characters long.");
            confirmedPasswordEditText.setError("Password must be at least 6 characters long.");
            valuesCorrect = false;
            return;
        }

        passwordEditText.setError(null);
        confirmedPasswordEditText.setError(null);

        valuesCorrect = true;
    }
    @Override
    public void onRegistrationCompleted(ServiceResponse<User> serviceResponse) {

        pd.dismiss();
        Toast toast;

        if (serviceResponse.ServiceResponseCode == Constants.SERVICE_RESPONSE_SUCCESS)
        {
            toast = Toast.makeText(this, "Registered successfully, please login.", Toast.LENGTH_LONG);
            toast.show();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
        else
        {
            for(String error : serviceResponse.ErrorMessages)
            {
                errorMessagesEditText.append(error+"\n");
            }
        }
    }


    /**
     * Gets the current registration ID for application on GCM service.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    /*
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGCMPreferences(Context context) {
    // This sample app persists the registration ID in shared preferences, but
    // how you store the regID in your app is up to you.
    return getSharedPreferences(RegistrationActivity.class.getSimpleName(),
    Context.MODE_PRIVATE);
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }
}