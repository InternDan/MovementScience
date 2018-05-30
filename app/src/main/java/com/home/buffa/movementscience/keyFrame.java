package com.home.buffa.movementscience;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;

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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class keyFrame extends Activity {

    Bitmap bmp;
    Mat m;
    ImageView keyFrameView;
    String pathKeyFrame;

    Spinner pointDropdown;
    ArrayList<Point> points = new ArrayList<>();
    ArrayList<Integer> ptTypes = new ArrayList<>();
    ArrayList<Double> angles = new ArrayList<Double>();
    String gText;
    Scalar anglePointColor;
    Scalar angleLineColor;
    Scalar angleTextColor;
    Scalar currentPointColor;
    int currentPointSize;
    int angleTextSize;
    int rotateDegreesPostProcess;

    int selectionCap;
    int selectionCounter;

    String coordType = null;
    String coords = null;
    String pathKeyFrameOut;

    Point X;
    Integer ptType;

    String text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_key_frame);
        keyFrameView = (ImageView)findViewById(R.id.imageViewKeyFrame);

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

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

        pointDropdown = findViewById(R.id.spinnerOfflineKeyFrame);

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
                    pathKeyFrameOut = "Edited-" + pathKeyFrame;
                    bmp = BitmapFactory.decodeFile(imageKeyFrame.getAbsolutePath());
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



    public void addScribble(){
        Intent intentPassAddScribble = new Intent(this,addScribble.class);
        pathKeyFrameOut = createImageFromBitmap(bmp);
        intentPassAddScribble.putExtra("keyFramePathString",pathKeyFrameOut);
        startActivity(intentPassAddScribble);
    }

    public void rotate90Degrees(View view){
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bmp,bmp.getWidth(),bmp.getHeight(),true);
        bmp = Bitmap.createBitmap(scaledBitmap , 0, 0, scaledBitmap .getWidth(), scaledBitmap .getHeight(), matrix, true);
        pathKeyFrameOut = createImageFromBitmap(bmp);
        keyFrameView.setImageBitmap(bmp);
    }

    public String createImageFromBitmap(Bitmap bitmap){
        File mypath=new File(pathKeyFrame);
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
        return pathKeyFrame;

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // MotionEvent object holds X-Y values
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
            m = new Mat(height, width, CvType.CV_8UC3);
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
            Imgproc.cvtColor(m,m, Imgproc.COLOR_RGBA2RGB);
            Utils.matToBitmap(m,bmp);
            pathKeyFrameOut = createImageFromBitmap(bmp);
            keyFrameView.setImageBitmap(bmp);
            keyFrameView.requestFocus();
        }
        return super.onTouchEvent(event);
    }

    public void addFeature(View view) {
        ptType = pointDropdown.getSelectedItemPosition();
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
        if (ptType == 5){
            addScribble();
        }
        if (ptType == 6){
            getTextAndAdd();
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

    private void getTextAndAdd(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Title");

// Set up the input
        final EditText input = new EditText(this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
        builder.setView(input);

// Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                text = input.getText().toString();
                Intent intentPassAddText = new Intent(getApplicationContext(),addTextBox.class);
                pathKeyFrameOut = createImageFromBitmap(bmp);
                intentPassAddText.putExtra("keyFramePathString",pathKeyFrame);
                intentPassAddText.putExtra("keyFrameText",text);
                startActivity(intentPassAddText);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    public void shareImage(View view){
        File fShare = new File(pathKeyFrameOut);
        Uri imgUri = Uri.fromFile(fShare);
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("image/*");
        sharingIntent.putExtra(Intent.EXTRA_STREAM,imgUri);
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }

    public void goHome(View view){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

}
