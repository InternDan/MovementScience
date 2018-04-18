package com.home.buffa.movementscience;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.ipaulpro.afilechooser.utils.FileUtils;

import java.io.IOException;

import wseemann.media.FFmpegMediaMetadataRetriever;

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
    ImageView imageView;

    FrameLayout frameLayout;
    int bmpCount = 0;

    SeekBar seekbar;

    double frameRate;
    int currentFrame = 0;
    double duration;
    double mspf;

    int h1;
    int w1;

    int rotateDegreesPostProcess;

    FFmpegMediaMetadataRetriever mmr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_talk_over_video);
        mPreview = findViewById(R.id.textureView);
        seekbar = findViewById(R.id.seekbar);
        imageView = findViewById(R.id.imageView);
        imageView.setVisibility(ImageView.INVISIBLE);

        //need intent with video path
        Intent intentReceive = getIntent();
        videoAbsolutePath = intentReceive.getExtras().getString("videoPath");

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String rotDeg = sharedPref.getString("pref_rotateDegreesPostProcess","0");
        rotateDegreesPostProcess = Integer.valueOf(rotDeg);

        //start mmr for video
        mmr = new FFmpegMediaMetadataRetriever();
        mmr.setDataSource(videoAbsolutePath);
        //determine number of frames
        String fr = mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_FRAMERATE);
        frameRate = Double.valueOf(fr);
        mspf = 1/(frameRate / 1000);//frames per microsecond
        String dur = mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION);//in ms
        duration = Double.valueOf(dur);//in ms
        int frames = (int) Math.round( (duration/mspf));

        //set max frames for seekbar
        seekbar.setMax(frames);
        seekbar.setProgress(0);

        final Canvas[] canvas = new Canvas[1];
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //grab frames based on integer in progressChangedValue
                progressChangedValue = progress;
                int pos = (int) Math.round( ((double)progressChangedValue * mspf));
                //mMediaPlayer.seekTo(pos);//wants input in ms
                Bitmap bmp = mmr.getFrameAtTime(pos*1000,FFmpegMediaMetadataRetriever.OPTION_CLOSEST);
                if (bmp != null) {
                    Matrix matrix = new Matrix();
                    matrix.preRotate(rotateDegreesPostProcess);
                    bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
                    //bmp = Bitmap.createScaledBitmap(bmp, w1, h1, false);
                    imageView.setImageBitmap(bmp);
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                String dur = mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION);//in ms
                duration = Double.valueOf(dur);//in ms
                int frames = (int) Math.round( (duration/mspf));
                //set max frames for seekbar
                seekbar.setMax(frames);
                textureView.setVisibility(TextureView.INVISIBLE);
                imageView.setVisibility(ImageView.VISIBLE);
                //set image view as visible
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                //possibly set some values for things, likely nothing
                currentFrame = progressChangedValue;
            }
        });



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
                    mMediaPlayer.seekTo(200);
                    h1 = mPreview.getHeight();
                    w1 = mPreview.getWidth();
                    duration = mMediaPlayer.getDuration();
                    int frames = (int) Math.round( (duration/mspf));
                    //set max frames for seekbar
                    seekbar.setMax(frames);
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
        imageView.setMaxHeight(viewHeight);
        imageView.setMaxWidth(viewWidth);
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
