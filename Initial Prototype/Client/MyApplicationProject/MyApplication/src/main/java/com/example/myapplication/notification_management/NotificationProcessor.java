package com.example.myapplication.notification_management;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.myapplication.R;
import com.example.myapplication.activities.activities.FriendsListActivity;
import com.example.myapplication.activities.activities.InstantMessengerActivity;
import com.example.myapplication.activities.activities.JourneyChatActivity;
import com.example.myapplication.activities.activities.JourneyDetailsActivity;
import com.example.myapplication.activities.activities.JourneyRequestDialogActivity;
import com.example.myapplication.activities.activities.MyNotificationsActivity;
import com.example.myapplication.activities.activities.RatingsActivity;
import com.example.myapplication.activities.activities.ReceivedFriendRequestDialogActivity;
import com.example.myapplication.activities.activities.SearchResultsJourneyDetailsActivity;
import com.example.myapplication.app_management.AppManager;
import com.example.myapplication.broadcast_receivers.NotificationDeleteReceiver;
import com.example.myapplication.constants.BroadcastTypes;
import com.example.myapplication.constants.IntentConstants;
import com.example.myapplication.constants.NotificationContentTypes;
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.domain_objects.ChatMessage;
import com.example.myapplication.domain_objects.FriendRequest;
import com.example.myapplication.domain_objects.Journey;
import com.example.myapplication.domain_objects.JourneyMessage;
import com.example.myapplication.domain_objects.JourneyRequest;
import com.example.myapplication.domain_objects.Notification;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.domain_objects.User;
import com.example.myapplication.dtos.NotificationMarkerDTO;
import com.example.myapplication.interfaces.NotificationTargetRetrieved;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WcfPostServiceTask;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

/**
 * Created by Michal on 14/02/14.
 */
public class NotificationProcessor
{
    public void process(final Context context, final AppManager appManager, Notification notification, NotificationTargetRetrieved listener)
    {
        int type = notification.NotificationContentType;

        Bundle bundle = new Bundle();
        bundle.putString(IntentConstants.NOTIFICATION, new Gson().toJson(notification));

        switch(type)
        {
            case NotificationContentTypes.NOTIFICATION_FRIEND_REQUEST_RECEIVED:

                new NotificationObjectRetriever<FriendRequest>().processNotification(context.getResources().getString(R.string.GetFriendRequestURL),
                        notification.TargetObjectId, new TypeToken<ServiceResponse<FriendRequest>>() {}.getType(),
                        context, appManager, notification, ReceivedFriendRequestDialogActivity.class, IntentConstants.FRIEND_REQUEST, listener);

                break;
            case NotificationContentTypes.NOTIFICATION_FRIEND_OFFERED_NEW_JOURNEY:
                new NotificationObjectRetriever<Journey>().processNotification(context.getResources().getString(R.string.GetSingleJourneyURL),
                        notification.TargetObjectId, new TypeToken<ServiceResponse<Journey>>() {}.getType(),
                        context, appManager, notification, SearchResultsJourneyDetailsActivity.class, IntentConstants.JOURNEY, listener);
                break;
            case NotificationContentTypes.NOTIFICATION_FRIEND_REQUEST_ACCEPTED:

                new NotificationDisplayManager<Void>().showNotification(appManager, context, notification, null, FriendsListActivity.class, null);
                break;
            case NotificationContentTypes.NOTIFICATION_FRIEND_REQUEST_DENIED:

                new NotificationDisplayManager<Void>().showNotification(appManager, context, notification, null, MyNotificationsActivity.class, null);
                break;
            case NotificationContentTypes.NOTIFICATION_JOURNEY_REQUEST_RECEIVED:

                new NotificationObjectRetriever<JourneyRequest>().processNotification(context.getResources().getString(R.string.GetJourneyRequestURL), notification.TargetObjectId,
                        new TypeToken<ServiceResponse<JourneyRequest>>() {}.getType(), context, appManager, notification, JourneyRequestDialogActivity.class, IntentConstants.JOURNEY_REQUEST, listener);
                break;
            case NotificationContentTypes.NOTIFICATION_JOURNEY_REQUEST_ACCEPTED:

                new NotificationObjectRetriever<Journey>().processNotification(context.getResources().getString(R.string.GetSingleJourneyURL),
                        notification.TargetObjectId, new TypeToken<ServiceResponse<Journey>>() {}.getType(),
                        context, appManager, notification, JourneyDetailsActivity.class, IntentConstants.JOURNEY, listener);
                break;
            case NotificationContentTypes.NOTIFICATION_JOURNEY_REQUEST_DENIED:
                new NotificationDisplayManager<Void>().showNotification(appManager, context, notification, null, MyNotificationsActivity.class, null);
                break;
            case NotificationContentTypes.NOTIFICATION_JOURNEY_CHAT_MESSAGE:
                new NotificationObjectRetriever<JourneyMessage>().processNotification(context.getResources().getString(R.string.GetJourneyMessageURL),
                        notification.TargetObjectId, new TypeToken<ServiceResponse<JourneyMessage>>() {
                }.getType(),
                        context, appManager, notification, JourneyChatActivity.class, IntentConstants.PAYLOAD, listener);
                break;
            case NotificationContentTypes.NOTIFICATION_INSTANT_MESSENGER:
                new NotificationObjectRetriever<ChatMessage>().processNotification(context.getResources().getString(R.string.GetMessageURL),
                        notification.TargetObjectId, new TypeToken<ServiceResponse<ChatMessage>>() {
                }.getType(),
                        context, appManager, notification, InstantMessengerActivity.class, IntentConstants.PAYLOAD, listener);
                break;
            case NotificationContentTypes.NOTIFICATION_JOURNEY_MODIFIED:
                new NotificationObjectRetriever<Journey>().processNotification(context.getResources().getString(R.string.GetSingleJourneyURL),
                        notification.TargetObjectId, new TypeToken<ServiceResponse<Journey>>() {}.getType(),
                        context, appManager, notification, JourneyDetailsActivity.class, IntentConstants.JOURNEY, listener);
                break;
            case NotificationContentTypes.NOTIFICATION_PASSENGER_LEFT_JOURNEY:
                new NotificationObjectRetriever<Journey>().processNotification(context.getResources().getString(R.string.GetSingleJourneyURL),
                        notification.TargetObjectId, new TypeToken<ServiceResponse<Journey>>() {}.getType(),
                        context, appManager, notification, JourneyDetailsActivity.class, IntentConstants.JOURNEY, listener);
                break;
            case NotificationContentTypes.NOTIFICATION_JOURNEY_CANCELLED:
                new NotificationObjectRetriever<Journey>().processNotification(context.getResources().getString(R.string.GetSingleJourneyURL),
                        notification.TargetObjectId, new TypeToken<ServiceResponse<Journey>>() {}.getType(),
                        context, appManager, notification, JourneyDetailsActivity.class, IntentConstants.JOURNEY, listener);
                break;
            case NotificationContentTypes.NOTIFICATION_RATING_RECEIVED:
                new NotificationDisplayManager<User>().showNotification(appManager, context, notification, appManager.getUser(), RatingsActivity.class, IntentConstants.USER);
                break;
        }
    }

