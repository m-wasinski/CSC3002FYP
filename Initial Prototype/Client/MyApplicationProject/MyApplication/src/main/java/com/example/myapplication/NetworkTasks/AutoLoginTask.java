package com.example.myapplication.NetworkTasks;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.example.myapplication.Constants.Constants;
import com.example.myapplication.DomainObjects.ServiceResponse;
import com.example.myapplication.DomainObjects.User;
import com.example.myapplication.Experimental.SSLSocketFactory;
import com.example.myapplication.Helpers.ApplicationFileManager;
import com.example.myapplication.Helpers.DeviceID;
import com.example.myapplication.Interfaces.mainActivityInterface;
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
 public class AutoLoginTask extends AsyncTask<TextView, String, Boolean> {

    private mainActivityInterface listener;
    private ServiceResponse<User> serviceResponse;

    private final String TAG = "AutoLoginTask";

    public AutoLoginTask(mainActivityInterface lis)
    {
        listener = lis;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        listener.autoLoginCompleted(serviceResponse);
    }

    @Override
    protected Boolean doInBackground(TextView... textViews) {

        ApplicationFileManager fileManager = new ApplicationFileManager();

        try {

            HttpClient httpClient = SSLSocketFactory.getNewHttpClient();
            URI uri = new URI("https://findndrive.no-ip.co.uk/Services/UserService.svc/autologin");
            HttpPost postRequest = new HttpPost(uri);

            Gson gson = new Gson();
            String jsonLoginDTO = gson.toJson("");
            StringEntity se = new StringEntity(jsonLoginDTO,"UTF-8");
            se.setContentType("application/json;charset=UTF-8");

            postRequest.setEntity(se);

            postRequest.addHeader(Constants.DEVICE_ID, DeviceID.getID());
            postRequest.addHeader(Constants.SESSION_ID, fileManager.GetTokenValue());

            HttpResponse httpResponse = httpClient.execute(postRequest);
            String serviceResponseString = EntityUtils.toString(httpResponse.getEntity());


            Type userType = new TypeToken<ServiceResponse<User>>() {}.getType();

            Log.i(TAG, "Received the following service response: " + serviceResponseString);
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
