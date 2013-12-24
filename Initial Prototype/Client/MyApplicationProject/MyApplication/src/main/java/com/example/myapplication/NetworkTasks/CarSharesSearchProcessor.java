package com.example.myapplication.NetworkTasks;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.example.myapplication.DomainObjects.CarShare;
import com.example.myapplication.DomainObjects.ServiceResponse;
import com.example.myapplication.Experimental.MySSLSocketFactory;
import com.example.myapplication.Interfaces.SearchCompleted;
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
 * Created by Michal on 01/12/13.
 */
public class CarSharesSearchProcessor extends AsyncTask<TextView, String, Boolean> {

    private CarShare carShare;
    private SearchCompleted searchCompleted;
    private ServiceResponse<ArrayList<CarShare>> serviceResponse;

    public CarSharesSearchProcessor(CarShare cs, SearchCompleted sc)
    {
        carShare = cs;
        searchCompleted = sc;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        searchCompleted.onSearchCompleted(serviceResponse);
    }

    @Override
    protected Boolean doInBackground(TextView... textViews) {
        try {

            HttpClient httpClient = MySSLSocketFactory.getNewHttpClient();
            URI uri = new URI("https://findndrive.no-ip.co.uk/Services/SearchService.svc/searchcarshare");
            HttpPost postRequest = new HttpPost(uri);

            Gson gson = new Gson();
            String stringId = gson.toJson(carShare);

            Log.e("Car shares:", stringId);
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
