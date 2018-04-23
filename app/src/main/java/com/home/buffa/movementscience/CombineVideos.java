package com.home.buffa.movementscience;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Environment;
import android.util.Log;

import org.jcodec.api.android.AndroidSequenceEncoder;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.io.SeekableByteChannel;
import org.jcodec.common.model.Rational;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import static android.content.ContentValues.TAG;
import static junit.framework.Assert.fail;

/**
 * Created by buffa on 4/23/2018.
 */

public class CombineVideos {

    public static String videoAbsolutePath1;
    public static String videoAbsolutePath2;
    public static String ppOrder;
    public static String ppSize;
    public static String ppOrientation;
    public static int postRotate1;
    public static int postRotate2;


    int frameRate1;
    int frameRate2;
    String eMagTime;
    String outPath;
    SeekableByteChannel out;
    SeekableByteChannel out2;
    AndroidSequenceEncoder enc;
    AndroidSequenceEncoder enc2;
    Boolean secondVidFlag = false;
    File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);

    MediaExtractor extractor1;
    int trackIndex1;
    MediaCodec decoder1;
    ExtractMpegFramesTest.CodecOutputSurface outputSurface1;
    MediaExtractor extractor2;
    int trackIndex2;
    MediaCodec decoder2;
    ExtractMpegFramesTest.CodecOutputSurface outputSurface2;
    MediaFormat format1;
    MediaFormat format2;

    VideoProcessing vp = new VideoProcessing();

    public void initialize(){
        decoder1 = null;
        outputSurface1 = null;
        extractor1 = null;
        File inputFile1 = new File(videoAbsolutePath1);
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
        trackIndex1 = selectTrack(extractor1);
        if (trackIndex1 < 0) {
            throw new RuntimeException("No video track found in " + inputFile1);
        }
        extractor1.selectTrack(trackIndex1);
        format1 = extractor1.getTrackFormat(trackIndex1);
        Log.d(TAG, "Video size is " + format1.getInteger(MediaFormat.KEY_WIDTH) + "x" +
                format1.getInteger(MediaFormat.KEY_HEIGHT));
        // Could use width/height from the MediaFormat to get full-size frames.
        format1.setInteger("rotation-degrees", 0);
        if (format1.containsKey("frame-rate")) {
            frameRate1 = format1.getInteger("frame-rate");
        }else{
            frameRate1 = 30;
        }
        outputSurface1 = new ExtractMpegFramesTest.CodecOutputSurface(format1.getInteger(MediaFormat.KEY_WIDTH),format1.getInteger(MediaFormat.KEY_HEIGHT));//check!
        String mime1 = format1.getString(MediaFormat.KEY_MIME);
        try {
            decoder1 = MediaCodec.createDecoderByType(mime1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        decoder1.configure(format1, outputSurface1.getSurface(), null, 0);
        decoder1.start();

        //try ExtractMpegFramesTest method to return a bitmap
        decoder2 = null;
        outputSurface2 = null;
        extractor2 = null;
        File inputFile2 = new File(videoAbsolutePath2);
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
        trackIndex2 = selectTrack(extractor2);
        if (trackIndex2 < 0) {
            throw new RuntimeException("No video track found in " + inputFile2);
        }
        extractor2.selectTrack(trackIndex2);
        format2 = extractor1.getTrackFormat(trackIndex2);
        Log.d(TAG, "Video size is " + format1.getInteger(MediaFormat.KEY_WIDTH) + "x" +
                format1.getInteger(MediaFormat.KEY_HEIGHT));
        // Could use width/height from the MediaFormat to get full-size frames.
        format2.setInteger("rotation-degrees", 0);
        if (format2.containsKey("frame-rate")) {
            frameRate2 = format2.getInteger("frame-rate");
        }else{
            frameRate2 = 30;
        }
        outputSurface2 = new ExtractMpegFramesTest.CodecOutputSurface(format2.getInteger(MediaFormat.KEY_WIDTH),format2.getInteger(MediaFormat.KEY_HEIGHT));//check!
        String mime2 = format2.getString(MediaFormat.KEY_MIME);
        try {
            decoder2 = MediaCodec.createDecoderByType(mime2);
        } catch (IOException e) {
            e.printStackTrace();
        }
        decoder2.configure(format2, outputSurface2.getSurface(), null, 0);
        decoder2.start();

        double frOut = ((double) frameRate1 + (double) frameRate2) / 2;


        DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd'at'HH-mm-ss");
        eMagTime = df2.format(Calendar.getInstance().getTime());
        outPath = directory.getAbsolutePath() + "/FullSize-Tracked-" + eMagTime + ".mp4";
        out = null;
        try {
            out = NIOUtils.writableFileChannel(outPath);
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
        try {
            enc = new AndroidSequenceEncoder(out, Rational.R((int) frOut,1));
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        outPath = directory.getAbsolutePath() + "/SmallSize-Tracked-" + eMagTime + ".mp4";
        out2 = null;
        try {
            out2 = NIOUtils.writableFileChannel(outPath);
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
        try {
            enc2 = new AndroidSequenceEncoder(out2, Rational.R((int) frOut,1));
        } catch (IOException e1) {
            e1.printStackTrace();
        }

    }

    public void combineVideos(){
        initialize();
        boolean isDone = false;

        while (isDone == false) {
            try {
                Bitmap bmp1 = null;
                Bitmap bmp2 = null;

                //pull frames from each video as appropriate
                if (ppOrientation.contains("s")) {
                    if (secondVidFlag == false) {
                        bmp1 = extractFrame1();
                        if (bmp1 == null) {
                            secondVidFlag = true;
                        }
                    } else {
                        bmp1 = extractFrame2();
                    }
                } else if (ppOrder.contains("lr")) {
                    bmp1 = extractFrame1();
                } else if (ppOrder.contains("rl")) {
                    bmp2 = extractFrame2();
                } else {
                    bmp1 = extractFrame1();
                    bmp2 = extractFrame2();
                }
                //check to see if we can close it out
                if (bmp1 == null && bmp2 == null) {
                    try {
                        isDone = true;
                        enc.finish();
                        enc2.finish();
                        NIOUtils.closeQuietly(out);
                        NIOUtils.closeQuietly(out2);
                        if (outputSurface1 != null) {
                            outputSurface1.release();
                            outputSurface1 = null;
                        }
                        if (decoder1 != null) {
                            decoder1.stop();
                            decoder1.release();
                            decoder1 = null;
                        }
                        if (extractor1 != null) {
                            extractor1.release();
                            extractor1 = null;
                        }
                        if (outputSurface2 != null) {
                            outputSurface2.release();
                            outputSurface2 = null;
                        }
                        if (decoder2 != null) {
                            decoder2.stop();
                            decoder2.release();
                            decoder2 = null;
                        }
                        if (extractor2 != null) {
                            extractor2.release();
                            extractor2 = null;
                        }
                        return;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                //if we can't close, set null bitmaps to zero for max compatibility
                if (bmp1 == null) {
                    bmp1 = Bitmap.createBitmap(format1.getInteger(MediaFormat.KEY_WIDTH), format1.getInteger(MediaFormat.KEY_HEIGHT), Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bmp1);
                    canvas.drawColor(Color.BLACK);
                    canvas.drawBitmap(bmp1, 0, 0, null);
                }
                if (bmp2 == null) {
                    bmp2 = Bitmap.createBitmap(format2.getInteger(MediaFormat.KEY_WIDTH), format2.getInteger(MediaFormat.KEY_HEIGHT), Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bmp2);
                    canvas.drawColor(Color.BLACK);
                    canvas.drawBitmap(bmp2, 0, 0, null);
                }
                //intialize and store bitmaps in arraylist for processing
                ArrayList<Bitmap> bmpList = new ArrayList<Bitmap>();
                bmpList.add(bmp1.copy(Bitmap.Config.ARGB_8888, true));
                bmpList.add(bmp2.copy(Bitmap.Config.ARGB_8888, true));
                bmp1.recycle();
                bmp2.recycle();
                //rotate bitmaps if needed
                Bitmap bmp = bmpList.get(0).copy(Bitmap.Config.ARGB_8888, true);
                vp.filePath = videoAbsolutePath1;
                bmp = vp.rotateFrame(bmp, postRotate1);
                bmpList.set(0, bmp.copy(Bitmap.Config.ARGB_8888, true));
                bmp.recycle();
                Bitmap bmptmp = bmpList.get(1).copy(Bitmap.Config.ARGB_8888, true);
                vp.filePath = videoAbsolutePath2;
                bmptmp = vp.rotateFrame(bmptmp, postRotate2);
                bmpList.set(1, bmptmp.copy(Bitmap.Config.ARGB_8888, true));
                bmptmp.recycle();
                //resize bitmaps if needed
                if (ppOrientation.contains("stacked") == false) {
                    bmpList = vp.scaleHeightAndWidth(bmpList.get(0), bmpList.get(1), ppSize, ppOrientation);
                } else {
                    Bitmap bmptmp2 = bmpList.get(0).copy(Bitmap.Config.ARGB_8888, true);
                    bmptmp2 = vp.resizeForStacked(bmptmp2, bmpList.get(0).getHeight(), bmpList.get(1).getHeight(), bmpList.get(0).getWidth(), bmpList.get(1).getWidth());
                    bmpList.set(0, bmptmp2.copy(Bitmap.Config.ARGB_8888, true));
                    bmptmp2.recycle();
                }
                //combine bitmaps if needed
                Bitmap bmpJoined = null;
                if (ppOrientation.contains("lr")) {
                    bmpJoined = vp.combineImagesLR(bmpList.get(0), bmpList.get(1));
                } else if (ppOrientation.contains("tb")) {
                    bmpJoined = vp.combineImagesUD(bmpList.get(0), bmpList.get(1));
                } else if (ppOrientation.contains("rl")) {
                    bmpJoined = vp.combineImagesLR(bmpList.get(1), bmpList.get(0));
                } else if (ppOrientation.contains("bt")) {
                    bmpJoined = vp.combineImagesUD(bmpList.get(1), bmpList.get(0));
                }
                enc.encodeImage(bmpJoined);
                enc2.encodeImage(vp.resizeForInstagram(bmpJoined));
                bmpJoined.recycle();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Bitmap getPreviewFrame() {
        initialize();

        try {
            Bitmap bmp1 = null;
            Bitmap bmp2 = null;

            //pull frames from each video as appropriate
            if (ppOrientation.contains("lr")) {
                bmp1 = extractFrame1();
                bmp2 = extractFrame2();
            } else if (ppOrientation.contains("rl")) {
                bmp2 = extractFrame2();
                bmp1 = extractFrame1();
            } else if (ppOrientation.contains("s")) {
                if (secondVidFlag == false) {
                    bmp1 = extractFrame1();
                    if (bmp1 == null) {
                        secondVidFlag = true;
                    }
                } else {
                    bmp1 = extractFrame2();
                }
            }
            //if we can't close, set null bitmaps to zero for max compatibility
            if (bmp1 == null) {
                bmp1 = Bitmap.createBitmap(format1.getInteger(MediaFormat.KEY_WIDTH), format1.getInteger(MediaFormat.KEY_HEIGHT), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bmp1);
                canvas.drawColor(Color.BLACK);
                canvas.drawBitmap(bmp1, 0, 0, null);
            }
            if (bmp2 == null) {
                bmp2 = Bitmap.createBitmap(format2.getInteger(MediaFormat.KEY_WIDTH), format2.getInteger(MediaFormat.KEY_HEIGHT), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bmp2);
                canvas.drawColor(Color.BLACK);
                canvas.drawBitmap(bmp2, 0, 0, null);
            }
            //intialize and store bitmaps in arraylist for processing
            ArrayList<Bitmap> bmpList = new ArrayList<Bitmap>();
            bmpList.add(bmp1.copy(Bitmap.Config.ARGB_8888, true));
            bmpList.add(bmp2.copy(Bitmap.Config.ARGB_8888, true));
            bmp1.recycle();
            bmp2.recycle();
            //rotate bitmaps if needed
            Bitmap bmp = bmpList.get(0).copy(Bitmap.Config.ARGB_8888, true);
            vp.filePath = videoAbsolutePath1;
            bmp = vp.rotateFrame(bmp, postRotate1);
            bmpList.set(0, bmp.copy(Bitmap.Config.ARGB_8888, true));
            bmp.recycle();
            Bitmap bmptmp = bmpList.get(1).copy(Bitmap.Config.ARGB_8888, true);
            vp.filePath = videoAbsolutePath2;
            bmptmp = vp.rotateFrame(bmptmp, postRotate2);
            bmpList.set(1, bmptmp.copy(Bitmap.Config.ARGB_8888, true));
            bmptmp.recycle();
            //resize bitmaps if needed
            if (ppOrientation.contains("stacked") == false) {
                bmpList = vp.scaleHeightAndWidth(bmpList.get(0), bmpList.get(1), ppSize, ppOrientation);
            } else {
                Bitmap bmptmp2 = bmpList.get(0).copy(Bitmap.Config.ARGB_8888, true);
                bmptmp2 = vp.resizeForStacked(bmptmp2, bmpList.get(0).getHeight(), bmpList.get(1).getHeight(), bmpList.get(0).getWidth(), bmpList.get(1).getWidth());
                bmpList.set(0, bmptmp2.copy(Bitmap.Config.ARGB_8888, true));
                bmptmp2.recycle();
            }
            //combine bitmaps if needed
            Bitmap bmpJoined = null;
            if (ppOrientation.contains("lr")) {
                bmpJoined = vp.combineImagesLR(bmpList.get(0), bmpList.get(1));
            } else if (ppOrientation.contains("tb")) {
                bmpJoined = vp.combineImagesUD(bmpList.get(0), bmpList.get(1));
            } else if (ppOrientation.contains("rl")) {
                bmpJoined = vp.combineImagesLR(bmpList.get(1), bmpList.get(0));
            } else if (ppOrientation.contains("bt")) {
                bmpJoined = vp.combineImagesUD(bmpList.get(1), bmpList.get(0));
            }
            return bmpJoined;
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (outputSurface1 != null) {
            outputSurface1.release();
            outputSurface1 = null;
        }
        if (decoder1 != null) {
            decoder1.stop();
            decoder1.release();
            decoder1 = null;
        }
        if (extractor1 != null) {
            extractor1.release();
            extractor1 = null;
        }
        if (outputSurface2 != null) {
            outputSurface2.release();
            outputSurface2 = null;
        }
        if (decoder2 != null) {
            decoder2.stop();
            decoder2.release();
            decoder2 = null;
        }
        if (extractor2 != null) {
            extractor2.release();
            extractor2 = null;
        }
        Bitmap bmpJoined = null;
        return bmpJoined;
    }



    public Bitmap extractFrame1()throws IOException{
        final int TIMEOUT_USEC = 100000;
        ByteBuffer[] decoderInputBuffers = decoder1.getInputBuffers();
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        int inputChunk = 0;
        int decodeCount = 0;
        Bitmap bmp = null;

        boolean outputDone = false;
        boolean inputDone = false;

        Log.d(TAG, "loop");
        // Feed more data to the decoder.
        while(!outputDone) {
            if (!inputDone) {
                int inputBufIndex = decoder1.dequeueInputBuffer(TIMEOUT_USEC);
                if (inputBufIndex >= 0) {
                    ByteBuffer inputBuf = decoderInputBuffers[inputBufIndex];
                    // Read the sample data into the ByteBuffer.  This neither respects nor
                    // updates inputBuf's position, limit, etc.
                    int chunkSize = extractor1.readSampleData(inputBuf, 0);
                    if (chunkSize < 0) {
                        // End of stream -- send empty frame with EOS flag set.
                        decoder1.queueInputBuffer(inputBufIndex, 0, 0, 0L,
                                MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        inputDone = true;
                        Log.d(TAG, "sent input EOS");
                    } else {
                        if (extractor1.getSampleTrackIndex() != trackIndex1) {
                            Log.w(TAG, "WEIRD: got sample from track " +
                                    extractor1.getSampleTrackIndex() + ", expected " + trackIndex1);
                        }
                        long presentationTimeUs = extractor1.getSampleTime();
                        decoder1.queueInputBuffer(inputBufIndex, 0, chunkSize,
                                presentationTimeUs, 0 /*flags*/);

                        Log.d(TAG, "submitted frame " + inputChunk + " to dec, size=" +
                                chunkSize);

                        inputChunk++;
                        extractor1.advance();
                    }
                } else {
                    Log.d(TAG, "input buffer not available");
                }
            }

            if (!outputDone) {
                int decoderStatus = decoder1.dequeueOutputBuffer(info, TIMEOUT_USEC);
                if (decoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    // no output available yet
                    Log.d(TAG, "no output from decoder available");
                } else if (decoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    // not important for us, since we're using Surface
                    Log.d(TAG, "decoder output buffers changed");
                } else if (decoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    MediaFormat newFormat = decoder1.getOutputFormat();
                    Log.d(TAG, "decoder output format changed: " + newFormat);
                } else if (decoderStatus < 0) {
                    fail("unexpected result from decoder.dequeueOutputBuffer: " + decoderStatus);
                } else { // decoderStatus >= 0
                    Log.d(TAG, "surface decoder given buffer " + decoderStatus +
                            " (size=" + info.size + ")");
                    if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        Log.d(TAG, "output EOS");
                        outputDone = true;
                        secondVidFlag = true;
                    }

                    boolean doRender = (info.size != 0);

                    // As soon as we call releaseOutputBuffer, the buffer will be forwarded
                    // to SurfaceTexture to convert to a texture.  The API doesn't guarantee
                    // that the texture will be available before the call returns, so we
                    // need to wait for the onFrameAvailable callback to fire.
                    decoder1.releaseOutputBuffer(decoderStatus, doRender);
                    if (doRender) {
                        Log.d(TAG, "awaiting decode of frame " + decodeCount);
                        outputSurface1.awaitNewImage();
                        outputSurface1.drawImage(true);
                        bmp = outputSurface1.returnFrame();
                        outputDone = true;

                        return bmp;
                    }
                }
            }
        }
        return bmp;
    }

    public Bitmap extractFrame2()throws IOException{
        final int TIMEOUT_USEC = 100000;
        ByteBuffer[] decoderInputBuffers = decoder2.getInputBuffers();
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        int inputChunk = 0;
        int decodeCount = 0;
        Bitmap bmp = null;

        boolean outputDone2 = false;
        boolean inputDone2 = false;
            Log.d(TAG, "loop");

        // Feed more data to the decoder.
        while (!outputDone2) {
            if (!inputDone2) {
                int inputBufIndex = decoder2.dequeueInputBuffer(TIMEOUT_USEC);
                if (inputBufIndex >= 0) {
                    ByteBuffer inputBuf = decoderInputBuffers[inputBufIndex];
                    // Read the sample data into the ByteBuffer.  This neither respects nor
                    // updates inputBuf's position, limit, etc.
                    int chunkSize = extractor2.readSampleData(inputBuf, 0);
                    if (chunkSize < 0) {
                        // End of stream -- send empty frame with EOS flag set.
                        decoder2.queueInputBuffer(inputBufIndex, 0, 0, 0L,
                                MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        inputDone2 = true;
                        Log.d(TAG, "sent input EOS");
                    } else {
                        if (extractor2.getSampleTrackIndex() != trackIndex2) {
                            Log.w(TAG, "WEIRD: got sample from track " +
                                    extractor2.getSampleTrackIndex() + ", expected " + trackIndex2);
                        }
                        long presentationTimeUs = extractor2.getSampleTime();
                        decoder2.queueInputBuffer(inputBufIndex, 0, chunkSize,
                                presentationTimeUs, 0 /*flags*/);

                        Log.d(TAG, "submitted frame " + inputChunk + " to dec, size=" +
                                chunkSize);

                        inputChunk++;
                        extractor2.advance();
                    }
                } else {
                    Log.d(TAG, "input buffer not available");
                }
            }

            if (!outputDone2) {
                int decoderStatus = decoder2.dequeueOutputBuffer(info, TIMEOUT_USEC);
                if (decoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    // no output available yet
                    Log.d(TAG, "no output from decoder available");
                } else if (decoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    // not important for us, since we're using Surface
                    Log.d(TAG, "decoder output buffers changed");
                } else if (decoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    MediaFormat newFormat = decoder2.getOutputFormat();
                    Log.d(TAG, "decoder output format changed: " + newFormat);
                } else if (decoderStatus < 0) {
                    fail("unexpected result from decoder.dequeueOutputBuffer: " + decoderStatus);
                } else { // decoderStatus >= 0
                    Log.d(TAG, "surface decoder given buffer " + decoderStatus +
                            " (size=" + info.size + ")");
                    if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        Log.d(TAG, "output EOS");
                        outputDone2 = true;
                    }

                    boolean doRender = (info.size != 0);

                    // As soon as we call releaseOutputBuffer, the buffer will be forwarded
                    // to SurfaceTexture to convert to a texture.  The API doesn't guarantee
                    // that the texture will be available before the call returns, so we
                    // need to wait for the onFrameAvailable callback to fire.
                    decoder2.releaseOutputBuffer(decoderStatus, doRender);
                    if (doRender) {
                        Log.d(TAG, "awaiting decode of frame " + decodeCount);
                        outputSurface2.awaitNewImage();
                        outputSurface2.drawImage(true);
                        bmp = outputSurface2.returnFrame();
                        outputDone2 = true;
                        return bmp;
                    }
                }
            }
        }
        return bmp;
    }

    private int selectTrack(MediaExtractor extractor) {
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






}
