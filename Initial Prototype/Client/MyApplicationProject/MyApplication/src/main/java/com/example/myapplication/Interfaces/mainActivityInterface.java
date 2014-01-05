package com.example.myapplication.Interfaces;

import com.example.myapplication.DomainObjects.ServiceResponse;
import com.example.myapplication.DomainObjects.User;

/**
 * Created by Michal on 12/11/13.
 */
public interface mainActivityInterface {
    void autoLoginCompleted(ServiceResponse<User> serviceResponse);
    void gcmRegistrationCompleted(String registrationId);
}
