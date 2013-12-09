package com.example.myapplication.Interfaces;

import com.example.myapplication.DomainObjects.CarShare;
import com.example.myapplication.DomainObjects.ServiceResponse;

import java.util.ArrayList;

/**
 * Created by Michal on 01/12/13.
 */
public interface SearchCompleted {
    void OnSearchCompleted(ServiceResponse<ArrayList<CarShare>> serviceResponse);
}
