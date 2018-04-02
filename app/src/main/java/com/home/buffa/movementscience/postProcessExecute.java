package com.home.buffa.movementscience;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.ipaulpro.afilechooser.utils.FileUtils;

import org.jcodec.api.android.AndroidSequenceEncoder;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.io.SeekableByteChannel;
import org.jcodec.common.model.Rational;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import wseemann.media.FFmpegMediaMetadataRetriever;

import static android.content.ContentValues.TAG;
import static junit.framework.Assert.fail;

public class postProcessExecute extends Activity {

    Uri vid1Uri;
    Uri vid2Uri;

    MediaFormat format1;
    MediaFormat format2;

    String ppOrder;
    String ppSize;
    String ppOrientation;

    AndroidSequenceEncoder enc;
    SeekableByteChannel out;
    String eMagTime;

    File directory;

    int frameRate1;
    int frameRate2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poat_process_execute);

        Intent intentReceive = getIntent();
        String vidPath = intentReceive.getExtras().getString("videoPath1");
        vid1Uri = Uri.parse(vidPath);
        vidPath = intentReceive.getExtras().getString("videoPath2");
        vid2Uri = Uri.parse(vidPath);



        directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        ppOrder = sharedPref.getString("pref_postProcessingPlayOrder","s");
        ppSize = sharedPref.getString("pref_postProcessingSize","small");
        ppOrientation = sharedPref.getString("pref_postProcessingOrientation","lr");


    }


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @SuppressLint("LongLogTag")
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    new postProcessExecute.beginTrackingProcedure().execute(null, null, null);//
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    Toast.makeText(getApplicationContext(),"Application will load video once merged", Toast.LENGTH_LONG).show();
                    startActivity(intent);

                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };


    public Bitmap combineImagesLR(Bitmap c, Bitmap s) { // can add a 3rd parameter 'String loc' if you want to save the new image - left some code to do that at the bottom
        //Bitmap cs = null;

        int width, height = 0;

        if(c.getWidth() > s.getWidth()) {
            width = c.getWidth() + s.getWidth();
            height = c.getHeight();
        } else {
            width = s.getWidth() + s.getWidth();
            height = c.getHeight();
        }

        Bitmap cs = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas comboImage = new Canvas(cs);

        comboImage.drawBitmap(c, 0f, 0f, null);
        comboImage.drawBitmap(s, c.getWidth(), 0f, null);

        // this is an extra bit I added, just incase you want to save the new image somewhere and then return the location
    /*String tmpImg = String.valueOf(System.currentTimeMillis()) + ".png";

    OutputStream os = null;
    try {
      os = new FileOutputStream(loc + tmpImg);
      cs.compress(CompressFormat.PNG, 100, os);
    } catch(IOException e) {
      Log.e("combineImages", "problem combining images", e);
    }*/

        return cs;
    }

    public Bitmap combineImagesUD(Bitmap c, Bitmap s) { // can add a 3rd parameter 'String loc' if you want to save the new image - left some code to do that at the bottom
        //Bitmap cs = null;

        int width, height = 0;

        if(c.getHeight() > s.getHeight()) {
            height = c.getHeight() + s.getHeight();
            width = c.getWidth();
        } else {
            height = s.getHeight() + s.getHeight();
            width = c.getWidth();
        }

        Bitmap cs = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas comboImage = new Canvas(cs);

        comboImage.drawBitmap(c, 0f, 0f, null);
        comboImage.drawBitmap(s, 0f, c.getHeight(), null);

        // this is an extra bit I added, just incase you want to save the new image somewhere and then return the location
    /*String tmpImg = String.valueOf(System.currentTimeMillis()) + ".png";

    OutputStream os = null;
    try {
      os = new FileOutputStream(loc + tmpImg);
      cs.compress(CompressFormat.PNG, 100, os);
    } catch(IOException e) {
      Log.e("combineImages", "problem combining images", e);
    }*/

        return cs;
    }

    public int selectTrack(MediaExtractor extractor) {
        // Select the first video track we find, ignore the rest.
        int numTracks = extractor.getTrackCount();
        for (int i = 0; i < numTracks; i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("video/")) {
                    Log.d(TAG, "Extractor selected track " + i + " (" + mime + "): " + format);
                return i;
            }
        }

        return -1;
    }


    private class beginTrackingProcedure extends AsyncTask<Void, Void, Void> {

        String outPath;

        protected Void doInBackground(Void... params) {

            //set up out movie file
            enc = null;
            try {
                DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd'at'HH-mm-ss");
                eMagTime = df2.format(Calendar.getInstance().getTime());
                outPath = directory.getAbsolutePath() + "/" + eMagTime + "-combinedVid.mp4";
                out = null;
                out = NIOUtils.writableFileChannel(outPath);
                enc = new AndroidSequenceEncoder(out, Rational.R(25, 1));
            } catch (IOException e) {
                e.printStackTrace();
            }
            //Configure for 2 movies

            FFmpegMediaMetadataRetriever mmr1 = new FFmpegMediaMetadataRetriever();
            mmr1.setDataSource(FileUtils.getPath(getApplicationContext(),vid1Uri));
            FFmpegMediaMetadataRetriever mmr2 = new FFmpegMediaMetadataRetriever();
            mmr2.setDataSource(FileUtils.getPath(getApplicationContext(),vid2Uri));

            MediaExtractor extractor1 = new MediaExtractor();
            File inputFile1 = new File(FileUtils.getPath(getApplicationContext(), vid1Uri));
            try {
                extractor1.setDataSource(inputFile1.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            int trackIndex1 = selectTrack(extractor1);
            extractor1.selectTrack(trackIndex1);
            format1 = extractor1.getTrackFormat(trackIndex1);
            frameRate1 = format1.getInteger("frame-rate");
            long duration1 = format1.getLong("durationUs");

            MediaExtractor extractor2 = new MediaExtractor();
            File inputFile2 = new File(FileUtils.getPath(getApplicationContext(), vid2Uri));
            try {
                extractor2.setDataSource(inputFile2.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            int trackIndex2 = selectTrack(extractor2);
            extractor2.selectTrack(trackIndex2);
            format2 = extractor2.getTrackFormat(trackIndex2);
            frameRate2 = format2.getInteger("frame-rate");
            long duration2 = format2.getLong("durationUs");

            Bitmap bmp1 = null;
            Bitmap bmp2 = null;

            double frames1 = (int) duration1 / frameRate1 / 1000;
            double frames2 = (int) duration2 / frameRate2 / 1000;

            double timePerFrame1 = duration1 / frames1;
            double timePerFrame2 = duration2 / frames2;


            for (int i = 0; i < frames1; i++) {
                double time1 = i * timePerFrame1;
                bmp1 = mmr1.getFrameAtTime((int) Math.round(time1), FFmpegMediaMetadataRetriever.OPTION_CLOSEST);
                double time2 = i * timePerFrame2;
                bmp2 = mmr2.getFrameAtTime((int) Math.round(time2), FFmpegMediaMetadataRetriever.OPTION_CLOSEST);
                if (bmp1 == null && bmp2 == null){
                    try {
                        enc.finish();
                        NIOUtils.closeQuietly(out);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;//handlesl both empty
                }else if (bmp1 == null && bmp2 != null){
                    bmp1 = Bitmap.createBitmap(format1.getInteger(MediaFormat.KEY_WIDTH),format1.getInteger(MediaFormat.KEY_HEIGHT), Bitmap.Config.ARGB_8888);
                }else if (bmp2 == null && bmp1 != null){
                    bmp2 = Bitmap.createBitmap(format2.getInteger(MediaFormat.KEY_WIDTH),format2.getInteger(MediaFormat.KEY_HEIGHT), Bitmap.Config.ARGB_8888);
                }
                //resize bitmaps
                if (ppSize.contains("s")) {
                    if (format1.getInteger(MediaFormat.KEY_HEIGHT) == format2.getInteger(MediaFormat.KEY_HEIGHT)) {
                        bmp1 = bmp1;
                        bmp2 = bmp2;
                    } else if (format1.getInteger(MediaFormat.KEY_HEIGHT) > format2.getInteger(MediaFormat.KEY_HEIGHT)) {
                        int h1 = format2.getInteger(MediaFormat.KEY_HEIGHT);
                        int w1 = (int) Math.round(format1.getInteger(MediaFormat.KEY_WIDTH) * (format2.getInteger(MediaFormat.KEY_HEIGHT) / format1.getInteger(MediaFormat.KEY_HEIGHT)));
                        bmp1 = Bitmap.createScaledBitmap(bmp1, w1, h1, false);
                    } else if (format2.getInteger(MediaFormat.KEY_HEIGHT) > format1.getInteger(MediaFormat.KEY_HEIGHT)) {
                        int h1 = format1.getInteger(MediaFormat.KEY_HEIGHT);
                        int w1 = (int) Math.round(format2.getInteger(MediaFormat.KEY_WIDTH) * (format1.getInteger(MediaFormat.KEY_HEIGHT) / format2.getInteger(MediaFormat.KEY_HEIGHT)));
                        bmp2 = Bitmap.createScaledBitmap(bmp1, w1, h1, false);
                    }
                } else if (ppSize.contains("l")) {
                    if (format1.getInteger(MediaFormat.KEY_HEIGHT) == format2.getInteger(MediaFormat.KEY_HEIGHT)) {
                        bmp1 = bmp1;
                        bmp2 = bmp2;
                    } else if (format1.getInteger(MediaFormat.KEY_HEIGHT) > format2.getInteger(MediaFormat.KEY_HEIGHT)) {
                        int h2 = format1.getInteger(MediaFormat.KEY_HEIGHT);
                        int w2 = (int) Math.round(format2.getInteger(MediaFormat.KEY_WIDTH) * (format1.getInteger(MediaFormat.KEY_HEIGHT) / format2.getInteger(MediaFormat.KEY_HEIGHT)));
                        bmp2 = Bitmap.createScaledBitmap(bmp2, w2, h2, false);
                    } else if (format2.getInteger(MediaFormat.KEY_HEIGHT) > format1.getInteger(MediaFormat.KEY_HEIGHT)) {
                        int h1 = format2.getInteger(MediaFormat.KEY_HEIGHT);
                        int w1 = (int) Math.round(format1.getInteger(MediaFormat.KEY_WIDTH) * (format2.getInteger(MediaFormat.KEY_HEIGHT) / format1.getInteger(MediaFormat.KEY_HEIGHT)));
                        bmp1 = Bitmap.createScaledBitmap(bmp1, w1, h1, false);
                    }
                }

                //put bitmaps together

                if (ppOrientation.contains("lr")) {
                    Bitmap bmpJoined = combineImagesLR(bmp1, bmp2);
                    try {
                        enc.encodeImage(bmpJoined);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (ppOrientation.contains("rl")) {
                    Bitmap bmpJoined = combineImagesLR(bmp2, bmp1);
                    try {
                        enc.encodeImage(bmpJoined);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (ppOrientation.contains("tb")) {
                    Bitmap bmpJoined = combineImagesUD(bmp1, bmp2);
                    try {
                        enc.encodeImage(bmpJoined);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (ppOrientation.contains("bt")) {
                    Bitmap bmpJoined = combineImagesUD(bmp2, bmp1);
                    try {
                        enc.encodeImage(bmpJoined);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }



        return null;
        }

        protected void onProgressUpdate(Void... progress) {

        }

        protected void onPostExecute(Void result) {

            Intent intentPass = new Intent(getApplicationContext(), displayTrackingResults.class);

            File file = new File(outPath);
            intentPass.putExtra("videoPath", file.toString());
            startActivity(intentPass);
        }


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

