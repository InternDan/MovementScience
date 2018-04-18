package com.home.buffa.movementscience;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
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
import org.jcodec.scale.BitmapUtil;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.Buffer;
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


    }


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @SuppressLint("LongLogTag")
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    new postProcessExecute.beginCombiningProcedure().execute(null, null, null);//
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

    public static Bitmap checkBitmapDimensions(Bitmap bmp){
        Bitmap bmp2 = null;
        if ( (bmp.getHeight() & 1) == 1 ){
            bmp2 =  Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight()-1);//even
        }
        if ( (bmp.getWidth() & 1) == 1 ){
            bmp2 =  Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth()-1, bmp.getHeight());//even
        }
        if (bmp2 == null){
            return bmp;
        }else if (bmp2 != null){
            return bmp2;
        }
        bmp2.recycle();
        return bmp;
    }

    private ArrayList<Bitmap> resizeBitmap(Bitmap bmp1, Bitmap bmp2){
        //resize bitmaps
        if (ppSize.contains("s")) {
            if (h1 == h2) {
                h1 = h1;
                h2 = h2;
                w1 = w1;
                w2 = w2;
            } else if (h1 > h2) {
                w1 = (int) Math.round((double)w1 * ((double)h2 / (double)h1));
                bmp1 = Bitmap.createScaledBitmap(bmp1, w1, h1, false);
                h2 = h2;
                w2 = w2;
            } else if (h2 > h1) {
                w2 = (int) Math.round((double) w2 * ((double)h1 / (double) h2));
                bmp2 = Bitmap.createScaledBitmap(bmp2, w2, h1, false);
                h2 = h2;
                w1 = w1;
            }
            //now scale width
        } else if (ppSize.contains("l")) {
            if (h1 == h2) {
                bmp1 = bmp1;
                bmp2 = bmp2;
                h1 = bmp1.getHeight();
                h2 = bmp2.getHeight();
                w1 = bmp1.getWidth();
                w2 = bmp2.getWidth();
            } else if (h1 > h2) {
                w2 = (int) Math.round((double)postProcessing.vid2Width * ((double)postProcessing.vid1Height / (double)postProcessing.vid2Height));
                bmp2 = Bitmap.createScaledBitmap(bmp2, w2, h1, false);
                h2 = bmp2.getHeight();
            } else if (h2 > h1) {
                w1 = (int) Math.round((double)postProcessing.vid1Width * ((double)postProcessing.vid2Height / (double)postProcessing.vid1Height));
                w2 = bmp2.getWidth();
                bmp1 = Bitmap.createScaledBitmap(bmp1, w1, h1, false);
            }
        }
        ArrayList<Bitmap> bMaps = new ArrayList<Bitmap>();
        bMaps.add(bmp1);
        bMaps.add(bmp2);

        return bMaps;
    }


    public static Bitmap combineImagesLR(Bitmap c, Bitmap s) { // can add a 3rd parameter 'String loc' if you want to save the new image - left some code to do that at the bottom
        Bitmap cs = null;

        int height = 0;
        int width = 0;

        int w1 = c.getWidth();
        int w2 = s.getWidth();

        width = w1 + w2;
        height = c.getHeight();


        Bitmap s2 = s.copy(Bitmap.Config.ARGB_8888, true);
        int[] pixels = new int[s2.getHeight() * s2.getWidth()];
        s2.getPixels(pixels, 0, s2.getWidth(), 0, 0, s2.getWidth(), s2.getHeight());
        int PixelSumS = 0;
        for (int i = 0; i < pixels.length;i++) {
            PixelSumS = PixelSumS + pixels[i];
        }

        Bitmap c2 = c.copy(Bitmap.Config.ARGB_8888, true);
        pixels = new int[c2.getHeight() * c2.getWidth()];
        c2.getPixels(pixels, 0, c2.getWidth(), 0, 0, c2.getWidth(), c2.getHeight());
        int PixelSumC = 0;
        for (int i = 0; i < pixels.length;i++) {
            PixelSumC = PixelSumC + pixels[i];
        }

        if (PixelSumS == 0) {
            s2 = s.copy(Bitmap.Config.ARGB_8888, true);
            pixels = new int[s2.getHeight() * s2.getWidth()];
            s2.getPixels(pixels, 0, s2.getWidth(), 0, 0, s2.getWidth(), s2.getHeight());
            for (int i = 0; i < s2.getHeight() * s2.getWidth(); i++) {
                pixels[i] = Color.BLACK;
            }
            s2.setPixels(pixels, 0, s2.getWidth(), 0, 0, s2.getWidth(), s2.getHeight());
            cs = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas comboImage = new Canvas(cs);
            comboImage.drawBitmap(c, 0f, 0f, null);
            comboImage.drawBitmap(s2, c.getWidth(), 0f, null);
        }else if(PixelSumC == 0){
            c2 = c.copy(Bitmap.Config.ARGB_8888, true);
            pixels = new int[c2.getHeight() * c2.getWidth()];
            c2.getPixels(pixels, 0, c2.getWidth(), 0, 0, c2.getWidth(), c2.getHeight());
            for (int i = 0; i < c2.getHeight() * c2.getWidth(); i++) {
                pixels[i] = Color.BLACK;
            }
            c2.setPixels(pixels, 0, c2.getWidth(), 0, 0, c2.getWidth(), c2.getHeight());
            cs = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas comboImage = new Canvas(cs);
            comboImage.drawBitmap(c2, 0f, 0f, null);
            comboImage.drawBitmap(s, c2.getWidth(), 0f, null);
        }else{
            cs = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas comboImage = new Canvas(cs);
            comboImage.drawBitmap(c, 0f, 0f, null);
            comboImage.drawBitmap(s, c.getWidth(), 0f, null);
        }

        c.recycle();
        s.recycle();
        return cs;
    }

    public static Bitmap combineImagesUD(Bitmap c, Bitmap s) {
        //Bitmap cs = null;

        int width = 0;
        int height = 0;

        Bitmap cs = null;

        if(c.getWidth() > s.getWidth()) {
            int hc = (int) Math.round((double)c.getHeight() * ( (double)s.getWidth() / (double)c.getWidth()) );
            c = Bitmap.createScaledBitmap(c, s.getWidth(), hc, false);
            int dd = c.getHeight();
            int cc = s.getHeight();
            height = dd + cc;
            width = s.getWidth();
        } else {
            int hs = (int) Math.round((double)s.getHeight() * ( (double)c.getWidth() / (double)s.getWidth()) );
            s = Bitmap.createScaledBitmap(s, c.getWidth(), hs, false);
            int dd = c.getHeight();
            int cc = s.getHeight();
            height = dd + cc;
            width = c.getWidth();
        }



        Bitmap s2 = s.copy(Bitmap.Config.ARGB_8888, true);
        int[] pixels = new int[s2.getHeight() * s2.getWidth()];
        s2.getPixels(pixels, 0, s2.getWidth(), 0, 0, s2.getWidth(), s2.getHeight());
        int PixelSumS = 0;
        for (int i = 0; i < pixels.length;i++) {
            PixelSumS = PixelSumS + pixels[i];
        }

        Bitmap c2 = c.copy(Bitmap.Config.ARGB_8888, true);
        pixels = new int[c2.getHeight() * c2.getWidth()];
        c2.getPixels(pixels, 0, c2.getWidth(), 0, 0, c2.getWidth(), c2.getHeight());
        int PixelSumC = 0;
        for (int i = 0; i < pixels.length;i++) {
            PixelSumC = PixelSumC + pixels[i];
        }

        if (PixelSumS == 0) {
            s2 = s.copy(Bitmap.Config.ARGB_8888, true);
            pixels = new int[s2.getHeight() * s2.getWidth()];
            s2.getPixels(pixels, 0, s2.getWidth(), 0, 0, s2.getWidth(), s2.getHeight());
            for (int i = 0; i < s2.getHeight() * s2.getWidth(); i++) {
                pixels[i] = Color.BLACK;
            }
            s2.setPixels(pixels, 0, s2.getWidth(), 0, 0, s2.getWidth(), s2.getHeight());
            cs = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas comboImage = new Canvas(cs);
            comboImage.drawBitmap(c, 0f, 0f, null);
            comboImage.drawBitmap(s2, 0,c.getHeight(), null);
        }else if(PixelSumC == 0){
            c2 = c.copy(Bitmap.Config.ARGB_8888, true);
            pixels = new int[c2.getHeight() * c2.getWidth()];
            c2.getPixels(pixels, 0, c2.getWidth(), 0, 0, c2.getWidth(), c2.getHeight());
            for (int i = 0; i < c2.getHeight() * c2.getWidth(); i++) {
                pixels[i] = Color.BLACK;
            }
            c2.setPixels(pixels, 0, c2.getWidth(), 0, 0, c2.getWidth(), c2.getHeight());
            cs = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas comboImage = new Canvas(cs);
            comboImage.drawBitmap(c2, 0f, 0f, null);
            comboImage.drawBitmap(s, 0, c.getHeight(), null);
        }else{
            cs = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas comboImage = new Canvas(cs);
            comboImage.drawBitmap(c, 0f, 0f, null);
            comboImage.drawBitmap(s, 0, c.getHeight(), null);
        }

        // this is an extra bit I added, just incase you want to save the new image somewhere and then return the location
    /*String tmpImg = String.valueOf(System.currentTimeMillis()) + ".png";

    OutputStream os = null;
    try {
      os = new FileOutputStream(loc + tmpImg);
      cs.compress(CompressFormat.PNG, 100, os);
    } catch(IOException e) {
      Log.e("combineImages", "problem combining images", e);
    }*/
        c.recycle();
        s.recycle();
        pixels = null;
        return cs;
    }

    public static Bitmap resizeForStacked(Bitmap bitmap,int hVid1,int hVid2,int wVid1, int wVid2){
        int useHeight = 0;
        int useWidth = 0;
        double ratio = 0;
        //Determine initial height scaling
        if (hVid1 > hVid2){
            int height = hVid2;
            ratio = (double)height / (double)bitmap.getHeight();
            useHeight = (int)Math.round((double)bitmap.getHeight() * ratio);
            useWidth = (int)Math.round((double)bitmap.getWidth() * ratio);
            bitmap = Bitmap.createScaledBitmap(bitmap,useWidth,useHeight,false);

            int diff1 = wVid1 - bitmap.getWidth();
            int diff2 = wVid2 - bitmap.getWidth();
            int diff = 0;
            if (diff1 > 0) {
                diff = diff1;
            }else if (diff2 > 0) {
                diff = diff2;
            }
            if (diff != 0) {
                int padLeft = (int) Math.floor((double) diff / 2);
                int padRight = (int) Math.ceil((double) diff / 2);
                Bitmap holder = Bitmap.createBitmap(diff + bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(holder);
                canvas.drawColor(Color.BLACK);
                canvas.drawBitmap(bitmap,padLeft,0,null);
                return holder;
            }

        }else if(hVid2 > hVid1){
            int height = hVid1;
            ratio = (double)height/ (double)bitmap.getHeight();
            useHeight = (int)Math.round((double)bitmap.getHeight() * ratio);
            useWidth = (int)Math.round((double)bitmap.getWidth() * ratio);
            bitmap = Bitmap.createScaledBitmap(bitmap,useWidth,useHeight,false);

            int diff1 = wVid1 - bitmap.getWidth();
            int diff2 = wVid2 - bitmap.getWidth();
            int diff = 0;
            if (diff1 > 0) {
                diff = diff1;
            }else if (diff2 > 0) {
                diff = diff2;
            }
            if (diff != 0) {
                int padLeft = (int) Math.floor((double) diff / 2);
                int padRight = (int) Math.ceil((double) diff / 2);
                Bitmap holder = Bitmap.createBitmap(diff + bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(holder);
                canvas.drawColor(Color.BLACK);
                canvas.drawBitmap(bitmap,padLeft,0,null);
                return holder;
            }
        }else{
            int height = hVid1;
            ratio = (double)height / (double)bitmap.getHeight();
            useHeight = (int)Math.round((double)bitmap.getHeight() * ratio);
            useWidth = (int)Math.round((double)bitmap.getWidth() * ratio);
            bitmap = Bitmap.createScaledBitmap(bitmap,useWidth,useHeight,false);
            int diff1 = wVid1 - bitmap.getWidth(); //deal with signs
            int diff2 = wVid2 - bitmap.getWidth();
            int diff = 0;
            if (diff1 > 0) {
                diff = diff1;
            }else if (diff2 > 0) {
                diff = diff2;
            }
            if (diff != 0) {
                int padLeft = (int) Math.floor((double) diff / 2);
                int padRight = (int) Math.ceil((double) diff / 2);
                Bitmap holder = Bitmap.createBitmap(diff + bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(holder);
                canvas.drawColor(Color.BLACK);
                canvas.drawBitmap(bitmap,padLeft,0,null);
                return holder;
            }
        }

        return bitmap;

    }

    public static Bitmap resizeForInstagram(Bitmap bmp){
        //IG recommended size 600x600 max
        int hout;
        int wout;

        double h = bmp.getHeight();
        double w = bmp.getWidth();

        double ratio;
        //check height
        if (h > 600){
            ratio = 600 / h;
            h = h * ratio;
            w = w * ratio;
        }//height scaled, now do width and redo height
        if (w > 600){
            ratio = 600 / w;
            w = w * ratio;
            h = h * ratio;
        }
        hout = (int) Math.round(h);
        wout = (int) Math.round(w);
        if ((hout & 1) == 1 ){
            hout = hout - 1;
        }
        if ((wout & 1) == 1 ){
            wout = wout - 1;
        }

        bmp = Bitmap.createScaledBitmap(bmp, wout, hout, false);

        return bmp;
    }

    public static int selectTrack(MediaExtractor extractor) {
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


    private class beginCombiningProcedure extends AsyncTask<Void, Void, Void> {

        String outPath;
        String outPath2;

        protected Void doInBackground(Void... params) {

            //set up out movie file
            enc = null;
            enc2 = null;
            try {
                DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd'at'HH-mm-ss");
                eMagTime = df2.format(Calendar.getInstance().getTime());
                outPath = directory.getAbsolutePath() + "/LargeComboVid" + eMagTime + ".mp4";
                out = null;
                out = NIOUtils.writableFileChannel(outPath);
                outPath2 = directory.getAbsolutePath() + "/SmallComboVid" + eMagTime + ".mp4";
                out2 = null;
                out2 = NIOUtils.writableFileChannel(outPath2);

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

            Bitmap bmp1=null;
            Bitmap bmp2=null;

            double frames1 = (int) duration1 / frameRate1 / 1000;
            double frames2 = (int) duration2 / frameRate2 / 1000;

            double timePerFrame1 = duration1 / frames1;
            double timePerFrame2 = duration2 / frames2;

            try {
                enc = new AndroidSequenceEncoder(out, Rational.R( (int) Math.round( (frameRate1+frameRate2)/2), 1));
                enc2 = new AndroidSequenceEncoder(out2, Rational.R( (int) Math.round( (frameRate1+frameRate2)/2), 1));
            } catch (IOException e) {
                e.printStackTrace();
            }

            int c = 0;
            for (int i = 0; i < (frames1+frames2); i++) {
                bmp1 = null;
                bmp2 = null;
                if (ppOrder.contains("lr")) {
                    if (i < frames1) {
                        double time1 = i * timePerFrame1;
                        bmp1 = mmr1.getFrameAtTime((int) Math.round(time1), FFmpegMediaMetadataRetriever.OPTION_CLOSEST);
                        c++;
                    }else {
                        bmp1=null;
                    }
                    if (bmp1 != null) {
                        bmp2 = Bitmap.createBitmap(w2,h2, Bitmap.Config.ARGB_8888);;
                    } else if (bmp1 == null) {
                        double time2 = (i - (c-1)) * timePerFrame2;
                        bmp2 = mmr2.getFrameAtTime((int) Math.round(time2), FFmpegMediaMetadataRetriever.OPTION_CLOSEST);
                    }
                }else if (ppOrder.contains("rl")){
                    if (i < frames2) {
                        double time2 = i * timePerFrame2;
                        bmp2 = mmr2.getFrameAtTime((int) Math.round(time2), FFmpegMediaMetadataRetriever.OPTION_CLOSEST);
                        c++;
                    }else{
                        bmp2 = null;
                    }
                    if (bmp2 != null) {
                        bmp1 = Bitmap.createBitmap(w2,h1, Bitmap.Config.ARGB_8888);
                    } else if (bmp2 == null) {
                        double time1 = (i - (c-1)) * timePerFrame1;
                        bmp1 = mmr1.getFrameAtTime((int) Math.round(time1), FFmpegMediaMetadataRetriever.OPTION_CLOSEST);
                    }
                }else {
                    double time1 = i * timePerFrame1;
                    bmp1 = mmr1.getFrameAtTime((int) Math.round(time1), FFmpegMediaMetadataRetriever.OPTION_CLOSEST);
                    double time2 = i * timePerFrame2;
                    bmp2 = mmr2.getFrameAtTime((int) Math.round(time2), FFmpegMediaMetadataRetriever.OPTION_CLOSEST);
                }

                if (ppOrientation.contains("stacked")) {
                    if (i < frames1) {
                        double time1 = i * timePerFrame1;
                        bmp1 = mmr1.getFrameAtTime((int) Math.round(time1), FFmpegMediaMetadataRetriever.OPTION_CLOSEST);
                        c++;
                    }else {
                        bmp1=null;
                    }
                    if (bmp1 != null) {
                        bmp2 = Bitmap.createBitmap(w2,h2, Bitmap.Config.ARGB_8888);
                    } else if (bmp1 == null) {
                        secondVidFlag = true;
                        double time2 = (i - (c-1)) * timePerFrame2;
                        bmp1 = mmr2.getFrameAtTime((int) Math.round(time2), FFmpegMediaMetadataRetriever.OPTION_CLOSEST);
                        bmp2 = Bitmap.createBitmap(w2,h2, Bitmap.Config.ARGB_8888);
                    }
                }

                if (bmp1 == null && bmp2 == null){
                    try {
                        enc.finish();
                        enc2.finish();
                        NIOUtils.closeQuietly(out);
                        NIOUtils.closeQuietly(out2);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;//handlesl both empty
                }else if (bmp1 == null && bmp2 != null){
                    bmp1 = Bitmap.createBitmap(w2,h1, Bitmap.Config.ARGB_8888);//may need fixing
                }else if (bmp2 == null && bmp1 != null){
                    bmp2 = Bitmap.createBitmap(w2,h2, Bitmap.Config.ARGB_8888);
                }

                Matrix matrix = new Matrix();
                if (secondVidFlag == false) {
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
                }else{
                    if (h2 > w2) {
                        matrix.preRotate(rotateDegreesPostProcess + 90);
                    } else {
                        matrix.preRotate(rotateDegreesPostProcess);
                    }
                    bmp1 = Bitmap.createBitmap(bmp1, 0, 0, bmp1.getWidth(), bmp1.getHeight(), matrix, true);
                    bmp1 = Bitmap.createScaledBitmap(bmp1, w2, h2, false);
                    matrix = new Matrix();
                    if (h2 > w2) {
                        matrix.preRotate(rotateDegreesPostProcess2 + 90);
                    } else {
                        matrix.preRotate(rotateDegreesPostProcess2);
                    }
                    bmp2 = Bitmap.createBitmap(bmp2, 0, 0, bmp2.getWidth(), bmp2.getHeight(), matrix, true);
                    bmp2 = Bitmap.createScaledBitmap(bmp2, w1, h1, false);
                }

                ArrayList<Bitmap> bmpList = new ArrayList<Bitmap>();
                bmpList = resizeBitmap(bmp1,bmp2);
                bmp1 = bmpList.get(0);
                bmp2 = bmpList.get(1);

                //put bitmaps together




                if (ppOrientation.contains("lr")) {
                    Bitmap bmpJoined = combineImagesLR(bmp1, bmp2);
                    bmp1.recycle();
                    bmp2.recycle();
                    bmpJoined = checkBitmapDimensions(bmpJoined);
                    try {
                        enc.encodeImage(bmpJoined);
                        enc2.encodeImage(resizeForInstagram(bmpJoined));
                        bmpJoined.recycle();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (ppOrientation.contains("rl")) {
                    Bitmap bmpJoined = combineImagesLR(bmp2, bmp1);
                    bmp1.recycle();
                    bmp2.recycle();
                    bmpJoined = checkBitmapDimensions(bmpJoined);
                    try {
                        enc.encodeImage(bmpJoined);
                        enc2.encodeImage(resizeForInstagram(bmpJoined));
                        bmpJoined.recycle();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (ppOrientation.contains("tb")) {
                    Bitmap bmpJoined = combineImagesUD(bmp1, bmp2);
                    bmp1.recycle();
                    bmp2.recycle();
                    bmpJoined = checkBitmapDimensions(bmpJoined);
                    try {
                        enc.encodeImage(bmpJoined);
                        enc2.encodeImage(resizeForInstagram(bmpJoined));
                        bmpJoined.recycle();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (ppOrientation.contains("bt")) {
                    Bitmap bmpJoined = combineImagesUD(bmp2, bmp1);
                    bmp1.recycle();
                    bmp2.recycle();
                    bmpJoined = checkBitmapDimensions(bmpJoined);
                    try {
                        enc.encodeImage(bmpJoined);
                        enc2.encodeImage(resizeForInstagram(bmpJoined));
                        bmpJoined.recycle();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else if (ppOrientation.contains("stacked")) {
                    try {
                        if (secondVidFlag == false) {
                            bmp1 = resizeForStacked(bmp1, h1, h2, w1, w2);
                        }else{
                            bmp1 = resizeForStacked(bmp1, h2, h1, w2, w1);
                        }
                        bmp1 = checkBitmapDimensions(bmp1);
                        enc.encodeImage(bmp1);
                        enc2.encodeImage(resizeForInstagram(bmp1));
                        bmp1.recycle();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }


            try {
                enc.finish();
                NIOUtils.closeQuietly(out);
                enc2.finish();
                NIOUtils.closeQuietly(out2);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onProgressUpdate(Void... progress) {

        }

        protected void onPostExecute(Void result) {

            Intent intentPass = new Intent(getApplicationContext(), displayTrackingResults.class);

            File file = new File(outPath2);
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

