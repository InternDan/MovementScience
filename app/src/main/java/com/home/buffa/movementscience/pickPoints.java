package com.home.buffa.movementscience;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.DrawableRes;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.ipaulpro.afilechooser.utils.FileUtils;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import com.home.buffa.movementscience.SimpleImageArrayAdapter;

public class pickPoints extends Activity {

    ImageView imageViewPicker;
    static Intent intentPass = new Intent();
    static String coords = null;
    String coordType = null;
    static String vidPath;
    static int firstFramePosition;
    Uri videoUri;
    String path;
    Point X;
    Uri imageUri;
    Bitmap bmp;
    Mat m;
    int frameRows;
    int frameCols;
    int selectionCounter = 0;
    int selectionCap = 0;
    File image;
    View mDecorView;
    String videoAbsolutePath;
    int currentPointSize;
    Scalar currentPointColor;
    Integer ptType;
    ArrayList<Point> points = new ArrayList<>();
    ArrayList<Integer> ptTypes = new ArrayList<>();
    ArrayList<Double> angles = new ArrayList<Double>();
    String gText;
    Scalar anglePointColor;
    Scalar angleLineColor;
    Scalar angleTextColor;
    int angleTextSize;
    String vidPathPass;
    int rotateDegreesPostProcess;
    LinearLayout linearLayout;

    // Declaring the String Array with the Text Data for the Spinners
    String[] objects = {"  Select a Feature to Track  ",
                        "Point  ",
                        "2-Point Line  ",
                        "2-Point Angle  ",
                        "3-Point Angle  ",
                        "4-Point Angle  " };
    // Declaring the Integer Array with resourse Id's of Images for the Spinners
    Integer[] objectsImages = { 0, R.mipmap.point, R.mipmap.line, R.mipmap.two_point_angle,
            R.mipmap.three_point_angle, R.mipmap.four_point_angle };
    Spinner spinner;



    //ImageButton buttonPoint;
    //ImageButton buttonLine;
    //ImageButton button2Angle;
    //ImageButton button3Angle;
    //ImageButton button4Angle;

    boolean buttonPointPressed = false;
    boolean buttonLinePressed = false;
    boolean button2AnglePressed = false;
    boolean button3AnglePressed = false;
    boolean button4AnglePressed = false;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("OpenCV", "OpenCV loaded successfully");

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
        setContentView(R.layout.activity_pick_points);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        String curPtSize = sharedPref.getString("pref_currentPointSize","5");
        currentPointSize = Integer.valueOf(curPtSize);

        spinner = (Spinner) findViewById(R.id.spinner1);
        SimpleImageArrayAdapter adapter = new
                SimpleImageArrayAdapter(getApplicationContext(),R.layout.spinner_value_layout,objects,objectsImages);
        spinner.setAdapter(adapter);

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

        String rotDeg = sharedPref.getString("pref_rotateDegreesPostProcess","90");
        rotateDegreesPostProcess = Integer.valueOf(rotDeg);


        Intent intentReceive = getIntent();
        path = intentReceive.getExtras().getString("firstFramePathString");
        File image2 = new File(path);
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        try {
            bmp = MediaStore.Images.Media.getBitmap(this.getContentResolver(),Uri.fromFile(image2));
        } catch (IOException e) {
            e.printStackTrace();
        }
        frameRows = bmp.getHeight();
        frameCols = bmp.getWidth();
        m = new Mat(frameRows, frameCols, CvType.CV_8UC1);
        coords = null;
        videoAbsolutePath = intentReceive.getExtras().getString("videoAbsolutePath");
        videoUri = Uri.parse(videoAbsolutePath);
//        int firstFramePosition = 0;
        imageViewPicker = (ImageView) findViewById(R.id.imageViewPicker);
        //get first frame bitmap
        frameRows = bmp.getHeight();
        frameCols = bmp.getWidth();
        imageViewPicker.setImageBitmap(bmp);
        imageViewPicker.requestFocus();
//        hideSystemUI();

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

        linearLayout = findViewById(R.id.linearLayoutPickPoints);
        //setButtons();
        setLinearLayoutOnTouchListenerReset();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // MotionEvent object holds X-Y values

