package com.example.myapplication.network_tasks;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;

import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.experimental.FindNDriveManager;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.utilities.Pair;
import com.example.myapplication.utilities.Utilities;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by Michal on 20/02/14.
 */
public class WcfBaseServiceTask extends AsyncTask<Void, Void, Void> {

    protected String url;

    protected Type type;

    protected final String TAG = this.getClass().getSimpleName();

    protected WCFServiceCallback wcfServiceCallback;

    protected List<Pair> httpHeaders;

    protected final int HTTPConnectionTimeout = 10000;

    protected final int HTTPSocketTimeout = 15000;

    protected Context context;

    protected ServiceResponse serviceResponse;

    @Override
    protected void onPostExecute(Void avoid) {

        super.onPostExecute(avoid);

        if(serviceResponse == null)
        {
            this.displayErrorDialog("Server error has occurred, please try again later.");
            return;
        }

        if(this.serviceResponse.ServiceResponseCode == ServiceResponseCode.UNAUTHORISED)
        {
            FindNDriveManager findNDriveManager = ((FindNDriveManager)this.context.getApplicationContext());
            findNDriveManager.logout(true, false);
            return;
        }

        if(this.serviceResponse.ServiceResponseCode == ServiceResponseCode.SERVER_ERROR)
        {
            this.displayErrorDialog("Server error has occurred, please try again later.");
            return;
        }

        if(this.serviceResponse.ServiceResponseCode == ServiceResponseCode.FAILURE)
        {
            if(!serviceResponse.ErrorMessages.toString().contains("MANUAL LOGIN"))
            {
                this.displayErrorDialog(serviceResponse.ErrorMessages.toString());
            }
        }
    }

    @Override
    protected Void doInBackground(Void... voids) {
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if(!Utilities.isNetworkAvailable(this.context))
        {
            cancel(true);
            this.displayErrorDialog("Network unavailable, please check your internet connection and try again.");
        }
    }

    private void displayErrorDialog(String message)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.context);
        builder.setMessage(message)
                .setCancelable(false)
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        wcfServiceCallback.onServiceCallCompleted(new ServiceResponse(null, ServiceResponseCode.FAILURE, null), null);
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

    public WcfBaseServiceTask(Context context, String url, List<Pair> httpHeaders, WCFServiceCallback wcfServiceCallback, Type type)
    {
        this.wcfServiceCallback = wcfServiceCallback;
        this.httpHeaders = httpHeaders;
        this.type = type;
        this.context = context;
        this.url = url;
    }
}
