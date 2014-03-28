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
import com.example.myapplication.activities.activities.JourneyManagementActivity;
import com.example.myapplication.activities.activities.JourneyRequestDialogActivity;
import com.example.myapplication.activities.activities.MyNotificationsActivity;
import com.example.myapplication.activities.activities.RatingsActivity;
import com.example.myapplication.activities.activities.ReceivedFriendRequestActivity;
import com.example.myapplication.activities.activities.SearchResultDetailsActivity;
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
 * Analyses the content type of a given notification and takes appropriate action.
 */
public class NotificationProcessor
{
    /**
     * Based on the notification content type, this method calls the notification display manager to display the notification immediately or
     * calls the NotificationObjectRetriever which in turns calls the web service to retrieve the object which is related to this notification from the database.
     * @param context
     * @param appManager
     * @param notification
     * @param listener
     */
    public void process(final Context context, final AppManager appManager, Notification notification, NotificationTargetRetrieved listener)
    {
        int type = notification.getNotificationContentType();

        Bundle bundle = new Bundle();
        bundle.putString(IntentConstants.NOTIFICATION, new Gson().toJson(notification));

        switch(type)
        {
            case NotificationContentTypes.NOTIFICATION_FRIEND_REQUEST_RECEIVED:

                new NotificationObjectRetriever<FriendRequest>().processNotification(context.getResources().getString(R.string.GetFriendRequestURL),
                        notification.getTargetObjectId(), new TypeToken<ServiceResponse<FriendRequest>>() {}.getType(),
                        context, appManager, notification, ReceivedFriendRequestActivity.class, IntentConstants.FRIEND_REQUEST, listener);

                break;
            case NotificationContentTypes.NOTIFICATION_FRIEND_OFFERED_NEW_JOURNEY:
                new NotificationObjectRetriever<Journey>().processNotification(context.getResources().getString(R.string.GetSingleJourneyURL),
                        notification.getTargetObjectId(), new TypeToken<ServiceResponse<Journey>>() {}.getType(),
                        context, appManager, notification, SearchResultDetailsActivity.class, IntentConstants.JOURNEY, listener);
                break;
            case NotificationContentTypes.NOTIFICATION_FRIEND_REQUEST_ACCEPTED:

                new NotificationDisplayManager<Void>().showNotification(appManager, context, notification, null, FriendsListActivity.class, null);
                break;
            case NotificationContentTypes.NOTIFICATION_FRIEND_REQUEST_DENIED:

                new NotificationDisplayManager<Void>().showNotification(appManager, context, notification, null, MyNotificationsActivity.class, null);
                break;
            case NotificationContentTypes.NOTIFICATION_JOURNEY_REQUEST_RECEIVED:

                new NotificationObjectRetriever<JourneyRequest>().processNotification(context.getResources().getString(R.string.GetJourneyRequestURL), notification.getTargetObjectId(),
                        new TypeToken<ServiceResponse<JourneyRequest>>() {}.getType(), context, appManager, notification, JourneyRequestDialogActivity.class, IntentConstants.JOURNEY_REQUEST, listener);
                break;
            case NotificationContentTypes.NOTIFICATION_JOURNEY_REQUEST_ACCEPTED:

                new NotificationObjectRetriever<Journey>().processNotification(context.getResources().getString(R.string.GetSingleJourneyURL),
                        notification.getTargetObjectId(), new TypeToken<ServiceResponse<Journey>>() {}.getType(),
                        context, appManager, notification, JourneyManagementActivity.class, IntentConstants.JOURNEY, listener);
                break;
            case NotificationContentTypes.NOTIFICATION_JOURNEY_REQUEST_DENIED:
                new NotificationDisplayManager<Void>().showNotification(appManager, context, notification, null, MyNotificationsActivity.class, null);
                break;
            case NotificationContentTypes.NOTIFICATION_JOURNEY_CHAT_MESSAGE:
                new NotificationObjectRetriever<JourneyMessage>().processNotification(context.getResources().getString(R.string.GetJourneyMessageURL),
                        notification.getTargetObjectId(), new TypeToken<ServiceResponse<JourneyMessage>>() {
                }.getType(),
                        context, appManager, notification, JourneyChatActivity.class, IntentConstants.PAYLOAD, listener);
                break;
            case NotificationContentTypes.NOTIFICATION_INSTANT_MESSENGER:
                new NotificationObjectRetriever<ChatMessage>().processNotification(context.getResources().getString(R.string.GetMessageURL),
                        notification.getTargetObjectId(), new TypeToken<ServiceResponse<ChatMessage>>() {
                }.getType(),
                        context, appManager, notification, InstantMessengerActivity.class, IntentConstants.PAYLOAD, listener);
                break;
            case NotificationContentTypes.NOTIFICATION_JOURNEY_MODIFIED:
                new NotificationObjectRetriever<Journey>().processNotification(context.getResources().getString(R.string.GetSingleJourneyURL),
                        notification.getTargetObjectId(), new TypeToken<ServiceResponse<Journey>>() {}.getType(),
                        context, appManager, notification, JourneyManagementActivity.class, IntentConstants.JOURNEY, listener);
                break;
            case NotificationContentTypes.NOTIFICATION_PASSENGER_LEFT_JOURNEY:
                new NotificationObjectRetriever<Journey>().processNotification(context.getResources().getString(R.string.GetSingleJourneyURL),
                        notification.getTargetObjectId(), new TypeToken<ServiceResponse<Journey>>() {}.getType(),
                        context, appManager, notification, JourneyManagementActivity.class, IntentConstants.JOURNEY, listener);
                break;
            case NotificationContentTypes.NOTIFICATION_JOURNEY_CANCELLED:
                new NotificationObjectRetriever<Journey>().processNotification(context.getResources().getString(R.string.GetSingleJourneyURL),
                        notification.getTargetObjectId(), new TypeToken<ServiceResponse<Journey>>() {}.getType(),
                        context, appManager, notification, JourneyManagementActivity.class, IntentConstants.JOURNEY, listener);
                break;
            case NotificationContentTypes.NOTIFICATION_RATING_RECEIVED:
                new NotificationDisplayManager<User>().showNotification(appManager, context, notification, appManager.getUser(), RatingsActivity.class, IntentConstants.USER);
                break;
            case NotificationContentTypes.NOTIFICATION_JOURNEY_MATCHING_TEMPLATE_OFFERED:
                new NotificationObjectRetriever<Journey>().processNotification(context.getResources().getString(R.string.GetSingleJourneyURL),
                        notification.getTargetObjectId(), new TypeToken<ServiceResponse<Journey>>() {}.getType(),
                        context, appManager, notification, SearchResultDetailsActivity.class, IntentConstants.JOURNEY, listener);
                break;
        }
    }

