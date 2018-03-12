package com.home.buffa.movementscience;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;

public class recordVideo extends Activity {
    static final int REQUEST_VIDEO_CAPTURE = 1;



    VideoView result_video;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_video);
        result_video = (VideoView)findViewById(R.id.videoView1);
        MediaController mediaController = new
                MediaController(this);
        mediaController.setAnchorView(result_video);
        result_video.setMediaController(mediaController);
        dispatchTakeVideoIntent(result_video);
    }

    public void dispatchTakeVideoIntent(View v) {

        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            Uri videoUri = data.getData();
            result_video.setVideoURI(videoUri);
            result_video.requestFocus();
            //result_video.start();
        }

    }


}
