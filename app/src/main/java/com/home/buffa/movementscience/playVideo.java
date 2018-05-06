package com.home.buffa.movementscience;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;

import com.ipaulpro.afilechooser.utils.FileUtils;

import java.io.IOException;

public class playVideo extends Activity {

    Uri vidUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_video);

        View decorView = getWindow().getDecorView();
        // Hide both the navigation bar and the status bar.
        // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
        // a general rule, you should design your app to hide the status bar whenever you
        // hide the navigation bar.
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        resizableVideoView videoView;

        int width;
        int height;

        ExifInterface exifInterface;

        String uri;

        Intent intentReceive = getIntent();
        uri = intentReceive.getExtras().getString("vidUri");
        vidUri = Uri.parse(uri);
        videoView = findViewById(R.id.videoViewPlayVideo);

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        Bitmap bmp = null;

        retriever.setDataSource(FileUtils.getPath(getApplicationContext(),vidUri));
        bmp = retriever.getFrameAtTime(200,MediaMetadataRetriever.OPTION_CLOSEST);
        if (bmp != null) {
            height = bmp.getHeight();
            width = bmp.getWidth();

            int swidth = getWindowManager().getDefaultDisplay().getWidth();
            int sheight = getWindowManager().getDefaultDisplay().getHeight();

            double hRatio = (double) sheight / (double) height;
            double wRatio = (double) swidth / (double) width;

            height = (int) Math.round(hRatio * (double) height);
            width = (int) Math.round(wRatio * (double) width);


            videoView.setDimensions(width,height);
            videoView.seekTo(200);

            MediaController mediaController = new
                    MediaController(this);
            mediaController.setAnchorView(videoView);
            videoView.setMediaController(mediaController);
            videoView.setVideoURI(vidUri);

        }else{
            Toast.makeText(this, "Video is not yet fully created", Toast.LENGTH_LONG).show();
        }
    }


    public void shareVideo(View view){
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("video/*");
        sharingIntent.putExtra(Intent.EXTRA_STREAM,vidUri);
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }

    public void goHome(View view){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

}
