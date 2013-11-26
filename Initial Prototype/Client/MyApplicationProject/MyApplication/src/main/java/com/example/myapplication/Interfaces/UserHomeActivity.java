package com.example.myapplication.Interfaces;

import com.example.myapplication.DomainObjects.ServiceResponse;

/**
 * Created by Michal on 23/11/13.
 */
public interface UserHomeActivity {
    void OnLogoutCompleted(ServiceResponse<Boolean> serviceResponse);
}
