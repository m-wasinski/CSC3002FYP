package com.example.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

/**
 * Created by Michal on 12/11/13.
 */
public class RegistrationActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.registration_activity);

    }
}