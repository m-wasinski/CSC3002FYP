package com.example.myapplication.Helpers;

import com.example.myapplication.Activities.MainActivity;
import com.example.myapplication.DomainObjects.CarShare;
import com.example.myapplication.Experimental.AppData;
import com.example.myapplication.Fragments.MyCarSharesFragment;
import com.example.myapplication.Interfaces.loginActivityInterface;
import com.example.myapplication.Interfaces.mainActivityInterface;
import com.example.myapplication.Interfaces.OnCarSharePosted;
import com.example.myapplication.Interfaces.SearchCompleted;
import com.example.myapplication.Interfaces.UserHomeActivity;
import com.example.myapplication.Interfaces.OnRegistrationCompleted;
import com.example.myapplication.NetworkTasks.AutoLoginTask;
import com.example.myapplication.NetworkTasks.CarSharesSearchProcessor;
import com.example.myapplication.NetworkTasks.GCMRegistrationTask;
import com.example.myapplication.NetworkTasks.ManualLoginTask;
import com.example.myapplication.NetworkTasks.LogoutUserTask;
import com.example.myapplication.NetworkTasks.MyCarSharesRetriever;
import com.example.myapplication.NetworkTasks.PostNewCarShareTask;
import com.example.myapplication.NetworkTasks.RegisterNewUserTask;

/**
 * Created by Michal on 12/11/13.
 */
public class ServiceHelpers {

    public static void RegisterNewUser(String userName, String email, String password, String confirmedPassword, int gender, OnRegistrationCompleted onRegistrationCompleted)
    {
        RegisterNewUserTask userRegisterTask = new RegisterNewUserTask(userName, email, password, confirmedPassword, gender, onRegistrationCompleted);
        userRegisterTask.execute();
    }

    public static void LogoutUser(UserHomeActivity userHomeActivity, boolean forceDelete)
    {
        LogoutUserTask logoutUser = new LogoutUserTask(userHomeActivity, forceDelete);
        logoutUser.execute();
    }

    public static void PostNewCarShare(OnCarSharePosted onCarSharePosted, CarShare carShare)
    {
        PostNewCarShareTask post = new PostNewCarShareTask(onCarSharePosted, carShare);
        post.execute();
    }

    public static void SearchCarShares(CarShare carshare, SearchCompleted sc)
    {
        CarSharesSearchProcessor searchProcessor  = new CarSharesSearchProcessor(carshare, sc);
        searchProcessor.execute();
    }
}

