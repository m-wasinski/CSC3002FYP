package com.example.myapplication.Interfaces;

import com.example.myapplication.DomainObjects.CarShareRequest;
import com.example.myapplication.DomainObjects.ServiceResponse;

/**
 * Created by Michal on 02/01/14.
 */
public interface ContactDriverActivityInteface {
    void carShareRequestSent(ServiceResponse<CarShareRequest> serviceResponse);
}
