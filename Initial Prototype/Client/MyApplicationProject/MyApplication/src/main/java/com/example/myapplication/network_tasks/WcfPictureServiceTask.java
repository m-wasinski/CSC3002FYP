package com.example.myapplication.network_tasks;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.constants.SessionConstants;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.experimental.SSLSocketFactory;
import com.example.myapplication.interfaces.WCFImageRetrieved;
import com.example.myapplication.utilities.Pair;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Michal on 20/02/14.
 */
public class WcfPictureServiceTask extends AsyncTask<Void, Void, Void> {

    private Bitmap bitmap;
    private String url;
    private List<Pair> httpHeaders;
    private WCFImageRetrieved listener;

    private final int HTTPConnectionTimeout = 10000;
    private final int HTTPSocketTimeout = 15000;

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        listener.onImageRetrieved(this.bitmap);
    }

    public WcfPictureServiceTask(String url, List<Pair> httpHeaders, WCFImageRetrieved wcfImageRetrieved)
    {
        this.url = url;
        this.httpHeaders = httpHeaders;
        this.listener = wcfImageRetrieved;
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
            HttpGet httpGet = new HttpGet(new URI(url));
            httpGet.setParams(httpParameters);

            if(httpHeaders != null)
            {
                for(Pair httpHeader : httpHeaders)
                {
                    httpGet.addHeader(httpHeader.getKey(), httpHeader.getValue());
                }
            }

            this.bitmap = httpClient.execute(httpGet, new ResponseHandler<Bitmap>() {

                public Bitmap handleResponse(HttpResponse response) throws IOException {
                    switch(response.getStatusLine().getStatusCode()) {

                        case HttpStatus.SC_OK:
                            return BitmapFactory.decodeStream(response.getEntity().getContent());
                        case HttpStatus.SC_NOT_FOUND:
                            throw new IOException("Data Not Found");
                    }
                    return null;
                }
            });


        } catch (URISyntaxException e) {
            e.printStackTrace();
            //this.serviceResponse = new ServiceResponse(null, ServiceResponseCode.SERVER_ERROR, Arrays.asList(e));
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            //this.serviceResponse = new ServiceResponse(null, ServiceResponseCode.SERVER_ERROR,  Arrays.asList(e));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            //this.serviceResponse = new ServiceResponse(null, ServiceResponseCode.SERVER_ERROR,  Arrays.asList(e));
        } catch (IOException e) {
            e.printStackTrace();
            //this.serviceResponse = new ServiceResponse(null, ServiceResponseCode.SERVER_ERROR,  Arrays.asList(e));
        } catch (JsonSyntaxException e){
            e.printStackTrace();
            //this.serviceResponse = new ServiceResponse(null, ServiceResponseCode.SERVER_ERROR,  Arrays.asList(e));
        } catch (NullPointerException e){
            e.printStackTrace();
            //this.serviceResponse = new ServiceResponse(null, ServiceResponseCode.SERVER_ERROR,  Arrays.asList(e));
        }

        return null;
    }
}
