package com.home.buffa.movementscience;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static android.media.ExifInterface.TAG_ORIENTATION;

/**
 * Created by buffa on 4/23/2018.
 */

public class VideoProcessing {


    //to rotate a video, make a VideoProcessing object, set the file path, and then feed it bitmaps successively in a loop
    static String filePath;

    public static Bitmap rotateFrame(Bitmap bmp, Integer prefRotatePost){
        ExifInterface exifInterface = null;
        try {
            exifInterface = new ExifInterface(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int orientation = exifInterface.getAttributeInt(TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_UNDEFINED:
                matrix.postRotate(0+prefRotatePost);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.postRotate(90+prefRotatePost);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.postRotate(180+prefRotatePost);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.postRotate(270+prefRotatePost);
                break;
            default:
                break;
        }
        bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
        return bmp;
    }

    public static Bitmap rotateImage(Bitmap bmp, Integer prefRotatePost){
        Matrix matrix = new Matrix();
        matrix.postRotate(prefRotatePost);
        bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
        return bmp;
    }

    //scale the height and width of the images based on the settings of the user (passed in as arguments)
    public static ArrayList<Bitmap> scaleHeightAndWidth(Bitmap bmp1, Bitmap bmp2, String ppSize, String ppOrientation) {
        ArrayList<Bitmap> outBmps = new ArrayList<Bitmap>();
        if (ppSize.contains("small")) {
            if (ppOrientation.contains("lr") || ppOrientation.contains("rl")) {//scale to height of
                int h1 = bmp1.getHeight();
                int h2 = bmp2.getHeight();
                int w1 = bmp1.getWidth();
                int w2 = bmp1.getWidth();
                if (h1 > h2){//if bmp1 is taller, scale to h2
                    int hOut = h2;
                    double ratio = ((double)h2/(double)h1);
                    int w1Out = (int) Math.round((double)w1 * ratio);
                    bmp1 = Bitmap.createScaledBitmap(bmp1, w1Out, hOut, false);
                    outBmps.add(bmp1);
                    outBmps.add(bmp2);
                }else if (h2 > h1){//if bmp1 is taller, scale to h2
                    int hOut = h1;
                    double ratio = ((double)h1/(double)h2);
                    int w2Out = (int) Math.round((double)w2 * ratio);
                    bmp2 = Bitmap.createScaledBitmap(bmp2, w2Out, hOut, false);
                    outBmps.add(bmp1);
                    outBmps.add(bmp2);
                }else{
                    outBmps.add(bmp1);
                    outBmps.add(bmp2);
                }
            }
            if (ppOrientation.contains("tb") || ppOrientation.contains("bt")) {//scale to height of
                int h1 = bmp1.getHeight();
                int h2 = bmp2.getHeight();
                int w1 = bmp1.getWidth();
                int w2 = bmp1.getWidth();
                if (w1 > w2){
                    int wOut = w2;
                    double ratio = ((double)w2/(double)w1);
                    int h1Out = (int) Math.round((double)h1 * ratio);
                    bmp1 = Bitmap.createScaledBitmap(bmp1, wOut, h1Out, false);
                    outBmps.add(bmp1);
                    outBmps.add(bmp2);
                }else if (w2 > w1){
                    int wOut = w1;
                    double ratio = ((double)w1/(double)w2);
                    int h2Out = (int) Math.round((double)h2 * ratio);
                    bmp2 = Bitmap.createScaledBitmap(bmp2, wOut, h2Out, false);
                    outBmps.add(bmp1);
                    outBmps.add(bmp2);
                }else{
                    outBmps.add(bmp1);
                    outBmps.add(bmp2);
                }
            }
        }
        if (ppSize.contains("large")) {
            if (ppOrientation.contains("lr") || ppOrientation.contains("rl")) {//scale to height of
                int h1 = bmp1.getHeight();
                int h2 = bmp2.getHeight();
                int w1 = bmp1.getWidth();
                int w2 = bmp1.getWidth();
                if (h1 > h2){//if bmp1 is taller, scale to h2
                    int hOut = h1;
                    double ratio = ((double)h1/(double)h2);
                    int w2Out = (int) Math.round((double)w2 * ratio);
                    bmp2 = Bitmap.createScaledBitmap(bmp2, w2Out, hOut, false);
                    outBmps.add(bmp1);
                    outBmps.add(bmp2);
                }else if (h2 > h1){//if bmp1 is taller, scale to h2
                    int hOut = h2;
                    double ratio = ((double)h2/(double)h1);
                    int w1Out = (int) Math.round((double)w1 * ratio);
                    bmp1 = Bitmap.createScaledBitmap(bmp1, w1Out, hOut, false);
                    outBmps.add(bmp1);
                    outBmps.add(bmp2);
                }else{
                    outBmps.add(bmp1);
                    outBmps.add(bmp2);
                }
            }
            if (ppOrientation.contains("tb") || ppOrientation.contains("bt")) {//scale to height of
                int h1 = bmp1.getHeight();
                int h2 = bmp2.getHeight();
                int w1 = bmp1.getWidth();
                int w2 = bmp1.getWidth();
                if (w1 > w2){
                    int wOut = w1;
                    double ratio = ((double)w1/(double)w2);
                    int h2Out = (int) Math.round((double)h2 * ratio);
                    bmp2 = Bitmap.createScaledBitmap(bmp2, wOut, h2Out, false);
                    outBmps.add(bmp1);
                    outBmps.add(bmp2);
                }else if (w2 > w1){
                    int wOut = w2;
                    double ratio = ((double)w2/(double)w1);
                    int h1Out = (int) Math.round((double)h1 * ratio);
                    bmp1 = Bitmap.createScaledBitmap(bmp1, wOut, h1Out, false);
                    outBmps.add(bmp1);
                    outBmps.add(bmp2);
                }else{
                    outBmps.add(bmp1);
                    outBmps.add(bmp2);
                }
            }
        }
        return outBmps;
    }

    //combine images left to right, should be scaled and rotated first
    public static Bitmap combineImagesLR(Bitmap c, Bitmap s) { // can add a 3rd parameter 'String loc' if you want to save the new image - left some code to do that at the bottom
        Bitmap cs = null;
        int height = 0;
        int width = 0;
        int w1 = c.getWidth();
        int w2 = s.getWidth();
        width = w1 + w2;
        height = c.getHeight();

        int[] pixels = new int[s.getHeight() * s.getWidth()];
        s.getPixels(pixels, 0, s.getWidth(), 0, 0, s.getWidth(), s.getHeight());
        int PixelSumS = 0;
        for (int i = 0; i < pixels.length;i++) {
            PixelSumS = PixelSumS + pixels[i];
        }
        pixels = new int[c.getHeight() * c.getWidth()];
        c.getPixels(pixels, 0, c.getWidth(), 0, 0, c.getWidth(), c.getHeight());
        int PixelSumC = 0;
        for (int i = 0; i < pixels.length;i++) {
            PixelSumC = PixelSumC + pixels[i];
        }
        if (PixelSumS == 0) {
            pixels = new int[s.getHeight() * s.getWidth()];
            s.getPixels(pixels, 0, s.getWidth(), 0, 0, s.getWidth(), s.getHeight());
            for (int i = 0; i < s.getHeight() * s.getWidth(); i++) {
                pixels[i] = Color.BLACK;
            }
            s.setPixels(pixels, 0, s.getWidth(), 0, 0, s.getWidth(), s.getHeight());
            cs = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas comboImage = new Canvas(cs);
            comboImage.drawBitmap(c, 0f, 0f, null);
            comboImage.drawBitmap(s, c.getWidth(), 0f, null);
        }else if(PixelSumC == 0){
            pixels = new int[c.getHeight() * c.getWidth()];
            c.getPixels(pixels, 0, c.getWidth(), 0, 0, c.getWidth(), c.getHeight());
            for (int i = 0; i < c.getHeight() * c.getWidth(); i++) {
                pixels[i] = Color.BLACK;
            }
            c.setPixels(pixels, 0, c.getWidth(), 0, 0, c.getWidth(), c.getHeight());
            cs = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas comboImage = new Canvas(cs);
            comboImage.drawBitmap(c, 0f, 0f, null);
            comboImage.drawBitmap(s, c.getWidth(), 0f, null);
        }else{
            cs = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas comboImage = new Canvas(cs);
            comboImage.drawBitmap(c, 0f, 0f, null);
            comboImage.drawBitmap(s, c.getWidth(), 0f, null);
        }
        return cs;
    }

    public static Bitmap combineImagesUD(Bitmap c, Bitmap s) {
        Bitmap cs = null;
        int width = 0;
        int height = 0;

        if(c.getWidth() > s.getWidth()) {
            int hc = (int) Math.round((double)c.getHeight() * ( (double)s.getWidth() / (double)c.getWidth()) );
            c = Bitmap.createScaledBitmap(c, s.getWidth(), hc, false);
            int dd = c.getHeight();
            int cc = s.getHeight();
            height = dd + cc;
            width = s.getWidth();
        } else {
            int hs = (int) Math.round((double)s.getHeight() * ( (double)c.getWidth() / (double)s.getWidth()) );
            s = Bitmap.createScaledBitmap(s, c.getWidth(), hs, false);
            int dd = c.getHeight();
            int cc = s.getHeight();
            height = dd + cc;
            width = c.getWidth();
        }

        int[] pixels = new int[s.getHeight() * s.getWidth()];
        s.getPixels(pixels, 0, s.getWidth(), 0, 0, s.getWidth(), s.getHeight());
        int PixelSumS = 0;
        for (int i = 0; i < pixels.length;i++) {
            PixelSumS = PixelSumS + pixels[i];
        }
        pixels = new int[c.getHeight() * c.getWidth()];
        c.getPixels(pixels, 0, c.getWidth(), 0, 0, c.getWidth(), c.getHeight());
        int PixelSumC = 0;
        for (int i = 0; i < pixels.length;i++) {
            PixelSumC = PixelSumC + pixels[i];
        }
        if (PixelSumS == 0) {
            pixels = new int[s.getHeight() * s.getWidth()];
            s.getPixels(pixels, 0, s.getWidth(), 0, 0, s.getWidth(), s.getHeight());
            for (int i = 0; i < s.getHeight() * s.getWidth(); i++) {
                pixels[i] = Color.BLACK;
            }
            s.setPixels(pixels, 0, s.getWidth(), 0, 0, s.getWidth(), s.getHeight());
            cs = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas comboImage = new Canvas(cs);
            comboImage.drawBitmap(c, 0f, 0f, null);
            comboImage.drawBitmap(s, 0,c.getHeight(), null);
        }else if(PixelSumC == 0){
            pixels = new int[c.getHeight() * c.getWidth()];
            c.getPixels(pixels, 0, c.getWidth(), 0, 0, c.getWidth(), c.getHeight());
            for (int i = 0; i < c.getHeight() * c.getWidth(); i++) {
                pixels[i] = Color.BLACK;
            }
            c.setPixels(pixels, 0, c.getWidth(), 0, 0, c.getWidth(), c.getHeight());
            cs = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas comboImage = new Canvas(cs);
            comboImage.drawBitmap(c, 0f, 0f, null);
            comboImage.drawBitmap(s, 0, c.getHeight(), null);
        }else{
            cs = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas comboImage = new Canvas(cs);
            comboImage.drawBitmap(c, 0f, 0f, null);
            comboImage.drawBitmap(s, 0, c.getHeight(), null);
        }
        return cs;
    }

    //resize images for stacked setting, creating frames of equal size.
    public static Bitmap resizeForStacked(Bitmap bitmap,int hVid1,int hVid2,int wVid1, int wVid2){
        int useHeight = 0;
        int useWidth = 0;
        double ratio = 0;
        //Determine initial height scaling
        if (hVid1 > hVid2){
            int height = hVid2;
            ratio = (double)height / (double)bitmap.getHeight();
            useHeight = (int)Math.round((double)bitmap.getHeight() * ratio);
            useWidth = (int)Math.round((double)bitmap.getWidth() * ratio);
            bitmap = Bitmap.createScaledBitmap(bitmap,useWidth,useHeight,false);
            int diff1 = wVid1 - bitmap.getWidth();
            int diff2 = wVid2 - bitmap.getWidth();
            int diff = 0;
            if (diff1 > 0) {
                diff = diff1;
            }else if (diff2 > 0) {
                diff = diff2;
            }
            if (diff != 0) {
                int padLeft = (int) Math.floor((double) diff / 2);
                Bitmap holder = Bitmap.createBitmap(diff + bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(holder);
                canvas.drawColor(Color.BLACK);
                canvas.drawBitmap(bitmap,padLeft,0,null);
                return holder;
            }

        }else if(hVid2 > hVid1){
            int height = hVid1;
            ratio = (double)height/ (double)bitmap.getHeight();
            useHeight = (int)Math.round((double)bitmap.getHeight() * ratio);
            useWidth = (int)Math.round((double)bitmap.getWidth() * ratio);
            bitmap = Bitmap.createScaledBitmap(bitmap,useWidth,useHeight,false);

            int diff1 = wVid1 - bitmap.getWidth();
            int diff2 = wVid2 - bitmap.getWidth();
            int diff = 0;
            if (diff1 > 0) {
                diff = diff1;
            }else if (diff2 > 0) {
                diff = diff2;
            }
            if (diff != 0) {
                int padLeft = (int) Math.floor((double) diff / 2);
                Bitmap holder = Bitmap.createBitmap(diff + bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(holder);
                canvas.drawColor(Color.BLACK);
                canvas.drawBitmap(bitmap,padLeft,0,null);
                return holder;
            }
        }else{
            int height = hVid1;
            ratio = (double)height / (double)bitmap.getHeight();
            useHeight = (int)Math.round((double)bitmap.getHeight() * ratio);
            useWidth = (int)Math.round((double)bitmap.getWidth() * ratio);
            bitmap = Bitmap.createScaledBitmap(bitmap,useWidth,useHeight,false);
            int diff1 = wVid1 - bitmap.getWidth(); //deal with signs
            int diff2 = wVid2 - bitmap.getWidth();
            int diff = 0;
            if (diff1 > 0) {
                diff = diff1;
            }else if (diff2 > 0) {
                diff = diff2;
            }
            if (diff != 0) {
                int padLeft = (int) Math.floor((double) diff / 2);
                Bitmap holder = Bitmap.createBitmap(diff + bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(holder);
                canvas.drawColor(Color.BLACK);
                canvas.drawBitmap(bitmap,padLeft,0,null);
                return holder;
            }
        }
        return bitmap;
    }

    public static Bitmap resizeForInstagram(Bitmap bmp){
        //IG recommended size 600x600 max
        int hout;
        int wout;

        double h = bmp.getHeight();
        double w = bmp.getWidth();

        double ratio;
        //check height
        if (h > 600){
            ratio = 600 / h;
            h = h * ratio;
            w = w * ratio;
        }//height scaled, now do width and redo height
        if (w > 600){
            ratio = 600 / w;
            w = w * ratio;
            h = h * ratio;
        }
        hout = (int) Math.round(h);
        wout = (int) Math.round(w);
        if ((hout & 1) == 1 ){
            hout = hout - 1;
        }
        if ((wout & 1) == 1 ){
            wout = wout - 1;
        }

        bmp = Bitmap.createScaledBitmap(bmp, wout, hout, false);

        return bmp;
    }

    //check dimensions for encoder; must be even
    public static Bitmap checkBitmapDimensions(Bitmap bmp){
        Bitmap bmp2 = null;
        if ( (bmp.getHeight() & 1) == 1 ){
            bmp2 =  Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight()-1);//even
        }
        if ( (bmp.getWidth() & 1) == 1 ){
            bmp2 =  Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth()-1, bmp.getHeight());//even
        }
        if (bmp2 == null){
            return bmp;
        }else if (bmp2 != null){
            return bmp2;
        }
        bmp2.recycle();
        return bmp;
    }

}
