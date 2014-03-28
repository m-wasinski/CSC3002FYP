package com.example.myapplication.app_management;

import android.annotation.TargetApi;
import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;
import android.util.LruCache;

import com.example.myapplication.R;
import com.example.myapplication.activities.activities.LoginActivity;
import com.example.myapplication.constants.SessionConstants;
import com.example.myapplication.constants.SharedPreferencesConstants;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.domain_objects.User;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WcfPostServiceTask;
import com.example.myapplication.utilities.Pair;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * Globally available object providing access to various components required by other classes throughout the program.
 * This object resembles a singleton behaviour meaning that it is only instantiated once and each new reference
 * points to exactly the same object in memory.
 */
public class AppManager extends Application {

    // Represents currently logged in user.
    private User user;

    // Represents the newly generated identifier for this session.
    private String uuid;

    // Represents current session id.
    private String sessionId;

    // Represents current GCM registration id.
    private String registrationId;

    // Represents the id number of this device.
    private String uniqueDeviceId;

    // SharedPreferences used to persist data.
    private SharedPreferences sharedPreferences;

    private ArrayList<Integer> notificationIds;
    private LruCache<String, Bitmap> bitmapLruCache;

    public void addNotificationId(Integer id)
    {
        if(notificationIds == null)
        {
            notificationIds = new ArrayList<Integer>();
        }

        notificationIds.add(id);
    }

    public Boolean hasAppBeenKilled()
    {
        return user == null && getSessionId().endsWith("0");
    }

    public void setSessionId(String sessionId)
    {
        this.sessionId = sessionId;

        if(sharedPreferences == null)
        {
            sharedPreferences = retrieveSharedPreferences();
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPreferencesConstants.PROPERTY_SESSION_ID, sessionId);
        editor.commit();
    }

    public void setUser(User u)
    {
        user = u;

        if(sharedPreferences == null)
        {
            sharedPreferences = retrieveSharedPreferences();
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPreferencesConstants.PROPERTY_USER, u == null ? "" : new Gson().toJson(user));
        editor.commit();
    }

    public LruCache<String, Bitmap> getBitmapLruCache()
    {
        if(bitmapLruCache == null)
        {
            bitmapLruCache = createBitmapLruCache();
        }

        return bitmapLruCache;
    }

    public User getUser()
    {
        if(user == null)
        {
            if(sharedPreferences == null)
            {
                sharedPreferences = retrieveSharedPreferences();
            }

            user = new Gson().fromJson(sharedPreferences.getString(SharedPreferencesConstants.PROPERTY_USER, ""), new TypeToken<User>() {}.getType());
        }

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
        {
            sessionId = retrieveSessionId();
        }

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
    public String getRegistrationId()
    {
        if(registrationId == null)
        {
            registrationId = retrieveRegistrationId();
        }

        return registrationId;
    }

    public void setRegistrationId(String registrationId)
    {
        this.registrationId = registrationId;

        if(sharedPreferences == null)
        {
            sharedPreferences = retrieveSharedPreferences();
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPreferencesConstants.PROPERTY_REG_ID, registrationId);
        editor.putInt(SharedPreferencesConstants.PROPERTY_APP_VERSION, getAppVersion());
        editor.commit();
    }
    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    public int getAppVersion() {
        try
        {
            PackageInfo packageInfo = getPackageManager()
                    .getPackageInfo(getPackageName(), 0);

            return packageInfo.versionCode;
        }
        catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Could not get package name: " + e);
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    public String getUniqueDeviceId()
    {
        if(uniqueDeviceId == null)
        {
            uniqueDeviceId = retrieveUniqueDeviceId();
        }

        return uniqueDeviceId;
    }

    public List<Pair> getAuthorisationHeaders()
    {
        return asList(new Pair(SessionConstants.DEVICE_ID, getUniqueDeviceId()),
                new Pair(SessionConstants.SESSION_ID, getSessionId()),
                new Pair(SessionConstants.UUID, getUUID()));
    }

    /**
     * Responsible for logging the current user out.
     * @param force - Determines whether current session between the app and the web service
     *              should be invalidated and the user should be permanently logged out. This means that even though
     *              the user has had a permanent session established through auto-login, they will be asked to log-in again
     *              if the force boolean is set to true.
     * @param setOfflineOnServer - Determines whether user's status should be changed to
     *                           offline and their GCM registration id should be set to null.
     *
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void logout(final boolean force, boolean setOfflineOnServer)
    {
        if(setOfflineOnServer)
        {
            new WcfPostServiceTask<Boolean>(this, getResources().getString(R.string.UserLogoutURL),
                    force, new TypeToken<ServiceResponse<Boolean>>(){}.getType(), getAuthorisationHeaders(), new WCFServiceCallback<Boolean, Void>() {
                @Override
                public void onServiceCallCompleted(ServiceResponse<Boolean> serviceResponse, Void parameter) {
                }
            }).execute();
        }

        if(getSessionId().endsWith("0") || force)
        {
            setUser(null);
            setSessionId("");
            clearNotifications();
        }

        if(force)
        {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }

    /**
     * When the user logs out, we must remember to clear
     * all notifications that have not been opened yet.
     */
    private void clearNotifications()
    {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(getApplicationContext().NOTIFICATION_SERVICE);

        if(notificationIds != null)
        {
            for(Integer id : notificationIds)
            {
                Log.i(getClass().getSimpleName(), "Notification: " + id + " is being cleared");
                notificationManager.cancel(id);
            }
        }
    }

    /**
     * Since the AppManager extends Application, its creation happens along with the
     * initialisation of the entire app.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        sharedPreferences = retrieveSharedPreferences();
        registrationId = retrieveRegistrationId();
        sessionId = retrieveSessionId();
        uniqueDeviceId = retrieveUniqueDeviceId();
        notificationIds = new ArrayList<Integer>();
        bitmapLruCache = createBitmapLruCache();
    }

    private String retrieveUniqueDeviceId()
    {
        return ""+ Build.BOARD.length()%10+ Build.BRAND.length()%10 +
                Build.CPU_ABI.length()%10 + Build.DEVICE.length()%10 +
                Build.DISPLAY.length()%10 + Build.HOST.length()%10 +
                Build.ID.length()%10 + Build.MANUFACTURER.length()%10 +
                Build.MODEL.length()%10 + Build.PRODUCT.length()%10 +
                Build.TAGS.length()%10 + Build.TYPE.length()%10 +
                Build.USER.length()%10 ;
    }

    private String retrieveSessionId()
    {
        if(sharedPreferences == null)
        {
            sharedPreferences = retrieveSharedPreferences();
        }

        String session = sharedPreferences.getString(SessionConstants.SESSION_ID, "");

        sessionId = session == null ? "" : session;

        return sessionId;
    }

    private String retrieveRegistrationId()
    {
        if(sharedPreferences == null)
        {
            sharedPreferences = retrieveSharedPreferences();
        }

        String registrationId = sharedPreferences.getString(SharedPreferencesConstants.PROPERTY_REG_ID, "");

        registrationId = registrationId == null ? "0" : registrationId;

        return registrationId;
    }

    private SharedPreferences retrieveSharedPreferences()
    {
        return getSharedPreferences(SharedPreferencesConstants.GLOBAL_APP_DATA, Context.MODE_PRIVATE);
    }

    private LruCache<String, Bitmap> createBitmapLruCache()
    {
        return new LruCache<String, Bitmap>((int)(Runtime.getRuntime().maxMemory() / 1024) / 4);
    }
}
