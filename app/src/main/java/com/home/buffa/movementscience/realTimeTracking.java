package com.home.buffa.movementscience;


import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.ipaulpro.afilechooser.utils.FileUtils;

import org.jcodec.api.android.AndroidSequenceEncoder;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.io.SeekableByteChannel;
import org.jcodec.common.model.Rational;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.Video;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

import static android.media.MediaRecorder.AudioSource.MIC;
import static org.opencv.core.TermCriteria.COUNT;

//import static org.opencv.android.CameraBridgeViewBase.cHeight;
//import static org.opencv.android.CameraBridgeViewBase.cWidth;

public class realTimeTracking extends Activity implements CvCameraViewListener2 {

    private static final String TAG = "realTimeTracking";
    private CameraBridgeViewBase mOpenCvCameraView;

    int frameCountForOpenCVMemoryShit = 0;

    MediaRecorder mediaRecorder;
    LinearLayout linearLayout;
    String eMagTimeVideo;
    File tmpVid;
    String outputVideoFileName;

    String audioFileName;

    int screenHeight;
    int screenWidth;
    ArrayList<Point> points = new ArrayList<Point>();
    int numPoints;
    Mat prevFrame;
    Mat frameGray;
    int numTrailPoints;
    int currentPointSize;
    int trailPointSize;
    int angleTextSize;
    int selectionCounter = 0;
    int selectionCap = 0;
    int ptType;
    int maxLevel;
    int winSearchSize;
    int rotateRealTime;
    double epsilon;
    double eigenThreshold;
    Scalar currentPointColor;
    Scalar trailPointColor;
    Scalar anglePointColor;
    Scalar textBoxTextColor;
    Scalar angleLineColor;
    Scalar angleTextColor;
    ArrayList<Double> angles = new ArrayList<Double>();
    ArrayList<Integer> pointTypes = new ArrayList<Integer>();
    String gText;

    ArrayList<String> realtimeBmpPaths = new ArrayList<String>();

    ImageButton buttonPoint;
    ImageButton buttonLine;
    ImageButton button2Angle;
    ImageButton button3Angle;
    ImageButton button4Angle;
    ImageButton buttonRecord;

    boolean buttonPointPressed = false;
    boolean buttonLinePressed = false;
    boolean button2AnglePressed = false;
    boolean button3AnglePressed = false;
    boolean button4AnglePressed = false;

    int frameCounter;

    boolean record = false;

    AndroidSequenceEncoder enc;
    SeekableByteChannel out;
    File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

