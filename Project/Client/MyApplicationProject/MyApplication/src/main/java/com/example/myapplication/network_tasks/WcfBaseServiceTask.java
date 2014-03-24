package com.example.myapplication.network_tasks;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;

import com.example.myapplication.app_management.AppManager;
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.utilities.Pair;
import com.example.myapplication.utilities.Utilities;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Serves as a base class for the WcfGetServiceTask and WcfPostServiceTask.
 */
public class WcfBaseServiceTask extends AsyncTask<Void, Void, Void> {

    protected String url;

    protected Type type;

    protected WCFServiceCallback wcfServiceCallback;

    protected List<Pair> httpHeaders;

    protected final int HTTPConnectionTimeout = 10000;

    protected final int HTTPSocketTimeout = 15000;

    protected Context context;

    protected ServiceResponse serviceResponse;

    /***
     * Called after receiving reply from the web service.
     * @param avoid - empty parameter.
     */
    @Override
    protected void onPostExecute(Void avoid) {

        super.onPostExecute(avoid);

        if(serviceResponse == null)
        {
            displayErrorDialog("Server error has occurred, please try again later.");
            return;
        }

        if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SERVER_ERROR)
        {
            displayErrorDialog("Server error has occurred, please try again later.");
            return;
        }

        if(serviceResponse.ServiceResponseCode == ServiceResponseCode.UNAUTHORISED)
        {
            if (context.getApplicationContext() != null) {
                ((AppManager)context.getApplicationContext()).logout(true, false);
            }
            return;
        }

        if(serviceResponse.ServiceResponseCode == ServiceResponseCode.FAILURE)
        {
            if(serviceResponse.ErrorMessages != null)
            {
                displayErrorDialog(serviceResponse.ErrorMessages.toString());
            }
        }
    }

    @Override
    protected Void doInBackground(Void... voids) {
        return null;
    }

    /***
     * Checks whether network connection is available before starting the task.
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if(!Utilities.isNetworkAvailable(context))
        {
            cancel(true);
            displayErrorDialog("Network unavailable, please check your internet connection and try again.");
        }
    }

    /**
     * Displays an error dialog with error message returned from the server after unsuccessful query.
     * @param message - Error message retrieved from the server.
     */
    private void displayErrorDialog(String message)
    {
        // We must check if the activity is still active, trying to display a dialog inside closed activity will result in an fatal exception and crash the app.
        if (!((Activity) context).isFinishing())
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
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
    }

    /**
     * Initialises the base service task.
     * @param context - Current activity context.
     * @param url - url to containing the address of the web service to call.
     * @param httpHeaders - HTTP session headers required for user authentication.
     * @param wcfServiceCallback - Callback to be called after receiving response from the server.
     * @param type - Type of object that we will expect to receive from the server.
     */
    public WcfBaseServiceTask(Context context, String url, List<Pair> httpHeaders, WCFServiceCallback wcfServiceCallback, Type type)
    {
        this.wcfServiceCallback = wcfServiceCallback;
        this.httpHeaders = httpHeaders;
        this.type = type;
        this.context = context;
        this.url = url;
    }
}
