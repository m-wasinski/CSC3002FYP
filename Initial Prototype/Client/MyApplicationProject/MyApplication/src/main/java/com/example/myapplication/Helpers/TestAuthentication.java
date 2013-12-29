package com.example.myapplication.Helpers;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.example.myapplication.DomainObjects.ServiceResponse;
import com.example.myapplication.DomainObjects.User;
import com.example.myapplication.Experimental.SSLSocketFactory;
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
 * Created by Michal on 13/11/13.
 */
public class TestAuthentication extends AsyncTask<TextView, String, Boolean> {

    private User CurrentUser;
    private ServiceResponse<User> serviceResponse;

    public TestAuthentication(User user)
    {
        CurrentUser = user;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
    }

    @Override
    protected Boolean doInBackground(TextView... textViews) {

        try {
            HttpClient httpClient = SSLSocketFactory.getNewHttpClient();
            URI uri = new URI("https://asus:443/Services/UserService.svc/test");
            HttpPost postRequest = new HttpPost(uri);

            Gson gson = new Gson();
            String jsonUserDTO = gson.toJson(CurrentUser);

            StringEntity se = new StringEntity(jsonUserDTO,"UTF-8");
            se.setContentType("application/json;charset=UTF-8");

            postRequest.setEntity(se);

            HttpResponse httpResponse = httpClient.execute(postRequest);
            String serviceResponseString = EntityUtils.toString(httpResponse.getEntity());

            Type userType = new TypeToken<ServiceResponse<User>>() {}.getType();

            serviceResponse = gson.fromJson(serviceResponseString, userType);

            Log.e("AUTHENTICATION",serviceResponseString);

            if (serviceResponse.Result == null)
            {
                serviceResponse.Result = new User();
            }

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

