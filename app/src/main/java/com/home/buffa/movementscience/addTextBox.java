package com.home.buffa.movementscience;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.app.Activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class addTextBox extends Activity {

    String text;
    String pathKeyFrame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_text_box);

        Intent intentReceive = getIntent();
        pathKeyFrame = intentReceive.getExtras().getString("keyFramePathString");
        text = intentReceive.getExtras().getString("keyFrameText");

        File imageKeyFrame = new File(pathKeyFrame);
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bmp = BitmapFactory.decodeFile(imageKeyFrame.getAbsolutePath(),bmOptions);
        if (text != null) {
            bmp = writeTextOnDrawable(bmp, text);
        }

        //potentially let user move box around
        Intent intentPass = new Intent(this,keyFrame.class);
        pathKeyFrame = createImageFromBitmap(bmp);
        intentPass.putExtra("keyFramePathString",pathKeyFrame);
        startActivity(intentPass);
    }

    private Bitmap writeTextOnDrawable(Bitmap bmp, String text) {

        Typeface tf = Typeface.create("Helvetica", Typeface.BOLD);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTypeface(tf);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(convertToPixels(getApplicationContext(), 11));

        Rect textRect = new Rect();
        paint.getTextBounds(text, 0, text.length(), textRect);

        Bitmap bmpOut = bmp.copy(Bitmap.Config.ARGB_8888,true);
        Canvas canvas = new Canvas(bmpOut);

        //If the text is bigger than the canvas , reduce the font size
        if(textRect.width() >= (canvas.getWidth() - 4))     //the padding on either sides is considered as 4, so as to appropriately fit in the text
            paint.setTextSize(convertToPixels(getApplicationContext(), 7));        //Scaling needs to be used for different dpi's

        //Calculate the positions
        int xPos = (canvas.getWidth() / 2) - 2;     //-2 is for regulating the x position offset

        //"- ((paint.descent() + paint.ascent()) / 2)" is the distance from the baseline to the center.
        int yPos = (int) ((canvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2)) ;

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

}
