package com.example.myapplication.NetworkTasks;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.example.myapplication.DomainObjects.CarShare;
import com.example.myapplication.DomainObjects.ServiceResponse;
import com.example.myapplication.DomainObjects.User;
import com.example.myapplication.Experimental.MySSLSocketFactory;
import com.example.myapplication.Helpers.ApplicationFileManager;
import com.example.myapplication.Interfaces.OnCarSharesRetrieved;
import com.example.myapplication.Interfaces.OnLoginCompleted;
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
 * Created by Michal on 28/11/13.
 */
public class MyCarSharesRetriever extends AsyncTask<TextView, String, Boolean> {

    private OnCarSharesRetrieved listener;
    private ServiceResponse<ArrayList<CarShare>> serviceResponse;
    private int userId;

    public MyCarSharesRetriever(int id, OnCarSharesRetrieved lis)
    {
        userId = id;
        listener = lis;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        listener.onCarSharesRetrieved(serviceResponse);
    }

    @Override
    protected Boolean doInBackground(TextView... textViews) {
        ApplicationFileManager fileManager = new ApplicationFileManager();

        try {

            HttpClient httpClient = MySSLSocketFactory.getNewHttpClient();
            URI uri = new URI("https://asus/Services/CarShareService.svc/getall");
            HttpPost postRequest = new HttpPost(uri);

            Gson gson = new Gson();
            String stringId = gson.toJson(userId);
            StringEntity se = new StringEntity(stringId,"UTF-8");
            se.setContentType("application/json;charset=UTF-8");

            postRequest.setEntity(se);

            //postRequest.addHeader(Constants.DeviceId, DeviceID.getID());
            //postRequest.addHeader(Constants.SessionID, fileManager.GetTokenValue());

            HttpResponse httpResponse = httpClient.execute(postRequest);
            String serviceResponseString = EntityUtils.toString(httpResponse.getEntity());


            Type carSharesType = new TypeToken<ServiceResponse<ArrayList<CarShare>>>() {}.getType();

            Log.e("Car shares:", serviceResponseString);
            serviceResponse = gson.fromJson(serviceResponseString, carSharesType);

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
}
