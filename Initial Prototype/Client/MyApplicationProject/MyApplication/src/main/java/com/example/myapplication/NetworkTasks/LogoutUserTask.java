package com.example.myapplication.NetworkTasks;

import android.os.AsyncTask;
import android.support.v7.appcompat.R;
import android.util.Log;
import android.widget.TextView;

import com.example.myapplication.Constants.Constants;
import com.example.myapplication.DomainObjects.ServiceResponse;
import com.example.myapplication.DomainObjects.User;
import com.example.myapplication.Experimental.MySSLSocketFactory;
import com.example.myapplication.Helpers.ApplicationFileManager;
import com.example.myapplication.Helpers.DeviceID;
import com.example.myapplication.Interfaces.UserHomeActivity;
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
 * Created by Michal on 22/11/13.
 */
public class LogoutUserTask extends AsyncTask<TextView, String, Boolean> {

    private UserHomeActivity userHomeActivity;
    private ServiceResponse<Boolean> serviceResponse;
    private boolean forceDelete;
    private ApplicationFileManager fileManager;

    public LogoutUserTask(UserHomeActivity activity, boolean force)
    {
        fileManager = new ApplicationFileManager();
        userHomeActivity = activity;
        forceDelete = force;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        userHomeActivity.OnLogoutCompleted(serviceResponse);
    }

    @Override
    protected Boolean doInBackground(TextView... textViews) {
        try {

            HttpClient httpClient = MySSLSocketFactory.getNewHttpClient();
            URI uri = new URI("https://findndrive.no-ip.co.uk/Services/UserService.svc/logout");
            HttpPost postRequest = new HttpPost(uri);

            Gson gson = new Gson();
            String jsonLoginDTO = gson.toJson(forceDelete);

            StringEntity se = new StringEntity(jsonLoginDTO,"UTF-8");
            se.setContentType("application/json;charset=UTF-8");

            postRequest.setEntity(se);

            postRequest.addHeader(Constants.SessionID, fileManager.GetTokenValue());
            postRequest.addHeader(Constants.DeviceId, DeviceID.getID());
            HttpResponse httpResponse = httpClient.execute(postRequest);
            String serviceResponseString = EntityUtils.toString(httpResponse.getEntity());
            Log.e("Service Response:", serviceResponseString);

            Type userType = new TypeToken<ServiceResponse<Boolean>>() {}.getType();
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
