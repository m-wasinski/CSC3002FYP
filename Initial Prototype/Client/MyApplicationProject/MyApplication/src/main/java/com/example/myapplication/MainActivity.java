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
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends ActionBarActivity {

    private TextView textView1;


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

        textView1 = (TextView) findViewById(R.id.TextView1);
        GetPersons();
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
                        textView1.setText(line);
                        JSONObject jsonResponse = new JSONObject(line);
                        JSONArray jsonArray = jsonResponse.getJSONArray("GetUserResult");

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

}
