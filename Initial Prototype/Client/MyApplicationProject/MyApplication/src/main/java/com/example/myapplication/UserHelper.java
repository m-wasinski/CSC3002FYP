package com.example.myapplication;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

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
import java.util.List;

/**
 * Created by Michal on 12/11/13.
 */
public class UserHelper{

    public void LoginUser(String userName, String password, OnLoginCompleted onLoginCompleted)
    {
        UserLoginTask userLoginTask = new UserLoginTask(userName, password, onLoginCompleted);
        userLoginTask.execute();
    }

    private class UserLoginTask extends AsyncTask<TextView, String, Boolean>{

        private LoginDTO loginDTO;
        private OnLoginCompleted listener;
        private ServiceResponse<User> serviceResponse;

        public UserLoginTask(String userName, String password, OnLoginCompleted lis)
        {
            loginDTO = new LoginDTO(userName, password);
            listener = lis;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            listener.onTaskCompleted(serviceResponse);
        }

        @Override
        protected Boolean doInBackground(TextView... textViews) {

            try {
                HttpClient httpClient = MainActivity.getNewHttpClient();
                URI uri = new URI("https://asus:443/Services/UserService.svc/login");
                HttpPost postRequest = new HttpPost(uri);

                Gson gson = new Gson();
                String jsonLoginDTO = gson.toJson(loginDTO);

                StringEntity se = new StringEntity(jsonLoginDTO,"UTF-8");
                se.setContentType("application/json;charset=UTF-8");

                postRequest.setEntity(se);

                HttpResponse httpResponse = httpClient.execute(postRequest);
                String serviceResponseString = EntityUtils.toString(httpResponse.getEntity());

                Type userType = new TypeToken<ServiceResponse<User>>() {}.getType();
                Log.e("RESPONSE", serviceResponseString);
                serviceResponse = gson.fromJson(serviceResponseString, userType);

                if (listener == null)
                {
                    Log.e("RESPONSE", "LISTENER IS NULL");
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
}

