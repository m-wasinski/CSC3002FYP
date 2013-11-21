package com.example.myapplication.Helpers;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.widget.TextView;

import com.example.myapplication.Activities.MainActivity;
import com.example.myapplication.DTOs.LoginDTO;
import com.example.myapplication.DTOs.RegisterDTO;
import com.example.myapplication.DomainObjects.ServiceResponse;
import com.example.myapplication.DomainObjects.User;
import com.example.myapplication.Experimental.MySSLSocketFactory;
import com.example.myapplication.Interfaces.OnLoginCompleted;
import com.example.myapplication.Interfaces.OnRegistrationCompleted;
import com.example.myapplication.NetworkTasks.AttemptAutoLoginTask;
import com.example.myapplication.NetworkTasks.AttemptManualLoginTask;
import com.example.myapplication.NetworkTasks.RegisterNewUserTask;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.AbstractHttpMessage;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by Michal on 12/11/13.
 */
public class UserHelper{

    public void LoginUser(String userName, String password, OnLoginCompleted onLoginCompleted, boolean rememberMe)
    {
        AttemptManualLoginTask userLoginTask = new AttemptManualLoginTask(userName, password, onLoginCompleted, rememberMe);
        userLoginTask.execute();
    }

    public void RegisterNewUser(String userName, String email, String password, String confirmedPassword, OnRegistrationCompleted onRegistrationCompleted)
    {
        RegisterNewUserTask userRegisterTask = new RegisterNewUserTask(userName, email, password, confirmedPassword, onRegistrationCompleted);
        userRegisterTask.execute();
    }

    public void AttemptAutoLogin(OnLoginCompleted onLoginCompleted)
    {
        AttemptAutoLoginTask autoLogin = new AttemptAutoLoginTask(onLoginCompleted);
        autoLogin.execute();
    }


}

