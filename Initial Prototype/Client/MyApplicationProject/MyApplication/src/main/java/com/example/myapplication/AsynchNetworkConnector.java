package com.example.myapplication;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.google.gson.Gson;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by Michal on 03/11/13.
 */
public class AsynchNetworkConnector extends AsyncTask <TextView, String, Boolean>{
    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected Boolean doInBackground(TextView... textViews) {

        String username = "test";
        String password = "testpassword";

        /*ArrayList<HashMap<String, String>> jsonlist = new ArrayList<HashMap<String, String>>();
        // Creating new JSON Parser
        JSONParser jParser = new JSONParser();

        // get JSON data from URL
        JSONArray json = jParser.getJSONFromUrl("http://192.168.1.80:8080/userservice/login");

        for (int i = 0; i < json.length(); i++) {

            try {
                JSONObject c = json.getJSONObject(i);

                Log.e("FirstName", c.getString("FirstName"));

                HashMap<String, String> map = new HashMap<String, String>();

                // Add child node to HashMap key & value
                map.put("FirstName",  c.getString("FirstName"));
                jsonlist.add(map);
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }*/

        URI uri = null;
        try {

            //Logging in.
            /*DefaultHttpClient httpClient = new DefaultHttpClient();
            uri = new URI("http://192.168.1.80:8080/userservice/login");
            HttpPost postRequest = new HttpPost(uri);
            // Build JSON string
            JSONStringer userInfo = new JSONStringer().object()
                    .key("UserName").value("UserName")
                    .key("Password").value("test").endObject();

            StringEntity se = new StringEntity(userInfo.toString(),"UTF-8");

            se.setContentType("application/json;charset=UTF-8");
            postRequest.setEntity(se);

            HttpResponse httpResponse = httpClient.execute(postRequest);
            String wtf = EntityUtils.toString(httpResponse.getEntity());

            // Read response data into buffer
            //char[] buffer = new char[(int)httpEntity.getContentLength()];
            //InputStream stream = httpEntity.getContent();
            //InputStreamReader reader = new InputStreamReader(stream);
            //reader.read(buffer);

            JSONObject plates = new JSONObject(wtf);

            Log.e("HTTP RESPONSE: ", "MESSAGE: "+plates.toString());*/


            //Registering.
            DefaultHttpClient httpClient = new DefaultHttpClient();
            uri = new URI("http://192.168.1.80:8080/userservice/register");
            HttpPost postRequest = new HttpPost(uri);
            // Build JSON string
            JSONStringer userInfo = new JSONStringer()
                    .object().key("User")
                    .object().key("FirstName").value("ExampleName")
                    .key("LastName").value("ExampleLastName")
                    .key("Age").value(30)
                    .endObject()
                    .key("UserName").value("Alesia")
                    .key("Password").value("test")
                    .key("ConfirmedPassword").value("test").endObject();

            User user = new User("alex1710@vp.pl", "Aleksandra", "Szczypior", Gender.Female);
            RegisterDTO regdto = new RegisterDTO(user, "testpassword", "testpassword");

            Gson gson = new Gson();
            String jsonuser = gson.toJson(regdto);

            Log.e("PARSED JSON: ", "CONVERTED JSON GSON: "+jsonuser);

            StringEntity se = new StringEntity(jsonuser,"UTF-8");

            //Log.e("HTTP RESPONSE: ", "CONVERTED JSON: "+userInfo.toString());

            se.setContentType("application/json;charset=UTF-8");

            postRequest.setEntity(se);

            HttpResponse httpResponse = httpClient.execute(postRequest);
            String wtf = EntityUtils.toString(httpResponse.getEntity());

            // Read response data into buffer
            //char[] buffer = new char[(int)httpEntity.getContentLength()];
            //InputStream stream = httpEntity.getContent();
            //InputStreamReader reader = new InputStreamReader(stream);
            //reader.read(buffer);

            //JSONObject plates = new JSONObject(wtf);
            Log.e("HTTP RESPONSE: ", "HTTP RESPONSE "+wtf);
            //Log.e("HTTP RESPONSE: ", "MESSAGE: "+plates.toString());
        } catch (URISyntaxException e) {
            Log.e("URISyntaxException: ", e.toString());
            e.printStackTrace();
        }catch (JSONException e) {
            Log.e("JSONException: ", e.toString());
            e.printStackTrace();
        }catch (UnsupportedEncodingException e){
            Log.e("UnsupportedEncodingException: ", e.toString());
            e.printStackTrace();
        }catch (IOException e){
            Log.e("IOException: ", e.toString());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
    }
}
