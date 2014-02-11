package com.example.myapplication.constants;

import com.example.myapplication.domain_objects.FriendRequest;
import com.example.myapplication.domain_objects.JourneyRequest;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.domain_objects.User;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

/**
 * Created by Michal on 09/02/14.
 */
public class TokenTypes {

    public static Type getServiceResponseJourneyRequestToken()
    {
        return new TypeToken<ServiceResponse<JourneyRequest>>() {}.getType();
    }

    public static Type getServiceResponseBooleanToken()
    {
        return new TypeToken<ServiceResponse<Boolean>>() {}.getType();
    }

    public static Type getJourneyRequestToken()
    {
        return new TypeToken<JourneyRequest>() {}.getType();
    }

    public static Type getBooleanToken()
    {
        return new TypeToken<Boolean>() {}.getType();
    }

    public static Type getFriendRequestToken()
    {
        return new TypeToken<FriendRequest>() {}.getType();
    }

    public static Type getUserToken()
    {
        return new TypeToken<User>() {}.getType();
    }
}
