package com.example.myapplication.DomainObjects;

import android.app.Service;
import android.support.v7.appcompat.*;
import android.support.v7.appcompat.R;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Michal on 06/11/13.
 */
public class ServiceResponse<T> {
    public T Result;
    public int ServiceResponseCode;
    public List<String> ErrorMessages;
}
