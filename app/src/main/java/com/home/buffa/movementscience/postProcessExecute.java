package com.home.buffa.movementscience;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class postProcessExecute extends Activity {

    Uri vid1URI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poat_process_execute);

        Intent intentReceive = getIntent();
        String vidPath = intentReceive.getExtras().getString("vidPath1");
        vid1URI = Uri.parse(vidPath);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
    }
}
