package com.example.myapplication.experimental;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import com.example.myapplication.constants.SessionConstants;
import com.example.myapplication.constants.SharedPreferencesConstants;
import com.example.myapplication.dtos.User;
import com.example.myapplication.utilities.Pair;

import java.util.ArrayList;
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
    private ArrayList<User> friends;
    private String currentlyVisibleActivity;
    private int currentChatRecipient;
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

    public ArrayList<User> getFriends()
    {
        return this.friends;
    }

    public void setFriends(ArrayList<User> friends)
    {
        this.friends = friends;
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

    public void setCurrentlyVisibleActivity(String activityName)
    {
        this.currentlyVisibleActivity = activityName;
    }

    public String getCurrentlyVisibleActivity()
    {
        return this.currentlyVisibleActivity;
    }

    public int getCurrentChatRecipient()
    {
        return this.currentChatRecipient;
    }

    public void setCurrentChatRecipient(int id)
    {
        this.currentChatRecipient = id;
    }
    public int getItemsPerCall()
    {
        return 10;
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
        SharedPreferences sharedPreferences = getSharedPreferences(SharedPreferencesConstants.GLOBAL_APP_DATA, Context.MODE_PRIVATE);

        registrationId = sharedPreferences.getString(SharedPreferencesConstants.PROPERTY_REG_ID, "");
        sessionId = sharedPreferences.getString(SessionConstants.SESSION_ID, "");
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
        return asList(new Pair(SessionConstants.DEVICE_ID, this.uniqueDeviceId),
                new Pair(SessionConstants.SESSION_ID, this.sessionId),
                new Pair(SessionConstants.UUID, this.uuid));
    }
}
