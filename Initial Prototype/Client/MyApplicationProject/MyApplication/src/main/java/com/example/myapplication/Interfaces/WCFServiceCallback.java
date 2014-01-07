package com.example.myapplication.Interfaces;

import com.example.myapplication.DomainObjects.ServiceResponse;

/**
 * Created by Michal on 05/01/14.
 */
public interface WCFServiceCallback<T, U> {
    void onServiceCallCompleted(ServiceResponse<T> serviceResponse, U parameter);
}
