package com.example.myapplication.factories;

import android.content.Context;

import com.example.myapplication.R;
import com.example.myapplication.app_management.AppManager;
import com.example.myapplication.domain_objects.Journey;
import com.example.myapplication.domain_objects.JourneyTemplate;
import com.example.myapplication.domain_objects.Notification;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.domain_objects.User;
import com.example.myapplication.dtos.PrivacySettingsUpdaterDTO;
import com.example.myapplication.dtos.UserRetrieverDTO;
import com.example.myapplication.interfaces.WCFImageRetrieved;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WcfPictureServiceTask;
import com.example.myapplication.network_tasks.WcfPostServiceTask;
import com.example.myapplication.utilities.Pair;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Michal on 06/03/14.
 */
public class ServiceTaskFactory
{
    public static WcfPostServiceTask<JourneyTemplate> getJourneySearch(Context context, List<Pair> httpHeaders, JourneyTemplate journeyTemplate, WCFServiceCallback<ArrayList<Journey>, Void> callback)
    {
        return new WcfPostServiceTask<JourneyTemplate>(context, context.getResources().getString(R.string.SearchForJourneysURL),
                journeyTemplate, new TypeToken<ServiceResponse<ArrayList<Journey>>>() {}.getType(), httpHeaders, callback);
    }

    public static WcfPostServiceTask<Integer> getFriendsList(Context context, List<Pair> httpHeaders, Integer id, WCFServiceCallback<ArrayList<User>, Void> callback)
    {
        return new WcfPostServiceTask<Integer>(context, context.getString(R.string.GetFriendsURL), id,
                new TypeToken<ServiceResponse<ArrayList<User>>>() {}.getType(), httpHeaders, callback);
    }

    public static WcfPostServiceTask<Integer> getUnreadAppNotificationsCount(Context context, List<Pair> httpHeaders, Integer id, WCFServiceCallback<Integer, Void> callback)
    {
        return new WcfPostServiceTask<Integer>(context, context.getResources().getString(R.string.GetUnreadAppNotificationsCountURL), id,
                new TypeToken<ServiceResponse<Integer>>() {}.getType(), httpHeaders, callback);
    }

    public static WcfPostServiceTask<Integer> getUnreadMessagesCount(Context context, List<Pair> httpHeaders, Integer id, WCFServiceCallback<Integer, Void> callback)
    {
        return new WcfPostServiceTask<Integer>(context, context.getResources().getString(R.string.GetUnreadMessagesCountURL), id,
                new TypeToken<ServiceResponse<Integer>>() {}.getType(), httpHeaders, callback);
    }

    public static WcfPictureServiceTask getProfilePicture(AppManager appManager, Context context, Integer id, WCFImageRetrieved callback)
    {
        return  new WcfPictureServiceTask(appManager.getBitmapLruCache(), context.getResources().getString(R.string.GetProfilePictureURL),
                id, appManager.getAuthorisationHeaders(), callback);
    }

    public static WcfPostServiceTask<Integer> getDeviceNotifications(Context context, List<Pair> httpHeaders, Integer id, WCFServiceCallback<ArrayList<Notification>, Void> callback)
    {
        return new WcfPostServiceTask<Integer>(context, context.getResources().getString(R.string.GetDeviceNotificationsURL), id,
                new TypeToken<ServiceResponse<ArrayList<Notification>>>() {}.getType(), httpHeaders, callback);
    }

    public static WcfPostServiceTask<UserRetrieverDTO> getPersonDetails(Context context, List<Pair> httpHeaders, UserRetrieverDTO userRetrieverDTO,
                                                               WCFServiceCallback<User, Void> callback)
    {
        return new WcfPostServiceTask<UserRetrieverDTO>(context, context.getResources().getString(R.string.GetUserURL), userRetrieverDTO,
                new TypeToken<ServiceResponse<User>>() {}.getType(), httpHeaders, callback);
    }

    public static WcfPostServiceTask<PrivacySettingsUpdaterDTO> getPrivacySettingsUpdater(Context context, List<Pair> httpHeaders, PrivacySettingsUpdaterDTO privacySettingsUpdaterDTO,
                                                                        WCFServiceCallback<User, Void> callback)
    {
        return new WcfPostServiceTask<PrivacySettingsUpdaterDTO>(context, context.getResources().getString(R.string.UpdateUserPrivacySettingsURL), privacySettingsUpdaterDTO,
                new TypeToken<ServiceResponse<User>>() {}.getType(), httpHeaders, callback);
    }
}
