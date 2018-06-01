package com.home.buffa.movementscience;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

public class realTimeChooser extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_real_time_chooser);

        NavigationView navigationView  = findViewById(R.id.nav_view);
        final DrawerLayout mDrawerLayout = new DrawerLayout(getApplicationContext());
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // set item as selected to persist highlight
                        menuItem.setChecked(true);
                        // close drawer when item is tapped
                        mDrawerLayout.closeDrawers();
                        // Add code here to update the UI based on the item selected
                        // For example, swap UI fragments here
                        switch (menuItem.getItemId()) {
                            case R.id.action_capture:
                                Intent intent = new Intent(getApplicationContext(), CaptureLauncher.class);
                                startActivity(intent);
                                break;
                            case R.id.action_edit:
                                intent = new Intent(getApplicationContext(), EditLauncher.class);
                                startActivity(intent);
                                break;
                            case R.id.action_utilities:
                                intent = new Intent(getApplicationContext(), UtilityLauncher.class);
                                startActivity(intent);
                                break;
                            case R.id.action_help:
                                break;
                        }
                        return false;
                    }
                });
        View headerview = navigationView.getHeaderView(0);
        LinearLayout header = (LinearLayout) headerview.findViewById(R.id.navigation_header);
        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
            }
        });

    }

    public void realTimeTracking(View view) {
        Intent intent = new Intent(getApplicationContext(), realTimeTracking.class);
        intent.putExtra("Record",false);
        startActivity(intent);
    }

    public void realTimeTrackingWithRecording(View view) {
        /*Intent intent = new Intent(getApplicationContext(), realTimeTracking.class);
        startActivity(intent);*/
        Intent intent = new Intent(getApplicationContext(), realTimeTracking.class);
        intent.putExtra("Record",true);
        startActivity(intent);
        //Toast.makeText(this, "Feature not yet implemented", Toast.LENGTH_SHORT).show();
    }



}
