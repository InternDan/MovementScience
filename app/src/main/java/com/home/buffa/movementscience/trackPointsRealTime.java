package com.home.buffa.movementscience;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
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

import java.util.ArrayList;
import java.util.Objects;

import static org.opencv.core.TermCriteria.COUNT;

//import static org.opencv.android.CameraBridgeViewBase.cHeight;
//import static org.opencv.android.CameraBridgeViewBase.cWidth;

public class trackPointsRealTime extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "realTimeTracking";
    private CameraBridgeViewBase mOpenCvCameraView;

    int screenHeight;
    int screenWidth;
    ArrayList<Point> points = new ArrayList<Point>();
    int numPoints;
    int numTrailPoints;
    int currentPointSize;
    int trailPointSize;
    Scalar currentPointColor;
    Scalar trailPointColor;
    Mat prevFrame;
    Mat frameGray;
    ArrayList<ArrayList> allPoints = new ArrayList<>();

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
        setContentView(R.layout.activity_track_points_real_time);
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

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        String numTrPts = sharedPref.getString("pref_trailingPointNumber","20");
        numTrailPoints = Integer.valueOf(numTrPts);

        String trPtSize = sharedPref.getString("pref_trailingPointSize","5");
        trailPointSize = Integer.valueOf(trPtSize);

        String curPtSize = sharedPref.getString("pref_currentPointSize","5");
        currentPointSize = Integer.valueOf(curPtSize);

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

        display.getRealSize(size);
        screenHeight = size.x;
        screenWidth = size.y;
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

            Size winSize = new Size(20, 20);
            TermCriteria termCrit = new TermCriteria(COUNT, 20, 0.1);
            points.clear();
            Video.calcOpticalFlowPyrLK(prevFrame, frameGray, prevFeatures, nextFeatures, status, err, winSize, 3, termCrit, 0, 0.001);
            points.addAll(nextFeatures.toList());

            if (allPoints.size() < numTrailPoints){
                allPoints.add((ArrayList) points.clone());
            }
            if (allPoints.size() == numTrailPoints){
                allPoints.remove(0);
                allPoints.add((ArrayList) points.clone());
            }


            for (int i = 0; i < allPoints.size(); i++) {
                ArrayList<Point> pts = allPoints.get(i);
                for (int j = 0; j < pts.size(); j++) {
                    if (i == allPoints.size()-1) {
                        Imgproc.circle(mRgbaT, pts.get(j), currentPointSize, currentPointColor, 10, 10, 0);
                    } else{
                        Imgproc.circle(mRgbaT, pts.get(j), trailPointSize, trailPointColor, 10, 10, 0);
                    }
                }
//                pts.clear();
            }

            prevFrame = frameGray.clone();
            return mRgbaT;
        } else if (points.size() == 0) {
            prevFrame = inputFrame.gray().t().clone();
            Core.flip(prevFrame, prevFrame, 1);
            Imgproc.resize(prevFrame, prevFrame, prevFrame.size());
            Log.v("myTag", "no points");
            return mRgbaT;
        } else {
            Log.v("myTag", "somehow missed both");
            return mRgbaT;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        // MotionEvent object holds X-Y values
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();
            x = x * ((float) prevFrame.width() / (float) screenHeight);
            y = y * ((float) prevFrame.height() / (float) screenWidth);
            Point X = new Point((double) x, (double) y);
            points.add(X);
            numPoints = points.size();
        }
        return super.onTouchEvent(event);
    }

    public void resetPoints(View view){//mildly unstable
//        allPoints.clear();
//        points.clear();
        Intent intent = new Intent(getApplicationContext(), trackPointsRealTime.class);
        startActivity(intent);
    }

    public void goHome(View view){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }
}
