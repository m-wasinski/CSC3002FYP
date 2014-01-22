package com.example.myapplication.dtos;

import java.util.List;

/**
 * Created by Michal on 06/11/13.
 */
public class ServiceResponse<T> {
    public T Result;
    public int ServiceResponseCode;
    public List<String> ErrorMessages;

    public ServiceResponse(T result, int serviceResponseCode, List<String> errorMessages)
    {
        this.Result = result;
        this.ServiceResponseCode = serviceResponseCode;
        this.ErrorMessages = errorMessages;
    }
}
