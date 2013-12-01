package com.example.myapplication.DomainObjects;

import java.util.List;

/**
 * Created by Michal on 06/11/13.
 */
public class ServiceResponse<T> {
    public T Result;
    public int ServiceResponseCode;
    public List<String> ErrorMessages;
}
