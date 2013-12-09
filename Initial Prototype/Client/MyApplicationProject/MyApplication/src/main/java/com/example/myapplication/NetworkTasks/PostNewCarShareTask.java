package com.example.myapplication.NetworkTasks;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.example.myapplication.DomainObjects.CarShare;
import com.example.myapplication.DomainObjects.ServiceResponse;
import com.example.myapplication.DomainObjects.User;
import com.example.myapplication.Experimental.MySSLSocketFactory;
import com.example.myapplication.Interfaces.OnCarSharePosted;
import com.example.myapplication.Interfaces.OnCarSharesRetrieved;
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

/**
 * Created by Michal on 08/12/13.
 */
public class PostNewCarShareTask extends AsyncTask<TextView, String, Boolean> {

    private OnCarSharePosted listener;
    private CarShare carShare;
    private ServiceResponse<CarShare> serviceResponse;

    public PostNewCarShareTask(OnCarSharePosted l, CarShare c)
    {
        listener = l;
        carShare = c;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);

    }

    @Override
    protected Boolean doInBackground(TextView... textViews) {
        try {
            HttpClient httpClient = MySSLSocketFactory.getNewHttpClient();
            URI uri = new URI("https://findndrive.no-ip.co.uk/Services/CarShareService.svc/create");
            HttpPost postRequest = new HttpPost(uri);

            Gson gson = new Gson();
            String jsonRegisterDTO = gson.toJson(carShare);

            StringEntity se = new StringEntity(jsonRegisterDTO,"UTF-8");
            se.setContentType("application/json;charset=UTF-8");

            postRequest.setEntity(se);

            HttpResponse httpResponse = httpClient.execute(postRequest);
            String serviceResponseString = EntityUtils.toString(httpResponse.getEntity());
            Log.e("Login Service Response:", serviceResponseString);
            Type userType = new TypeToken<ServiceResponse<CarShare>>() {}.getType();

            serviceResponse = gson.fromJson(serviceResponseString, userType);

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