    /**
     * Calls the web service to mark the notification which is passed in as one of the arguments as delivered.
     * @param context - Context passed in from currently visible activity.
     * @param appManager - The globally available app manager object.
     * @param notification - The notification to be marked as delivered.
     * @param listener - Callback method to be invoked after response from web service has been retrieved.
     */
    public void MarkDelivered(Context context, AppManager appManager, Notification notification, final WCFServiceCallback<Void, Void> listener)
    {
        if(notification.getDelivered())
        {
            return;
        }

        // Call the web service.
        new WcfPostServiceTask<NotificationMarkerDTO>(context, context.getResources().getString(R.string.MarkNotificationDeliveredURL),
                new NotificationMarkerDTO(appManager.getUser().getUserId(), notification.getNotificationId()),
                new TypeToken<ServiceResponse<Void>>() {}.getType(),
                appManager.getAuthorisationHeaders(), new WCFServiceCallback<Void, Void>() {
            @Override
            public void onServiceCallCompleted(ServiceResponse<Void> serviceResponse, Void parameter) {
                if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
                {
                    // Calls the callback method after the web service marks the notification as read successfully.
                    listener.onServiceCallCompleted(serviceResponse, parameter);
                }
            }
        }).execute();
    }

    /**
     * Generic class used to retrieve object directly related to
     * @param <U>
     */
    private class NotificationObjectRetriever<U>
    {
        /**
         * This method retrieves the object related to the notification being passed in as one of the parameters from the web service.
         * For example, when a new friend request is retrieved, the notification which appears in the top left corner only informs the user that
         * a new friend request has been received. The moment the user clicks on the notification, the actual friend request
         * is downloaded from the web service using the below method.
         */
        public void processNotification(String url, int outgoingType, Type serviceResponseType, final Context context, final AppManager appManager, final Notification notification, final Class pendingIntentClass, final String intentConstant, final NotificationTargetRetrieved listener)
        {
            new WcfPostServiceTask<Integer>(context, url,
                    outgoingType, serviceResponseType, appManager.getAuthorisationHeaders(), new WCFServiceCallback<U, Void>() {
                @Override
                public void onServiceCallCompleted(ServiceResponse<U> serviceResponse, Void parameter) {
                    if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
                    {
                        // Let the listener know as soon as object is retrieved from the web service.
                        if(listener != null)
                        {
                            Gson gson = new Gson();
                            Bundle bundle = new Bundle();
                            bundle.putString(intentConstant, gson.toJson(serviceResponse.Result));
                            bundle.putString(IntentConstants.NOTIFICATION, gson.toJson(notification));
                            ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(notification.getCollapsibleKey());
                            listener.onNotificationTargetRetrieved(new Intent(context, pendingIntentClass).putExtras(bundle));
                        }
                        else
                        {
                            // If no listener is waiting for the object, display the notification immediately.
                            new NotificationDisplayManager<U>().showNotification(appManager, context, notification, serviceResponse.Result, pendingIntentClass, intentConstant);
                        }
                    }
                }
            }).execute();
        }
    }

