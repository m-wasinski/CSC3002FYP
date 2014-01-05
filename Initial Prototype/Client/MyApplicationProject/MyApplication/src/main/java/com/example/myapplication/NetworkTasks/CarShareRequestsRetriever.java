package com.example.myapplication.NetworkTasks;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.example.myapplication.DomainObjects.CarShareRequest;
import com.example.myapplication.DomainObjects.ServiceResponse;
import com.example.myapplication.Experimental.SSLSocketFactory;
import com.example.myapplication.Interfaces.CarShareRequestRetrieverInterface;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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
import java.util.ArrayList;

/**
 * Created by Michal on 02/01/14.
 */
public class CarShareRequestsRetriever extends AsyncTask<TextView, String, Boolean> {

    private int carShareId;
    private ServiceResponse serviceResponse;
    private CarShareRequestRetrieverInterface listener;

    public CarShareRequestsRetriever(int id, CarShareRequestRetrieverInterface l)
    {
        carShareId = id;
        listener = l;
    }

    @Override
    protected Boolean doInBackground(TextView... textViews) {
        try {

            HttpClient httpClient = SSLSocketFactory.getNewHttpClient();
            URI uri = new URI("https://findndrive.no-ip.co.uk/Services/RequestService.svc/getrequests");
            HttpPost postRequest = new HttpPost(uri);

            Gson gson = new Gson();
            String stringId = gson.toJson(carShareId);

            StringEntity se = new StringEntity(stringId,"UTF-8");
            se.setContentType("application/json;charset=UTF-8");

            postRequest.setEntity(se);

            HttpResponse httpResponse = httpClient.execute(postRequest);
            String serviceResponseString = EntityUtils.toString(httpResponse.getEntity());

            Type carSharerequestType = new TypeToken<ServiceResponse<ArrayList<CarShareRequest>>>() {}.getType();

            Log.e("Car shares:", serviceResponseString);
            serviceResponse = gson.fromJson(serviceResponseString, carSharerequestType);

        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        listener.carShareRequestsRetrieved(serviceResponse);
    }
}
