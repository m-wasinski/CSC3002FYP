package com.example.myapplication.Interfaces;

import com.example.myapplication.DomainObjects.ServiceResponse;
import com.example.myapplication.DomainObjects.User;

/**
 * Created by Michal on 01/01/14.
 */
public interface loginActivityInterface {
    public void manualLoginCompleted(ServiceResponse<User> serviceResponse);
}
