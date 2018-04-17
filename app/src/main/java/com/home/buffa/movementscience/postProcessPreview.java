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
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
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

    Bitmap bmpJoined;

    int h1;
    int h2;
    int w1;
    int w2;

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

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        ppOrder = sharedPref.getString("pref_postProcessingPlayOrder","s");
        ppSize = sharedPref.getString("pref_postProcessingSize","small");
        ppOrientation = sharedPref.getString("pref_postProcessingOrientation","lr");
        String rotDeg = sharedPref.getString("pref_rotateDegreesPostProcess","90");
        rotateDegreesPostProcess = Integer.valueOf(rotDeg);
        rotDeg = sharedPref.getString("pref_rotateDegreesPostProcess2","90");
        rotateDegreesPostProcess2 = Integer.valueOf(rotDeg);
    }

    private class beginCombiningProcedure extends AsyncTask<Void, Void, Void> {

        protected Void doInBackground(Void... params) {

            FFmpegMediaMetadataRetriever mmr1 = new FFmpegMediaMetadataRetriever();
            mmr1.setDataSource(FileUtils.getPath(getApplicationContext(), videoUri1));
            FFmpegMediaMetadataRetriever mmr2 = new FFmpegMediaMetadataRetriever();
            mmr2.setDataSource(FileUtils.getPath(getApplicationContext(), videoUri2));

            MediaExtractor extractor1 = new MediaExtractor();
            File inputFile1 = new File(FileUtils.getPath(getApplicationContext(), videoUri1));
            try {
                extractor1.setDataSource(inputFile1.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            int trackIndex1 = postProcessExecute.selectTrack(extractor1);
            extractor1.selectTrack(trackIndex1);
            format1 = extractor1.getTrackFormat(trackIndex1);
            long duration1 = format1.getLong("durationUs");

            MediaExtractor extractor2 = new MediaExtractor();
            File inputFile2 = new File(FileUtils.getPath(getApplicationContext(), videoUri2));
            try {
                extractor2.setDataSource(inputFile2.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            int trackIndex2 = postProcessExecute.selectTrack(extractor2);
            extractor2.selectTrack(trackIndex2);
            format2 = extractor2.getTrackFormat(trackIndex2);
            long duration2 = format2.getLong("durationUs");

            Bitmap bmp1 = null;
            Bitmap bmp2 = null;

            int time = 0;

            bmp1 = mmr1.getFrameAtTime(time, FFmpegMediaMetadataRetriever.OPTION_CLOSEST);
            bmp2 = mmr2.getFrameAtTime(time, FFmpegMediaMetadataRetriever.OPTION_CLOSEST);

            if (ppSize.contains("s")) {
                if (postProcessing.vid1Height == postProcessing.vid2Height) {
                    bmp1 = bmp1;
                    bmp2 = bmp2;
                    h1 = postProcessing.vid1Height;
                    h2 = postProcessing.vid2Height;
                    w1 = postProcessing.vid1Width;
                    w2 = postProcessing.vid2Width;
                } else if (postProcessing.vid1Height > postProcessing.vid2Height) {
                    h1 = postProcessing.vid2Height;
                    w1 = (int) Math.round((double)postProcessing.vid1Width * ((double)postProcessing.vid2Height / (double)postProcessing.vid1Height));
                    bmp1 = Bitmap.createScaledBitmap(bmp1, w1, h1, false);
                    h2 = bmp2.getHeight();
                    w2 = bmp2.getWidth();
                } else if (postProcessing.vid2Height > postProcessing.vid1Height) {
                    h1 = postProcessing.vid1Height;
                    w2 = (int) Math.round((double) postProcessing.vid2Width * (double) ((double)postProcessing.vid1Height / (double) postProcessing.vid2Height));
                    bmp2 = Bitmap.createScaledBitmap(bmp2, w2, h1, false);
                    h2 = bmp2.getHeight();
                    w1 = bmp1.getWidth();
                }
            } else if (ppSize.contains("l")) {
                if (postProcessing.vid1Height == postProcessing.vid2Height) {
                    bmp1 = bmp1;
                    bmp2 = bmp2;
                    h1 = bmp1.getHeight();
                    h2 = bmp2.getHeight();
                    w1 = bmp1.getWidth();
                    w2 = bmp2.getWidth();
                } else if (postProcessing.vid1Height > postProcessing.vid2Height) {
                    h1 = postProcessing.vid1Height;
                    w1 = postProcessing.vid1Width;
                    w2 = (int) Math.round((double)postProcessing.vid2Width * ((double)postProcessing.vid1Height / (double)postProcessing.vid2Height));
                    bmp2 = Bitmap.createScaledBitmap(bmp2, w2, h1, false);
                    h2 = bmp2.getHeight();
                } else if (postProcessing.vid2Height > postProcessing.vid1Height) {
                    h2 = postProcessing.vid2Height;
                    h1 = postProcessing.vid2Height;
                    w1 = (int) Math.round((double)postProcessing.vid1Width * ((double)postProcessing.vid2Height / (double)postProcessing.vid1Height));
                    w2 = bmp2.getWidth();
                    bmp1 = Bitmap.createScaledBitmap(bmp1, w1, h1, false);
                }
            }

            Matrix matrix = new Matrix();
            if (h1 > w1) {
                matrix.preRotate(rotateDegreesPostProcess + 90);
            } else {
                matrix.preRotate(rotateDegreesPostProcess);
            }
            bmp1 = Bitmap.createBitmap(bmp1, 0, 0, bmp1.getWidth(), bmp1.getHeight(), matrix, true);
            bmp1 = Bitmap.createScaledBitmap(bmp1, w1, h1, false);
            matrix = new Matrix();
            if (h2 > w2) {
                matrix.preRotate(rotateDegreesPostProcess2 + 90);
            } else {
                matrix.preRotate(rotateDegreesPostProcess2);
            }
            bmp2 = Bitmap.createBitmap(bmp2, 0, 0, bmp2.getWidth(), bmp2.getHeight(), matrix, true);
            bmp2 = Bitmap.createScaledBitmap(bmp2, w2, h2, false);

            if (ppOrientation.contains("lr")) {
                bmpJoined = postProcessExecute.combineImagesLR(bmp1, bmp2);
                bmpJoined = postProcessExecute.checkBitmapDimensions(bmpJoined);
            } else if (ppOrientation.contains("rl")) {
                bmpJoined = postProcessExecute.combineImagesLR(bmp2, bmp1);
                bmpJoined = postProcessExecute.checkBitmapDimensions(bmpJoined);
            } else if (ppOrientation.contains("tb")) {
                bmpJoined = postProcessExecute.combineImagesUD(bmp1, bmp2);
                bmpJoined = postProcessExecute.checkBitmapDimensions(bmpJoined);
            } else if (ppOrientation.contains("bt")) {
                bmpJoined = postProcessExecute.combineImagesUD(bmp2, bmp1);
                bmpJoined = postProcessExecute.checkBitmapDimensions(bmpJoined);
            } else if (ppOrientation.contains("stacked")) {
                bmpJoined = bmp1.copy(Bitmap.Config.ARGB_8888,false);
            }
            bmp1.recycle();
            bmp2.recycle();
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
        bmpJoined.recycle();
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
