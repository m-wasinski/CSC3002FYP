package com.example.myapplication.google_maps_utilities;

import android.location.Address;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.Locale;

/**
 * Should the primary Geocoder be unavailable for whatever reason, the app will fall back on this backup Geocoder which will call the
 * Google's web API to decode the address supplied by the user.
 */
public class BackupGeocoder {

    public String TAG = this.getClass().getSimpleName();

    public BackupGeocoder() {
    }

    public JSONObject getJson(String address)
    {
        StringBuilder stringBuilder = new StringBuilder();

        Log.d(TAG, address);

        address = address.replace(" ", ",");

        String url = "http://maps.googleapis.com/maps/api/geocode/json?address=" + address + "&sensor=true";

        try {
            HttpClient httpClient = new DefaultHttpClient();
            HttpContext localContext = new BasicHttpContext();
            HttpPost httpPost = new HttpPost(url);
            HttpResponse response = httpClient.execute(httpPost, localContext);
            InputStream in = response.getEntity().getContent();

            int b;

            while ((b = in.read()) != -1) {
                stringBuilder.append((char) b);
            }

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject = new JSONObject(stringBuilder.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return jsonObject;

        } catch (Exception e) {
            Log.e("CAUGHT: ",e.toString());
            e.printStackTrace();
        }

        return null;
    }

    public Address getAddress(JSONObject jsonObject)
    {
        Address address = new Address(Locale.getDefault());

        JSONObject location;

        try
        {
            //Get JSON Array called "results" and then get the 0th complete object as JSON
            location = jsonObject.getJSONArray("results").getJSONObject(0);

            // Get the value of the attribute whose name is "formatted_string"
            address.setAddressLine(0,location.getString("formatted_address"));

            JSONObject jsonLtdLng = location.getJSONObject("geometry").getJSONObject("location");

            LatLng latLng = new LatLng(Double.parseDouble(jsonLtdLng.getString("lat")), Double.parseDouble(jsonLtdLng.getString("lng")));

            address.setLatitude(latLng.latitude);
            address.setLongitude(latLng.longitude);

        }
        catch (JSONException e)
        {
            e.printStackTrace();
            return null;
        } catch (NumberFormatException e)
        {
            e.printStackTrace();
            return null;
        }

        return address;
    }
}
