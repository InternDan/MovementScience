package com.home.buffa.movementscience;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class addAngle extends Activity {

    String pathKeyFrame;
    Bitmap bmp;
    Mat m;
    ImageView keyFrameView;
    int currentPointSize;
    int angleTextSize;
    Scalar currentPointColor;
    Scalar anglePointColor;
    Scalar angleLineColor;
    Scalar angleTextColor;
    ArrayList<Point> points = new ArrayList<>();
    int textFlag = 0;
    Mat matOrig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_add_angle);
        keyFrameView = (ImageView)findViewById(R.id.imageViewKeyFrameAngle);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        String curPtSize = sharedPref.getString("pref_currentPointSize","5");
        currentPointSize = Integer.valueOf(curPtSize);

        String txtBoxTxtSize = sharedPref.getString("pref_angleTextSize","5");
        angleTextSize = Integer.valueOf(txtBoxTxtSize);

        String angPtClr = sharedPref.getString("pref_anglePointColor","r");
        if (Objects.equals(angPtClr,new String("r")) == true){
            anglePointColor = new Scalar(255,0,0);
        }else if(Objects.equals(angPtClr,new String("g")) == true){
            anglePointColor = new Scalar(0,255,0);
        }else if(Objects.equals(angPtClr,new String("b")) == true) {
            anglePointColor = new Scalar(0, 0, 255);
        }else if(Objects.equals(angPtClr,new String("y")) == true) {
            anglePointColor = new Scalar(0, 255, 255);
        }else if(Objects.equals(angPtClr,new String("c")) == true) {
            anglePointColor = new Scalar(255, 255, 0);
        }else if(Objects.equals(angPtClr,new String("k")) == true) {
            anglePointColor = new Scalar(0, 0, 0);
        }else if(Objects.equals(angPtClr,new String("w")) == true) {
            anglePointColor = new Scalar(255, 255, 255);
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
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // MotionEvent object holds X-Y values
        if(event.getAction() == MotionEvent.ACTION_DOWN) {

            int height = bmp.getHeight();
            int width = bmp.getWidth();
            Display display = getWindowManager().getDefaultDisplay();
            android.graphics.Point size = new android.graphics.Point();
            display.getRealSize(size);
            int heightScreen = size.y;
            int widthScreen = size.x;

            float scaleHeight = (float) height / (float) heightScreen;
            float scaleWidth = (float) width / (float) widthScreen;

            float x = event.getX() * scaleWidth;
            float y = event.getY() * scaleHeight;

            Point X = new Point((double) x, (double) y);
            //convert imageUri to Mat and draw this
            if (textFlag == 0) {
                Utils.bitmapToMat(bmp, m);
                matOrig = m.clone();
            }
            if (textFlag == 1){
                m = matOrig.clone();
            }
            points.add(X);
            Imgproc.cvtColor(m,m, Imgproc.COLOR_RGBA2RGB);
            for (int i = 0; i < points.size(); i++) {
                Imgproc.circle(m, points.get(i), currentPointSize, anglePointColor, 8);//*Math.round((scaleHeight+scaleWidth)/(float) 2)
            }
            m = addLinesBetweenAngles(m);
            if (points.size() > 1) {
                ArrayList<Double> angles = calculateAngles(points);
                String gText = String.format("%.2f", (angles.get(angles.size() - 1)));
                if (gText != null) {
                    Imgproc.putText(m, gText, points.get(0), 2, angleTextSize, angleTextColor, 2, 1, false);
                    textFlag = 1;
                }
            }
            Imgproc.cvtColor(m,m, Imgproc.COLOR_RGB2RGBA);
            Utils.matToBitmap(m, bmp);
            keyFrameView.setImageBitmap(bmp);
            keyFrameView.requestFocus();

        }
        return super.onTouchEvent(event);
    }

    public ArrayList<Double> calculateAngles(ArrayList<Point> points){
        ArrayList<Double> angles = new ArrayList<Double>();
        int numPoints = points.size();
        if (numPoints == 2) {
            angles = nonlinearMath.twoPointGlobalAngle(points);
        }
        if (numPoints == 3) {
            angles = nonlinearMath.threePointSegmentAngle(points);
        }
        if (numPoints == 4) {
            angles = nonlinearMath.fourPointSegmentAngle(points);
        }
        return angles;
    }

    public void saveChanges(View view){
        pathKeyFrame = createImageFromBitmapKey(bmp);
        Intent intentPassKeyFrame = new Intent(this,keyFrame.class);
        intentPassKeyFrame.putExtra("keyFramePathString",pathKeyFrame);
        startActivity(intentPassKeyFrame);
    }

    public String createImageFromBitmapKey(Bitmap bitmap){
//
        // Create imageDir
        File mypath = new File(pathKeyFrame);


        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(pathKeyFrame))));
        return pathKeyFrame;

    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("OpenCV", "OpenCV loaded successfully");
                    Intent intentReceive = getIntent();
                    pathKeyFrame = intentReceive.getExtras().getString("keyFramePathString");
                    File imageKeyFrame = new File(pathKeyFrame);
                    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                    bmp = BitmapFactory.decodeFile(imageKeyFrame.getAbsolutePath(),bmOptions);
                    m = new Mat();
                    keyFrameView.setImageBitmap(bmp);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
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
}
