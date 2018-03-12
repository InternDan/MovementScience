package com.home.buffa.movementscience;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
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
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import org.jcodec.api.SequenceEncoder;
import org.jcodec.api.android.AndroidSequenceEncoder;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.io.SeekableByteChannel;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Picture;
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

import static com.home.buffa.movementscience.ExtractMpegFramesTest.*;
import static junit.framework.Assert.fail;
import static org.opencv.core.TermCriteria.COUNT;

public class trackPointsTest extends Activity {

    private static final String TAG = "trackPointsTest";
    private static final boolean VERBOSE = false;           // lots of logging

    static Intent intentPass = new Intent();
    static String coords;
    MediaMetadataRetriever mediaMetadataRetriever;
    MediaMetadataRetriever mediaMetadataRetrieverTracked;

    //set the working directory to put the tracked video in

    ContextWrapper cw;
    public File directory;

    static ArrayList<Point> points = new ArrayList<Point>();
    public static ArrayList<Point> pointsOut = new ArrayList<Point>();
    ArrayList<Double> angles = new ArrayList<Double>();

    static Mat mGray;
    static Mat mPrevGray;

    static MatOfPoint2f prevFeatures;
    static MatOfPoint2f nextFeatures;
    static MatOfPoint features;

    static MatOfByte status;
    static MatOfFloat err;

    String videoPath;
    static String videoAbsolutePath;

    Bitmap bmp;
    int c = 0;
    int numPoints = 0;
    int frameHeight = 0;
    int frameWidth = 0;
    long duration = 0;
    int frameRate = 0;

    int currentPointSize;
    int rotateDegreesPostProcess;
    Scalar angleLineColor;
    Scalar anglePointColor;
    Scalar currentPointColor;
    Scalar angleTextColor;
    String aglLnClr;
    String txtLnClr;

    AndroidSequenceEncoder enc;
    SeekableByteChannel out;

    String eMagTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
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
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        String curPtSize = sharedPref.getString("pref_currentPointSize","5");
        currentPointSize = Integer.valueOf(curPtSize);

        txtLnClr = sharedPref.getString("pref_angleTextColor","r");
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

        aglLnClr = sharedPref.getString("pref_angleLineColor","r");
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

        String rotate = sharedPref.getString("pref_rotateDegreesPostProcess","90");
        if (Objects.equals(rotate,new String("0")) == true){
            rotateDegreesPostProcess = 0;
        }else if(Objects.equals(rotate,new String("90")) == true){
            rotateDegreesPostProcess = 90;
        }else if(Objects.equals(rotate,new String("180")) == true) {
            rotateDegreesPostProcess = 180;
        }else if(Objects.equals(rotate,new String("270")) == true) {
            rotateDegreesPostProcess = 270;
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


        videoPath = intentReceive.getExtras().getString("videoPath");
        videoAbsolutePath = intentReceive.getExtras().getString("videoAbsolutePath");
        coords = intentReceive.getExtras().getString("coordinates");
        coords = coords.substring(5);
        frameHeight = intentReceive.getExtras().getInt("frameHeight");
        frameWidth = intentReceive.getExtras().getInt("frameWidth");
        directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        //cw.getDir("imageDir", Context.MODE_PRIVATE);
        setContentView(R.layout.activity_track_points);

    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @SuppressLint("LongLogTag")
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    new beginTrackingProcedure().execute(null, null, null);//
                    Intent intent = new Intent(getApplicationContext(), offlineProcessing.class);
                    Toast.makeText(getApplicationContext(),"Application will load video once tracked", Toast.LENGTH_LONG).show();
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
                outPath = directory.getAbsolutePath() + "/" + eMagTime + "-trackedVid.mov";
                out = NIOUtils.writableFileChannel(outPath);
                enc = new AndroidSequenceEncoder(out, Rational.R(25,1));
            } catch (IOException e) {
                e.printStackTrace();
            }

