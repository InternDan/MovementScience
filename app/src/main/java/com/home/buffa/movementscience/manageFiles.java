package com.home.buffa.movementscience;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;


public class manageFiles extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_files);
    }

    public void uploadImage(View view) {
        Intent intent = new Intent(getApplicationContext(), uploadImage.class);
        startActivity(intent);
    }

    public void uploadVideo(View view) {
        Intent intent = new Intent(getApplicationContext(), uploadVideo.class);
        startActivity(intent);
    }





}