    private Handler mHandler = new Handler();

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

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
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.texture);
        mOpenCvCameraView.setCvCameraViewListener(this);
        Display display = getWindowManager().getDefaultDisplay();
        android.graphics.Point size = new android.graphics.Point();
        display.getRealSize(size);

        linearLayout = findViewById(R.id.linearLayoutRealTimeTracking);
        setButtons();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String numTrPts = sharedPref.getString("pref_trailingPointNumber","20");
        numTrailPoints = Integer.valueOf(numTrPts);
        String trPtSize = sharedPref.getString("pref_trailingPointSize","5");
        trailPointSize = Integer.valueOf(trPtSize);
        String curPtSize = sharedPref.getString("pref_currentPointSize","5");
        currentPointSize = Integer.valueOf(curPtSize);
        String txtBoxTxtSize = sharedPref.getString("pref_angleTextSize","5");
        angleTextSize = Integer.valueOf(txtBoxTxtSize);
        String txtWinSearchSize = sharedPref.getString("pref_trackingSearchWindowSize","20");
        winSearchSize = Integer.valueOf(txtWinSearchSize);
        String txtSearchEpsilon = sharedPref.getString("pref_trackingSearchEpsilon","0.1");
        epsilon = Double.valueOf(txtSearchEpsilon);
        String txtMaxLevel = sharedPref.getString("pref_trackingSearchMaxLevel","3");
        maxLevel = Integer.valueOf(txtMaxLevel);
        String txtEigenThreshold = sharedPref.getString("pref_trackingSearchMinEigenThreshold","0.001");
        eigenThreshold = Double.valueOf(txtEigenThreshold);
        String rotate = sharedPref.getString("pref_rotateDegreesRealTime","0");
        rotateRealTime = Integer.valueOf(rotate);


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

        String txtBxTxtClr = sharedPref.getString("pref_angleTextColor","r");
        if (Objects.equals(txtBxTxtClr,new String("r")) == true){
            textBoxTextColor = new Scalar(255,0,0);
        }else if(Objects.equals(txtBxTxtClr,new String("g")) == true){
            textBoxTextColor = new Scalar(0,255,0);
        }else if(Objects.equals(txtBxTxtClr,new String("b")) == true) {
            textBoxTextColor = new Scalar(0, 0, 255);
        }else if(Objects.equals(txtBxTxtClr,new String("y")) == true) {
            textBoxTextColor = new Scalar(0, 255, 255);
        }else if(Objects.equals(txtBxTxtClr,new String("c")) == true) {
            textBoxTextColor = new Scalar(255, 255, 0);
        }else if(Objects.equals(txtBxTxtClr,new String("k")) == true) {
            textBoxTextColor = new Scalar(0, 0, 0);
        }else if(Objects.equals(txtBxTxtClr,new String("w")) == true) {
            textBoxTextColor = new Scalar(255, 255, 255);
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

        screenHeight = size.x;
        screenWidth = size.y;

        frameCounter = 0;

        NavigationView navigationView  = findViewById(R.id.nav_view);
        final DrawerLayout mDrawerLayout = new DrawerLayout(getApplicationContext());
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // set item as selected to persist highlight
                        menuItem.setChecked(true);
                        // close drawer when item is tapped
                        mDrawerLayout.closeDrawers();
                        // Add code here to update the UI based on the item selected
                        // For example, swap UI fragments here
                        switch (menuItem.getItemId()) {
                            case R.id.action_capture:
                                Intent intent = new Intent(getApplicationContext(), CaptureLauncher.class);
                                startActivity(intent);
                                break;
                            case R.id.action_edit:
                                intent = new Intent(getApplicationContext(), EditLauncher.class);
                                startActivity(intent);
                                break;
                            case R.id.action_utilities:
                                intent = new Intent(getApplicationContext(), UtilityLauncher.class);
                                startActivity(intent);
                                break;
                            case R.id.action_help:
                                break;
                        }
                        return false;
                    }
                });
        View headerview = navigationView.getHeaderView(0);
        LinearLayout header = (LinearLayout) headerview.findViewById(R.id.navigation_header);
        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
            }
        });
        setLinearLayoutOnTouchListenerReset();

    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {

    }

    public void onCameraViewStopped() {

    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        //frameCountForOpenCVMemoryShit++;
        //if (frameCountForOpenCVMemoryShit == 2) {
        //    System.gc();
       //     System.runFinalization();
       //     frameCountForOpenCVMemoryShit = 0;
        //}
        Imgproc.cvtColor(inputFrame.rgba().t(),inputFrame.rgba().t(), Imgproc.COLOR_RGBA2RGB);
        Bitmap bmpOutt = Bitmap.createBitmap(inputFrame.rgba().t().width(), inputFrame.rgba().t().height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(inputFrame.rgba().t(),bmpOutt);
        Mat mRgbaT = inputFrame.rgba().t().clone();
        Core.flip(mRgbaT, mRgbaT, 1);
        Imgproc.resize(mRgbaT, mRgbaT, mRgbaT.size());



        if (points.size() > 0) {
            frameGray = inputFrame.gray().t().clone();
            Core.flip(frameGray, frameGray, 1);
            Imgproc.resize(frameGray, frameGray, frameGray.size());

            Bitmap tmpBmp = Bitmap.createBitmap(frameGray.width(), frameGray.height(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(frameGray, tmpBmp);

            MatOfPoint2f prevFeatures = new MatOfPoint2f();
            MatOfPoint2f nextFeatures = new MatOfPoint2f();
            MatOfByte status = new MatOfByte();
            MatOfFloat err = new MatOfFloat();

            prevFeatures.fromList(points);
            nextFeatures.fromList(points);

            Size winSize = new Size(winSearchSize, winSearchSize);
            TermCriteria termCrit = new TermCriteria(COUNT, 20,epsilon);
            Video.calcOpticalFlowPyrLK(prevFrame, frameGray, prevFeatures, nextFeatures, status, err, winSize, maxLevel, termCrit, 0, eigenThreshold);

            points.clear();
            points.addAll(nextFeatures.toList());
            //now need to break it up by type
            for (int i = 0; i<points.size(); i++) {
                if (pointTypes.get(i) == 0){
                    Imgproc.circle(mRgbaT, points.get(i), currentPointSize, currentPointColor, 8, 8, 0);
                }else if (pointTypes.get(i) == 1){
                    ArrayList<Point> pts = new ArrayList<>();
                    for (int j = 0; j < 2; j++){
                        if(points.size() > i+j) {
                            pts.add(points.get(i + j));
                            Imgproc.circle(mRgbaT, pts.get(j), currentPointSize, anglePointColor, 8, 8, 0);
                        }
                        if (pts.size() == 2) {
                            mRgbaT = addLinesBetweenAngles(mRgbaT, pts);
                        }
                    }
                    i++;
                }else if (pointTypes.get(i) == 2) {
                    ArrayList<Point> pts = new ArrayList<Point>();
                    for (int j = 0; j < 2; j++) {
                        if(points.size() > i+j) {
                            pts.add(points.get(i + j));
                            Imgproc.circle(mRgbaT, pts.get(j), currentPointSize, anglePointColor, 8, 8, 0);
                        }
                    }
                    if (pts.size() == 2) {
                        mRgbaT = addLinesBetweenAngles(mRgbaT, pts);
                        angles = calculateAngles(pts);
                        gText = String.format("%.2f", (angles.get(angles.size() - 1)));
                        Imgproc.putText(mRgbaT, gText, pts.get(0), 3, angleTextSize, angleTextColor, 2, 1, false);
                        i++;
                    }
                }else if (pointTypes.get(i) == 3) {
                    ArrayList<Point> pts = new ArrayList<Point>();
                    for (int j = 0; j < 3; j++) {
                        if(points.size() > i+j) {
                            pts.add(points.get(i + j));
                            Imgproc.circle(mRgbaT, pts.get(j), currentPointSize, anglePointColor, 8, 8, 0);
                        }
                    }
                    if (pts.size() == 3) {
                        mRgbaT = addLinesBetweenAngles(mRgbaT, pts);
                        angles = calculateAngles(pts);
                        gText = String.format("%.2f", (angles.get(angles.size() - 1)));
                        Imgproc.putText(mRgbaT, gText, pts.get(0), 3, angleTextSize, angleTextColor, 2, 1, false);
                        i++;
                        i++;
                    }
                }else if (pointTypes.get(i) == 4) {
                    ArrayList<Point> pts = new ArrayList<Point>();
                    for (int j = 0; j < 4; j++) {
                        if(points.size() > i+j) {
                            pts.add(points.get(i + j));
                            Imgproc.circle(mRgbaT, pts.get(j), currentPointSize, anglePointColor, 8, 8, 0);
                        }
                    }
                    if (pts.size() == 4) {
                        mRgbaT = addLinesBetweenAngles(mRgbaT, pts);
                        angles = calculateAngles(pts);
                        gText = String.format("%.2f", (angles.get(angles.size() - 1)));
                        Imgproc.putText(mRgbaT, gText, pts.get(0), 3, angleTextSize, angleTextColor, 2, 1, false);
                        i++;
                        i++;
                        i++;
                    }
                }
            }

            prevFrame = frameGray.clone();
            if(record){
                Imgproc.cvtColor(mRgbaT,mRgbaT, Imgproc.COLOR_RGBA2RGB);
                Bitmap bmpOut = Bitmap.createBitmap(mRgbaT.width(), mRgbaT.height(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(mRgbaT,bmpOut);
                frameCounter++;
                String format = String.format("%%0%dd", 6);
                String result = String.format(format, frameCounter);
                String outPath = directory.getAbsolutePath() + "/RealTime-" + result + ".png";
                FileOutputStream out = null;
                try {
                    out = new FileOutputStream(outPath);
                    bmpOut.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
                    realtimeBmpPaths.add(outPath);
                    // PNG is a lossless format, the compression factor (100) is ignored
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (out != null) {
                            out.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                bmpOut.recycle();
            }
            return mRgbaT;
        } else if (points.size() == 0) {
            prevFrame = inputFrame.gray().t().clone();
            Core.flip(prevFrame, prevFrame, 1);
            Imgproc.resize(prevFrame, prevFrame, prevFrame.size());
            Log.v("myTag", "no points");
            if(record){
                Imgproc.cvtColor(mRgbaT,mRgbaT, Imgproc.COLOR_RGBA2RGB);
                Bitmap bmpOut = Bitmap.createBitmap(mRgbaT.width(), mRgbaT.height(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(mRgbaT,bmpOut);
                DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd'at'HH-mm-ss");
                String eMagTime = df2.format(Calendar.getInstance().getTime());
                String outPath = directory.getAbsolutePath() + "/RealTime-" + eMagTime + ".png";
                FileOutputStream out = null;
                try {
                    out = new FileOutputStream(outPath);
                    bmpOut.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
                    realtimeBmpPaths.add(outPath);
                    // PNG is a lossless format, the compression factor (100) is ignored
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (out != null) {
                            out.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                bmpOut.recycle();
            }

            return mRgbaT;
        }
        return mRgbaT;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        linearLayout.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

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

    public void clearPoints(View view){
        Intent intent = new Intent(getApplicationContext(), realTimeTracking.class);
        startActivity(intent);
    }

    public void addFeature(View view) {
       // getPointType();
        setLinearLayoutOnTouchListener();
        selectionCounter = 0;
        if (ptType == 0) {
            selectionCap = 1;
        } else if (ptType == 1) {
            selectionCap = 2;
        } else if (ptType == 2) {
            selectionCap = 2;
        } else if (ptType == 3) {
            selectionCap = 3;
        } else if (ptType == 4){
            selectionCap = 4;
        }
    }

    private void setButtons(){

        buttonRecord = findViewById(R.id.buttonRealTimeRecord);
        buttonPoint = findViewById(R.id.buttonPointRealTime);
        buttonLine = findViewById(R.id.buttonLineRealTime);
        button2Angle = findViewById(R.id.button2AngleRealTime);
        button3Angle = findViewById(R.id.button3AngleRealTime);
        button4Angle = findViewById(R.id.button4AngleRealTime);

        buttonPoint.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                ptType = 0;
                buttonPoint.setBackgroundColor(Color.parseColor("#101010"));
                buttonPointPressed = true;
                setLinearLayoutOnTouchListener();
                buttonLine.setBackgroundColor(Color.parseColor("#999999"));
                button2Angle.setBackgroundColor(Color.parseColor("#999999"));
                button3Angle.setBackgroundColor(Color.parseColor("#999999"));
                button4Angle.setBackgroundColor(Color.parseColor("#999999"));

                buttonLinePressed = false;
                button2AnglePressed = false;
                button3AnglePressed = false;
                button4AnglePressed = false;
            }
        });
        buttonLine.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ptType = 1;
                // Code here executes on main thread after user presses button
                buttonLine.setBackgroundColor(Color.parseColor("#101010"));
                buttonLinePressed = true;
                setLinearLayoutOnTouchListener();
                buttonPoint.setBackgroundColor(Color.parseColor("#999999"));
                button2Angle.setBackgroundColor(Color.parseColor("#999999"));
                button3Angle.setBackgroundColor(Color.parseColor("#999999"));
                button4Angle.setBackgroundColor(Color.parseColor("#999999"));

                buttonPointPressed = false;
                button2AnglePressed = false;
                button3AnglePressed = false;
                button4AnglePressed = false;
            }
        });
        button2Angle.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ptType = 2;
                // Code here executes on main thread after user presses button
                button2Angle.setBackgroundColor(Color.parseColor("#101010"));
                button2AnglePressed = true;
                setLinearLayoutOnTouchListener();
                buttonPoint.setBackgroundColor(Color.parseColor("#999999"));
                buttonLine.setBackgroundColor(Color.parseColor("#999999"));
                button3Angle.setBackgroundColor(Color.parseColor("#999999"));
                button4Angle.setBackgroundColor(Color.parseColor("#999999"));

                buttonPointPressed = false;
                buttonLinePressed = false;
                button3AnglePressed = false;
                button4AnglePressed = false;
            }
        });
        button3Angle.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ptType = 3;
                // Code here executes on main thread after user presses button
                button3Angle.setBackgroundColor(Color.parseColor("#101010"));
                button3AnglePressed = true;
                setLinearLayoutOnTouchListener();
                buttonPoint.setBackgroundColor(Color.parseColor("#999999"));
                buttonLine.setBackgroundColor(Color.parseColor("#999999"));
                button2Angle.setBackgroundColor(Color.parseColor("#999999"));
                button4Angle.setBackgroundColor(Color.parseColor("#999999"));

                buttonPointPressed = false;
                buttonLinePressed = false;
                button2AnglePressed = false;
                button4AnglePressed = false;

            }
        });
        button4Angle.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ptType = 4;
                // Code here executes on main thread after user presses button
                button4Angle.setBackgroundColor(Color.parseColor("#101010"));
                button4AnglePressed = true;
                setLinearLayoutOnTouchListener();
                buttonPoint.setBackgroundColor(Color.parseColor("#999999"));
                buttonLine.setBackgroundColor(Color.parseColor("#999999"));
                button3Angle.setBackgroundColor(Color.parseColor("#999999"));
                button2Angle.setBackgroundColor(Color.parseColor("#999999"));

                buttonPointPressed = false;
                buttonLinePressed = false;
                button3AnglePressed = false;
                button2AnglePressed = false;
            }
        });

    }

    private void setLinearLayoutOnTouchListener(){
        linearLayout.setOnTouchListener(
                new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        // MotionEvent object holds X-Y values
                        if (event.getAction() == MotionEvent.ACTION_DOWN && selectionCounter < selectionCap) {
                            float x = event.getX();
                            float y = event.getY();
                            x = x * ((float) prevFrame.width() / (float) screenHeight);
                            y = y * ((float) prevFrame.height() / (float) screenWidth);
                            Point X = new Point((double) x, (double) y);
                            points.add(X);
                            pointTypes.add(ptType);
                            numPoints = points.size();
                            selectionCounter++;
                            if (selectionCounter == selectionCap) {
                                selectionCap = 0;
                                selectionCounter = 0;
                            }
                        }
                        return true;
                    }
                }
        );}

    private void setLinearLayoutOnTouchListenerReset(){
        linearLayout.setOnTouchListener(
                new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return false;
                    }
                });
    }

    public void setRecordFlag(View view){
        //TODO set icon to change when pressed as a toggle
        if(record){
            record = false;
            mediaRecorder.stop();
            mediaRecorder.release();
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    //TODO encode video
                    mergeVideoAudio();
                }
            });
        }else if(!record){
            record = true;
            DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd'at'HH-mm-ss");
            eMagTimeVideo = df2.format(Calendar.getInstance().getTime());
            beginRecordingAudio();
        }
    }

    private void beginRecordingAudio(){
        AsyncTask.execute(new Runnable(){
            @Override
            public void run() {
                //Your recording portion of the code goes here.
                mediaRecorder = new MediaRecorder();
                mediaRecorder.setAudioSource(MIC);
                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.HE_AAC);
                    mediaRecorder.setAudioEncodingBitRate(48000);
                } else {
                    mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                    mediaRecorder.setAudioEncodingBitRate(64000);
                }
                mediaRecorder.setAudioSamplingRate(16000);
                File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                File mypath = new File(directory,"TalkFrame.mp4");
                audioFileName = mypath.getAbsolutePath();
                mediaRecorder.setOutputFile(mypath.getAbsolutePath());
                try{
                    mediaRecorder.prepare();
                    mediaRecorder.start();
                }catch (IOException e) {
                    Log.e("Voice Recorder", "prepare() failed "+e.getMessage());
                }
            }
        });
    }

    private void mergeVideoAudio(){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                AndroidSequenceEncoder enc = null;
                //Your recording portion of the code goes here.
                DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd'at'HH-mm-ss");
                String eMagTime = df2.format(Calendar.getInstance().getTime());
                File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
                String outPath = directory.getAbsolutePath() + "/RealtimeTmp-" + eMagTime + ".mp4";
                tmpVid = new File(outPath);
                SeekableByteChannel out = null;
                //determine total time images were being recorded

                try {
                    out = NIOUtils.writableFileChannel(outPath);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                try {
                    Long lastmodified1;
                    Long lastmodifiedEnd;
                    File f = new File(realtimeBmpPaths.get(0));
                    lastmodified1 = f.lastModified();
                    f = new File(realtimeBmpPaths.get(realtimeBmpPaths.size()-1));
                    lastmodifiedEnd = f.lastModified();
                    long recordTime = lastmodifiedEnd - lastmodified1;
                    double fps = (double)realtimeBmpPaths.size() / (double)(recordTime / 1000);
                    enc = new AndroidSequenceEncoder(out, Rational.R((int)(Math.round(fps)),1));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //build initial image based video
                if (enc != null) {
                    for (int i = 0; i < realtimeBmpPaths.size(); i++) {
                        //Bitmap writeBmp;
                        try {
                            Bitmap writeBmp = BitmapFactory.decodeFile(Uri.parse(realtimeBmpPaths.get(i)).toString());
                            if(writeBmp != null) {
                                enc.encodeImage(writeBmp);
                            }
                            File f = new File(realtimeBmpPaths.get(i));
                            f.delete();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        enc.finish();
                        NIOUtils.closeQuietly(out);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                //add audio
                realTimeTracking.MediaMultiplexer mm = new realTimeTracking.MediaMultiplexer();
                mm.startMuxing(getApplicationContext());
            }
        });
    }

    public class MediaMultiplexer {
        private static final int MAX_SAMPLE_SIZE = 8 * 2880 * 2880;

        public void startMuxing(Context context) {
            MediaMuxer muxer = null;
            MediaFormat VideoFormat = null;
            File d = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
            String Path = d.getAbsolutePath() + "/Realtime-" + eMagTimeVideo + ".mp4";
            outputVideoFileName = Path;
            try {
                muxer = new MediaMuxer(outputVideoFileName, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            } catch (IOException e) {
                e.printStackTrace();
            }
            MediaExtractor extractorVideo = new MediaExtractor();
            try {
                extractorVideo.setDataSource(tmpVid.toString());
                int tracks = extractorVideo.getTrackCount();
                for (int i = 0; i < tracks; i++) {
                    MediaFormat mf = extractorVideo.getTrackFormat(i);
                    String mime = mf.getString(MediaFormat.KEY_MIME);
                    if (mime.startsWith("video/")) {
                        extractorVideo.selectTrack(i);
                        VideoFormat = extractorVideo.getTrackFormat(i);
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            MediaExtractor extractorAudio = new MediaExtractor();
            try {
                extractorAudio.setDataSource(audioFileName);
                int tracks = extractorAudio.getTrackCount();
                extractorAudio.selectTrack(0);

                MediaFormat AudioFormat = extractorAudio.getTrackFormat(0);
                int audioTrackIndex = muxer.addTrack(AudioFormat);
                int videoTrackIndex = muxer.addTrack(VideoFormat);

                boolean sawEOS = false;
                boolean sawAudioEOS = false;
                int bufferSize = MAX_SAMPLE_SIZE;
                ByteBuffer dstBuf = ByteBuffer.allocate(bufferSize);
                int offset = 100;
                MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                muxer.start();

                while (!sawEOS) {
                    bufferInfo.offset = offset;
                    bufferInfo.size = extractorVideo.readSampleData(dstBuf, offset);
                    if (bufferInfo.size < 0) {
                        sawEOS = true;
                        bufferInfo.size = 0;
                    } else {
                        bufferInfo.presentationTimeUs = extractorVideo.getSampleTime();
                        bufferInfo.flags = extractorVideo.getSampleFlags();
                        int trackIndex = extractorVideo.getSampleTrackIndex();
                        muxer.writeSampleData(videoTrackIndex, dstBuf, bufferInfo);
                        extractorVideo.advance();
                    }
                }
                ByteBuffer audioBuf = ByteBuffer.allocate(bufferSize);
                while (!sawAudioEOS) {
                    bufferInfo.offset = offset;
                    bufferInfo.size = extractorAudio.readSampleData(audioBuf, offset);
                    if (bufferInfo.size < 0) {
                        sawAudioEOS = true;
                        bufferInfo.size = 0;
                    } else {
                        bufferInfo.presentationTimeUs = extractorAudio.getSampleTime();
                        bufferInfo.flags = extractorAudio.getSampleFlags();
                        int trackIndex = extractorAudio.getSampleTrackIndex();
                        muxer.writeSampleData(audioTrackIndex, audioBuf, bufferInfo);
                        extractorAudio.advance();
                    }
                }
                muxer.stop();
                muxer.release();
                tmpVid.delete();
                updateTapToPlay();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }

    }

    private void updateTapToPlay(){
        Intent intent = new Intent(getApplicationContext(), playVideo.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        File file = new File(outputVideoFileName);
        Uri vidUriActual = Uri.fromFile(file);
        intent.putExtra("vidUri",vidUriActual.toString());
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        MainActivity.mBuilder.setContentIntent(pendingIntent);
        MainActivity.mBuilder.setContentText("Processing completed! Click to play.");
        MainActivity.notificationManager.notify(MainActivity.notificationID, MainActivity.mBuilder.build());
    }
}
