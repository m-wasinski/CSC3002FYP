package com.example.myapplication.Interfaces;

import com.example.myapplication.DomainObjects.CarShareRequest;
import com.example.myapplication.DomainObjects.ServiceResponse;

/**
 * Created by Michal on 03/01/14.
 */
public interface DecisionSubmittedInterface {
    void decisionSubmitted(ServiceResponse<CarShareRequest> serviceResponse);
}