    public void MarkDelivered(Context context, AppManager appManager, Notification notification, final WCFServiceCallback<Boolean, Void> listener)
    {
        if(notification.Delivered)
        {
            return;
        }

        new WcfPostServiceTask<NotificationMarkerDTO>(context, context.getResources().getString(R.string.MarkNotificationDeliveredURL),
                new NotificationMarkerDTO(appManager.getUser().getUserId(), notification.NotificationId),
                new TypeToken<ServiceResponse<Boolean>>() {}.getType(),
                appManager.getAuthorisationHeaders(), new WCFServiceCallback<Boolean, Void>() {
            @Override
            public void onServiceCallCompleted(ServiceResponse<Boolean> serviceResponse, Void parameter) {
                if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
                {
                    listener.onServiceCallCompleted(serviceResponse, parameter);
                }
            }
        }).execute();
    } 


    private class NotificationObjectRetriever<U>
    {
        public void processNotification(String url, int outgoingType, Type serviceResponseType, final Context context, final AppManager appManager, final Notification notification, final Class pendingIntentClass, final String intentConstant, final NotificationTargetRetrieved listener)
        {
            new WcfPostServiceTask<Integer>(context, url,
                    outgoingType, serviceResponseType, appManager.getAuthorisationHeaders(), new WCFServiceCallback<U, Void>() {
                @Override
                public void onServiceCallCompleted(ServiceResponse<U> serviceResponse, Void parameter) {
                    if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
                    {
                        if(listener != null)
                        {
                            Gson gson = new Gson();
                            Bundle bundle = new Bundle();
                            bundle.putString(intentConstant, gson.toJson(serviceResponse.Result));
                            bundle.putString(IntentConstants.NOTIFICATION, gson.toJson(notification));
                            listener.onNotificationTargetRetrieved(new Intent(context, pendingIntentClass).putExtras(bundle));
                        }
                        else
                        {
                            new NotificationDisplayManager<U>().showNotification(appManager, context, notification, serviceResponse.Result, pendingIntentClass, intentConstant);
                        }
                    }
                }
            }).execute();
        }
    }

    private class NotificationDisplayManager<T> {

        public NotificationDisplayManager()
        {
        }

        public void showNotification(AppManager appManager, Context context, Notification notification, T object, Class c, String intentConstant)
        {
            if(context == null)
            {
                return;
            }

            int notificationId = notification.CollapsibleKey;

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.logo)
                            .setContentTitle(notification.NotificationTitle)
                            .setStyle(new NotificationCompat.BigTextStyle().bigText(notification.NotificationMessage))
                            .setContentText(notification.NotificationMessage)
                            .setDeleteIntent(PendingIntent.getBroadcast(
                                    context, 0, new Intent(context, NotificationDeleteReceiver.class).putExtra(IntentConstants.NOTIFICATION, new Gson().toJson(notification)).setAction(BroadcastTypes.BROADCAST_NOTIFICATION_DELETED), PendingIntent.FLAG_CANCEL_CURRENT));

            PendingIntent pendingIntent = null;

            if(c != null)
            {
                Gson gson = new Gson();
                Bundle bundle = new Bundle();

                bundle.putString(IntentConstants.NOTIFICATION, gson.toJson(notification));

                if(object != null)
                {
                    bundle.putString(intentConstant, gson.toJson(object));
                }


                Log.i("Notification Display Manager ", gson.toJson(object));

                pendingIntent = PendingIntent.getActivity(context, notificationId,
                        new Intent(context, c).putExtras(bundle), PendingIntent.FLAG_CANCEL_CURRENT);
            }

            if(pendingIntent != null)
            {
                builder.setContentIntent(pendingIntent);
            }

            android.app.Notification appNotification = builder.build();


            appNotification.flags = android.app.Notification.DEFAULT_LIGHTS | android.app.Notification.FLAG_AUTO_CANCEL;

            notificationManager.notify(notificationId, appNotification);
            appManager.addNotificationId(notificationId);
        }

    }
}
