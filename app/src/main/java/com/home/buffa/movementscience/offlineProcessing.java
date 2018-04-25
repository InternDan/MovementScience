package com.home.buffa.movementscience;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveFile;
import com.ipaulpro.afilechooser.utils.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import wseemann.media.FFmpegMediaMetadataRetriever;

import static android.content.ContentValues.TAG;
import static android.media.MediaMetadataRetriever.OPTION_CLOSEST;
import static junit.framework.Assert.fail;

public class offlineProcessing extends Activity {

    VideoView result_video;
    static final int READ_REQUEST_CODE_VIDEO = 2;
    static final int READ_REQUEST_CODE_IMAGE = 3;
    MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
    static Intent intentPass;
    static String vidPath;
    Uri videoUri;
    String videoAbsolutePath;
    Bitmap bmp;
    Bitmap bitmap;
    String bmpPath;
    private static final int  MY_PERMISSIONS_REQUEST_INTERNET = 131728;
    GoogleApiClient mGoogleApiClient;
    boolean VERBOSE = true;

    int rotateDegreesPostProcess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        View decorView = getWindow().getDecorView();
        // Hide both the navigation bar and the status bar.
        // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
        // a general rule, you should design your app to hide the status bar whenever you
        // hide the navigation bar.
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        setContentView(R.layout.activity_offline_processing);

        intentPass = new Intent(this,pickPoints.class);
        result_video = (VideoView)findViewById(R.id.videoView);
        MediaController mediaController = new
                MediaController(this);
        mediaController.setAnchorView(result_video);
        result_video.setMediaController(mediaController);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String str = sharedPref.getString("pref_rotateDegreesPostProcess","0");
        rotateDegreesPostProcess = Integer.valueOf(str);

        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                android.Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(offlineProcessing.this,
                    android.Manifest.permission.INTERNET)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(offlineProcessing.this,
                        new String[]{android.Manifest.permission.INTERNET},
                        MY_PERMISSIONS_REQUEST_INTERNET);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    public void loadVideo(View v) {
        //Load a video to edit
        Intent intentGetVideo = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intentGetVideo.addCategory(Intent.CATEGORY_OPENABLE);
        intentGetVideo.setType("video/*");
        startActivityForResult(intentGetVideo, READ_REQUEST_CODE_VIDEO);
    }

