package com.home.buffa.movementscience;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;

import java.io.File;

public class displayTrackingResults extends Activity {

    int frameRate = 0;
    long duration = 0;
    VideoView videoViewResult;

    //opencv custom player ideas
//    VideoCapture cap = new VideoCapture();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_real_time_tracking);
        View decorView = getWindow().getDecorView();
        // Hide both the navigation bar and the status bar.
        // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
        // a general rule, you should design your app to hide the status bar whenever you
        // hide the navigation bar.
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        Intent intentReceive = getIntent();
        String vidPath = intentReceive.getExtras().getString("videoPath");
        frameRate = intentReceive.getExtras().getInt("frameRate");
        duration = intentReceive.getExtras().getLong("duration");
        setContentView(R.layout.activity_display_tracking_results);
        Uri videoUri = Uri.fromFile(new File((vidPath)));
        videoViewResult = findViewById(R.id.videoViewResult);
        MediaController mediaController = new
                MediaController(this);
        mediaController.setAnchorView(videoViewResult);
        videoViewResult.setMediaController(mediaController);
        videoViewResult.setVideoURI(videoUri);
        videoViewResult.seekTo(300);
        videoViewResult.start();
    }

    public void goHome(View view){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }
}


