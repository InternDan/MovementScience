package com.home.buffa.movementscience;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.ipaulpro.afilechooser.utils.FileUtils;

import org.jcodec.api.android.AndroidSequenceEncoder;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.io.SeekableByteChannel;
import org.jcodec.common.model.Rational;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static android.content.ContentValues.TAG;
import static junit.framework.Assert.fail;

public class postProcessExecute extends Activity {

    Uri vid1Uri;
    Uri vid2Uri;

    MediaFormat format1;
    MediaFormat format2;

    String ppOrder;
    String ppSize;
    String ppOrientation;

    AndroidSequenceEncoder enc;
    SeekableByteChannel out;
    String eMagTime;

    File directory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poat_process_execute);

        Intent intentReceive = getIntent();
        String vidPath = intentReceive.getExtras().getString("vidPath1");
        vid1Uri = Uri.parse(vidPath);
        vidPath = intentReceive.getExtras().getString("vidPath2");
        vid2Uri = Uri.parse(vidPath);

        directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        ppOrder = sharedPref.getString("pref_postProcessingPlayOrder","s");
        ppSize = sharedPref.getString("pref_postProcessingSize","small");
        ppOrientation = sharedPref.getString("pref_postProcessingOrientation","lr");


    }


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @SuppressLint("LongLogTag")
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    new postProcessExecute.beginTrackingProcedure().execute(null, null, null);//
                    Intent intent = new Intent(getApplicationContext(), offlineProcessing.class);
                    Toast.makeText(getApplicationContext(),"Application will load video once tracked", Toast.LENGTH_LONG).show();
                    startActivity(intent);

                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public int selectTrack(MediaExtractor extractor) {
        // Select the first video track we find, ignore the rest.
        int numTracks = extractor.getTrackCount();
        for (int i = 0; i < numTracks; i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("video/")) {
                Log.d(TAG, "Extractor selected track " + i + " (" + mime + "): " + format);
                return i;
            }
        }

        return -1;
    }

    public Bitmap combineImages(Bitmap c, Bitmap s) { // can add a 3rd parameter 'String loc' if you want to save the new image - left some code to do that at the bottom
        Bitmap cs = null;

        int width, height = 0;

        if(c.getWidth() > s.getWidth()) {
            width = c.getWidth() + s.getWidth();
            height = c.getHeight();
        } else {
            width = s.getWidth() + s.getWidth();
            height = c.getHeight();
        }

        cs = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas comboImage = new Canvas(cs);

        comboImage.drawBitmap(c, 0f, 0f, null);
        comboImage.drawBitmap(s, c.getWidth(), 0f, null);

        // this is an extra bit I added, just incase you want to save the new image somewhere and then return the location
    /*String tmpImg = String.valueOf(System.currentTimeMillis()) + ".png";

    OutputStream os = null;
    try {
      os = new FileOutputStream(loc + tmpImg);
      cs.compress(CompressFormat.PNG, 100, os);
    } catch(IOException e) {
      Log.e("combineImages", "problem combining images", e);
    }*/

        return cs;
    }


    private class beginTrackingProcedure extends AsyncTask<Void, Void, Void> {

        String outPath;

        protected Void doInBackground(Void... params) {

            //set up out movie file
            enc = null;
            try {
                DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd'at'HH-mm-ss");
                eMagTime = df2.format(Calendar.getInstance().getTime());
                outPath = directory.getAbsolutePath() + "/" + eMagTime + "-combinedVid.mp4";
                out = null;
                out = NIOUtils.writableFileChannel(outPath);
                enc = new AndroidSequenceEncoder(out, Rational.R(25, 1));
            } catch (IOException e) {
                e.printStackTrace();
            }
            //Configure for 2 movies
            MediaCodec decoder1 = null;
            ExtractMpegFramesTest.CodecOutputSurface outputSurface1 = null;
            MediaExtractor extractor1 = null;
            File inputFile1 = new File(FileUtils.getPath(getApplicationContext(), vid1Uri));
            if (!inputFile1.canRead()) {
                try {
                    throw new FileNotFoundException("Unable to read " + inputFile1);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            extractor1 = new MediaExtractor();
            try {
                extractor1.setDataSource(inputFile1.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            int trackIndex1 = selectTrack(extractor1);
            if (trackIndex1 < 0) {
                throw new RuntimeException("No video track found in " + inputFile1);
            }
            extractor1.selectTrack(trackIndex1);
            format1 = extractor1.getTrackFormat(trackIndex1);
            Log.d(TAG, "Video size is " + format1.getInteger(MediaFormat.KEY_WIDTH) + "x" +
                    format1.getInteger(MediaFormat.KEY_HEIGHT));
            // Could use width/height from the MediaFormat to get full-size frames.
            format1.setInteger("rotation-degrees", 0);
            int frameRate1;
            if (format1.containsKey("frame-rate")) {
                frameRate1 = format1.getInteger("frame-rate");
            } else {
                frameRate1 = 30;
            }
            long duration1 = format1.getLong("durationUs");
            outputSurface1 = new ExtractMpegFramesTest.CodecOutputSurface(format1.getInteger(MediaFormat.KEY_WIDTH), format1.getInteger(MediaFormat.KEY_HEIGHT));
            String mime1 = format1.getString(MediaFormat.KEY_MIME);
            try {
                decoder1 = MediaCodec.createDecoderByType(mime1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            decoder1.configure(format1, outputSurface1.getSurface(), null, 0);
            decoder1.start();

            MediaCodec decoder2 = null;
            ExtractMpegFramesTest.CodecOutputSurface outputSurface2 = null;
            MediaExtractor extractor2 = null;
            File inputFile2 = new File(FileUtils.getPath(getApplicationContext(), vid2Uri));
            if (!inputFile2.canRead()) {
                try {
                    throw new FileNotFoundException("Unable to read " + inputFile2);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            extractor2 = new MediaExtractor();
            try {
                extractor2.setDataSource(inputFile2.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            int trackIndex2 = selectTrack(extractor2);
            if (trackIndex2 < 0) {
                throw new RuntimeException("No video track found in " + inputFile2);
            }
            extractor2.selectTrack(trackIndex2);
            format2 = extractor2.getTrackFormat(trackIndex2);
            Log.d(TAG, "Video size is " + format2.getInteger(MediaFormat.KEY_WIDTH) + "x" +
                    format2.getInteger(MediaFormat.KEY_HEIGHT));
            // Could use width/height from the MediaFormat to get full-size frames.
            format2.setInteger("rotation-degrees", 0);
            int frameRate2;
            if (format1.containsKey("frame-rate")) {
                frameRate2 = format2.getInteger("frame-rate");
            } else {
                frameRate2 = 30;
            }
            long duration2 = format2.getLong("durationUs");
            outputSurface2 = new ExtractMpegFramesTest.CodecOutputSurface(format2.getInteger(MediaFormat.KEY_WIDTH), format2.getInteger(MediaFormat.KEY_HEIGHT));
            String mime2 = format2.getString(MediaFormat.KEY_MIME);
            try {
                decoder2 = MediaCodec.createDecoderByType(mime2);
            } catch (IOException e) {
                e.printStackTrace();
            }
            decoder2.configure(format2, outputSurface2.getSurface(), null, 0);
            decoder2.start();
            try {
                doExtract(extractor1, trackIndex1, decoder1, outputSurface1, extractor2, trackIndex2, decoder2, outputSurface2);
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        private void doExtract(MediaExtractor extractor1, int trackIndex1, MediaCodec decoder1,
                               ExtractMpegFramesTest.CodecOutputSurface outputSurface1, MediaExtractor extractor2, int trackIndex2, MediaCodec decoder2,
                               ExtractMpegFramesTest.CodecOutputSurface outputSurface2) throws IOException {
            final int TIMEOUT_USEC = 10000;
            ByteBuffer[] decoderInputBuffers1 = decoder1.getInputBuffers();
            MediaCodec.BufferInfo info1 = new MediaCodec.BufferInfo();
            int inputChunk1 = 0;
            int decodeCount1 = 0;

            boolean outputDone1 = false;
            boolean inputDone1 = false;

            ByteBuffer[] decoderInputBuffers2 = decoder2.getInputBuffers();
            MediaCodec.BufferInfo info2 = new MediaCodec.BufferInfo();
            int inputChunk2 = 0;
            int decodeCount2 = 0;

            boolean outputDone2 = false;
            boolean inputDone2 = false;

            boolean outputDoneBoth = false;

            Bitmap bmp1;
            Bitmap bmp2;

            while (!outputDoneBoth) {
                bmp1=null;
                bmp2=null;

                Log.d(TAG, "loop");

                // Feed more data to the decoder.
                if (!inputDone1) {
                    int inputBufIndex1 = decoder1.dequeueInputBuffer(TIMEOUT_USEC);
                    if (inputBufIndex1 >= 0) {
                        ByteBuffer inputBuf1 = decoderInputBuffers1[inputBufIndex1];
                        // Read the sample data into the ByteBuffer.  This neither respects nor
                        // updates inputBuf's position, limit, etc.
                        int chunkSize1 = extractor1.readSampleData(inputBuf1, 0);
                        if (chunkSize1 < 0) {
                            // End of stream -- send empty frame with EOS flag set.
                            decoder1.queueInputBuffer(inputBufIndex1, 0, 0, 0L,
                                    MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                            inputDone1 = true;
                            Log.d(TAG, "sent input EOS");
                        } else {
                            if (extractor1.getSampleTrackIndex() != trackIndex1) {
                                Log.w(TAG, "WEIRD: got sample from track " +
                                        extractor1.getSampleTrackIndex() + ", expected " + trackIndex1);
                            }
                            long presentationTimeUs1 = extractor1.getSampleTime();
                            decoder1.queueInputBuffer(inputBufIndex1, 0, chunkSize1,
                                    presentationTimeUs1, 0 /*flags*/);
                                Log.d(TAG, "submitted frame " + inputChunk1 + " to dec, size=" +
                                        chunkSize1);
                            inputChunk1++;
                            extractor1.advance();
                        }
                    } else {
                        Log.d(TAG, "input buffer not available");
                    }
                }

                if (!outputDone1) {
                    int decoderStatus1 = decoder1.dequeueOutputBuffer(info1, TIMEOUT_USEC);
                    if (decoderStatus1 == MediaCodec.INFO_TRY_AGAIN_LATER) {
                        // no output available yet
                        Log.d(TAG, "no output from decoder available");
                    } else if (decoderStatus1 == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                        // not important for us, since we're using Surface
                        Log.d(TAG, "decoder output buffers changed");
                    } else if (decoderStatus1 == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        MediaFormat newFormat1 = decoder1.getOutputFormat();
                        Log.d(TAG, "decoder output format changed: " + newFormat1);
                    } else if (decoderStatus1 < 0) {
                        fail("unexpected result from decoder.dequeueOutputBuffer: " + decoderStatus1);
                    } else { // decoderStatus >= 0
                        Log.d(TAG, "surface decoder given buffer " + decoderStatus1 +
                                " (size=" + info1.size + ")");
                        if ((info1.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                            Log.d(TAG, "output EOS");
                            outputDone1 = true;
                        }

                        boolean doRender = (info1.size != 0);

                        // As soon as we call releaseOutputBuffer, the buffer will be forwarded
                        // to SurfaceTexture to convert to a texture.  The API doesn't guarantee
                        // that the texture will be available before the call returns, so we
                        // need to wait for the onFrameAvailable callback to fire.
                        decoder1.releaseOutputBuffer(decoderStatus1, doRender);
                        if (doRender) {
                            Log.d(TAG, "awaiting decode of frame " + decodeCount1);
                            outputSurface1.awaitNewImage();
                            outputSurface1.drawImage(true);
                            bmp1 = outputSurface1.returnFrame();
                        }else {
                            outputDone1 = true;
                            if (outputDone1 && outputDone2){
                                enc.finish();
                                NIOUtils.closeQuietly(out);
                                outputDoneBoth = true;
                            }else{
                                bmp1 = Bitmap.createBitmap(format1.getInteger(MediaFormat.KEY_WIDTH),format1.getInteger(MediaFormat.KEY_HEIGHT), Bitmap.Config.ARGB_8888);
                            }
                        }

                        while (!outputDone2) {
                            Log.d(TAG, "loop");

                            // Feed more data to the decoder.
                            if (!inputDone2) {
                                int inputBufIndex2 = decoder2.dequeueInputBuffer(TIMEOUT_USEC);
                                if (inputBufIndex2 >= 0) {
                                    ByteBuffer inputBuf2 = decoderInputBuffers1[inputBufIndex2];
                                    // Read the sample data into the ByteBuffer.  This neither respects nor
                                    // updates inputBuf's position, limit, etc.
                                    int chunkSize2 = extractor2.readSampleData(inputBuf2, 0);
                                    if (chunkSize2 < 0) {
                                        // End of stream -- send empty frame with EOS flag set.
                                        decoder2.queueInputBuffer(inputBufIndex2, 0, 0, 0L,
                                                MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                                        inputDone2 = true;
                                        Log.d(TAG, "sent input EOS");
                                    } else {
                                        if (extractor2.getSampleTrackIndex() != trackIndex2) {
                                            Log.w(TAG, "WEIRD: got sample from track " +
                                                    extractor2.getSampleTrackIndex() + ", expected " + trackIndex2);
                                        }
                                        long presentationTimeUs2 = extractor2.getSampleTime();
                                        decoder1.queueInputBuffer(inputBufIndex2, 0, chunkSize2,
                                                presentationTimeUs2, 0 /*flags*/);
                                        Log.d(TAG, "submitted frame " + inputChunk2 + " to dec, size=" +
                                                chunkSize2);
                                        inputChunk2++;
                                        extractor2.advance();
                                    }
                                } else {
                                    Log.d(TAG, "input buffer not available");
                                }
                            }

                            if (!outputDone2) {
                                int decoderStatus2 = decoder2.dequeueOutputBuffer(info2, TIMEOUT_USEC);
                                if (decoderStatus2 == MediaCodec.INFO_TRY_AGAIN_LATER) {
                                    // no output available yet
                                    Log.d(TAG, "no output from decoder available");
                                } else if (decoderStatus2 == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                                    // not important for us, since we're using Surface
                                    Log.d(TAG, "decoder output buffers changed");
                                } else if (decoderStatus2 == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                                    MediaFormat newFormat2 = decoder2.getOutputFormat();
                                    Log.d(TAG, "decoder output format changed: " + newFormat2);
                                } else if (decoderStatus2 < 0) {
                                    fail("unexpected result from decoder.dequeueOutputBuffer: " + decoderStatus2);
                                } else { // decoderStatus >= 0
                                    Log.d(TAG, "surface decoder given buffer " + decoderStatus2 +
                                            " (size=" + info2.size + ")");
                                    if ((info2.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                                        Log.d(TAG, "output EOS");
                                        outputDone2 = true;
                                    }

                                    doRender = (info1.size != 0);

                                    // As soon as we call releaseOutputBuffer, the buffer will be forwarded
                                    // to SurfaceTexture to convert to a texture.  The API doesn't guarantee
                                    // that the texture will be available before the call returns, so we
                                    // need to wait for the onFrameAvailable callback to fire.
                                    decoder2.releaseOutputBuffer(decoderStatus2, doRender);
                                    if (doRender) {
                                        Log.d(TAG, "awaiting decode of frame " + decodeCount1);
                                        outputSurface2.awaitNewImage();
                                        outputSurface2.drawImage(true);
                                        bmp2 = outputSurface2.returnFrame();
                                        //
                                    }else {
                                        outputDone2 = true;
                                        if (outputDone1 && outputDone2){
                                            enc.finish();
                                            NIOUtils.closeQuietly(out);
                                            outputDoneBoth = true;
                                        }else{
                                            bmp2 = Bitmap.createBitmap(format2.getInteger(MediaFormat.KEY_WIDTH),format2.getInteger(MediaFormat.KEY_HEIGHT), Bitmap.Config.ARGB_8888);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                //resize bitmaps
                if (ppSize.contains("s")){
                    if (format1.getInteger(MediaFormat.KEY_HEIGHT) == format2.getInteger(MediaFormat.KEY_HEIGHT)){
                        bmp1 = bmp1;
                        bmp2 = bmp2;
                    }else if (format1.getInteger(MediaFormat.KEY_HEIGHT) > format2.getInteger(MediaFormat.KEY_HEIGHT)){
                        int h1 = format2.getInteger(MediaFormat.KEY_HEIGHT);
                        int w1 = (int) Math.round(format1.getInteger(MediaFormat.KEY_WIDTH) * (format2.getInteger(MediaFormat.KEY_HEIGHT) / format1.getInteger(MediaFormat.KEY_HEIGHT)) );
                        bmp1 = Bitmap.createScaledBitmap(bmp1, w1, h1, false);
                    }else if (format2.getInteger(MediaFormat.KEY_HEIGHT) > format1.getInteger(MediaFormat.KEY_HEIGHT)){
                        int h1 = format1.getInteger(MediaFormat.KEY_HEIGHT);
                        int w1 = (int) Math.round(format2.getInteger(MediaFormat.KEY_WIDTH) * (format1.getInteger(MediaFormat.KEY_HEIGHT) / format2.getInteger(MediaFormat.KEY_HEIGHT)) );
                        bmp2 = Bitmap.createScaledBitmap(bmp1, w1, h1, false);
                    }
                }else if (ppSize.contains("l")) {

                }

                //put bitmaps together
                Bitmap bmpJoined = null;
                if (ppOrder.contains("lr")) {
                    bmpJoined = combineImages(bmp1, bmp2);
                }else if (ppOrder.contains("rl")) {
                    bmpJoined = combineImages(bmp2, bmp1);
                }
                enc.encodeImage(bmpJoined);
            }
        }


    }

}

