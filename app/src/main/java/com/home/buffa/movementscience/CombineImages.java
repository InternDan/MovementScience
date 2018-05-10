package com.home.buffa.movementscience;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class CombineImages extends Activity {

    static final int READ_REQUEST_CODE_IMAGE1 = 1;
    static final int READ_REQUEST_CODE_IMAGE2 = 2;

    Bitmap bmp1;
    Bitmap bmp2;

    ImageView img1;
    ImageView img2;

    int clickTrack1;
    int clickTrack2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        setContentView(R.layout.activity_combine_images);

        img1 = findViewById(R.id.imageViewImage1);
        img2 = findViewById(R.id.imageViewImage2);

        final View button1 = findViewById(R.id.imageViewImage1);
        button1.setOnClickListener (new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(clickTrack1 < 1) {
                    clickTrack1 = 1;
                    loadImage1();
                }
            }
        });


        final View button2 = findViewById(R.id.imageViewImage2);
        button2.setOnClickListener (new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(clickTrack2 < 1) {
                    clickTrack2 = 1;
                    loadImage2();

                }
            }
        });


    }

    public void loadImage1(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), READ_REQUEST_CODE_IMAGE1);

    }

    public void loadImage2(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), READ_REQUEST_CODE_IMAGE2);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == READ_REQUEST_CODE_IMAGE1) {
            if (resultCode==RESULT_OK){
                Uri uri = data.getData();
                try {
                    bmp1 = MediaStore.Images.Media.getBitmap(this.getContentResolver(),uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                img1.setImageBitmap(bmp1);
            }else{
                Toast.makeText(this, "Image is not available", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        if (requestCode == READ_REQUEST_CODE_IMAGE2) {
            if (resultCode==RESULT_OK){
                Uri uri = data.getData();
                try {
                    bmp2 = MediaStore.Images.Media.getBitmap(this.getContentResolver(),uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                img2.setImageBitmap(bmp2);
            }else{
                Toast.makeText(this, "Image is not available", Toast.LENGTH_SHORT).show();
                return;
            }
        }
    }



}
