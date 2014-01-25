package com.example.myapplication.experimental;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import com.example.myapplication.R;
import com.example.myapplication.activities.activities.LoginActivity;
import com.example.myapplication.constants.SessionConstants;
import com.example.myapplication.constants.SharedPreferencesConstants;
import com.example.myapplication.dtos.ServiceResponse;
import com.example.myapplication.dtos.User;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WCFServiceTask;
import com.example.myapplication.utilities.Pair;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

import static java.util.Arrays.asList;

/**
 * Created by Michal on 01/01/14.
 */
public class FindNDriveManager extends Application {

    private User user;
    private String uuid;
    private String sessionId;
    private String registrationId;
    private String uniqueDeviceId;
    private SharedPreferences sharedPreferences;
    private Gson gson;

    public void setSessionId(String sessionId)
    {
        this.sessionId = sessionId;
        SharedPreferences.Editor editor = this.sharedPreferences.edit();
        editor.putString(SharedPreferencesConstants.PROPERTY_SESSION_ID, sessionId);
        editor.commit();
    }

    public void setUser(User u)
    {
        this.user = u;
        SharedPreferences.Editor editor = this.sharedPreferences.edit();
        editor.putString(SharedPreferencesConstants.PROPERTY_USER, u == null ? "" : gson.toJson(user));
        editor.commit();
    }

    public User getUser()
    {
        if(this.user == null)
        {
            this.user = gson.fromJson(sharedPreferences.getString(SharedPreferencesConstants.PROPERTY_USER, ""), new TypeToken<User>() {}.getType());
        }
        return this.user;
    }

    public void setUUID(String u)
    {
        this.uuid = u;
    }

    public String getUUID()
    {
        return this.uuid;
    }

    public String getSessionId()
    {
        return this.sessionId.isEmpty() || this.sessionId == null ? "" : this.sessionId;
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
    public String getRegistrationId()
    {
        return this.registrationId.isEmpty() || this.registrationId == null ? "" : this.registrationId;
    }

    public void setRegistrationId(String registrationId)
    {
        this.registrationId = registrationId;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPreferencesConstants.PROPERTY_REG_ID, registrationId);
        editor.putInt(SharedPreferencesConstants.PROPERTY_APP_VERSION, this.getAppVersion());
        editor.commit();
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
            e.printStackTrace();
            throw new RuntimeException("Could not get package name: " + e);
        }catch (NullPointerException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    public String getUniqueDeviceId()
    {
        return this.uniqueDeviceId;
    }

    public List<Pair> getAuthorisationHeaders()
    {
        return asList(new Pair(SessionConstants.DEVICE_ID, this.uniqueDeviceId),
                new Pair(SessionConstants.SESSION_ID, this.sessionId),
                new Pair(SessionConstants.UUID, this.uuid));
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void logout(boolean forceLogout)
    {
        new WCFServiceTask<Boolean>(this, getResources().getString(R.string.UserLogoutURL),
                forceLogout, new TypeToken<ServiceResponse<Boolean>>(){}.getType(), this.getAuthorisationHeaders(), new WCFServiceCallback<Boolean, Void>() {
            @Override
            public void onServiceCallCompleted(ServiceResponse<Boolean> serviceResponse, Void parameter) {

            }
        }).execute();

        if(this.getSessionId().endsWith("0") || forceLogout)
        {
            this.setUser(null);
            this.setSessionId("");
        }

        if(forceLogout)
        {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.gson = new Gson();
        this.sharedPreferences = getSharedPreferences(SharedPreferencesConstants.GLOBAL_APP_DATA, Context.MODE_PRIVATE);
        this.registrationId = sharedPreferences.getString(SharedPreferencesConstants.PROPERTY_REG_ID, "");
        this.sessionId = sharedPreferences.getString(SessionConstants.SESSION_ID, "");
        this.uniqueDeviceId = ""+ Build.BOARD.length()%10+ Build.BRAND.length()%10 +
                Build.CPU_ABI.length()%10 + Build.DEVICE.length()%10 +
                Build.DISPLAY.length()%10 + Build.HOST.length()%10 +
                Build.ID.length()%10 + Build.MANUFACTURER.length()%10 +
                Build.MODEL.length()%10 + Build.PRODUCT.length()%10 +
                Build.TAGS.length()%10 + Build.TYPE.length()%10 +
                Build.USER.length()%10 ;

    }
}
