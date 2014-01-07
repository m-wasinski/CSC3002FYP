package com.example.myapplication.Experimental;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import com.example.myapplication.Constants.Constants;
import com.example.myapplication.DomainObjects.User;
import com.example.myapplication.Helpers.Pair;

import java.util.List;

import static java.util.Arrays.asList;

/**
 * Created by Michal on 01/01/14.
 */
public class AppData extends Application {

    private User user;
    private String uuid;
    private String sessionId;
    private String registrationId;
    private String uniqueDeviceId;

    public void setSessionId(String sessionId)
    {
        this.sessionId = sessionId;
    }

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

    public String getSessionId()
    {
        if(sessionId == null)
            return "";

        return sessionId;
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

        if (registrationId.isEmpty()) {
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

    public String getUniqueDeviceId()
    {
        return uniqueDeviceId;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.GLOBAL_APP_DATA, Context.MODE_PRIVATE);

        registrationId = sharedPreferences.getString(Constants.PROPERTY_REG_ID, "");
        sessionId = sharedPreferences.getString(Constants.SESSION_ID, "");
        uniqueDeviceId = ""+ Build.BOARD.length()%10+ Build.BRAND.length()%10 +
                Build.CPU_ABI.length()%10 + Build.DEVICE.length()%10 +
                Build.DISPLAY.length()%10 + Build.HOST.length()%10 +
                Build.ID.length()%10 + Build.MANUFACTURER.length()%10 +
                Build.MODEL.length()%10 + Build.PRODUCT.length()%10 +
                Build.TAGS.length()%10 + Build.TYPE.length()%10 +
                Build.USER.length()%10 ;
    }

    public List<Pair> getAuthorisationHeaders()
    {
        return asList(new Pair(Constants.DEVICE_ID, this.uniqueDeviceId),
                new Pair(Constants.SESSION_ID, this.sessionId),
                new Pair(Constants.UUID, this.uuid));
    }
}
