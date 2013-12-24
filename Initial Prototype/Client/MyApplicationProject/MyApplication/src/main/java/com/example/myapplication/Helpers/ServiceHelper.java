package com.example.myapplication.Helpers;

import com.example.myapplication.DomainObjects.CarShare;
import com.example.myapplication.Fragments.MyCarSharesFragment;
import com.example.myapplication.Interfaces.OnCarSharePosted;
import com.example.myapplication.Interfaces.OnLoginCompleted;
import com.example.myapplication.Interfaces.UserHomeActivity;
import com.example.myapplication.Interfaces.OnRegistrationCompleted;
import com.example.myapplication.NetworkTasks.AutoLoginTask;
import com.example.myapplication.NetworkTasks.ManualLoginTask;
import com.example.myapplication.NetworkTasks.LogoutUserTask;
import com.example.myapplication.NetworkTasks.MyCarSharesRetriever;
import com.example.myapplication.NetworkTasks.PostNewCarShareTask;
import com.example.myapplication.NetworkTasks.RegisterNewUserTask;

/**
 * Created by Michal on 12/11/13.
 */
public class ServiceHelper {

    public static void LoginUser(String userName, String password, OnLoginCompleted onLoginCompleted, boolean rememberMe)
    {
        ManualLoginTask userLoginTask = new ManualLoginTask(userName, password, onLoginCompleted, rememberMe);
        userLoginTask.execute();
    }

    public static void RegisterNewUser(String userName, String email, String password, String confirmedPassword, int gender, OnRegistrationCompleted onRegistrationCompleted)
    {
        RegisterNewUserTask userRegisterTask = new RegisterNewUserTask(userName, email, password, confirmedPassword, gender, onRegistrationCompleted);
        userRegisterTask.execute();
    }

    public static void AttemptAutoLogin(OnLoginCompleted onLoginCompleted)
    {
        AutoLoginTask autoLogin = new AutoLoginTask(onLoginCompleted);
        autoLogin.execute();
    }

    public static void LogoutUser(UserHomeActivity userHomeActivity, boolean forceDelete)
    {
        LogoutUserTask logoutUser = new LogoutUserTask(userHomeActivity, forceDelete);
        logoutUser.execute();
    }

    public static void RetrieveMyCarShares(int id, MyCarSharesFragment fragmentMyCarShares)
    {
        MyCarSharesRetriever retriever = new MyCarSharesRetriever(id, fragmentMyCarShares);
        retriever.execute();
    }

    public static void PostNewCarShare(OnCarSharePosted onCarSharePosted, CarShare carShare)
    {
        PostNewCarShareTask post = new PostNewCarShareTask(onCarSharePosted, carShare);
        post.execute();
    }
}