            //try ExtractMpegFramesTest method to return a bitmap
            MediaCodec decoder = null;
            CodecOutputSurface outputSurface = null;
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
                frameRate = 25;
            }
            duration = format.getLong("durationUs");
            outputSurface = new CodecOutputSurface(format.getInteger(MediaFormat.KEY_WIDTH),format.getInteger(MediaFormat.KEY_HEIGHT));
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

            Intent intentPass = new Intent(getApplicationContext(), displayTrackingResults.class);

            File file = new File(outPath);
            intentPass.putExtra("videoPath", file.toString());
            intentPass.putExtra("duration", duration);
            intentPass.putExtra("frameRate", frameRate);
            startActivity(intentPass);
        }

    }





    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
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


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
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
//
//    public static void fromBitmap(Bitmap src, Picture dst) {
//        int[] dstData = dst.getPlaneData(0);
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

    private void initialFrameTrackPoints(Bitmap bmp){//expects c to be used as counter
        List<String> numbers = Arrays.asList(coords.split(","));
        List<Integer> numbersInt = new ArrayList<>();
        for (String number : numbers) {//convert string to int
            float tmp = Float.parseFloat(number.trim());
            int tmp2 = (int) tmp;
            numbersInt.add(tmp2);
        }
        //numbersInt.remove(0);//remove initialization position
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
            ct++;
            double b = numbersInt.get(ct);
            ct++;
            Point point = new Point((int) a, (int) b);
            points.add(point);
        }
        features.fromList(points);
        prevFeatures.fromList(features.toList());//initializes prevFeatures to the first point

        int height = bmp.getHeight();
        int width = bmp.getWidth();
        DisplayMetrics metrics = new DisplayMetrics();
        Display display = getWindowManager().getDefaultDisplay();
        display.getRealMetrics(metrics);

        int heightScreen = metrics.heightPixels;
        int widthScreen = metrics.widthPixels;

        float scaleHeight = (float) height / (float) heightScreen;
        float scaleWidth = (float) width / (float) widthScreen;

        Utils.bitmapToMat(bmp, mPrevGray);
        Imgproc.cvtColor(mPrevGray,mPrevGray, Imgproc.COLOR_RGBA2RGB);
        for (int i = 0; i < points.size(); i++) {
            Point ptTmp = points.get(i);
            ptTmp.x = ptTmp.x / scaleWidth;
            ptTmp.y = ptTmp.y / scaleHeight;
            Imgproc.circle(mPrevGray, ptTmp, currentPointSize, anglePointColor, 8, 8, 0);
        }
        mPrevGray = addLinesBetweenAngles(mPrevGray);
        Imgproc.cvtColor(mPrevGray,mPrevGray, Imgproc.COLOR_RGB2RGBA);
        prevFeatures.fromList(points);
        Utils.matToBitmap(mPrevGray, bmp);
        //vidFrameArray.add(bmp);

        if (numPoints == 2) {
            angles = nonlinearMath.twoPointGlobalAngle(points);
            bmp = drawAngleToBitmap(bmp);
        }
        if (numPoints == 3) {
            angles = nonlinearMath.threePointSegmentAngle(points);
            bmp = drawAngleToBitmap(bmp);
        }
        if (numPoints == 4) {
            angles = nonlinearMath.fourPointSegmentAngle(points);
            bmp = drawAngleToBitmap(bmp);
        }




//        Picture pic = fromBitmap(bmp);
        try {
            enc.encodeImage(bmp);
        } catch (IOException e) {
            e.printStackTrace();
        }
