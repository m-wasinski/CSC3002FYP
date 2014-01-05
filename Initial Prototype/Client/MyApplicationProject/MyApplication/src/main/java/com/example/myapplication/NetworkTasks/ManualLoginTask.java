package com.example.myapplication.NetworkTasks;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;
import com.example.myapplication.Constants.Constants;
import com.example.myapplication.DTOs.LoginDTO;
import com.example.myapplication.DomainObjects.ServiceResponse;
import com.example.myapplication.DomainObjects.User;
import com.example.myapplication.Experimental.AppData;
import com.example.myapplication.Experimental.SSLSocketFactory;
import com.example.myapplication.Helpers.ApplicationFileManager;
import com.example.myapplication.Helpers.DeviceID;
import com.example.myapplication.Interfaces.loginActivityInterface;
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
public class ManualLoginTask extends AsyncTask<TextView, String, Boolean> {

    private final String TAG = "ManualLoginTask";

    private LoginDTO loginDTO;
    private loginActivityInterface listener;
    private ServiceResponse<User> serviceResponse;
    private boolean RememberMe;
    private AppData appData;

    public ManualLoginTask(String userName, String password, boolean rememberMe, loginActivityInterface lis, AppData app)
    {
        appData = app;
        loginDTO = new LoginDTO(userName, password, appData.getRegistrationId());
        listener = lis;
        RememberMe = rememberMe;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        listener.manualLoginCompleted(serviceResponse);
    }

    @Override
    protected Boolean doInBackground(TextView... textViews) {

        try {

            HttpClient httpClient = SSLSocketFactory.getNewHttpClient();
            URI uri = new URI("https://findndrive.no-ip.co.uk/Services/UserService.svc/manuallogin");
            HttpPost postRequest = new HttpPost(uri);

            Gson gson = new Gson();
            String jsonLoginDTO = gson.toJson(loginDTO);

            StringEntity se = new StringEntity(jsonLoginDTO,"UTF-8");
            se.setContentType("application/json;charset=UTF-8");

            postRequest.setEntity(se);

            postRequest.addHeader(Constants.REMEMBER_ME, ""+RememberMe);
            postRequest.addHeader(Constants.DEVICE_ID, DeviceID.getID());
            postRequest.addHeader(Constants.UUID, appData.getUUID());

            HttpResponse httpResponse = httpClient.execute(postRequest);
            String serviceResponseString = EntityUtils.toString(httpResponse.getEntity());


            Type userType = new TypeToken<ServiceResponse<User>>() {}.getType();
            Log.e(TAG, serviceResponseString);
            serviceResponse = gson.fromJson(serviceResponseString, userType);

            if(serviceResponse.ServiceResponseCode == Constants.SERVICE_RESPONSE_SUCCESS)
            {
                String session = httpResponse.getFirstHeader(Constants.SESSION_ID).getValue();
                ApplicationFileManager fileManager = new ApplicationFileManager();
                Log.e("Session", session);
                fileManager.CreateSessionCookie(session);
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