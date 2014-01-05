package com.example.myapplication.Interfaces;

import com.example.myapplication.DomainObjects.CarShareRequest;
import com.example.myapplication.DomainObjects.ServiceResponse;

import java.util.ArrayList;

/**
 * Created by Michal on 02/01/14.
 */
public interface CarShareRequestRetrieverInterface {
    void carShareRequestsRetrieved(ServiceResponse<ArrayList<CarShareRequest>> carShareRequests);
    void requestMarkedAsRead(ServiceResponse<CarShareRequest> carShareRequest);
}