        linearLayout.onTouchEvent(event);
        return super.onTouchEvent(event);

    }


    public void trackPoints(View view){
        if (coords == null){
            Intent intent = new Intent(this, pickPoints.class);
            Toast.makeText(this, "Please select some points first!", Toast.LENGTH_LONG).show();
            intent.putExtra("firstFramePathString", path);
            intent.putExtra("videoAbsolutePath", videoAbsolutePath);
            intent.putExtra("videoPath", vidPathPass);
            startActivity(intent);
            return;
        }
        Intent intentPass = new Intent(this,trackPointsOffline.class);
        intentPass.putExtra("coordinates",coords);
        intentPass.putExtra("coordinateTypes",coordType);
        intentPass.putExtra("videoPath",videoUri.toString());
        intentPass.putExtra("firstFramePosition",firstFramePosition);
        intentPass.putExtra("firstFramePathString",path);
        intentPass.putExtra("videoAbsolutePath",videoAbsolutePath);
        intentPass.putExtra("frameHeight",frameRows);
        intentPass.putExtra("frameWidth",frameCols);
        startActivity(intentPass);
    }

    public void addFeature(View view) {
        setLinearLayoutOnTouchListener();
        switch(spinner.getSelectedItemPosition()){
            case 0:
                ptType = null;
                break;
            case 1:
                ptType = 0;
                break;
            case 2:
                ptType = 1;
                break;
            case 3:
                ptType = 2;
                break;
            case 4:
                ptType = 3;
                break;
            case 5:
                ptType = 4;
                break;
        }
        selectionCounter = 0;
        if (ptType == null){
            selectionCap = 0;
            return;
        }
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

    /*public void onClick(View view){
        switch(spinner.getSelectedItemPosition()){
            case 1:
                buttonPointPressed = true;
                break;
            case 2:
                buttonLinePressed = true;
                break;
            case 3:
                button2AnglePressed = true;
                break;
            case 4:
                button3AnglePressed = true;
                break;
            case 5:
                button4AnglePressed = true;
                break;
        }
    }/*
    /*private void setButtons(){

        buttonPoint = findViewById(R.id.buttonPointOffline);
        buttonLine = findViewById(R.id.buttonLineOffline);
        button2Angle = findViewById(R.id.button2AngleOffline);
        button3Angle = findViewById(R.id.button3AngleOffline);
        button4Angle = findViewById(R.id.button4AngleOffline);

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

    }*/

    private void setLinearLayoutOnTouchListener(){
        linearLayout.setOnTouchListener(
                new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if(event.getAction() == MotionEvent.ACTION_DOWN && selectionCounter < selectionCap) {
                            coordType = coordType + "," + ptType + "," + ptType;
                            selectionCounter++;

                            int height = bmp.getHeight();
                            int width = bmp.getWidth();
                            Display display = getWindowManager().getDefaultDisplay();
                            DisplayMetrics metrics = new DisplayMetrics();
                            display.getRealMetrics(metrics);
                            int heightScreen = metrics.heightPixels;
                            int widthScreen = metrics.widthPixels;

                            float scaleHeight = (float) height / (float) heightScreen;
                            float scaleWidth = (float) width / (float) widthScreen;

                            float x = event.getX() * scaleWidth;
                            float y = event.getY() * scaleHeight;

                            coords = coords + "," + x + "," + y;

                            X = new Point((double) x, (double) y);
                            points.add(X);
                            ptTypes.add(ptType);
                            //convert imageUri to Mat and draw this
                            Utils.bitmapToMat(bmp, m);
                            if (selectionCounter == selectionCap){
                                selectionCap = 0;
                                selectionCounter = 0;
                            }
                            for (int i = 0; i<points.size(); i++) {
                                if (ptTypes.get(i) == 0) {
                                    Imgproc.circle(m, points.get(i), currentPointSize, currentPointColor, 8, 8, 0);
                                } else if (ptTypes.get(i) == 1) {
                                    ArrayList<Point> pts = new ArrayList<>();
                                    for (int j = 0; j < 2; j++) {
                                        if (points.size() > i + j) {
                                            pts.add(points.get(i + j));
                                            Imgproc.circle(m, pts.get(j), currentPointSize, anglePointColor, 8, 8, 0);
                                        }
                                        if (pts.size() == 2) {
                                            m = addLinesBetweenAngles(m, pts);
                                        }
                                    }
                                    i++;
                                } else if (ptTypes.get(i) == 2) {
                                    ArrayList<Point> pts = new ArrayList<Point>();
                                    for (int j = 0; j < 2; j++) {
                                        if (points.size() > i + j) {
                                            pts.add(points.get(i + j));
                                            Imgproc.circle(m, pts.get(j), currentPointSize, anglePointColor, 8, 8, 0);
                                        }
                                    }
                                    if (pts.size() == 2) {
                                        m = addLinesBetweenAngles(m, pts);
                                        angles = calculateAngles(pts);
                                        gText = String.format("%.2f", (angles.get(angles.size() - 1)));
                                        Imgproc.putText(m, gText, pts.get(0), 3, angleTextSize, angleTextColor, 2, 1, false);
                                        i++;
                                    }
                                } else if (ptTypes.get(i) == 3) {
                                    ArrayList<Point> pts = new ArrayList<Point>();
                                    for (int j = 0; j < 3; j++) {
                                        if (points.size() > i + j) {
                                            pts.add(points.get(i + j));
                                            Imgproc.circle(m, pts.get(j), currentPointSize, anglePointColor, 8, 8, 0);
                                        }
                                    }
                                    if (pts.size() == 3) {
                                        m = addLinesBetweenAngles(m, pts);
                                        angles = calculateAngles(pts);
                                        gText = String.format("%.2f", (angles.get(angles.size() - 1)));
                                        Imgproc.putText(m, gText, pts.get(0), 3, angleTextSize, angleTextColor, 2, 1, false);
                                        i++;
                                        i++;
                                    }
                                } else if (ptTypes.get(i) == 4) {
                                    ArrayList<Point> pts = new ArrayList<Point>();
                                    for (int j = 0; j < 4; j++) {
                                        if (points.size() > i + j) {
                                            pts.add(points.get(i + j));
                                            Imgproc.circle(m, pts.get(j), currentPointSize, anglePointColor, 8, 8, 0);
                                        }
                                    }
                                    if (pts.size() == 4) {
                                        m = addLinesBetweenAngles(m, pts);
                                        angles = calculateAngles(pts);
                                        gText = String.format("%.2f", (angles.get(angles.size() - 1)));
                                        Imgproc.putText(m, gText, pts.get(0), 3, angleTextSize, angleTextColor, 2, 1, false);
                                        i++;
                                        i++;
                                        i++;
                                    }
                                }
                            }
                            Utils.matToBitmap(m,bmp);
                            imageViewPicker.setImageBitmap(bmp);
                            imageViewPicker.requestFocus();
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


    public void onResume() {
        super.onResume();
        coords = null;
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

        /*Intent intentPass = new Intent(getApplicationContext(), pickPoints.class);
        intentPass.putExtra("firstFramePathString",path);
        intentPass.putExtra("videoAbsolutePath",videoAbsolutePath);
        startActivity(intentPass);*/
    }
}
