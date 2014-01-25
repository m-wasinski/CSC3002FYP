package com.example.myapplication.experimental;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.myapplication.constants.SessionConstants;
import com.example.myapplication.constants.SharedPreferencesConstants;

/**
 * Created by Michal on 24/01/14.
 */
public class SessionManager {

    private SharedPreferences sharedPreferences;

    public SessionManager(Context context)
    {
        sharedPreferences = context.getSharedPreferences(SharedPreferencesConstants.GLOBAL_APP_DATA, Context.MODE_PRIVATE);
    }

    public String getSessionId()
    {
        String sessionId = this.sharedPreferences.getString(SessionConstants.SESSION_ID, "");
        return sessionId.isEmpty() || sessionId == null ? "" : sessionId;
    }

    public void storeSessionId(String sessionId)
    {
        SharedPreferences.Editor editor = this.sharedPreferences.edit();
        editor.putString(SharedPreferencesConstants.PROPERTY_SESSION_ID, sessionId);
        editor.commit();
    }
}
