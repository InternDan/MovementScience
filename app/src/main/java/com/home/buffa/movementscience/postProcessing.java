package com.home.buffa.movementscience;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.ipaulpro.afilechooser.utils.FileUtils;

import org.jcodec.api.android.AndroidSequenceEncoder;

import java.io.IOException;

public class postProcessing extends Activity {

    AndroidSequenceEncoder enc;
    static final int READ_REQUEST_CODE_VIDEO1 = 1;
    static final int READ_REQUEST_CODE_VIDEO2 = 2;
    MediaMetadataRetriever mediaMetadataRetriever1 = new MediaMetadataRetriever();
    MediaMetadataRetriever mediaMetadataRetriever2 = new MediaMetadataRetriever();
    Uri videoUri1;
    Uri videoUri2;
    String videoAbsolutePath;
    Bitmap bmp;
    String bmpPath;
    VideoView vid1;
    VideoView vid2;
    LinearLayout linearLayoutVid1;
    LinearLayout linearLayoutVid2;

    int clickTrack1 = 0;
    int clickTrack2 = 0;

    public static int vid1Height;
    public static int vid1Width;
    public static int vid2Height;
    public static int vid2Width;

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



        final View button1 = findViewById(R.id.linearLayoutVid1);
        button1.setOnClickListener (new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickTrack1 == 0) {
                    loadVideo1();

                }
            }
        });

        final View button2 = findViewById(R.id.linearLayoutVid2);
        button2.setOnClickListener (new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickTrack2 == 0) {
                    loadVideo2();
                }
            }
        });

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
            videoUri1 = data.getData();
            videoAbsolutePath = FileUtils.getPath(getApplicationContext(), videoUri1);
            if (videoAbsolutePath == null) {
                Context context = getApplicationContext();
                CharSequence text = "Unable to load from that location; ensure file is stored locally on device.";
                int duration = Toast.LENGTH_LONG;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                return;
            }
            mediaMetadataRetriever1.setDataSource(this, videoUri1);
            bmp = mediaMetadataRetriever1.getFrameAtTime(200);
            vid1Height = bmp.getHeight();
            vid1Width = bmp.getWidth();
            //BitmapDrawable bitmapDrawable = new BitmapDrawable(bmp);
            //vid1.setBackground(bitmapDrawable);
            MediaController mediaController1 = new MediaController(this);
            mediaController1.setAnchorView(vid1);
            vid1.setMediaController(mediaController1);
            vid1.setVideoURI(videoUri1);
            vid1.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    vid1.setBackgroundColor(Color.TRANSPARENT);
                }
            });
            clickTrack1 = 1;
            //result_video.requestFocus();
            if (videoUri2 == null) {
                loadVideo2();
            }


        }
        if (requestCode == READ_REQUEST_CODE_VIDEO2 && resultCode == RESULT_OK) {
            videoUri2 = data.getData();
            videoAbsolutePath = FileUtils.getPath(getApplicationContext(), videoUri2);
            if (videoAbsolutePath == null) {
                Context context = getApplicationContext();
                CharSequence text = "Unable to load from that location; ensure file is stored locally on device.";
                int duration = Toast.LENGTH_LONG;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                return;
            }
            mediaMetadataRetriever2.setDataSource(this, videoUri2);
            bmp = mediaMetadataRetriever2.getFrameAtTime(200);
            vid2Height = bmp.getHeight();
            vid2Width = bmp.getWidth();
            BitmapDrawable bitmapDrawable = new BitmapDrawable(bmp);
            vid2.setBackground(bitmapDrawable);
            vid2.setVideoURI(videoUri2);
            MediaController mediaController2 = new MediaController(this);
            mediaController2.setAnchorView(vid2);
            vid2.setMediaController(mediaController2);
            clickTrack2 = 1;
            vid2.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    vid2.setBackgroundColor(Color.TRANSPARENT);
                }
            });
            //result_video.requestFocus();
            if (videoUri1 == null) {
                loadVideo1();
            }
        }
    }

    public void resetViews(View view){
        Intent intent = new Intent(getApplicationContext(),postProcessing.class);
        startActivity(intent);
    }

    public void editSettings(View view){
        Intent intent = new Intent(getApplicationContext(),settingsActivity.class);
        startActivity(intent);
    }

    public void onResume() {
        super.onResume();
    }

    protected void onPause() {
        super.onPause();
    }

    public void postProcessExectute(View view){
        if (videoUri1 != null && videoUri2 != null) {
            Intent intentPassPostProcessing = new Intent(getApplicationContext(), postProcessPreview.class);
            intentPassPostProcessing.putExtra("videoPath1", videoUri1.toString());
            intentPassPostProcessing.putExtra("videoPath2", videoUri2.toString());
            startActivity(intentPassPostProcessing);
        }else{
            Toast.makeText(this, "Make sure both videos are selected!", Toast.LENGTH_LONG).show();
        }
    }
}
