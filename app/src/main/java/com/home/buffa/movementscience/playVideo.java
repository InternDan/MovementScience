package com.home.buffa.movementscience;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.support.constraint.ConstraintLayout;
import android.text.BoringLayout;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;

import com.ipaulpro.afilechooser.utils.FileUtils;

import java.io.IOException;

public class playVideo extends Activity {

    static final int READ_REQUEST_CODE_VIDEO = 1;
    MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
    Uri uri;
    Uri vidUri;
    Bitmap bmp;
    VideoView vid;
    LinearLayout linearLayoutVid;

    int clickTrack = 0;

    int height;
    int width;
    int swidth;
    int sheight;

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

        ExifInterface exifInterface;

        String uri;

        Intent intentReceive = getIntent();
        uri = intentReceive.getExtras().getString("vidUri");
        vidUri = Uri.parse(uri);
        vid = findViewById(R.id.videoView);


        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        bmp = null;

        retriever.setDataSource(FileUtils.getPath(getApplicationContext(),vidUri));
        bmp = retriever.getFrameAtTime(200,MediaMetadataRetriever.OPTION_CLOSEST);
        if (bmp != null) {
            scaleVideo();

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width,height);

            int top = (int) Math.round((((double)sheight - (double)height)/2));
            int left = (int) Math.round((((double)swidth - (double)width)/2));
            lp.setMargins(left,top,0,0);
            linearLayoutVid = findViewById(R.id.linearLayoutVid);
            linearLayoutVid.setLayoutParams(lp);

            vid.seekTo(500);

            MediaController mediaController = new
                    MediaController(this);
            mediaController.setAnchorView(vid);
            vid.setMediaController(mediaController);
            vid.setVideoURI(vidUri);

        }else{
            Toast.makeText(this, "Video is not yet fully created", Toast.LENGTH_LONG).show();
        }
    }

    private void scaleVideo(){

        double ratio;

        //get height and width of video
        height = bmp.getHeight();
        width = bmp.getWidth();
        //get height and width of available screen
        swidth = getWindowManager().getDefaultDisplay().getWidth();
        sheight = getWindowManager().getDefaultDisplay().getHeight();//1.8 is linearlayout weight
        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();

        sheight = sheight - (int)Math.round((TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40,dm)));//leave room for button
        //calculate height and weight of video that will fit in this available space
        if (height > sheight){
             ratio = sheight / (double)height;
             height = (int)Math.round((double)height * ratio);
             width = (int)Math.round((double)width * ratio);
        }
        if (width > swidth){
            ratio = swidth / (double)width;
            width = (int)Math.round((double)width * ratio);
            height = (int)Math.round((double)height * ratio);
        }
        if (width < swidth){
            ratio = swidth / (double)width;
            width = (int)Math.round((double)width * ratio);
            height = (int)Math.round((double)height * ratio);
            if (height > sheight){
                ratio = sheight / (double)height;
                height = (int)Math.round((double)height * ratio);
                width = (int)Math.round((double)width * ratio);
            }
        }
        if (height < sheight){
            ratio = sheight / (double)height;
            height = (int)Math.round((double)height * ratio);
            width = (int)Math.round((double)width * ratio);
            if (width > swidth){
                ratio = swidth / (double)width;
                width = (int)Math.round((double)width * ratio);
                height = (int)Math.round((double)height * ratio);
            }
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
