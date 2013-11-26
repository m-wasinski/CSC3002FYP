package com.example.myapplication.Helpers;

import com.example.myapplication.Interfaces.OnLoginCompleted;
import com.example.myapplication.Interfaces.UserHomeActivity;
import com.example.myapplication.Interfaces.OnRegistrationCompleted;
import com.example.myapplication.NetworkTasks.AttemptAutoLoginTask;
import com.example.myapplication.NetworkTasks.AttemptManualLoginTask;
import com.example.myapplication.NetworkTasks.LogoutUserTask;
import com.example.myapplication.NetworkTasks.RegisterNewUserTask;

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

    public void LogoutUser(UserHomeActivity userHomeActivity, boolean forceDelete)
    {
        LogoutUserTask logoutUser = new LogoutUserTask(userHomeActivity, forceDelete);
        logoutUser.execute();
    }
}

