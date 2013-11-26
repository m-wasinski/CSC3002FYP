package com.example.myapplication.NetworkTasks;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.example.myapplication.Constants.Constants;
import com.example.myapplication.DTOs.LoginDTO;
import com.example.myapplication.DomainObjects.ServiceResponse;
import com.example.myapplication.DomainObjects.User;
import com.example.myapplication.Experimental.MySSLSocketFactory;
import com.example.myapplication.Helpers.ApplicationFileManager;
import com.example.myapplication.Helpers.DeviceID;
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

/**
 * Created by Michal on 17/11/13.
 */
public class AttemptAutoLoginTask extends AsyncTask<TextView, String, Boolean> {

    private OnLoginCompleted listener;
    private ServiceResponse<User> serviceResponse;

    public AttemptAutoLoginTask(OnLoginCompleted lis)
    {
        listener = lis;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        listener.onTaskCompleted(serviceResponse);
    }

    @Override
    protected Boolean doInBackground(TextView... textViews) {

        ApplicationFileManager fileManager = new ApplicationFileManager();

        try {

            HttpClient httpClient = MySSLSocketFactory.getNewHttpClient();
            URI uri = new URI("https://asus/Services/UserService.svc/autologin");
            HttpPost postRequest = new HttpPost(uri);

            Gson gson = new Gson();
            String jsonLoginDTO = gson.toJson("");
            StringEntity se = new StringEntity(jsonLoginDTO,"UTF-8");
            se.setContentType("application/json;charset=UTF-8");

            postRequest.setEntity(se);

            postRequest.addHeader(Constants.DeviceId, DeviceID.getID());
            postRequest.addHeader(Constants.SessionID, fileManager.GetTokenValue());

            HttpResponse httpResponse = httpClient.execute(postRequest);
            String serviceResponseString = EntityUtils.toString(httpResponse.getEntity());


            Type userType = new TypeToken<ServiceResponse<User>>() {}.getType();
            Log.e("Service Response:", serviceResponseString);
            serviceResponse = gson.fromJson(serviceResponseString, userType);

//            Log.e("Header", serviceResponse.ErrorMessages.get(0).toString());

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
