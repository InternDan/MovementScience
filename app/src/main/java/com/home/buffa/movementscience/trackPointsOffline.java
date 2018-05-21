package com.home.buffa.movementscience;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import org.jcodec.api.android.AndroidSequenceEncoder;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.io.SeekableByteChannel;
import org.jcodec.common.model.Rational;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.Video;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import static com.home.buffa.movementscience.MainActivity.notificationID;
import static junit.framework.Assert.fail;
import static org.opencv.core.TermCriteria.COUNT;

public class trackPointsOffline extends Activity {

    private static final String TAG = "trackPointsOffline";
    private static final boolean VERBOSE = false;
    String coords;
    String coordType;
    public File directory;

    ArrayList<Point> points = new ArrayList<Point>();
    ArrayList<Integer> pointTypes = new ArrayList<>();
    ArrayList<ArrayList> allPoints = new ArrayList();
    ArrayList<ArrayList> allPointTypes = new ArrayList();
    ArrayList<Point> trailPoints = new ArrayList();
    ArrayList<Point> trailPointsCollect = new ArrayList();
    ArrayList<Double> angles = new ArrayList<Double>();

    Mat mGray;
    Mat mPrevGray;

    MatOfPoint2f prevFeatures = null;
    MatOfPoint2f nextFeatures = null;
    MatOfPoint features = null;

    MatOfByte status;
    MatOfFloat err;

    String videoPath;
    String videoAbsolutePath;

    int c = 0;
    int numPoints = 0;
    int frameHeight = 0;
    int frameWidth = 0;
    long duration = 0;
    int frameRate = 0;
    int numRegPoints = 0;

    int currentPointSize;
    int trailPointSize;
    int numTrailPoints;
    int rotateDegreesPostProcess;
    int maxLevel;
    int winSearchSize;
    double epsilon;
    double eigenThreshold;
    Scalar currentPointColor;
    Scalar trailPointColor;

    Scalar anglePointColor;
    Scalar angleLineColor;
    Scalar angleTextColor;
    String gText;
    int angleTextSize;

    AndroidSequenceEncoder enc;
    AndroidSequenceEncoder enc2;
    SeekableByteChannel out;
    SeekableByteChannel out2;

    double seconds;
    int frames;

    String eMagTime;//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_real_time_tracking);
        View decorView = getWindow().getDecorView();
        // Hide both the navigation bar and the status bar.
        // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
        // a general rule, you should design your app to hide the status bar whenever you
        // hide the navigation bar.
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        //need to get intent with picked file path
        Intent intentReceive = getIntent();
        videoPath = intentReceive.getExtras().getString("videoPath");
        videoAbsolutePath = intentReceive.getExtras().getString("videoAbsolutePath");
        coords = intentReceive.getExtras().getString("coordinates");
        coords = coords.substring(5);
        coordType = intentReceive.getExtras().getString("coordinateTypes");
        coordType = coordType.substring(5);
        frameHeight = intentReceive.getExtras().getInt("frameHeight");
        frameWidth = intentReceive.getExtras().getInt("frameWidth");
        directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        String numTrPts = sharedPref.getString("pref_trailingPointNumber","20");
        numTrailPoints = Integer.valueOf(numTrPts);

        String trPtSize = sharedPref.getString("pref_trailingPointSize","5");
        trailPointSize = Integer.valueOf(trPtSize);

        String curPtSize = sharedPref.getString("pref_currentPointSize","5");
        currentPointSize = Integer.valueOf(curPtSize);

        String txtWinSearchSize = sharedPref.getString("pref_trackingSearchWindowSize","20");
        winSearchSize = Integer.valueOf(txtWinSearchSize);

        String txtSearchEpsilon = sharedPref.getString("pref_trackingSearchEpsilon","0.1");
        epsilon = Double.valueOf(txtSearchEpsilon);

        String txtMaxLevel = sharedPref.getString("pref_trackingSearchMaxLevel","3");
        maxLevel = Integer.valueOf(txtMaxLevel);

        String txtEigenThreshold = sharedPref.getString("pref_trackingSearchMinEigenThreshold","0.001");
        eigenThreshold = Double.valueOf(txtEigenThreshold);

