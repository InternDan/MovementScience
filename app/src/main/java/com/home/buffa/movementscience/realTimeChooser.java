package com.home.buffa.movementscience;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Toast;

public class realTimeChooser extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_real_time_chooser);

    }

    public void realTimeTracking(View view) {
        Intent intent = new Intent(getApplicationContext(), realTimeTracking.class);
        startActivity(intent);
    }

    public void realTimeTrackingWithRecording(View view) {
        /*Intent intent = new Intent(getApplicationContext(), realTimeTracking.class);
        startActivity(intent);*/
        Toast.makeText(this, "Feature not yet implemented", Toast.LENGTH_SHORT).show();
    }

}
