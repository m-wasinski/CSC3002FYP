package com.example.myapplication.network_tasks;

import android.content.Context;
import android.util.Log;

import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.constants.SessionConstants;
import com.example.myapplication.domain_objects.ServiceResponse;
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
import java.util.Arrays;
import java.util.List;

/**
 * Created by Michal on 05/01/14.
 */
@SuppressWarnings("unchecked")
public class WcfPostServiceTask<T> extends WcfBaseServiceTask {

    private T objectToSerialise;
    private String sessionInformation;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    public WcfPostServiceTask(Context context, String url, T objectToSerialise, Type type, List<Pair> httpHeaders, WCFServiceCallback wcfServiceCallback)
    {
        super(context, url, httpHeaders, wcfServiceCallback, type);
        this.objectToSerialise = objectToSerialise;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        this.wcfServiceCallback.onServiceCallCompleted(serviceResponse, this.sessionInformation);
    }

    @Override
    protected Void doInBackground(Void... voids) {
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

            Log.i(TAG + "Serialised object: ", toParse);

            StringEntity stringEntity = new StringEntity(toParse,"UTF-8");
            stringEntity.setContentType("application/json;charset=UTF-8");
            postRequest.setEntity(stringEntity);

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
            this.serviceResponse = new ServiceResponse(null, ServiceResponseCode.SERVER_ERROR, Arrays.asList(e));
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            this.serviceResponse = new ServiceResponse(null, ServiceResponseCode.SERVER_ERROR,  Arrays.asList(e));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            this.serviceResponse = new ServiceResponse(null, ServiceResponseCode.SERVER_ERROR,  Arrays.asList(e));
        } catch (IOException e) {
            e.printStackTrace();
            this.serviceResponse = new ServiceResponse(null, ServiceResponseCode.SERVER_ERROR,  Arrays.asList(e));
        } catch (JsonSyntaxException e){
            e.printStackTrace();
            this.serviceResponse = new ServiceResponse(null, ServiceResponseCode.SERVER_ERROR,  Arrays.asList(e));
        } catch (NullPointerException e){
            e.printStackTrace();
            this.serviceResponse = new ServiceResponse(null, ServiceResponseCode.SERVER_ERROR,  Arrays.asList(e));
        }

        return null;
    }
}