//        bmp.recycle();

        c++;
    }

    private void nextFrameTrackPoints(Bitmap bmp){
        mGray = new Mat(mGray.rows(), mGray.cols(), CvType.CV_8UC3);
        if (c > 1) {
            prevFeatures = new MatOfPoint2f();
            prevFeatures.fromList(nextFeatures.toList());
        }
        nextFeatures = new MatOfPoint2f();

//        float cx = bmp.getWidth() / 2f;
//        float cy = bmp.getHeight() / 2f;
//        Matrix m = new Matrix();
//        m.postScale(1, -1, cx, cy);
        Utils.bitmapToMat(bmp, mGray);

        if (c == 1) {
            Imgproc.cvtColor(mPrevGray, mPrevGray, Imgproc.COLOR_RGBA2GRAY);
        }
        Imgproc.cvtColor(mGray, mGray, Imgproc.COLOR_RGBA2GRAY);

        nextFeatures.fromArray(prevFeatures.toArray());//feature(point) set to be calculated
        Size winSize = new Size(15, 15);
        TermCriteria termCrit = new TermCriteria(COUNT, 20, 0.03);
        Video.calcOpticalFlowPyrLK(mPrevGray, mGray, prevFeatures, nextFeatures, status, err, winSize, 3, termCrit, 0, 0.001);

        prevFeatures.fromList(nextFeatures.toList());
        mPrevGray = new Mat(mGray.rows(), mGray.cols(), CvType.CV_8UC1);
        mPrevGray = mGray.clone();

        points.clear();
        points.addAll(nextFeatures.toList());

        List<Point> nextList = nextFeatures.toList();
        Utils.bitmapToMat(bmp, mGray);
        Imgproc.cvtColor(mGray,mGray, Imgproc.COLOR_RGBA2RGB);
        for (int i = 0; i < points.size(); i++) {
            Imgproc.circle(mGray, nextList.get(i), currentPointSize, anglePointColor, 8,8,0);
        }
        mGray = addLinesBetweenAngles(mGray);
        Imgproc.cvtColor(mGray,mGray, Imgproc.COLOR_RGB2RGBA);
        Utils.matToBitmap(mGray, bmp);
        if (points.size() == 2) {
            angles = nonlinearMath.twoPointGlobalAngle(points);
            bmp = drawAngleToBitmap(bmp);
        }
        if (points.size() == 3) {
            angles = nonlinearMath.threePointSegmentAngle(points);
            bmp = drawAngleToBitmap(bmp);
        }
        if (points.size() == 4) {
            angles = nonlinearMath.fourPointSegmentAngle(points);
            bmp = drawAngleToBitmap(bmp);
        }

//        Picture pic = fromBitmap(bmp);
        try {
            enc.encodeImage(bmp);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mGray.release();
//        bmp.recycle();

        c++;
    }

    private void doExtract(MediaExtractor extractor, int trackIndex, MediaCodec decoder,
                           CodecOutputSurface outputSurface) throws IOException {
        final int TIMEOUT_USEC = 10000;
        ByteBuffer[] decoderInputBuffers = decoder.getInputBuffers();
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        int inputChunk = 0;
        int decodeCount = 0;
        Bitmap bmp2;

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
                        if (bmp.getWidth() > bmp.getHeight()) {
                            Matrix matrix = new Matrix();
                            if (rotateDegreesPostProcess != 0){
                                matrix.postRotate(rotateDegreesPostProcess);
                            }
                            bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
                        }
                        if (c == 0){
                            initialFrameTrackPoints(bmp);
                        }
                        if (c > 0){
                            nextFrameTrackPoints(bmp);
                        }
                        bmp.recycle();

                    }else{
                        outputDone = true;
                        enc.finish();
                        NIOUtils.closeQuietly(out);
                        coords = null;
                    }
                }
            }
        }
    }

    private Bitmap drawAngleToBitmap(Bitmap bmp){
        Canvas canvas = new Canvas(bmp);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);


        if (Objects.equals(txtLnClr,new String("r")) == true){
            paint.setColor(Color.RED);
        }else if(Objects.equals(txtLnClr,new String("g")) == true){
            paint.setColor(Color.GREEN);
        }else if(Objects.equals(txtLnClr,new String("b")) == true) {
            paint.setColor(Color.BLUE);
        }else if(Objects.equals(txtLnClr,new String("y")) == true) {
            paint.setColor(Color.YELLOW);
        }else if(Objects.equals(txtLnClr,new String("c")) == true) {
            paint.setColor(Color.CYAN);
        }else if(Objects.equals(txtLnClr,new String("k")) == true) {
            paint.setColor(Color.BLACK);
        }else if(Objects.equals(txtLnClr,new String("w")) == true) {
            paint.setColor(Color.WHITE);
        }

        paint.setTextSize((int) (70));

        paint.setShadowLayer(1f, 0f, 1f, Color.WHITE);

        Rect bounds = new Rect();
        String gText = String.format("%.2f",(angles.get(angles.size()-1)));
        paint.getTextBounds(gText, 0, gText.length(), bounds);
//        int x = (bmp.getWidth() - bounds.width()-8);
//        int y = (1 + (bounds.height()+ 8));

        Point pt = points.get(0);
        int x = (int) pt.x;
        int y = (int) pt.y;

        canvas.drawText(gText, x, y, paint);

        return bmp;
    }

    public Mat addLinesBetweenAngles(Mat m){
        if (points.size() == 2) {
            Imgproc.line(m,points.get(0),points.get(1),angleLineColor,8 );
        }
        if (points.size() == 3) {
            Imgproc.line(m,points.get(0),points.get(1),angleLineColor,8 );
            Imgproc.line(m,points.get(0),points.get(2),angleLineColor,8 );
        }
        if (points.size() == 4) {
            Imgproc.line(m,points.get(0),points.get(1),angleLineColor,8 );
            Imgproc.line(m,points.get(2),points.get(3),angleLineColor,8 );
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