        String curPtClr = sharedPref.getString("pref_currentPointColor","r");
        if (Objects.equals(curPtClr,new String("r")) == true){
            currentPointColor = new Scalar(255,0,0);
        }else if(Objects.equals(curPtClr,new String("g")) == true){
            currentPointColor = new Scalar(0,255,0);
        }else if(Objects.equals(curPtClr,new String("b")) == true) {
            currentPointColor = new Scalar(0, 0, 255);
        }else if(Objects.equals(curPtClr,new String("y")) == true) {
            currentPointColor = new Scalar(0, 255, 255);
        }else if(Objects.equals(curPtClr,new String("c")) == true) {
            currentPointColor = new Scalar(255, 255, 0);
        }else if(Objects.equals(curPtClr,new String("k")) == true) {
            currentPointColor = new Scalar(0, 0, 0);
        }else if(Objects.equals(curPtClr,new String("w")) == true) {
            currentPointColor = new Scalar(255, 255, 255);
        }

        String trlPtClr = sharedPref.getString("pref_trailPointColor","r");
        if (Objects.equals(trlPtClr,new String("r")) == true){
            trailPointColor = new Scalar(255,0,0);
        }else if(Objects.equals(trlPtClr,new String("g")) == true){
            trailPointColor = new Scalar(0,255,0);
        }else if(Objects.equals(trlPtClr,new String("b")) == true) {
            trailPointColor = new Scalar(0, 0, 255);
        }else if(Objects.equals(trlPtClr,new String("y")) == true) {
            trailPointColor = new Scalar(0, 255, 255);
        }else if(Objects.equals(trlPtClr,new String("c")) == true) {
            trailPointColor = new Scalar(255, 255, 0);
        }else if(Objects.equals(trlPtClr,new String("k")) == true) {
            trailPointColor = new Scalar(0, 0, 0);
        }else if(Objects.equals(trlPtClr,new String("w")) == true) {
            trailPointColor = new Scalar(255, 255, 255);
        }

        String rotDeg = sharedPref.getString("pref_rotateDegreesPostProcess","90");
        if (Objects.equals(rotDeg,new String("90")) == true){
            rotateDegreesPostProcess = Integer.valueOf(rotDeg);
        }else if(Objects.equals(rotDeg,new String("180")) == true){
            rotateDegreesPostProcess = Integer.valueOf(rotDeg);
        }else if(Objects.equals(rotDeg,new String("270")) == true) {
            rotateDegreesPostProcess = Integer.valueOf(rotDeg);
        }else{
            rotateDegreesPostProcess = 0 ;
        }

        String aglPtClr = sharedPref.getString("pref_anglePointColor","r");
        if (Objects.equals(aglPtClr,new String("r")) == true){
            anglePointColor = new Scalar(255,0,0);
        }else if(Objects.equals(aglPtClr,new String("g")) == true){
            anglePointColor = new Scalar(0,255,0);
        }else if(Objects.equals(aglPtClr,new String("b")) == true) {
            anglePointColor = new Scalar(0, 0, 255);
        }else if(Objects.equals(aglPtClr,new String("y")) == true) {
            anglePointColor = new Scalar(0, 255, 255);
        }else if(Objects.equals(aglPtClr,new String("c")) == true) {
            anglePointColor = new Scalar(255, 255, 0);
        }else if(Objects.equals(aglPtClr,new String("k")) == true) {
            anglePointColor = new Scalar(0, 0, 0);
        }else if(Objects.equals(aglPtClr,new String("w")) == true) {
            anglePointColor = new Scalar(255, 255, 255);
        }

        String aglLnClr = sharedPref.getString("pref_angleLineColor","r");
        if (Objects.equals(aglLnClr,new String("r")) == true){
            angleLineColor = new Scalar(255,0,0);
        }else if(Objects.equals(aglLnClr,new String("g")) == true){
            angleLineColor = new Scalar(0,255,0);
        }else if(Objects.equals(aglLnClr,new String("b")) == true) {
            angleLineColor = new Scalar(0, 0, 255);
        }else if(Objects.equals(aglLnClr,new String("y")) == true) {
            angleLineColor = new Scalar(0, 255, 255);
        }else if(Objects.equals(aglLnClr,new String("c")) == true) {
            angleLineColor = new Scalar(255, 255, 0);
        }else if(Objects.equals(aglLnClr,new String("k")) == true) {
            angleLineColor = new Scalar(0, 0, 0);
        }else if(Objects.equals(aglLnClr,new String("w")) == true) {
            angleLineColor = new Scalar(255, 255, 255);
        }

