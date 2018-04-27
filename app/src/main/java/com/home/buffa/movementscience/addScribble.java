package com.home.buffa.movementscience;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Scalar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

public class addScribble extends Activity {

    DrawingView dv;
    private Paint mPaint;
    Bitmap bmp;
    String pathKeyFrame;
    String scribClr;
    int scribbleWeight;

    int origHeight;
    int origWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_scribble);
        FrameLayout fl = (FrameLayout) findViewById(R.id.frameLayout);
        dv = new DrawingView(this);
        dv.setDrawingCacheEnabled(true);
        fl.addView(dv);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        scribClr = sharedPref.getString("pref_scribbleColor","r");

        String scribWt = sharedPref.getString("pref_scribbleWeight","5");
        scribbleWeight = Integer.valueOf(scribWt);


        Intent intentReceive = getIntent();
        pathKeyFrame = intentReceive.getExtras().getString("keyFramePathString");

        Button b2 = new Button(this);
        b2.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                saveDrawing();
                Intent intent = new Intent(getApplicationContext(), keyFrame.class);
                intent.putExtra("keyFramePathString",pathKeyFrame);
                startActivity(intent);
            }
        });
        b2.setText("Save Scribble");
        b2.setBackgroundColor(Color.TRANSPARENT);
        b2.setTextColor(Color.RED);
        LinearLayout.LayoutParams b2p = new LinearLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        b2p.weight = 1.0f;
        b2p.gravity= Gravity.TOP;
        b2.setLayoutParams(b2p);

        Button bHome = new Button(this);
        bHome.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(getApplicationContext(), offlineProcessing.class);
                startActivity(intent);
            }
        });
        bHome.setText("Return to Offline Processing");
        bHome.setBackgroundColor(Color.TRANSPARENT);
        bHome.setTextColor(Color.RED);
        LinearLayout.LayoutParams bHomep = new LinearLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        bHomep.weight = 1.0f;
        bHomep.gravity= Gravity.TOP;
        bHomep.rightMargin = 8;
        bHomep.leftMargin = 700;

        bHome.setLayoutParams(bHomep);


        fl.addView(b2);
        fl.addView(bHome);



        File imageKeyFrame = new File(pathKeyFrame);
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmp = BitmapFactory.decodeFile(imageKeyFrame.getAbsolutePath(),bmOptions);
        origHeight = bmp.getHeight();
        origWidth = bmp.getWidth();
        dv.setBackgroundDrawable(new BitmapDrawable(bmp));
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        if (Objects.equals(scribClr,new String("r")) == true){
            mPaint.setColor(Color.RED);
        }else if(Objects.equals(scribClr,new String("g")) == true){
            mPaint.setColor(Color.GREEN);
        }else if(Objects.equals(scribClr,new String("b")) == true) {
            mPaint.setColor(Color.BLUE);
        }else if(Objects.equals(scribClr,new String("y")) == true) {
            mPaint.setColor(Color.YELLOW);
        }else if(Objects.equals(scribClr,new String("c")) == true) {
            mPaint.setColor(Color.CYAN);
        }else if(Objects.equals(scribClr,new String("k")) == true) {
            mPaint.setColor(Color.BLACK);
        }else if(Objects.equals(scribClr,new String("w")) == true) {
            mPaint.setColor(Color.WHITE);
        }
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(scribbleWeight);
    }

    public class DrawingView extends View {

        public int width;
        public  int height;
        private Bitmap mBitmap;
        private Canvas mCanvas;
        private Path mPath;
        private Paint mBitmapPaint;
        Context context;
        private Paint circlePaint;
        private Path circlePath;

        public DrawingView(Context c) {
            super(c);
            context=c;
            mPath = new Path();
            mBitmapPaint = new Paint(Paint.DITHER_FLAG);
            circlePaint = new Paint();
            circlePath = new Path();
            circlePaint.setAntiAlias(true);
            if (Objects.equals(scribClr,new String("r")) == true){
                circlePaint.setColor(Color.RED);
            }else if(Objects.equals(scribClr,new String("g")) == true){
                circlePaint.setColor(Color.GREEN);
            }else if(Objects.equals(scribClr,new String("b")) == true) {
                circlePaint.setColor(Color.BLUE);
            }else if(Objects.equals(scribClr,new String("y")) == true) {
                circlePaint.setColor(Color.YELLOW);
            }else if(Objects.equals(scribClr,new String("c")) == true) {
                circlePaint.setColor(Color.CYAN);
            }else if(Objects.equals(scribClr,new String("k")) == true) {
                circlePaint.setColor(Color.BLACK);
            }else if(Objects.equals(scribClr,new String("w")) == true) {
                circlePaint.setColor(Color.WHITE);
            }
            circlePaint.setStyle(Paint.Style.STROKE);
            circlePaint.setStrokeJoin(Paint.Join.MITER);
            circlePaint.setStrokeWidth(scribbleWeight);
            setDrawingCacheEnabled(true);

        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);

            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            canvas.drawBitmap( mBitmap, 0, 0, mBitmapPaint);
            canvas.drawPath( mPath,  mPaint);
            canvas.drawPath( circlePath,  circlePaint);
        }

        private float mX, mY;
        private static final float TOUCH_TOLERANCE = 4;

        private void touch_start(float x, float y) {
            mPath.reset();
            mPath.moveTo(x, y);
            mX = x;
            mY = y;
        }

        private void touch_move(float x, float y) {
            float dx = Math.abs(x - mX);
            float dy = Math.abs(y - mY);
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
                mX = x;
                mY = y;

                circlePath.reset();
                circlePath.addCircle(mX, mY, 30, Path.Direction.CW);
            }
        }

        private void touch_up() {
            mPath.lineTo(mX, mY);
            circlePath.reset();
            // commit the path to our offscreen
            mCanvas.drawPath(mPath,  mPaint);
            // kill this so we don't double draw
            mPath.reset();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touch_start(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    touch_move(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    touch_up();
                    invalidate();
                    break;
            }
            return true;
        }

    }

    public void saveDrawing()
    {
        Bitmap whatTheUserDrewBitmap = dv.getDrawingCache();

        // don't forget to clear it (see above) or you just get duplicates

//        // almost always you will want to reduce res from the very high screen res
//        whatTheUserDrewBitmap = ThumbnailUtils.extractThumbnail(whatTheUserDrewBitmap, 256, 256);
//        // NOTE that's an incredibly useful trick for cropping/resizing squares
//        // while handling all memory problems etc
//        // http://stackoverflow.com/a/17733530/294884

        // you can now save the bitmap to a file, or display it in an ImageView:
        //putOverlay(bmp,whatTheUserDrewBitmap);
        bmp = Bitmap.createScaledBitmap(whatTheUserDrewBitmap,origWidth,origHeight,false);
        pathKeyFrame = createImageFromBitmapScribble(bmp);
    }

    public void putOverlay(Bitmap bitmap, Bitmap overlay) {
//        Canvas canvas = new Canvas(bitmap);
//        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
//        canvas.drawBitmap(overlay, 0, 0, paint);
        Bitmap bmOverlay = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),bitmap.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(bitmap, new Matrix(), null);
        canvas.drawBitmap(overlay, new Matrix(), null);
        bmp = bmOverlay;
    }

    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
//            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, mLoaderCallback);
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
//            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    public String createImageFromBitmapScribble(Bitmap bitmap){
//        ContextWrapper cw = new ContextWrapper(getApplicationContext());
//        // path to /data/data/yourapp/app_data/imageDir
//        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);

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



}
