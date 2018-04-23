package com.home.buffa.movementscience;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.widget.Toast;

import com.ipaulpro.afilechooser.utils.FileUtils;

import org.jcodec.api.android.AndroidSequenceEncoder;
import org.jcodec.common.io.SeekableByteChannel;

import java.io.File;

public class postProcessExecute extends Activity {

    Uri vid1Uri;
    Uri vid2Uri;

    MediaFormat format1;
    MediaFormat format2;

    String ppOrder;
    String ppSize;
    String ppOrientation;

    AndroidSequenceEncoder enc;
    AndroidSequenceEncoder enc2;
    SeekableByteChannel out;
    SeekableByteChannel out2;
    String eMagTime;

    File directory;

    int frameRate1;
    int frameRate2;

    int rotateDegreesPostProcess;
    int rotateDegreesPostProcess2;

    int h1;
    int h2;
    int w1;
    int w2;

    int h;
    int w;

    boolean secondVidFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_process_execute);

        Intent intentReceive = getIntent();
        String vidPath = intentReceive.getExtras().getString("videoPath1");
        vid1Uri = Uri.parse(vidPath);
        vidPath = intentReceive.getExtras().getString("videoPath2");
        vid2Uri = Uri.parse(vidPath);
        h1 = intentReceive.getExtras().getInt("h1");
        h2 = intentReceive.getExtras().getInt("h2");
        w1 = intentReceive.getExtras().getInt("w1");
        w2 = intentReceive.getExtras().getInt("w2");

        directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        ppOrder = sharedPref.getString("pref_postProcessingPlayOrder","s");
        ppSize = sharedPref.getString("pref_postProcessingSize","small");
        ppOrientation = sharedPref.getString("pref_postProcessingOrientation","lr");
        String rotDeg = sharedPref.getString("pref_rotateDegreesPostProcess","0");
        rotateDegreesPostProcess = Integer.valueOf(rotDeg);
        rotDeg = sharedPref.getString("pref_rotateDegreesPostProcess2","0");
        rotateDegreesPostProcess2 = Integer.valueOf(rotDeg);
        final CombineVideos cv = new CombineVideos();
        cv.videoAbsolutePath1 = FileUtils.getPath(getApplicationContext(),vid1Uri);
        cv.videoAbsolutePath2 = FileUtils.getPath(getApplicationContext(),vid2Uri);
        cv.ppOrder = ppOrder;
        cv.ppSize = ppSize;
        cv.ppOrientation = ppOrientation;
        cv.postRotate1 = rotateDegreesPostProcess;
        cv.postRotate2 = rotateDegreesPostProcess2;

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                //TODO your background code
                cv.combineVideos();
            }
        });
        Toast.makeText(getApplicationContext(),"Video will build in the background; this may take a while", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);




    }









    @Override
    public void onResume() {
        super.onResume();

    }

}

