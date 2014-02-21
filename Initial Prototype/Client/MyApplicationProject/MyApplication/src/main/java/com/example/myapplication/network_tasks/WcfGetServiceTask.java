package com.example.myapplication.network_tasks;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
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
import org.apache.http.client.methods.HttpGet;
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
 * Created by Michal on 20/02/14.
 */
public class WcfGetServiceTask extends WcfBaseServiceTask {

    private final String TAG = this.getClass().getSimpleName();

    private List<Pair> queryStringParams;

    public WcfGetServiceTask(Context context, String url, WCFServiceCallback wcfServiceCallback, List<Pair> queryStringParams, List<Pair> httpHeaders, Type type) {
        super(context, url, httpHeaders, wcfServiceCallback, type);
        this.queryStringParams = queryStringParams;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        this.wcfServiceCallback.onServiceCallCompleted(serviceResponse, aVoid);
    }


    @Override
    protected Void doInBackground(Void... voids) {

        try
        {
            HttpParams httpParameters = new BasicHttpParams();
            // Set the timeout in milliseconds until a connection is established.
            // The default value is zero, that means the timeout is not used.
            HttpConnectionParams.setConnectionTimeout(httpParameters, HTTPConnectionTimeout);
            // Set the default socket timeout (SO_TIMEOUT)
            // in milliseconds which is the timeout for waiting for data.
            HttpConnectionParams.setSoTimeout(httpParameters, HTTPSocketTimeout);

            HttpClient httpClient = SSLSocketFactory.getNewHttpClient();

            Uri.Builder builder = new Uri.Builder();

            builder.scheme("https").authority("findndrive.no-ip.co.uk").appendPath("Services").appendPath("UserService.svc").appendPath("refresh");

            if(this.queryStringParams != null)
            {
                for(Pair pair : this.queryStringParams)
                {
                    builder.appendQueryParameter(pair.getKey(), pair.getValue());
                }
            }

            this.url = builder.build().toString();

            Log.i(TAG, "URL: " + url);

            HttpGet httpGet = new HttpGet(new URI(url));

            httpGet.setParams(httpParameters);

            if(httpHeaders != null)
            {
                for(Pair httpHeader : httpHeaders)
                {
                    httpGet.addHeader(httpHeader.getKey(), httpHeader.getValue());
                }
            }

            HttpResponse httpResponse = httpClient.execute(httpGet);
            String serviceResponseString = EntityUtils.toString(httpResponse.getEntity());

            Log.i(TAG, serviceResponseString);
            this.serviceResponse = new Gson().fromJson(serviceResponseString, this.type);

        }catch (URISyntaxException e) {
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
