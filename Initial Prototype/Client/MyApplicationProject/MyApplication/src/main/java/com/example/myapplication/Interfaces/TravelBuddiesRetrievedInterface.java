package com.example.myapplication.Interfaces;

import com.example.myapplication.DomainObjects.ServiceResponse;
import com.example.myapplication.DomainObjects.User;

import java.util.ArrayList;

/**
 * Created by Michal on 04/01/14.
 */
public interface TravelBuddiesRetrievedInterface {
    void travelBuddiesRetrieved(ServiceResponse<ArrayList<User>> travelBuddies);
}
