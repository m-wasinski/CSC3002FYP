package com.example.myapplication.NetworkTasks;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.example.myapplication.Constants.ServiceResponseCode;
import com.example.myapplication.DomainObjects.ServiceResponse;
import com.example.myapplication.Experimental.SSLSocketFactory;
import com.example.myapplication.Helpers.Pair;
import com.example.myapplication.Interfaces.WCFServiceCallback;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
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
public class WCFServiceTask<T, U> extends AsyncTask<TextView, String, Boolean> {

    private ServiceResponse<U> serviceResponse;
    private String url;
    private T object;
    private Type type;
    private final String TAG = this.getClass().getSimpleName();
    private WCFServiceCallback wcfServiceCallback;
    private List<Pair> httpHeaders;
    private String retrieveFromHttpHeader;
    private String retrievedValue;

    public WCFServiceTask(String url, T toParse, Type type, List<Pair> httpHeaders, String retrieveFromHttpHeader, WCFServiceCallback wcfServiceCallback)
    {
        this.object = toParse;
        this.url = url;
        this.type = type;
        this.wcfServiceCallback = wcfServiceCallback;
        this.httpHeaders = httpHeaders;
        this.retrieveFromHttpHeader = retrieveFromHttpHeader;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        this.wcfServiceCallback.onServiceCallCompleted(serviceResponse, this.retrievedValue);
    }

    @Override
    protected Boolean doInBackground(TextView... textViews) {
        try {

            HttpClient httpClient = SSLSocketFactory.getNewHttpClient();
            HttpPost postRequest = new HttpPost(new URI(url));

            Gson gson = new Gson();
            String toParse = gson.toJson(object);
            Log.i(TAG + "Serialised object", toParse);
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
            serviceResponse = gson.fromJson(serviceResponseString, type);

            if(this.retrieveFromHttpHeader != null && serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
            {
                retrievedValue = httpResponse.getFirstHeader(this.retrieveFromHttpHeader).getValue();
            }



        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JsonSyntaxException e){
            e.printStackTrace();
        } catch (NullPointerException e){
            e.printStackTrace();
        }
        return null;
    }
}
