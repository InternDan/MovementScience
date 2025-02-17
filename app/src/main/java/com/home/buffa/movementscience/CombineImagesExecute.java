package com.home.buffa.movementscience;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.Toast;

import com.ipaulpro.afilechooser.utils.FileUtils;

import org.opencv.android.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class CombineImagesExecute extends Activity {

    ImageView img;

    String ppOrder;
    String ppSize;
    String ppOrientation;
    int rotateDegreesPostProcess;
    int rotateDegreesPostProcess2;

    String imagePath;
    Uri imgUri;

    String bmp1Path;
    String bmp2Path;

    int height;
    int width;
    int sheight;
    int swidth;

    String redoFlag;

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
        setContentView(R.layout.activity_combine_images_execute);

        img = findViewById(R.id.imageViewCombining);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        ppOrder = sharedPref.getString("pref_postProcessingPlayOrder","s");
        ppSize = sharedPref.getString("pref_postProcessingSize","small");
        ppOrientation = sharedPref.getString("pref_postProcessingOrientation","lr");
        String rotDeg = sharedPref.getString("pref_rotateDegreesPostProcess","0");
        rotateDegreesPostProcess = Integer.valueOf(rotDeg);
        rotDeg = sharedPref.getString("pref_rotateDegreesPostProcess2","0");
        rotateDegreesPostProcess2 = Integer.valueOf(rotDeg);


        //load two bitmaps
        Intent intentReceive = getIntent();
        bmp1Path = intentReceive.getExtras().getString("imgPath1");
        bmp2Path = intentReceive.getExtras().getString("imgPath2");
        redoFlag = intentReceive.getExtras().getString("redo");

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
        showToasts();
        //AsyncTask.execute(new Runnable() {
          //  @Override
           // public void run() {
                //TODO your background code
                makeCombinedImage();
            //}
        //});



    }

    public void showToasts(){
        if (redoFlag == null) {
            Toast.makeText(this, "Putting your pictures together...", Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(this, "Putting your pictures together again...", Toast.LENGTH_LONG).show();
        }

    }

    public void makeCombinedImage(){
        final CombineVideos cv = new CombineVideos();
        cv.ppOrder = ppOrder;
        cv.ppSize = ppSize;
        cv.ppOrientation = ppOrientation;
        cv.postRotate1 = rotateDegreesPostProcess;
        cv.postRotate2 = rotateDegreesPostProcess2;

        Bitmap bmp1 = null;
        Bitmap bmp2 = null;
//        try {
            bmp1 = loadLargeBitmap(bmp1Path);
            //bmp1 = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(bmp1Path));
  //      } catch (IOException e) {
    //        e.printStackTrace();
     //   }
       // try {
         //   bmp2 = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(bmp2Path));
        bmp2 = loadLargeBitmap(bmp2Path);
        //} catch (IOException e) {
          //  e.printStackTrace();
        //}
        Bitmap bmp = cv.combineImagePair(bmp1,bmp2);
        if (bmp != null) {
            scaleImage(bmp);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width,height);
            int top = (int) Math.round((((double)sheight - (double)height)/2));
            int left = (int) Math.round((((double)swidth - (double)width)/2));
            lp.setMargins(left,top,0,0);
            LinearLayout linearLayoutImg = findViewById(R.id.linearLayoutImg);
            linearLayoutImg.setLayoutParams(lp);
            img.setImageBitmap(bmp);
            imagePath = createImageFromBitmap(bmp);
            File img = new File(imagePath);
            imgUri = Uri.fromFile(img);
        }else{
            Toast.makeText(this, "An error occurred creating these images", Toast.LENGTH_LONG).show();
        }
    }

    public void shareImage(View view){
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("image/*");
        sharingIntent.putExtra(Intent.EXTRA_STREAM,imgUri);
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }

    public void updateImage(View view){
        Intent intent = new Intent(this, CombineImagesExecute.class);
        intent.putExtra("imgPath1", bmp1Path);
        intent.putExtra("imgPath2", bmp2Path);
        intent.putExtra("redo", bmp2Path);
        startActivity(intent);
    }

    public String createImageFromBitmap(Bitmap bitmap){
        DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd 'at' HH-mm-ss");
        String eMagTime = df2.format(Calendar.getInstance().getTime());
        File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        // Create imageDir
        File mypath=new File(directory,"CombinedImage-" + eMagTime + ".png");
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
        return mypath.getAbsolutePath();

    }

    public void editSettings(View view){
        Intent intent = new Intent(getApplicationContext(),settingsActivity.class);
        startActivity(intent);
    }

    private void scaleImage(Bitmap bmp){

        double ratio;

        //get height and width of video
        height = bmp.getHeight();
        width = bmp.getWidth();
        //get height and width of available screen
        swidth = getWindowManager().getDefaultDisplay().getWidth();
        sheight = getWindowManager().getDefaultDisplay().getHeight();//1.8 is linearlayout weight
        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();

        sheight = sheight - (int)Math.round((TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40,dm)));//leave room for button
        //calculate height and weight of video that will fit in this available space
        if (height > sheight){
            ratio = sheight / (double)height;
            height = (int)Math.round((double)height * ratio);
            width = (int)Math.round((double)width * ratio);
        }
        if (width > swidth){
            ratio = swidth / (double)width;
            width = (int)Math.round((double)width * ratio);
            height = (int)Math.round((double)height * ratio);
        }
        if (width < swidth){
            ratio = swidth / (double)width;
            width = (int)Math.round((double)width * ratio);
            height = (int)Math.round((double)height * ratio);
            if (height > sheight){
                ratio = sheight / (double)height;
                height = (int)Math.round((double)height * ratio);
                width = (int)Math.round((double)width * ratio);
            }
        }
        if (height < sheight){
            ratio = sheight / (double)height;
            height = (int)Math.round((double)height * ratio);
            width = (int)Math.round((double)width * ratio);
            if (width > swidth){
                ratio = swidth / (double)width;
                width = (int)Math.round((double)width * ratio);
                height = (int)Math.round((double)height * ratio);
            }
        }


    }


    private Bitmap loadLargeBitmap(String path){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(FileUtils.getPath(getApplicationContext(),Uri.parse(path)), options);
        //define default max allowed sizes
        int maxHeight = 860;
        int maxWidth = 540;
        options.inSampleSize = calculateInSampleSize(options,maxWidth,maxHeight);
        options.inJustDecodeBounds = false;
        Bitmap bmp = BitmapFactory.decodeFile(FileUtils.getPath(getApplicationContext(),Uri.parse(path)), options);
        return bmp;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public void onResume() {
        super.onResume();
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }
}
