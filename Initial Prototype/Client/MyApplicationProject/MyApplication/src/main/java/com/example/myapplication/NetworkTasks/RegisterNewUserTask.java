package com.example.myapplication.NetworkTasks;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.example.myapplication.DTOs.RegisterDTO;
import com.example.myapplication.DomainObjects.ServiceResponse;
import com.example.myapplication.DomainObjects.User;
import com.example.myapplication.Experimental.MySSLSocketFactory;
import com.example.myapplication.Interfaces.OnRegistrationCompleted;
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
 * Created by Michal on 17/11/13.
 */
public class RegisterNewUserTask extends AsyncTask<TextView, String, Boolean> {

    private RegisterDTO registerDTO;
    private OnRegistrationCompleted listener;
    private ServiceResponse<User> serviceResponse;

    public RegisterNewUserTask(String userName, String email, String pass, String confirmedPass, OnRegistrationCompleted lis)
    {
        User user = new User(userName, email, "", "", 0, 2);
        registerDTO = new RegisterDTO(user, pass, confirmedPass);
        listener = lis;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        listener.onRegistrationCompleted(serviceResponse);
    }

    @Override
    protected Boolean doInBackground(TextView... textViews) {

        try {
            HttpClient httpClient = MySSLSocketFactory.getNewHttpClient();
            URI uri = new URI("https://findndrive.no-ip.co.uk/Services/UserService.svc/register");
            HttpPost postRequest = new HttpPost(uri);

            Gson gson = new Gson();
            String jsonRegisterDTO = gson.toJson(registerDTO);

            StringEntity se = new StringEntity(jsonRegisterDTO,"UTF-8");
            se.setContentType("application/json;charset=UTF-8");

            postRequest.setEntity(se);

            HttpResponse httpResponse = httpClient.execute(postRequest);
            String serviceResponseString = EntityUtils.toString(httpResponse.getEntity());
            Log.e("Login Service Response:", serviceResponseString);
            Type userType = new TypeToken<ServiceResponse<User>>() {}.getType();

            serviceResponse = gson.fromJson(serviceResponseString, userType);

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