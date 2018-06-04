package com.home.buffa.movementscience;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
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

    public void cleanTemporaryFiles(View view){
        //FileManagement.removeAllFiles(getApplicationContext());
        //Folders that are written to
        final File picDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        final File vidDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);

        //prefixes used
        final String combinedImage = "CombinedImage";
        final String fullSize = "FullSize";
        final String smallSize = "SmallSize";
        final String firstFrame = "FirstFrame";
        final String keyFrame = "KeyFrame";
        final String tmp = "Tmp";
        final String talkFrame = "TalkFrame";
        final String voiceover = "Voiceover";
        final String realtime = "Realtime";

        AlertDialog.Builder builder = new AlertDialog.Builder(UtilityLauncher.this);
        builder.setCancelable(true);
        builder.setMessage("Are you sure? This will delete all pictures and movies generated by this app that you haven't shared yet!");
        builder.setPositiveButton("Confirm",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        File[] files = new File(picDir.toString()).listFiles();
                        for(File file : files){
                            if(file.isFile()){
                                if(file.toString().contains(combinedImage)) {
                                    file.delete();
                                }
                                if(file.toString().contains(fullSize)) {
                                    file.delete();
                                }
                                if(file.toString().contains(smallSize)) {
                                    file.delete();
                                }
                                if(file.toString().contains(firstFrame)) {
                                    file.delete();
                                }
                                if(file.toString().contains(keyFrame)) {
                                    file.delete();
                                }
                                if(file.toString().contains(tmp)) {
                                    file.delete();
                                }
                                if(file.toString().contains(talkFrame)) {
                                    file.delete();
                                }
                                if(file.toString().contains(voiceover)) {
                                    file.delete();
                                }
                                if(file.toString().contains(realtime)) {
                                    file.delete();
                                }
                            }
                        }

                        files = new File(vidDir.toString()).listFiles();
                        for(File file : files){
                            if(file.isFile()){
                                if(file.toString().contains(combinedImage)) {
                                    file.delete();
                                }
                                if(file.toString().contains(fullSize)) {
                                    file.delete();
                                }
                                if(file.toString().contains(smallSize)) {
                                    file.delete();
                                }
                                if(file.toString().contains(firstFrame)) {
                                    file.delete();
                                }
                                if(file.toString().contains(keyFrame)) {
                                    file.delete();
                                }
                                if(file.toString().contains(tmp)) {
                                    file.delete();
                                }
                                if(file.toString().contains(talkFrame)) {
                                    file.delete();
                                }
                                if(file.toString().contains(voiceover)) {
                                    file.delete();
                                }
                                if(file.toString().contains(realtime)) {
                                    file.delete();
                                }
                            }
                        }
                        Toast.makeText(getApplicationContext(), "Files Deleted!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(),UtilityLauncher.class);
                        startActivity(intent);
                    }
                });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
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
