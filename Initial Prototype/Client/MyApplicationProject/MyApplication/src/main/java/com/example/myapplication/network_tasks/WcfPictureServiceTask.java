package com.example.myapplication.network_tasks;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.util.LruCache;

import com.example.myapplication.constants.WcfConstants;
import com.example.myapplication.utilities.SSLSocketFactory;
import com.example.myapplication.interfaces.WCFImageRetrieved;
import com.example.myapplication.utilities.Pair;
import com.google.gson.JsonSyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Used to retrieve picture from the web service.
 */
public class WcfPictureServiceTask extends AsyncTask<Void, Void, Void> {

    private Bitmap bitmap;
    private String url;
    private List<Pair> httpHeaders;
    private WCFImageRetrieved listener;

    private LruCache<String, Bitmap> bitmapLruCache;

    private int id;
    private final int HTTPConnectionTimeout = 10000;
    private final int HTTPSocketTimeout = 15000;

    private String TAG = "WCF Picture Service Task: ";

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        listener.onImageRetrieved(this.bitmap);
    }

    public WcfPictureServiceTask(LruCache<String, Bitmap> bitmapLruCache, String url, int id, List<Pair> httpHeaders, WCFImageRetrieved wcfImageRetrieved)
    {
        this.url = url+String.valueOf(id);
        this.id = id;
        this.httpHeaders = httpHeaders;
        this.listener = wcfImageRetrieved;
        this.bitmapLruCache = bitmapLruCache;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {

            if(WcfConstants.DEV_MODE)
            {
                url = url.replace("https://54.72.27.104/FindNDriveServices2_deploy", "https://findndrive.no-ip.co.uk");
            }

            Log.i(TAG, "Dev mode enabled: " + WcfConstants.DEV_MODE);
            Log.i(TAG, "Retrieving image from URL: " + this.url);

            this.bitmap = this.bitmapLruCache.get(String.valueOf(this.id));

            if(this.bitmap != null)
            {
                Log.i(TAG, "Picture retrieved from lru cache.");
                return null;
            }

            Log.i(TAG, "Picture not in cache, calling the webservice to retrieve it.");
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

            if(this.bitmap != null)
            {
                Log.i(TAG, "Saving picture in lru cache.");
                this.bitmapLruCache.put(String.valueOf(this.id), this.bitmap);
            }

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
