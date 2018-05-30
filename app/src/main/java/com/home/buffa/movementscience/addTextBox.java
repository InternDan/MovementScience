package com.home.buffa.movementscience;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.app.Activity;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class addTextBox extends Activity {

    String text;
    String pathKeyFrame;
    Bitmap bmp;
    Bitmap bmpOut;
    Canvas canvas;
    Paint paint;
    ImageView imageViewTextBox;

    String textColor;
    Integer textSize;

    float scaleHeight;
    float scaleWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_text_box);

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        Intent intentReceive = getIntent();
        pathKeyFrame = intentReceive.getExtras().getString("keyFramePathString");
        text = intentReceive.getExtras().getString("keyFrameText");

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        textColor = sharedPref.getString("pref_textBoxTextColor","r");
        String textSizeStr = sharedPref.getString("pref_trailingPointNumber","20");
        textSize = Integer.valueOf(textSizeStr);


        File imageKeyFrame = new File(pathKeyFrame);
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmp = BitmapFactory.decodeFile(imageKeyFrame.getAbsolutePath(),bmOptions);
        imageViewTextBox = findViewById(R.id.imageViewTextBox);

        int height = bmp.getHeight();
        int width = bmp.getWidth();
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getRealMetrics(metrics);
        int heightScreen = metrics.heightPixels;
        int widthScreen = metrics.widthPixels;

        scaleHeight = (float) height / (float) heightScreen;
        scaleWidth = (float) width / (float) widthScreen;
        //get first frame bitmap
        imageViewTextBox.setImageBitmap(bmp);
        if (text != null) {
            bmpOut = writeTextOnDrawable(bmp, text, 0, 0);
            imageViewTextBox.setImageBitmap(bmpOut);
        }

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

    @Override
    public boolean onTouchEvent(MotionEvent event){

        Point pt = new Point( (int)Math.round(event.getX() * scaleWidth),(int)Math.round(event.getY() * scaleHeight));
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            bmpOut = writeTextOnDrawable(bmp,text,pt.x,pt.y);
            imageViewTextBox.setImageBitmap(bmpOut);
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            pt = new Point( (int)Math.round(event.getX() * scaleWidth),(int)Math.round(event.getY() * scaleHeight));
            bmpOut = writeTextOnDrawable(bmp,text,pt.x,pt.y);
            imageViewTextBox.setImageBitmap(bmpOut);
        } else if (event.getAction() == MotionEvent.ACTION_UP) {

        }
        return true;
    }


    private Bitmap writeTextOnDrawable(Bitmap bmp, String text, int xPos, int yPos) {

        Typeface tf = Typeface.create("Helvetica", Typeface.BOLD);

        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        if (textColor.contains("r")) {
            paint.setColor(Color.RED);
        }else if(textColor.contains("g")) {
            paint.setColor(Color.GREEN);
        }else if(textColor.contains("b")) {
            paint.setColor(Color.BLUE);
        }else if(textColor.contains("y")) {
            paint.setColor(Color.YELLOW);
        }else if(textColor.contains("c")) {
            paint.setColor(Color.CYAN);
        }else if(textColor.contains("k")) {
            paint.setColor(Color.BLACK);
        }else if(textColor.contains("w")) {
            paint.setColor(Color.WHITE);
        }else {
            paint.setColor(Color.WHITE);
        }

        paint.setTypeface(tf);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(convertToPixels(getApplicationContext(), textSize));

        Rect textRect = new Rect();
        paint.getTextBounds(text, 0, text.length(), textRect);

        Bitmap bmpOut = bmp.copy(Bitmap.Config.ARGB_8888,true);
        canvas = new Canvas(bmpOut);

        //If the text is bigger than the canvas , reduce the font size
        if(textRect.width() >= (canvas.getWidth() - 4))     //the padding on either sides is considered as 4, so as to appropriately fit in the text
            paint.setTextSize(convertToPixels(getApplicationContext(), 7));        //Scaling needs to be used for different dpi's

        //Calculate the positions
        if (xPos == 0 && yPos == 0) {
            xPos = (canvas.getWidth() / 2) - 2;     //-2 is for regulating the x position offset

            //"- ((paint.descent() + paint.ascent()) / 2)" is the distance from the baseline to the center.
            yPos = (int) ((canvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2));
        }

        canvas.drawText(text, xPos, yPos, paint);

        return bmpOut;
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

    public static int convertToPixels(Context context, int nDP){
        final float conversionScale = context.getResources().getDisplayMetrics().density;

        return (int) ((nDP * conversionScale) + 0.5f) ;

    }

    public void completeTextBox(View view){
        Intent intentPass = new Intent(this,keyFrame.class);
        pathKeyFrame = createImageFromBitmap(bmpOut);
        intentPass.putExtra("keyFramePathString",pathKeyFrame);
        startActivity(intentPass);
    }

}
