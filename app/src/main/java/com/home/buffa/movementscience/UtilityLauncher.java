package com.home.buffa.movementscience;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.File;

public class UtilityLauncher extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_utility_launcher);

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
                            case R.id.action_recording:
                                Intent intent = new Intent(getApplicationContext(), recordVideo.class);
                                startActivity(intent);
                                break;
                            case R.id.action_editing:
                                intent = new Intent(getApplicationContext(), offlineProcessing.class);
                                startActivity(intent);
                                break;
                            case R.id.action_settings:
                                intent = new Intent(getApplicationContext(), settingsActivity.class);
                                startActivity(intent);
                                break;
                            case R.id.action_help:
                                break;
                        }
                        return false;
                    }
                });
    }

    public void editSettings(View view){
        Intent intent = new Intent(getApplicationContext(),settingsActivity.class);
        startActivity(intent);
    }

    public void updateThumbnails(View view){

        String path = Environment.getExternalStorageDirectory().toString()+"/Pictures";
        File directory = new File(path);
        File[] files = directory.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                updateGallery(files[i]);
            }
        }

        path = getApplicationContext().getFilesDir().toString()+"/Download";
        directory = new File(path);
        files = directory.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                updateGallery(files[i]);
            }
        }

        path = Environment.getExternalStorageDirectory().toString()+"/Movies";
        directory = new File(path);
        files = directory.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                updateGallery(files[i]);
            }
        }
    }

    public void updateGallery(File file){
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(file);
        mediaScanIntent.setData(uri);
        this.sendBroadcast(mediaScanIntent);
    }

    public void cleanTemporaryFiles(){
        Toast.makeText(this, "Not yet implemented", Toast.LENGTH_SHORT).show();
    }

    public void onResume() {
        super.onResume();
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }
}
