package com.example.myapplication;

import android.annotation.TargetApi;
import android.os.NetworkOnMainThreadException;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends ActionBarActivity {

    private TextView textView1;
    private Thread _networkThread;

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }


        _networkThread = new Thread(new Runnable() {
            @Override
            public void run() {
                final String USER_ID = "GetUserResult";
                final String TAG_ID = "Id";
                final String TAG_FIRST_NAME = "FirstName";
                final String TAG_LAST_NAME = "LastName";
                final String TAG_AGE = "Age";
                textView1 = (TextView) findViewById(R.id.TextView1);
                AsynchNetworkConnector aNetworkConnector = new AsynchNetworkConnector();
                aNetworkConnector.execute();
            }
        });

        _networkThread.start();

        //JSON Node Names
        final String USER_ID = "GetUserResult";
        final String TAG_ID = "Id";
        final String TAG_FIRST_NAME = "FirstName";
        final String TAG_LAST_NAME = "LastName";
        final String TAG_AGE = "Age";

        JSONArray user = null;

        // Creating new JSON Parser
        JSONParser jParser = new JSONParser();

        // Getting JSON from URL
        //JSONObject json = jParser.getJSONFromUrl("http://192.168.1.80:8080/prototypeservice/user");
        //user = json.getJSONArray(USER_ID);
        /*try {
            // Getting JSON Array
            //user = json.getJSONArray(USER_ID);
            //textView1.setText("Length " + user.length());
            //JSONObject c = user.getJSONObject(0);

            // Storing  JSON item in a Variable
            //String id = c.getString(TAG_ID);
            //String firstName = c.getString(TAG_FIRST_NAME);
            //String lasttName = c.getString(TAG_LAST_NAME);
            //String age = c.getString(TAG_AGE);

            //textView1.setText(id + " " + firstName + " " + lasttName);

        } catch (JSONException e) {
            e.printStackTrace();
            textView1.setText(e.toString());
        } catch (NetworkOnMainThreadException e)
        {

        }*/
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public void GetPersons() {
        ArrayList<String> persons = FromJSONtoArrayList();
        //ListView listView1 = (ListView)findViewById(R.id.ListView01);
        //listView1.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, persons));
}

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public ArrayList<String> FromJSONtoArrayList() {
        ArrayList<String> listItems = new ArrayList<String>();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
                try {

                        // Replace it with your own WCF service path
                        URL json = new URL("http://192.168.1.80:8080/prototypeservice/user");

                        URLConnection jc = json.openConnection();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(jc.getInputStream()));

                        String line = reader.readLine();

                        JSONObject jsonResponse = new JSONObject(line);

                        JSONArray jsonArray = jsonResponse.getJSONArray("GetUserResult");
                    textView1.setText("Test " + getString(jsonArray.length()) );
                        for (int i = 0; i < jsonArray.length(); i++) {

                            JSONObject jObject = (JSONObject)jsonArray.get(i);

                            // "FullName" is the property of .NET object spGetPersonsResult,
                            // and also the name of column in SQL Server 2008
                            listItems.add(jObject.getString("FirstName"));

                        }

                        reader.close();

                } catch(NullPointerException e){
                    //Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    //Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                } catch (IOException e) {
                   // Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
                    textView1.setText(e.toString());
                    e.printStackTrace();
                } catch (NetworkOnMainThreadException e){
                    //Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
                    textView1.setText(e.toString());
                    e.printStackTrace();
                } catch(Exception e){
                    //textView1.setText(e.toString());
                }

        return listItems;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }

    public static HttpClient getNewHttpClient() {
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);

            SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", sf, 443));

            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

            return new DefaultHttpClient(ccm, params);
        } catch (Exception e) {
            return new DefaultHttpClient();
        }
    }

}
