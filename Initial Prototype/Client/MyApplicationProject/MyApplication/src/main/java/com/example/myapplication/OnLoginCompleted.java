package com.example.myapplication;

/**
 * Created by Michal on 12/11/13.
 */
public interface OnLoginCompleted {
    void onTaskCompleted(ServiceResponse<User> serviceResponse);
}