    //public void loadOnlineVideo(View v){
   //     Intent intent = new Intent(this, internetProcessing.class);
   //     startActivity(intent);
   // }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == READ_REQUEST_CODE_VIDEO && resultCode == RESULT_OK) {
            videoUri = data.getData();
            videoAbsolutePath = FileUtils.getPath(getApplicationContext(),videoUri);
            if (videoAbsolutePath == null){
                Context context = getApplicationContext();
                CharSequence text = "Unable to load from that location; ensure file is stored locally on device.";
                int duration = Toast.LENGTH_LONG;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                return;
            }
            mediaMetadataRetriever.setDataSource(this, videoUri);
            result_video.setVideoURI(videoUri);
            result_video.seekTo(100);
            //result_video.requestFocus();
            intentPass.putExtra("videoPath",videoUri.toString());
        }
        if (requestCode == READ_REQUEST_CODE_IMAGE && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            try{
                bmp = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                DisplayMetrics metrics = new DisplayMetrics();
                Display display = getWindowManager().getDefaultDisplay();
                display.getRealMetrics(metrics);
                int width = metrics.widthPixels;
                int height = metrics.heightPixels;
                if (bmp.getHeight() > height) {
                    bmp = Bitmap.createScaledBitmap(bmp, width, height, false);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (bmp == null) {
                bmp = loadLargeImage(uri,result_video.getHeight(),result_video.getWidth());
            }
//            if (bmp.getHeight() > result_video.getHeight() && bmp.getWidth() > result_video.getWidth()) {
//                Display display = getWindowManager().getDefaultDisplay();
//                Point size = new Point();
//                display.getSize(size);
//                int width = size.x;
//                int height = size.y;
//                bmp = Bitmap.createScaledBitmap(bmp, height, width, false);
//            }
            String bmpPath = createImageFromBitmapKey(bmp);
            Intent intentBmpPass = new Intent(this,keyFrame.class);
            intentBmpPass.putExtra("keyFramePathString",bmpPath);
            startActivity(intentBmpPass);
        }
    }

    public void pickPoints(View view){
        int currentPosition = 200; //in millisecond
        Toast.makeText(offlineProcessing.this, "Loading first frame", Toast.LENGTH_LONG).show();
        new offlineProcessing.beginExtractionProcedure().execute(null, null, null);//

    }

    public void keyFrame(View view){
        int currentPosition = result_video.getCurrentPosition(); //in millisecond, change to get current frame
        FFmpegMediaMetadataRetriever mmr = new FFmpegMediaMetadataRetriever();
        mmr.setDataSource(videoAbsolutePath);
        Bitmap bitmap = mmr.getFrameAtTime(currentPosition*1000, FFmpegMediaMetadataRetriever.OPTION_CLOSEST);
        if (bitmap == null){
            Intent intent = new Intent(getApplicationContext(), offlineProcessing.class);
            Toast.makeText(this, "Please select a video first!", Toast.LENGTH_LONG).show();
            startActivity(intent);
            return;
        }
        Intent intentPassKeyFrame = new Intent(this,keyFrame.class);
        String path = createImageFromBitmapKey(bitmap);
        intentPassKeyFrame.putExtra("keyFramePathString",path);
        startActivity(intentPassKeyFrame);
    }

    public String createImageFromBitmapFirst(Bitmap bitmap){
        DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd 'at' HH-mm-ss");
        String eMagTime = df2.format(Calendar.getInstance().getTime());
        File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        // Create imageDir
        File mypath=new File(directory,eMagTime + "-firstFrame.png");


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
        return directory.getAbsolutePath() + "/" + eMagTime + "-firstFrame.png";

    }

    public String createImageFromBitmapKey(Bitmap bitmap){

        DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd'at'HH-mm-ss");
        String eMagTime = df2.format(Calendar.getInstance().getTime());
        File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        // Create imageDir
        File mypath = new File(directory,eMagTime +  "-keyFrame.png");


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
        return directory.getAbsolutePath() + "/" + eMagTime + "-keyFrame.png";

    }

    public void loadImage(View v){
        Intent intentGetImage = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intentGetImage.addCategory(Intent.CATEGORY_OPENABLE);
        intentGetImage.setType("image/*");
        startActivityForResult(intentGetImage, READ_REQUEST_CODE_IMAGE);
    }

    public Bitmap loadLargeImage(Uri uri, int reqHeight, int reqWidth){//add input height and width and required height and width
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        File file = new File(uri.getPath());
        BitmapFactory.decodeFile(file.getPath(),options);
        int height = options.outHeight;
        int width = options.outWidth;

        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                options.inSampleSize *= 2;
            }
        }
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(file.getPath(),options);


    }
    private class beginExtractionProcedure extends AsyncTask<Void, Void, Void> {

        String outPath;

        protected Void doInBackground(Void... params) {

            MediaCodec decoder = null;
            ExtractMpegFramesTest.CodecOutputSurface outputSurface = null;
            MediaExtractor extractor = null;
            File inputFile = new File(videoAbsolutePath);
            if (!inputFile.canRead()) {
                try {
                    throw new FileNotFoundException("Unable to read " + inputFile);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            extractor = new MediaExtractor();
            try {
                extractor.setDataSource(inputFile.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            int trackIndex = selectTrack(extractor);
            if (trackIndex < 0) {
                throw new RuntimeException("No video track found in " + inputFile);
            }
            extractor.selectTrack(trackIndex);
            MediaFormat format = extractor.getTrackFormat(trackIndex);
            Log.d(TAG, "Video size is " + format.getInteger(MediaFormat.KEY_WIDTH) + "x" +
                    format.getInteger(MediaFormat.KEY_HEIGHT));
            // Could use width/height from the MediaFormat to get full-size frames.
            format.setInteger("rotation-degrees", 0);
            outputSurface = new ExtractMpegFramesTest.CodecOutputSurface(format.getInteger(MediaFormat.KEY_WIDTH), format.getInteger(MediaFormat.KEY_HEIGHT));//check!frameWidth,frameHeight);
            String mime = format.getString(MediaFormat.KEY_MIME);
            try {
                decoder = MediaCodec.createDecoderByType(mime);
            } catch (IOException e) {
                e.printStackTrace();
            }
            decoder.configure(format, outputSurface.getSurface(), null, 0);
            decoder.start();

            try {
                doExtract(extractor, trackIndex, decoder, outputSurface);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (outputSurface != null) {
                outputSurface.release();
                outputSurface = null;
            }
            if (decoder != null) {
                decoder.stop();
                decoder.release();
                decoder = null;
            }
            if (extractor != null) {
                extractor.release();
                extractor = null;
            }

            return null;
        }

        protected void onPostExecute(Void result) {

            VideoProcessing vp = new VideoProcessing();
            vp.filePath = videoAbsolutePath;//not the same as the image probably!
            bitmap = vp.rotateFrame(bitmap,rotateDegreesPostProcess);
            if (bitmap == null){
                Intent intent = new Intent(getApplicationContext(), offlineProcessing.class);
                Toast.makeText(offlineProcessing.this, "Please select a video first!", Toast.LENGTH_LONG).show();
                startActivity(intent);
                return;
            }
            intentPass.putExtra("firstFramePosition", 200);
            String path = createImageFromBitmapFirst(bitmap);
            intentPass.putExtra("firstFramePathString", path);
            intentPass.putExtra("videoAbsolutePath", videoAbsolutePath);
            startActivity(intentPass);
        }
    }

    private void doExtract(MediaExtractor extractor, int trackIndex, MediaCodec decoder,
                           ExtractMpegFramesTest.CodecOutputSurface outputSurface) throws IOException {
        final int TIMEOUT_USEC = 10000;
        ByteBuffer[] decoderInputBuffers = decoder.getInputBuffers();
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        int inputChunk = 0;
        int decodeCount = 0;

        boolean outputDone = false;
        boolean inputDone = false;
        while (!outputDone) {
            if (VERBOSE) Log.d(TAG, "loop");

            // Feed more data to the decoder.
            if (!inputDone) {
                int inputBufIndex = decoder.dequeueInputBuffer(TIMEOUT_USEC);
                if (inputBufIndex >= 0) {
                    ByteBuffer inputBuf = decoderInputBuffers[inputBufIndex];
                    // Read the sample data into the ByteBuffer.  This neither respects nor
                    // updates inputBuf's position, limit, etc.
                    int chunkSize = extractor.readSampleData(inputBuf, 0);
                    if (chunkSize < 0) {
                        // End of stream -- send empty frame with EOS flag set.
                        decoder.queueInputBuffer(inputBufIndex, 0, 0, 0L,
                                MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        inputDone = true;
                        if (VERBOSE) Log.d(TAG, "sent input EOS");
                    } else {
                        if (extractor.getSampleTrackIndex() != trackIndex) {
                            Log.w(TAG, "WEIRD: got sample from track " +
                                    extractor.getSampleTrackIndex() + ", expected " + trackIndex);
                        }
                        long presentationTimeUs = extractor.getSampleTime();
                        decoder.queueInputBuffer(inputBufIndex, 0, chunkSize,
                                presentationTimeUs, 0 /*flags*/);
                        if (VERBOSE) {
                            Log.d(TAG, "submitted frame " + inputChunk + " to dec, size=" +
                                    chunkSize);
                        }
                        inputChunk++;
                        extractor.advance();
                    }
                } else {
                    if (VERBOSE) Log.d(TAG, "input buffer not available");
                }
            }

            if (!outputDone) {
                int decoderStatus = decoder.dequeueOutputBuffer(info, TIMEOUT_USEC);
                if (decoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    // no output available yet
                    if (VERBOSE) Log.d(TAG, "no output from decoder available");
                } else if (decoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    // not important for us, since we're using Surface
                    if (VERBOSE) Log.d(TAG, "decoder output buffers changed");
                } else if (decoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    MediaFormat newFormat = decoder.getOutputFormat();
                    if (VERBOSE) Log.d(TAG, "decoder output format changed: " + newFormat);
                } else if (decoderStatus < 0) {
                    fail("unexpected result from decoder.dequeueOutputBuffer: " + decoderStatus);
                } else { // decoderStatus >= 0
                    if (VERBOSE) Log.d(TAG, "surface decoder given buffer " + decoderStatus +
                            " (size=" + info.size + ")");
                    if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        if (VERBOSE) Log.d(TAG, "output EOS");
                        outputDone = true;
                    }

                    boolean doRender = (info.size != 0);

                    // As soon as we call releaseOutputBuffer, the buffer will be forwarded
                    // to SurfaceTexture to convert to a texture.  The API doesn't guarantee
                    // that the texture will be available before the call returns, so we
                    // need to wait for the onFrameAvailable callback to fire.
                    decoder.releaseOutputBuffer(decoderStatus, doRender);
                    if (doRender) {
                        if (VERBOSE) Log.d(TAG, "awaiting decode of frame " + decodeCount);
                        outputSurface.awaitNewImage();
                        outputSurface.drawImage(true);
                        bitmap = outputSurface.returnFrame();
                        //write out
                        outputDone = true;

                    }else{
                        outputDone = true;
                    }
                }
            }
        }
    }

    public int selectTrack(MediaExtractor extractor) {
        // Select the first video track we find, ignore the rest.
        int numTracks = extractor.getTrackCount();
        for (int i = 0; i < numTracks; i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("video/")) {
                if (VERBOSE) {
                    Log.d(TAG, "Extractor selected track " + i + " (" + mime + "): " + format);
                }
                return i;
            }
        }

        return -1;
    }

    public void goHome(View view){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    public void launchGooglePhotos(View view){
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setComponent(ComponentName.unflattenFromString("com.google.android.apps.photos.home.HomeActivity"));
        intent.addCategory("android.intent.category.LAUNCHER");
        intent.setPackage("com.google.android.apps.photos");
        startActivity(intent);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case  MY_PERMISSIONS_REQUEST_INTERNET: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    protected void onPause() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onPause();
    }
}
