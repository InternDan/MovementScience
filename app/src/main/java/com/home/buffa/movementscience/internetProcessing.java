package com.home.buffa.movementscience;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.VideoView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import wseemann.media.FFmpegMediaMetadataRetriever;

import static android.media.MediaMetadataRetriever.OPTION_CLOSEST;

public class internetProcessing extends Activity {


    VideoView webVid;
    MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
    Intent intentPass;
    String webVidUrl;

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
        setContentView(R.layout.activity_internet_processing);

        webVid = findViewById(R.id.videoView);
        MediaController mediaController = new
                MediaController(this);
        mediaController.setAnchorView(webVid);
        webVid.setMediaController(mediaController);
        intentPass = new Intent(this,offlineProcessing.class);
    }

    public void setWebVideoUrl(View view){
        EditText edit = findViewById(R.id.editTextWebVideoUrl);
        webVidUrl = edit.getText().toString();
        Uri webVidUri = Uri.parse(webVidUrl);
        webVid.setVideoURI(webVidUri);
        webVid.seekTo(100);
//        mediaMetadataRetriever.setDataSource(this, webVidUri);

        //result_video.requestFocus();
//        intentPass.putExtra("videoPath",webVidUri.toString());
    }

    public void pickPoints(View view){
        int currentPosition = 0; //in millisecond
        Bitmap bitmap = mediaMetadataRetriever.getFrameAtTime(currentPosition*1000, OPTION_CLOSEST); //unit in microsecons
//        FFmpegMediaMetadataRetriever mmr = new FFmpegMediaMetadataRetriever();
//        mmr.setDataSource(videoAbsolutePath);
//        Bitmap bitmap = mmr.getFrameAtTime(currentPosition, FFmpegMediaMetadataRetriever.OPTION_CLOSEST);
        intentPass.putExtra("firstFramePosition",currentPosition);
        String path = createImageFromBitmapFirst(bitmap);
        intentPass.putExtra("firstFramePathString",path);
        intentPass.putExtra("videoAbsolutePath",webVidUrl);
        startActivity(intentPass);
    }

    public void keyFrame(View view){
        int currentPosition = webVid.getCurrentPosition(); //in millisecond, change to get current frame
        FFmpegMediaMetadataRetriever mmr = new FFmpegMediaMetadataRetriever();
        mmr.setDataSource(webVidUrl);
        Bitmap bitmap = mmr.getFrameAtTime(currentPosition*1000, FFmpegMediaMetadataRetriever.OPTION_CLOSEST);

        Intent intentPassKeyFrame = new Intent(this,keyFrame.class);
        String path = createImageFromBitmapKey(bitmap);
        intentPassKeyFrame.putExtra("keyFramePathString",path);
        startActivity(intentPassKeyFrame);
    }








    public String createImageFromBitmapFirst(Bitmap bitmap){
        DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd 'at' HH-mm-ss");
        String eMagTime = df2.format(Calendar.getInstance().getTime());
        File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        // Create imageDir
        File mypath=new File(directory,eMagTime + "-firstFrame.png");


        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath() + "/" + eMagTime + "-firstFrame.png";

    }

    public String createImageFromBitmapKey(Bitmap bitmap){

        DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd'at'HH-mm-ss");
        String eMagTime = df2.format(Calendar.getInstance().getTime());
        File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        // Create imageDir
        File mypath = new File(directory,eMagTime +  "-keyFrame.png");


        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath() + "/" + eMagTime + "-keyFrame.png";

    }

    public void goHome(View view){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }


}