        String txtLnClr = sharedPref.getString("pref_angleTextColor","r");
        if (Objects.equals(txtLnClr,new String("r")) == true){
            angleTextColor = new Scalar(255,0,0);
        }else if(Objects.equals(txtLnClr,new String("g")) == true){
            angleTextColor = new Scalar(0,255,0);
        }else if(Objects.equals(txtLnClr,new String("b")) == true) {
            angleTextColor = new Scalar(0, 0, 255);
        }else if(Objects.equals(txtLnClr,new String("y")) == true) {
            angleTextColor = new Scalar(0, 255, 255);
        }else if(Objects.equals(txtLnClr,new String("c")) == true) {
            angleTextColor = new Scalar(255, 255, 0);
        }else if(Objects.equals(txtLnClr,new String("k")) == true) {
            angleTextColor = new Scalar(0, 0, 0);
        }else if(Objects.equals(txtLnClr,new String("w")) == true) {
            angleTextColor = new Scalar(255, 255, 255);
        }

        String txtBoxTxtSize = sharedPref.getString("pref_angleTextSize","5");
        angleTextSize = Integer.valueOf(txtBoxTxtSize);
        //cw.getDir("imageDir", Context.MODE_PRIVATE);
        setContentView(R.layout.activity_track_points_offline);
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @SuppressLint("LongLogTag")
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    new trackPointsOffline.beginTrackingProcedure().execute(null, null, null);//
                    Intent intent = new Intent(getApplicationContext(), offlineProcessing.class);
                    Toast.makeText(getApplicationContext(),"Tracking video - this may take a while", Toast.LENGTH_LONG).show();
                    startActivity(intent);

                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    private class beginTrackingProcedure extends AsyncTask<Void, Void, Void> {

        String outPath;

        protected Void doInBackground(Void... params) {

            //start frame grabber
//            FFmpegFrameGrabber g = new FFmpegFrameGrabber(videoAbsolutePath);
//            Frame frame;
//            AndroidFrameConverter converterToBitmap = new AndroidFrameConverter();
            enc = null;
            try {
                DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd'at'HH-mm-ss");
                eMagTime = df2.format(Calendar.getInstance().getTime());
                outPath = directory.getAbsolutePath() + "/FullSize-Tracked-" + eMagTime + ".mp4";
                out = null;
                out = NIOUtils.writableFileChannel(outPath);
                enc = new AndroidSequenceEncoder(out, Rational.R(25,1));
                outPath = directory.getAbsolutePath() + "/SmallSize-Tracked-" + eMagTime + ".mp4";
                out2 = null;
                out2 = NIOUtils.writableFileChannel(outPath);
                enc2 = new AndroidSequenceEncoder(out2, Rational.R(25,1));
            } catch (IOException e) {
                e.printStackTrace();
            }

            //try ExtractMpegFramesTest method to return a bitmap
            MediaCodec decoder = null;
            ExtractMpegFramesTest.CodecOutputSurface outputSurface = null;
            MediaExtractor extractor = null;
            File inputFile = new File(videoAbsolutePath);
            if (!inputFile.canRead()) {
                try {
                    throw new FileNotFoundException("Unable to read " + inputFile);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            extractor = new MediaExtractor();
            try {
                extractor.setDataSource(inputFile.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            int trackIndex = selectTrack(extractor);
            if (trackIndex < 0) {
                throw new RuntimeException("No video track found in " + inputFile);
            }
            extractor.selectTrack(trackIndex);
            MediaFormat format = extractor.getTrackFormat(trackIndex);
            if (VERBOSE) {
                Log.d(TAG, "Video size is " + format.getInteger(MediaFormat.KEY_WIDTH) + "x" +
                        format.getInteger(MediaFormat.KEY_HEIGHT));
            }
            // Could use width/height from the MediaFormat to get full-size frames.
            format.setInteger("rotation-degrees", 0);
            if (format.containsKey("frame-rate")) {
                frameRate = format.getInteger("frame-rate");
            }else{
                frameRate = 30;
            }
            duration = format.getLong("durationUs");
            outputSurface = new ExtractMpegFramesTest.CodecOutputSurface(format.getInteger(MediaFormat.KEY_WIDTH),format.getInteger(MediaFormat.KEY_HEIGHT));//check!frameWidth,frameHeight);
            String mime = format.getString(MediaFormat.KEY_MIME);
            try {
                decoder = MediaCodec.createDecoderByType(mime);
            } catch (IOException e) {
                e.printStackTrace();
            }
            decoder.configure(format, outputSurface.getSurface(), null, 0);
            decoder.start();

            try {
                doExtract(extractor, trackIndex, decoder, outputSurface);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (outputSurface != null) {
                outputSurface.release();
                outputSurface = null;
            }
            if (decoder != null) {
                decoder.stop();
                decoder.release();
                decoder = null;
            }
            if (extractor != null) {
                extractor.release();
                extractor = null;
            }

            return null;
        }

        protected void onProgressUpdate(Void... progress) {
            Toast.makeText(getApplicationContext(), "Currently processing frame " + String.valueOf(c+1), Toast.LENGTH_SHORT).show();
        }

        protected void onPostExecute(Void result) {

            //Intent intentPass = new Intent(getApplicationContext(), playVideo.class);

            //File file = new File(outPath);
           // Uri uri = Uri.fromFile(file);
            //intentPass.putExtra("vidUri", uri.toString());
            //startActivity(intentPass);
            Toast.makeText(trackPointsOffline.this, "Tracking completed!", Toast.LENGTH_SHORT).show();
        }

    }

    private void doExtract(MediaExtractor extractor, int trackIndex, MediaCodec decoder,
                           ExtractMpegFramesTest.CodecOutputSurface outputSurface) throws IOException {
        final int TIMEOUT_USEC = 10000;
        ByteBuffer[] decoderInputBuffers = decoder.getInputBuffers();
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        int inputChunk = 0;
        int decodeCount = 0;

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try{
            retriever.setDataSource(videoAbsolutePath);
        }catch (Exception e) {
            System.out.println("Exception= "+e);
        }
        String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        int duration_millisec = Integer.parseInt(duration); //duration in millisec
        seconds = ((double)duration_millisec / 1000);
        frames = (int)Math.round(seconds) * (int)Math.round(frameRate);

        boolean outputDone = false;
        boolean inputDone = false;
        while (!outputDone) {
            if (VERBOSE) Log.d(TAG, "loop");

            // Feed more data to the decoder.
            if (!inputDone) {
                int inputBufIndex = decoder.dequeueInputBuffer(TIMEOUT_USEC);
                if (inputBufIndex >= 0) {
                    ByteBuffer inputBuf = decoderInputBuffers[inputBufIndex];
                    // Read the sample data into the ByteBuffer.  This neither respects nor
                    // updates inputBuf's position, limit, etc.
                    int chunkSize = extractor.readSampleData(inputBuf, 0);
                    if (chunkSize < 0) {
                        // End of stream -- send empty frame with EOS flag set.
                        decoder.queueInputBuffer(inputBufIndex, 0, 0, 0L,
                                MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        inputDone = true;
                        if (VERBOSE) Log.d(TAG, "sent input EOS");
                    } else {
                        if (extractor.getSampleTrackIndex() != trackIndex) {
                            Log.w(TAG, "WEIRD: got sample from track " +
                                    extractor.getSampleTrackIndex() + ", expected " + trackIndex);
                        }
                        long presentationTimeUs = extractor.getSampleTime();
                        decoder.queueInputBuffer(inputBufIndex, 0, chunkSize,
                                presentationTimeUs, 0 /*flags*/);
                        if (VERBOSE) {
                            Log.d(TAG, "submitted frame " + inputChunk + " to dec, size=" +
                                    chunkSize);
                        }
                        inputChunk++;
                        extractor.advance();
                    }
                } else {
                    if (VERBOSE) Log.d(TAG, "input buffer not available");
                }
            }

            if (!outputDone) {
                int decoderStatus = decoder.dequeueOutputBuffer(info, TIMEOUT_USEC);
                if (decoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    // no output available yet
                    if (VERBOSE) Log.d(TAG, "no output from decoder available");
                } else if (decoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    // not important for us, since we're using Surface
                    if (VERBOSE) Log.d(TAG, "decoder output buffers changed");
                } else if (decoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    MediaFormat newFormat = decoder.getOutputFormat();
                    if (VERBOSE) Log.d(TAG, "decoder output format changed: " + newFormat);
                } else if (decoderStatus < 0) {
                    fail("unexpected result from decoder.dequeueOutputBuffer: " + decoderStatus);
                } else { // decoderStatus >= 0
                    if (VERBOSE) Log.d(TAG, "surface decoder given buffer " + decoderStatus +
                            " (size=" + info.size + ")");
                    if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        if (VERBOSE) Log.d(TAG, "output EOS");
                        outputDone = true;
                    }

                    boolean doRender = (info.size != 0);

                    // As soon as we call releaseOutputBuffer, the buffer will be forwarded
                    // to SurfaceTexture to convert to a texture.  The API doesn't guarantee
                    // that the texture will be available before the call returns, so we
                    // need to wait for the onFrameAvailable callback to fire.
                    decoder.releaseOutputBuffer(decoderStatus, doRender);
                    if (doRender) {
                        if (VERBOSE) Log.d(TAG, "awaiting decode of frame " + decodeCount);
                        outputSurface.awaitNewImage();
                        outputSurface.drawImage(true);
                        Bitmap bmp = outputSurface.returnFrame();
                        bmp = VideoProcessing.rotateFrame(bmp,rotateDegreesPostProcess);
                        trackPoints(bmp);
                        decodeCount++;
                        String msg = String.valueOf((int)Math.round(((double)decodeCount / (double)frames)*100));
                        MainActivity.mBuilder.setContentText(msg + "% completed");
                        MainActivity.notificationManager.notify(MainActivity.notificationID, MainActivity.mBuilder.build());
//                        bmp.recycle();
                    }else{
                        outputDone = true;
                        enc.finish();
                        NIOUtils.closeQuietly(out);
                        enc2.finish();
                        NIOUtils.closeQuietly(out2);
                        coords = null;

                    }
                }
            }
        }
    }



    private void trackPoints(Bitmap bmp){
        if (allPoints.size() == 0){
            List<String> numbers = Arrays.asList(coords.split(","));
            List<Integer> numbersInt = new ArrayList<>();
            for (String number : numbers) {//convert string to int
                float tmp = Float.parseFloat(number.trim());
                int tmp2 = (int) tmp;
                numbersInt.add(tmp2);
            }
            List<String> types = Arrays.asList(coordType.split(","));
            List<Integer> typesInt = new ArrayList<>();
            for (String type : types) {//convert string to int
                float tmp = Float.parseFloat(type.trim());
                int tmp2 = (int) tmp;
                typesInt.add(tmp2);
            }
            numPoints = numbersInt.size() / 2;//determine number of points to track
            //use this section to get information about video frame size
            int frameRows = bmp.getHeight();
            int frameCols = bmp.getWidth();
            //initialize matrices for tracking
            mGray = new Mat(frameRows, frameCols, CvType.CV_8UC3);
            mPrevGray = new Mat(frameRows, frameCols, CvType.CV_8UC3);
            features = new MatOfPoint();
            prevFeatures = new MatOfPoint2f();
            nextFeatures = new MatOfPoint2f();
            status = new MatOfByte();
            err = new MatOfFloat();
            int ct = 0;
            for (int i = 0; i < numPoints; i++) {
                double a = numbersInt.get(ct);
                int c = typesInt.get(ct);
                ct++;
                double b = numbersInt.get(ct);
                ct++;
                Point point = new Point((int) a, (int) b);
                points.add(point);
                pointTypes.add(c);
            }

            allPoints.add((ArrayList) points.clone());
            allPointTypes.add((ArrayList) pointTypes.clone());

            //if (allPoints.size() == numTrailPoints){
             //   allPoints.remove(0);
               // allPointTypes.remove(0);
                //allPoints.add((ArrayList) points.clone());
                //allPointTypes.add((ArrayList) pointTypes.clone());
            //}

            features.fromList(points);
            prevFeatures.fromList(features.toList());//initializes prevFeatures to the first point
            Utils.bitmapToMat(bmp, mPrevGray);
            Imgproc.cvtColor(mPrevGray,mPrevGray, Imgproc.COLOR_RGBA2RGB);

            ArrayList ptTypes = allPointTypes.get(0);
            int target = 0;
            for (int i=0; i < ptTypes.size(); i++){
                int ptp = (int) ptTypes.get(i);
                if (ptp == target){
                    numRegPoints++;
                }
            }

            for (int i = 0; i < allPoints.size(); i++) {
                ArrayList<Point> points2 = allPoints.get(i);
                ArrayList<Integer> pointTypes = allPointTypes.get(i);
                for (int j = 0; j < points2.size(); j++) {
                    if (pointTypes.get(j) == 0 && i == 0) {
                        Imgproc.circle(mPrevGray, points2.get(j), currentPointSize, currentPointColor, 8, 8, 0);
                        trailPointsCollect.add(points2.get(j));
                    }else if (pointTypes.get(j) == 0 && i != 0){
                        for (int k = 0; k < i; k++) {
                            ArrayList<Point> p2 = allPoints.get(i-k+1);
                            ArrayList<Integer> pT2 = allPointTypes.get(i-k+1);
                            for (int l = 0; l < p2.size(); l++) {
                                if (pT2.get(l) == 0 && k < numTrailPoints) {
                                    Imgproc.circle(mPrevGray, p2.get(l), trailPointSize, trailPointColor, 8, 8, 0);
                                    trailPointsCollect.add(p2.get(l));
                                }
                            }
                        }
                        if (i != 0){
                            int count = 0;
                            for (int k = 0; k < trailPointsCollect.size()-numRegPoints; k = k + numRegPoints){
                                if (count < numTrailPoints){
                                    for (int m = 0; m < numRegPoints; m++) {
                                        Imgproc.circle(mPrevGray, trailPointsCollect.get(m+(k*numRegPoints)), trailPointSize, trailPointColor, 8, 8, 0);
                                    }
                                    count++;
                                }
                            }
                        }
                    }else if (pointTypes.get(j) == 1){
                        ArrayList<Point> pts = new ArrayList<>();
                        for (int k = 0; k < 2; k++){
                            if(points.size() > j+k) {
                                pts.add(points.get(j + k));
                                Imgproc.circle(mPrevGray, pts.get(k), currentPointSize, anglePointColor, 8, 8, 0);
                            }
                            if (pts.size() == 2) {
                                mPrevGray = addLinesBetweenAngles(mPrevGray, pts);
                            }
                        }
                        j++;
                    }else if (pointTypes.get(j) == 2) {
                        ArrayList<Point> pts = new ArrayList<Point>();
                        for (int k = 0; k < 2; k++) {
                            if(points.size() > j+k) {
                                pts.add(points.get(j + k));
                                Imgproc.circle(mPrevGray, pts.get(k), currentPointSize, anglePointColor, 8, 8, 0);
                            }
                        }
                        if (pts.size() == 2) {
                            mPrevGray = addLinesBetweenAngles(mPrevGray, pts);
                            angles = calculateAngles(pts);
                            gText = String.format("%.2f", (angles.get(angles.size() - 1)));
                            Imgproc.putText(mPrevGray, gText, pts.get(0), 3, angleTextSize, angleTextColor, 2, 1, false);
                            j++;
                        }
                    }else if (pointTypes.get(j) == 3) {
                        ArrayList<Point> pts = new ArrayList<Point>();
                        for (int k = 0; k < 3; k++) {
                            if(points.size() > j+k) {
                                pts.add(points.get(j + k));
                                Imgproc.circle(mPrevGray, pts.get(k), currentPointSize, anglePointColor, 8, 8, 0);
                            }
                        }
                        if (pts.size() == 3) {
                            mPrevGray = addLinesBetweenAngles(mPrevGray, pts);
                            angles = calculateAngles(pts);
                            gText = String.format("%.2f", (angles.get(angles.size() - 1)));
                            Imgproc.putText(mPrevGray, gText, pts.get(0), 3, angleTextSize, angleTextColor, 2, 1, false);
                            j++;
                            j++;
                        }
                    }else if (pointTypes.get(i) == 4) {
                        ArrayList<Point> pts = new ArrayList<Point>();
                        for (int k = 0; k < 4; k++) {
                            if(points.size() > j+k) {
                                pts.add(points.get(j + k));
                                Imgproc.circle(mPrevGray, pts.get(k), currentPointSize, anglePointColor, 8, 8, 0);
                            }
                        }
                        if (pts.size() == 4) {
                            mPrevGray = addLinesBetweenAngles(mPrevGray, pts);
                            angles = calculateAngles(pts);
                            gText = String.format("%.2f", (angles.get(angles.size() - 1)));
                            Imgproc.putText(mPrevGray, gText, pts.get(0), 3, angleTextSize, angleTextColor, 2, 1, false);
                            j++;
                            j++;
                            j++;
                        }
                    }
                }
            }
            Imgproc.cvtColor(mPrevGray,mPrevGray, Imgproc.COLOR_RGB2RGBA);
            prevFeatures.fromList(points);
            Utils.matToBitmap(mPrevGray, bmp);
//            Picture pic = fromBitmap(bmp);
            try {
                enc.encodeImage(bmp);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Imgproc.cvtColor(mPrevGray, mPrevGray, Imgproc.COLOR_RGBA2GRAY);
        } else {
            mGray = new Mat(mGray.rows(), mGray.cols(), CvType.CV_8UC3);
            prevFeatures = new MatOfPoint2f();
            prevFeatures.fromList(allPoints.get(allPoints.size()-1));
            nextFeatures = new MatOfPoint2f();
            Utils.bitmapToMat(bmp, mGray);
            Imgproc.cvtColor(mGray, mGray, Imgproc.COLOR_RGBA2GRAY);
            nextFeatures.fromArray(prevFeatures.toArray());//feature(point) set to be calculated
            Size winSize = new Size(winSearchSize, winSearchSize);
            TermCriteria termCrit = new TermCriteria(COUNT, 20, epsilon);
            Video.calcOpticalFlowPyrLK(mPrevGray, mGray, prevFeatures, nextFeatures, status, err, winSize, maxLevel, termCrit, 0, eigenThreshold);
            prevFeatures.fromList(nextFeatures.toList());
            mPrevGray = new Mat(mGray.rows(), mGray.cols(), CvType.CV_8UC1);
            mPrevGray = mGray.clone();
            points.clear();
            points.addAll(nextFeatures.toList());
            allPoints.add((ArrayList) points.clone());
            allPointTypes.add((ArrayList) pointTypes.clone());

            Utils.bitmapToMat(bmp, mGray);
            Imgproc.cvtColor(mGray,mGray, Imgproc.COLOR_RGBA2RGB);
            for (int i = 0; i<points.size(); i++) {
                if (pointTypes.get(i) == 0) {
                    Imgproc.circle(mGray, points.get(i), currentPointSize, currentPointColor, 8, 8, 0);
                    trailPointsCollect.add(points.get(i));
                } else if (pointTypes.get(i) == 1) {
                    ArrayList<Point> pts = new ArrayList<>();
                    for (int j = 0; j < 2; j++) {
                        if (points.size() > i + j) {
                            pts.add(points.get(i + j));
                            Imgproc.circle(mGray, pts.get(j), currentPointSize, anglePointColor, 8, 8, 0);
                        }
                        if (pts.size() == 2) {
                            mGray = addLinesBetweenAngles(mGray, pts);
                        }
                    }
                    i++;
                } else if (pointTypes.get(i) == 2) {
                    ArrayList<Point> pts = new ArrayList<Point>();
                    for (int j = 0; j < 2; j++) {
                        if (points.size() > i + j) {
                            pts.add(points.get(i + j));
                            Imgproc.circle(mGray, pts.get(j), currentPointSize, anglePointColor, 8, 8, 0);
                        }
                    }
                    if (pts.size() == 2) {
                        mGray = addLinesBetweenAngles(mGray, pts);
                        angles = calculateAngles(pts);
                        gText = String.format("%.2f", (angles.get(angles.size() - 1)));
                        Imgproc.putText(mGray, gText, pts.get(0), 3, angleTextSize, angleTextColor, 2, 1, false);
                        i++;
                    }
                } else if (pointTypes.get(i) == 3) {
                    ArrayList<Point> pts = new ArrayList<Point>();
                    for (int j = 0; j < 3; j++) {
                        if (points.size() > i + j) {
                            pts.add(points.get(i + j));
                            Imgproc.circle(mGray, pts.get(j), currentPointSize, anglePointColor, 8, 8, 0);
                        }
                    }
                    if (pts.size() == 3) {
                        mGray = addLinesBetweenAngles(mGray, pts);
                        angles = calculateAngles(pts);
                        gText = String.format("%.2f", (angles.get(angles.size() - 1)));
                        Imgproc.putText(mGray, gText, pts.get(0), 3, angleTextSize, angleTextColor, 2, 1, false);
                        i++;
                        i++;
                    }
                } else if (pointTypes.get(i) == 4) {
                    ArrayList<Point> pts = new ArrayList<Point>();
                    for (int j = 0; j < 4; j++) {
                        if (points.size() > i + j) {
                            pts.add(points.get(i + j));
                            Imgproc.circle(mGray, pts.get(j), currentPointSize, anglePointColor, 8, 8, 0);
                        }
                    }
                    if (pts.size() == 4) {
                        mGray = addLinesBetweenAngles(mGray, pts);
                        angles = calculateAngles(pts);
                        gText = String.format("%.2f", (angles.get(angles.size() - 1)));
                        Imgproc.putText(mGray, gText, pts.get(0), 3, angleTextSize, angleTextColor, 2, 1, false);
                        i++;
                        i++;
                        i++;
                    }
                }
            }
            int count = 0;
            if (trailPointsCollect.size() != 0) {
                if (trailPointsCollect.size() <= numTrailPoints*numRegPoints) {
                    for (int k = 0; k < trailPointsCollect.size(); k++) {
                        Imgproc.circle(mGray, trailPointsCollect.get(k), trailPointSize, trailPointColor, 8, 8, 0);
                        }
                }else{
                    ArrayList<Point> p = new ArrayList<>();
                    for (int k = 0; k<numTrailPoints*numRegPoints; k++){
                        p.add(trailPointsCollect.get(trailPointsCollect.size() - (k+1)));
                    }
                    for (int k = 0; k < p.size(); k++) {
                        Imgproc.circle(mGray, p.get(k), trailPointSize, trailPointColor, 8, 8, 0);
                    }
                }
            }
            Imgproc.cvtColor(mGray,mGray, Imgproc.COLOR_RGB2RGBA);
            Utils.matToBitmap(mGray,bmp);
//            Picture pic = fromBitmap(bmp);
            try {
                enc.encodeImage(bmp);
                bmp = VideoProcessing.resizeForInstagram(bmp);
                enc2.encodeImage(bmp);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mGray.release();
        }
    }



    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                String debuggerString = cursor.getString(column_index);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }




    public int selectTrack(MediaExtractor extractor) {
        // Select the first video track we find, ignore the rest.
        int numTracks = extractor.getTrackCount();
        for (int i = 0; i < numTracks; i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("video/")) {
                if (VERBOSE) {
                    Log.d(TAG, "Extractor selected track " + i + " (" + mime + "): " + format);
                }
                return i;
            }
        }

        return -1;
    }


//    public static Picture fromBitmap(Bitmap src) {
//        Picture dst = Picture.create((int)src.getWidth(), (int)src.getHeight(), ColorSpace.RGB);
//        fromBitmap(src, dst);
//        return dst;
//    }

//    public static void fromBitmap(Bitmap src, Picture dst) {
//        byte[] dstData = dst.getPlaneData(0);
//        int[] packed = new int[src.getWidth() * src.getHeight()];
//
//        src.getPixels(packed, 0, src.getWidth(), 0, 0, src.getWidth(), src.getHeight());
//
//        for (int i = 0, srcOff = 0, dstOff = 0; i < src.getHeight(); i++) {
//            for (int j = 0; j < src.getWidth(); j++, srcOff++, dstOff += 3) {
//                int rgb = packed[srcOff];
//                dstData[dstOff]     = (rgb >> 16) & 0xff;
//                dstData[dstOff + 1] = (rgb >> 8) & 0xff;
//                dstData[dstOff + 2] = rgb & 0xff;
//            }
//        }
//    }

    public ArrayList<Double> calculateAngles(ArrayList<Point> points){
        ArrayList<Double> angles = new ArrayList<Double>();
        if (points.size() == 2) {
            angles = nonlinearMath.twoPointGlobalAngle(points);
            return angles;
        }
        if (points.size() == 3) {
            angles = nonlinearMath.threePointSegmentAngle(points);
            return angles;
        }
        if (points.size() == 4) {
            angles = nonlinearMath.fourPointSegmentAngle(points);
            return angles;
        }
        angles.add((double) 0);
        return angles;
    }

    public Mat addLinesBetweenAngles(Mat m, ArrayList<Point> pts){
        if (pts.size() == 2) {
            Imgproc.line(m,pts.get(0),pts.get(1),angleLineColor,8 );
        }
        if (pts.size() == 3) {
            Imgproc.line(m,pts.get(0),pts.get(1),angleLineColor,8 );
            Imgproc.line(m,pts.get(0),pts.get(2),angleLineColor,8 );
        }
        if (pts.size() == 4) {
            Imgproc.line(m,pts.get(0),pts.get(1),angleLineColor,8 );
            Imgproc.line(m,pts.get(2),pts.get(3),angleLineColor,8 );
        }
        return m;
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
