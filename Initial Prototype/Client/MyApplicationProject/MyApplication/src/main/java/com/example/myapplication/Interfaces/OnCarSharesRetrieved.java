package com.example.myapplication.Interfaces;

import com.example.myapplication.DomainObjects.CarShare;
import com.example.myapplication.DomainObjects.ServiceResponse;

import java.util.ArrayList;

/**
 * Created by Michal on 28/11/13.
 */
public interface OnCarSharesRetrieved {
    void onCarSharesRetrieved(ServiceResponse<ArrayList<CarShare>> serviceResponse);
}
