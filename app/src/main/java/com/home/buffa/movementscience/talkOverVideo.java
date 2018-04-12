package com.home.buffa.movementscience;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.ipaulpro.afilechooser.utils.FileUtils;

import java.io.IOException;

public class talkOverVideo extends Activity implements TextureView.SurfaceTextureListener{

    private static final String TAG = talkOverVideo.class.getName();

    private MediaPlayer mMediaPlayer;
    private TextureView mPreview;

    float vH;
    float vW;
    float vHOrig;
    float vWOrig;

    String videoAbsolutePath;

    TextureView textureView;

    FrameLayout frameLayout;
    int bmpCount = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_talk_over_video);
        mPreview = findViewById(R.id.textureView);

        Intent intentReceive = getIntent();
        videoAbsolutePath = intentReceive.getExtras().getString("videoPath");

        initView();
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i2) {
        Surface surface = new Surface(surfaceTexture);

        try {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setDataSource(videoAbsolutePath);
            mMediaPlayer.setSurface(surface);
            mMediaPlayer.setLooping(false);
            mMediaPlayer.prepareAsync();

            // Play video when the media source is ready for playback.
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    vH = mMediaPlayer.getVideoHeight();
                    vW = mMediaPlayer.getVideoWidth();
                    updateTextureViewSize(mPreview.getWidth(),mPreview.getHeight());
                    //textureView.setLayoutParams(new FrameLayout.LayoutParams(vW,vH));
                    mediaPlayer.start();
                }
            });

        } catch (IllegalArgumentException e) {
            Log.d(TAG, e.getMessage());
        } catch (SecurityException e) {
            Log.d(TAG, e.getMessage());
        } catch (IllegalStateException e) {
            Log.d(TAG, e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i2) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        Bitmap bmp = getBitmap();
        bmpCount ++;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    private void initView() {
        textureView = findViewById(R.id.textureView);
        textureView.setSurfaceTextureListener(this);
    }

    public Bitmap getBitmap(){
        return  mPreview.getBitmap();
    }

    private void updateTextureViewSize(int viewWidth, int viewHeight) {
        float scaleX = 1.0f;
        float scaleY = 1.0f;

        vHOrig = vH;
        vWOrig = vW;

        //vH and vW and viewWidth and viewHeight

        if (vH > viewHeight){
            vH = vH * (viewHeight / vH);
            vW = vW * (viewHeight / vH);
            if (vW > viewWidth){
                vW = vW * (viewWidth / vW);
                vH = vH * (viewWidth / vW);
            }
        }else if (vW > viewWidth){
            vW = vW * (viewWidth / vW);
            vH = vH * (viewWidth / vW);
            if (vH > viewHeight){
                vH = vH * (viewHeight / vH);
                vW = vW * (viewHeight / vH);
            }
        }

        scaleX = vW / viewWidth;
        scaleY = vH / viewHeight;


        // Calculate pivot points, in our case crop from center
        int pivotPointX = viewWidth / 2;
        int pivotPointY = viewHeight / 2;

        Matrix matrix = new Matrix();
        matrix.setScale(scaleX, scaleY, pivotPointX, pivotPointY);

        mPreview.setTransform(matrix);
        mPreview.setLayoutParams(new FrameLayout.LayoutParams(viewWidth, viewHeight));
    }

    public void playVideo(){
        if (mMediaPlayer != null){
            mMediaPlayer.start();
        }
    }

    public void stopVideo(){
        if (mMediaPlayer != null){
            mMediaPlayer.stop();
        }
    }






}
