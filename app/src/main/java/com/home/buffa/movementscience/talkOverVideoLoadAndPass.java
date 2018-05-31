package com.home.buffa.movementscience;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.ipaulpro.afilechooser.utils.FileUtils;

import java.io.IOException;

public class talkOverVideoLoadAndPass extends Activity {

    int READ_REQUEST_CODE_VIDEO = 1;
    String videoAbsolutePath;
    Uri inputFile;
    Intent intentPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_talk_over_video_load_and_pass);

        Intent intentGetVideo = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intentGetVideo.addCategory(Intent.CATEGORY_OPENABLE);
        intentGetVideo.setType("video/*");
        READ_REQUEST_CODE_VIDEO = 1;
        intentPass = new Intent(this,talkOverVideo.class);
        startActivityForResult(intentGetVideo, READ_REQUEST_CODE_VIDEO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == READ_REQUEST_CODE_VIDEO && resultCode == RESULT_OK) {
            inputFile = data.getData();
            videoAbsolutePath = FileUtils.getPath(getApplicationContext(), inputFile);
            if (videoAbsolutePath == null) {
                Context context = getApplicationContext();
                CharSequence text = "Unable to load from that location; ensure file is a video and stored locally on device.";
                int duration = Toast.LENGTH_LONG;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                goHome();
            }else {
                intentPass.putExtra("videoPath",videoAbsolutePath);
                startActivity(intentPass);
            }

            //result_video.requestFocus();
        }
    }

    public void goHome(){
        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
        startActivity(intent);
    }
}
