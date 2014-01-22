package com.example.myapplication.network_tasks;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.constants.SessionConstants;
import com.example.myapplication.dtos.ServiceResponse;
import com.example.myapplication.experimental.SSLSocketFactory;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.utilities.Pair;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Created by Michal on 05/01/14.
 */
@SuppressWarnings("unchecked")
public class WCFServiceTask<T> extends AsyncTask<TextView, String, Boolean> {

    private ServiceResponse serviceResponse;
    private String url;
    private T objectToSerialise;
    private Type type;
    private final String TAG = this.getClass().getSimpleName();
    private WCFServiceCallback wcfServiceCallback;
    private List<Pair> httpHeaders;
    private String sessionInformation;
    private final int HTTPConnectionTimeout = 10000;
    private final int HTTPSocketTimeout = 15000;

    public WCFServiceTask(String url, T objectToSerialise, Type type, List<Pair> httpHeaders, WCFServiceCallback wcfServiceCallback)
    {
        this.objectToSerialise = objectToSerialise;
        this.url = url;
        this.type = type;
        this.wcfServiceCallback = wcfServiceCallback;
        this.httpHeaders = httpHeaders;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        this.wcfServiceCallback.onServiceCallCompleted(serviceResponse, this.sessionInformation);
    }

    @Override
    protected Boolean doInBackground(TextView... textViews) {
        try {

            HttpParams httpParameters = new BasicHttpParams();
            // Set the timeout in milliseconds until a connection is established.
            // The default value is zero, that means the timeout is not used.
            HttpConnectionParams.setConnectionTimeout(httpParameters, HTTPConnectionTimeout);
            // Set the default socket timeout (SO_TIMEOUT)
            // in milliseconds which is the timeout for waiting for data.
            HttpConnectionParams.setSoTimeout(httpParameters, HTTPSocketTimeout);
            HttpClient httpClient = SSLSocketFactory.getNewHttpClient();
            HttpPost postRequest = new HttpPost(new URI(url));
            postRequest.setParams(httpParameters);

            Gson gson = new Gson();

            String toParse = gson.toJson(objectToSerialise);

            Log.i(TAG + "Serialised objectToSerialise", toParse);

            StringEntity se = new StringEntity(toParse,"UTF-8");
            se.setContentType("application/json;charset=UTF-8");
            postRequest.setEntity(se);

            if(httpHeaders != null)
            {
                for(Pair httpHeader : httpHeaders)
                {
                    postRequest.addHeader(httpHeader.getKey(), httpHeader.getValue());
                }
            }

            HttpResponse httpResponse = httpClient.execute(postRequest);
            String serviceResponseString = EntityUtils.toString(httpResponse.getEntity());

            Log.i(TAG, serviceResponseString);
            serviceResponse = gson.fromJson(serviceResponseString, this.type);

            if(httpResponse.getFirstHeader(SessionConstants.SESSION_ID) != null && serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
            {
                this.sessionInformation = httpResponse.getFirstHeader(SessionConstants.SESSION_ID).getValue();
            }

        } catch (URISyntaxException e) {
            e.printStackTrace();
            this.serviceResponse = new ServiceResponse(null, ServiceResponseCode.SERVER_ERROR, null);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            this.serviceResponse = new ServiceResponse(null, ServiceResponseCode.SERVER_ERROR, null);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            this.serviceResponse = new ServiceResponse(null, ServiceResponseCode.SERVER_ERROR, null);
        } catch (IOException e) {
            e.printStackTrace();
            this.serviceResponse = new ServiceResponse(null, ServiceResponseCode.SERVER_ERROR, null);
        } catch (JsonSyntaxException e){
            e.printStackTrace();
            this.serviceResponse = new ServiceResponse(null, ServiceResponseCode.SERVER_ERROR, null);
        } catch (NullPointerException e){
            e.printStackTrace();
            this.serviceResponse = new ServiceResponse(null, ServiceResponseCode.SERVER_ERROR, null);
        }
        return null;
    }
}
