package com.example.myapplication.Helpers;

import com.example.myapplication.DomainObjects.CarShare;
import com.example.myapplication.Interfaces.SearchCompleted;
import com.example.myapplication.NetworkTasks.CarSharesSearchProcessor;

/**
 * Created by Michal on 01/12/13.
 */
public class SearchHelper {
    public static void SearchCarShares(CarShare carshare, SearchCompleted sc)
    {
        CarSharesSearchProcessor searchProcessor  = new CarSharesSearchProcessor(carshare, sc);
        searchProcessor.execute();
    }
}
