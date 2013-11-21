package com.example.myapplication.Interfaces;

import com.example.myapplication.DomainObjects.User;
import com.example.myapplication.DomainObjects.ServiceResponse;

/**
 * Created by Michal on 13/11/13.
 */
public interface OnRegistrationCompleted {
    void onRegistrationCompleted(ServiceResponse<User> serviceResponse);
}
