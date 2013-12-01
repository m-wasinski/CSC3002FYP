package com.example.myapplication.Helpers;

import com.example.myapplication.Fragments.FragmentMyCarShares;
import com.example.myapplication.Interfaces.OnLoginCompleted;
import com.example.myapplication.Interfaces.UserHomeActivity;
import com.example.myapplication.Interfaces.OnRegistrationCompleted;
import com.example.myapplication.NetworkTasks.AutoLoginTask;
import com.example.myapplication.NetworkTasks.ManualLoginTask;
import com.example.myapplication.NetworkTasks.LogoutUserTask;
import com.example.myapplication.NetworkTasks.MyCarSharesRetriever;
import com.example.myapplication.NetworkTasks.RegisterNewUserTask;

/**
 * Created by Michal on 12/11/13.
 */
public class UserHelper{

    public static void LoginUser(String userName, String password, OnLoginCompleted onLoginCompleted, boolean rememberMe)
    {
        ManualLoginTask userLoginTask = new ManualLoginTask(userName, password, onLoginCompleted, rememberMe);
        userLoginTask.execute();
    }

    public static void RegisterNewUser(String userName, String email, String password, String confirmedPassword, OnRegistrationCompleted onRegistrationCompleted)
    {
        RegisterNewUserTask userRegisterTask = new RegisterNewUserTask(userName, email, password, confirmedPassword, onRegistrationCompleted);
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

    public static void RetrieveMyCarShares(int id, FragmentMyCarShares fragmentMyCarShares)
    {
        MyCarSharesRetriever retriever = new MyCarSharesRetriever(id, fragmentMyCarShares);
        retriever.execute();
    }
}

