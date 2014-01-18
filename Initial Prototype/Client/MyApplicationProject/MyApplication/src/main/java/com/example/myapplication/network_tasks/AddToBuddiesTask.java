package com.example.myapplication.network_tasks;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.example.myapplication.dtos.FriendDTO;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.domain_objects.User;
import com.example.myapplication.experimental.SSLSocketFactory;
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
 * Created by Michal on 04/01/14.
 */
public class AddToBuddiesTask extends AsyncTask<TextView, String, Boolean> {

    private FriendDTO friendDTO;
    private ServiceResponse serviceResponse;
    private final String TAG = this.getClass().getSimpleName();

    public AddToBuddiesTask(FriendDTO friendDTO)
    {
        this.friendDTO = friendDTO;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
    }

    @Override
    protected Boolean doInBackground(TextView... textViews) {
        try {

            HttpClient httpClient = SSLSocketFactory.getNewHttpClient();
            URI uri = new URI("https://findndrive.no-ip.co.uk/Services/UserService.svc/addtravelbuddy");
            HttpPost postRequest = new HttpPost(uri);

            Gson gson = new Gson();
            String jsonLoginDTO = gson.toJson(friendDTO);
            StringEntity se = new StringEntity(jsonLoginDTO,"UTF-8");
            se.setContentType("application/json;charset=UTF-8");

            postRequest.setEntity(se);

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
