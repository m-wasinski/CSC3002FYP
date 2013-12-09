package com.example.myapplication.Interfaces;

import com.example.myapplication.DomainObjects.CarShare;
import com.example.myapplication.DomainObjects.ServiceResponse;

/**
 * Created by Michal on 08/12/13.
 */
public interface OnCarSharePosted {
    void onCarSharePosted(ServiceResponse<CarShare> carShare);
}