    /**
     * Generic class used to display notifications.
     * @param <T>
     */
    private class NotificationDisplayManager<T> {

        private final String TAG = "Notification Display Manager ";

        /**
         * Shows the given notification by using Android's NotificationManager.
         * @param appManager  - The globally available app manager object.
         * @param context - Context passed in from currently visible activity.
         * @param notification - Notification to be displayed.
         * @param object - Object to be passed to pending intent which is fired once user clicks on the notification.
         * @param c - The class file identifying the class to be invoked once user clicks on the notification.
         * @param intentConstant - Used to indicate the type of object being passed in the intent to the pending intent activity.
         */
        public void showNotification(AppManager appManager, Context context, Notification notification, T object, Class c, String intentConstant)
        {
            if(context == null)
            {
                return;
            }

            int notificationId = notification.getCollapsibleKey();

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.logo)
                            .setContentTitle(notification.getNotificationTitle())
                            .setStyle(new NotificationCompat.BigTextStyle().bigText(notification.getNotificationMessage()))
                            .setContentText(notification.getNotificationMessage())
                            .setDeleteIntent(PendingIntent.getBroadcast(
                                    context, 0, new Intent(context, NotificationDeleteReceiver.class).putExtra(IntentConstants.NOTIFICATION, new Gson().toJson(notification)).setAction(BroadcastTypes.BROADCAST_NOTIFICATION_DELETED), PendingIntent.FLAG_CANCEL_CURRENT));

            PendingIntent pendingIntent = null;

            // PendingIntent launches an activity after clicking on the notification.
            if(c != null)
            {
                Gson gson = new Gson();
                Bundle bundle = new Bundle();

                bundle.putString(IntentConstants.NOTIFICATION, gson.toJson(notification));

                if(object != null)
                {
                    bundle.putString(intentConstant, gson.toJson(object));
                }


                Log.i(TAG, gson.toJson(object));

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
