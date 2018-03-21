package com.home.buffa.movementscience;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import org.jcodec.api.android.AndroidSequenceEncoder;

import java.io.IOException;

import static com.home.buffa.movementscience.trackPointsTest.getPath;

public class postProcessing extends Activity {

    AndroidSequenceEncoder enc;
    static final int READ_REQUEST_CODE_VIDEO1 = 1;
    static final int READ_REQUEST_CODE_VIDEO2 = 2;
    MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
    Uri videoUri;
    String videoAbsolutePath;
    Bitmap bmp;
    String bmpPath;
    VideoView vid1;
    VideoView vid2;
    Intent intentPassPostProcessing;
    LinearLayout linearLayoutVid1;
    LinearLayout linearLayoutVid2;

    int clickTrack1 = 0;
    int clickTrack2 = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View decorView = getWindow().getDecorView();
        // Hide both the navigation bar and the status bar.
        // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
        // a general rule, you should design your app to hide the status bar whenever you
        // hide the navigation bar.
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        setContentView(R.layout.activity_post_processing);

        vid1 = (VideoView)findViewById(R.id.videoViewVideo1);
        vid2 = (VideoView)findViewById(R.id.videoViewVideo2);

        MediaController mediaController1 = new
                MediaController(this);
        mediaController1.setAnchorView(vid1);
        vid1.setMediaController(mediaController1);
        MediaController mediaController2 = new
                MediaController(this);
        mediaController2.setAnchorView(vid2);
        vid2.setMediaController(mediaController2);
        intentPassPostProcessing = new Intent(this,postProcessExecute.class);

        final View button1 = findViewById(R.id.linearLayoutVid1);
        button1.setOnClickListener (new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(clickTrack1 < 1) {
                    clickTrack1 = 1;
                    loadVideo1();

                }
            }
        });

        final View button2 = findViewById(R.id.linearLayoutVid2);
        button2.setOnClickListener (new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(clickTrack2 < 1) {
                    clickTrack2 = 1;
                    loadVideo2();
                }
            }
        });


    }

    public void loadVideo1(){
        Intent intentGetVideo = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intentGetVideo.addCategory(Intent.CATEGORY_OPENABLE);
        intentGetVideo.setType("video/*");
        startActivityForResult(intentGetVideo, READ_REQUEST_CODE_VIDEO1);
    }
    public void loadVideo2(){
        Intent intentGetVideo = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intentGetVideo.addCategory(Intent.CATEGORY_OPENABLE);
        intentGetVideo.setType("video/*");
        startActivityForResult(intentGetVideo, READ_REQUEST_CODE_VIDEO2);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == READ_REQUEST_CODE_VIDEO1 && resultCode == RESULT_OK) {
            videoUri = data.getData();
            videoAbsolutePath = getPath(getApplicationContext(), videoUri);
            if (videoAbsolutePath == null) {
                Context context = getApplicationContext();
                CharSequence text = "Unable to load from that location; ensure file is stored locally on device.";
                int duration = Toast.LENGTH_LONG;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                return;
            }
            mediaMetadataRetriever.setDataSource(this, videoUri);
            vid1.setVideoURI(videoUri);
            vid1.seekTo(200);
            //result_video.requestFocus();
            intentPassPostProcessing.putExtra("videoPath1", videoUri.toString());
        }
        if (requestCode == READ_REQUEST_CODE_VIDEO2 && resultCode == RESULT_OK) {
            videoUri = data.getData();
            videoAbsolutePath = getPath(getApplicationContext(), videoUri);
            if (videoAbsolutePath == null) {
                Context context = getApplicationContext();
                CharSequence text = "Unable to load from that location; ensure file is stored locally on device.";
                int duration = Toast.LENGTH_LONG;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                return;
            }
            mediaMetadataRetriever.setDataSource(this, videoUri);
            vid2.setVideoURI(videoUri);
            vid2.seekTo(200);
            //result_video.requestFocus();
            intentPassPostProcessing.putExtra("videoPath2", videoUri.toString());
        }
    }

    public void resetViews(View view){
        clickTrack1 = 0;
        clickTrack2 = 0;
        loadVideo1();
        loadVideo2();
    }

    public void editSettings(View view){
        Intent intent = new Intent(getApplicationContext(),settingsActivity.class);
        startActivity(intent);
    }

    public void onResume() {
        super.onResume();
    }

    public void goHome(View view){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    protected void onPause() {
        super.onPause();
    }
}
