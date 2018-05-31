package com.home.buffa.movementscience;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.VideoView;

import com.ipaulpro.afilechooser.utils.FileUtils;

import org.jcodec.api.android.AndroidSequenceEncoder;
import org.jcodec.common.io.SeekableByteChannel;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import wseemann.media.FFmpegMediaMetadataRetriever;

public class postProcessPreview extends Activity {

    ImageView img1;
    Intent intentReceive;
    String videoPath1;
    String videoPath2;
    Uri videoUri1;
    Uri videoUri2;

    AndroidSequenceEncoder enc;
    SeekableByteChannel out;

    String ppOrder;
    String ppSize;
    String ppOrientation;
    int rotateDegreesPostProcess;
    int rotateDegreesPostProcess2;

    MediaFormat format1;
    MediaFormat format2;

    Bitmap bmpJoined = null;

    int h1;
    int h2;
    int w1;
    int w2;

    String videoAbsolutePath1;
    String videoAbsolutePath2;

    VideoProcessing vp = new VideoProcessing();

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @SuppressLint("LongLogTag")
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    new postProcessPreview.beginCombiningProcedure().execute(null, null, null);//
                    Toast.makeText(getApplicationContext(),"Preview image will load once merged; this may take a moment", Toast.LENGTH_LONG).show();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

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
        setContentView(R.layout.activity_post_process_preview);
        img1 = (ImageView)findViewById(R.id.imageViewPreview1);

        intentReceive = getIntent();
        videoPath1 = intentReceive.getExtras().getString("videoPath1");
        videoPath2 = intentReceive.getExtras().getString("videoPath2");
        videoUri1 = Uri.parse(videoPath1);
        videoUri2 = Uri.parse(videoPath2);

        videoAbsolutePath1 = FileUtils.getPath(getApplicationContext(),videoUri1);
        videoAbsolutePath2 = FileUtils.getPath(getApplicationContext(),videoUri2);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        ppOrder = sharedPref.getString("pref_postProcessingPlayOrder","s");
        ppSize = sharedPref.getString("pref_postProcessingSize","small");
        ppOrientation = sharedPref.getString("pref_postProcessingOrientation","lr");
        String rotDeg = sharedPref.getString("pref_rotateDegreesPostProcess","0");
        rotateDegreesPostProcess = Integer.valueOf(rotDeg);
        rotDeg = sharedPref.getString("pref_rotateDegreesPostProcess2","0");
        rotateDegreesPostProcess2 = Integer.valueOf(rotDeg);

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

    private class beginCombiningProcedure extends AsyncTask<Void, Void, Void> {

        protected Void doInBackground(Void... params) {
            MainActivity.mBuilder.setContentText("Initializing software tools");
            MainActivity.notificationManager.notify(MainActivity.notificationID, MainActivity.mBuilder.build());
            CombineVideos cv = new CombineVideos();
            cv.videoAbsolutePath1 = videoAbsolutePath1;
            cv.videoAbsolutePath2 = videoAbsolutePath2;
            cv.ppOrder = ppOrder;
            cv.ppSize = ppSize;
            cv.ppOrientation = ppOrientation;
            cv.postRotate1 = rotateDegreesPostProcess;
            cv.postRotate2 = rotateDegreesPostProcess2;
            bmpJoined = cv.getPreviewFrame();

            return null;
        }

        protected void onProgressUpdate(Void... progress) {

        }

        protected void onPostExecute(Void result) {
            img1.setImageBitmap(bmpJoined);
        }
    }



    public void editSettings(View view){
        Intent intent = new Intent(getApplicationContext(),settingsActivity.class);
        startActivity(intent);
    }

    public void postProcessExectute(View view){
        Intent intentPassPostProcessing = new Intent(getApplicationContext(),postProcessExecute.class);
        //bmpJoined.recycle();
        intentPassPostProcessing.putExtra("videoPath1", videoUri1.toString());
        intentPassPostProcessing.putExtra("videoPath2", videoUri2.toString());
        intentPassPostProcessing.putExtra("h1", h1);
        intentPassPostProcessing.putExtra("h2", h2);
        intentPassPostProcessing.putExtra("w1", w1);
        intentPassPostProcessing.putExtra("w2", w2);//passing resized to desired heights height and widths
        startActivity(intentPassPostProcessing);
    }

    public void goHome(View view){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }
}
