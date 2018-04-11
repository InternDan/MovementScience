package com.home.buffa.movementscience;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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

    int vH;
    int vW;

    String videoAbsolutePath;

    TextureView textureView;

    FrameLayout frameLayout;

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
            vH = mMediaPlayer.getVideoHeight();
            vW = mMediaPlayer.getVideoWidth();
            //textureView.setLayoutParams(new FrameLayout.LayoutParams(vW,vH));
            mMediaPlayer.prepareAsync();

            // Play video when the media source is ready for playback.
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
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
        int tt = 1;
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








}
