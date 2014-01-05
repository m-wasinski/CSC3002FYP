package com.example.myapplication.Experimental;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.example.myapplication.Constants.Constants;
import com.example.myapplication.DomainObjects.User;

/**
 * Created by Michal on 01/01/14.
 */
public class AppData extends Application {

    private User user;
    private String uuid;

    public void setUser(User u)
    {
        user = u;
    }

    public User getUser()
    {
        return user;
    }

    public void setUUID(String u)
    {
        uuid = u;
    }

    public String getUUID()
    {
        return uuid;
    }

    /**
     * Gets the current registration ID for application on GCM service.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    public String getRegistrationId() {

        SharedPreferences sharedPreferences = getSharedPreferences(Constants.GLOBAL_APP_DATA, Context.MODE_PRIVATE);
        String registrationId = sharedPreferences.getString(Constants.PROPERTY_REG_ID, "");

        if (registrationId.isEmpty()) {
            Log.i("", "Registration not found.");
            return "";
        }

        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = sharedPreferences.getInt(Constants.PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion();
        if (registeredVersion != currentVersion) {
            Log.i("", "App version changed.");
            return "";
        }
        return registrationId;
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    public int getAppVersion() {
        try {
            PackageInfo packageInfo = this.getPackageManager()
                    .getPackageInfo(this.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }
}
