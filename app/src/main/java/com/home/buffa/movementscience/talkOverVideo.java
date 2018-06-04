package com.home.buffa.movementscience;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.DataSource;
import com.googlecode.mp4parser.FileDataSourceImpl;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.tracks.AACTrackImpl;
import com.googlecode.mp4parser.authoring.tracks.h264.H264TrackImpl;
import com.ipaulpro.afilechooser.utils.FileUtils;

import org.jcodec.api.SequenceEncoder;
import org.jcodec.api.android.AndroidSequenceEncoder;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.io.SeekableByteChannel;
import org.jcodec.common.model.Rational;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import wseemann.media.FFmpegMediaMetadataRetriever;

import static android.media.MediaRecorder.AudioSource.MIC;

public class talkOverVideo extends Activity implements TextureView.SurfaceTextureListener{

    private static final String TAG = talkOverVideo.class.getName();

    private MediaPlayer mMediaPlayer;
    private TextureView mPreview;

    float vH;
    float vW;
    float vHOrig;
    float vWOrig;

    String videoAbsolutePath;

    TextureView textureView;
    ImageView imageView;

    FrameLayout frameLayout;
    int bmpCount = 0;

    SeekBar seekbar;

    double frameRate;
    int currentFrame = 0;
    double duration;
    double mspf;
    double fps;

    int h1;
    int w1;

    int rotateDegreesPostProcess;

    FFmpegMediaMetadataRetriever mmr;

    Boolean saveBMPFlag = false;
    Boolean bmpFromImageView = false;

    int frameCounter = 0;

    ArrayList<String> voiceOverBmpPaths = new ArrayList<>();
    String audioFileName;

    Thread recordBitmapInBackGround;
    Thread recordAudioInBackGround;
    Thread mergeInBackGround;

    MediaRecorder mediaRecorder;

    File mypath;
    File tmpVid;

    String eMagTime;

    String outPath;
    String outPathFinal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_talk_over_video);
        mPreview = findViewById(R.id.textureView);
        seekbar = findViewById(R.id.seekbar);
        imageView = findViewById(R.id.imageView);
        imageView.setVisibility(ImageView.INVISIBLE);

        //need intent with video path
        Intent intentReceive = getIntent();
        videoAbsolutePath = intentReceive.getExtras().getString("videoPath");

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String rotDeg = sharedPref.getString("pref_rotateDegreesPostProcess","0");
        rotateDegreesPostProcess = Integer.valueOf(rotDeg);

        //start mmr for video
        mmr = new FFmpegMediaMetadataRetriever();
        mmr.setDataSource(videoAbsolutePath);
        //determine number of frames
        String fr = mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_FRAMERATE);
        frameRate = Double.valueOf(fr);
        mspf = 1/(frameRate / 1000);//frames per microsecond
        String dur = mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION);//in ms
        duration = Double.valueOf(dur);//in ms
        int frames = (int) Math.round( (duration/mspf));

        //set max frames for seekbar
        seekbar.setMax(frames);
        seekbar.setProgress(0);

        final Canvas[] canvas = new Canvas[1];
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //grab frames based on integer in progressChangedValue
                progressChangedValue = progress;
                int pos = (int) Math.round( ((double)progressChangedValue * mspf));
                //mMediaPlayer.seekTo(pos);//wants input in ms
                Bitmap bmp = mmr.getFrameAtTime(pos*1000,FFmpegMediaMetadataRetriever.OPTION_CLOSEST);
                if (bmp != null) {
                    //Matrix matrix = new Matrix();
                    //matrix.preRotate(rotateDegreesPostProcess);
                    //bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
                    //bmp = Bitmap.createScaledBitmap(bmp, w1, h1, false);
                    imageView.setImageBitmap(bmp);
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                String dur = mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION);//in ms
                duration = Double.valueOf(dur);//in ms
                int frames = (int) Math.round( (duration/mspf));
                //set max frames for seekbar
                seekbar.setMax(frames);
                textureView.setVisibility(TextureView.INVISIBLE);
                imageView.setVisibility(ImageView.VISIBLE);
                bmpFromImageView = false;
                //set image view as visible
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                //possibly set some values for things, likely nothing
                currentFrame = progressChangedValue;
            }
        });

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

        initView();
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i2) {
        Surface surface = new Surface(surfaceTexture);

        try {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setDataSource(videoAbsolutePath);
            mMediaPlayer.setSurface(surface);
            mMediaPlayer.setLooping(false);
            mMediaPlayer.prepareAsync();


            // Play video when the media source is ready for playback.
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    vH = mMediaPlayer.getVideoHeight();
                    vW = mMediaPlayer.getVideoWidth();
                    //updateTextureViewSize(mPreview.getWidth(),mPreview.getHeight());
                    //textureView.setLayoutParams(new FrameLayout.LayoutParams(vW,vH));
                    mMediaPlayer.seekTo(200);
                    h1 = mPreview.getHeight();
                    w1 = mPreview.getWidth();
                    duration = mMediaPlayer.getDuration();
                    int frames = (int) Math.round( (duration/mspf));
                    //set max frames for seekbar
                    seekbar.setMax(frames);
                }
            });

        } catch (IllegalArgumentException e) {
            Log.d(TAG, e.getMessage());
        } catch (SecurityException e) {
            Log.d(TAG, e.getMessage());
        } catch (IllegalStateException e) {
            Log.d(TAG, e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i2) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        Bitmap bmp = getBitmap();
        bmpCount ++;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    private void saveBitmap(Bitmap bmp1){
        frameCounter++;
        String format = String.format("%%0%dd", 6);
        String result = String.format(format, frameCounter);
        File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        // Create imageDir
        File mypath=new File(directory,"TalkFrame-" + result + ".png");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bmp1.compress(Bitmap.CompressFormat.PNG, 100, fos);
            voiceOverBmpPaths.add(mypath.toString());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void beginRecordingAudio(){
        AsyncTask.execute(new Runnable(){
            @Override
            public void run() {
            //Your recording portion of the code goes here.
                mediaRecorder = new MediaRecorder();
                mediaRecorder.setAudioSource(MIC);
                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.HE_AAC);
                    mediaRecorder.setAudioEncodingBitRate(48000);
                } else {
                    mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                    mediaRecorder.setAudioEncodingBitRate(64000);
                }
                mediaRecorder.setAudioSamplingRate(16000);
                File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                mypath = new File(directory,"TalkFrame.mp4");
                audioFileName = mypath.getAbsolutePath();
                mediaRecorder.setOutputFile(mypath.getAbsolutePath());
                try{
                    mediaRecorder.prepare();
                    mediaRecorder.start();
                }catch (IOException e) {
                    Log.e("Voice Recorder", "prepare() failed "+e.getMessage());
                }
            }
        });
    }

    private void beginRecordingBitmap(){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                //Your recording portion of the code goes here.
                Bitmap bitmap;
                while (saveBMPFlag == true && bmpFromImageView == false) {
                    BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
                    if (drawable != null) {
                        bitmap = drawable.getBitmap();
                        saveBitmap(bitmap);
                    }
                }
                while (saveBMPFlag == true && bmpFromImageView == true) {
                    bitmap = mPreview.getBitmap();
                    saveBitmap(bitmap);
                }
            }
        });
    }

    private void mergeVideoAudio(){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                AndroidSequenceEncoder enc = null;
                //Your recording portion of the code goes here.
                DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd'at'HH-mm-ss");
                eMagTime = df2.format(Calendar.getInstance().getTime());
                File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
                outPath = directory.getAbsolutePath() + "/VoiceoverTmp-" + eMagTime + ".mp4";
                tmpVid = new File(outPath);
                SeekableByteChannel out = null;
                //determine total time images were being recorded

                try {
                    out = NIOUtils.writableFileChannel(outPath);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                try {
                    enc = new AndroidSequenceEncoder(out, Rational.R((int)(Math.round(fps)),1));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //build initial image based video
                if (enc != null) {
                    for (int i = 0; i < voiceOverBmpPaths.size(); i++) {
                        String msg = String.valueOf(i/voiceOverBmpPaths.size()) + "% completed merging voice and video frames";
                        MainActivity.mBuilder.setContentText(msg);
                        MainActivity.notificationManager.notify(MainActivity.notificationID, MainActivity.mBuilder.build());
                        Bitmap writeBmp = BitmapFactory.decodeFile(voiceOverBmpPaths.get(i));
                        try {
                            enc.encodeImage(writeBmp);
                            File f = new File(voiceOverBmpPaths.get(i));
                            f.delete();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        enc.finish();
                        NIOUtils.closeQuietly(out);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                //add audio
                MediaMultiplexer mm = new MediaMultiplexer();
                mm.startMuxing(getApplicationContext());
                updateTapToPlay();
            }
        });
    }

    private void initView() {
        textureView = findViewById(R.id.textureView);
        textureView.setSurfaceTextureListener(this);
    }

    private void updateTapToPlay(){
        Intent intent = new Intent(getApplicationContext(), playVideo.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        File file = new File(outPathFinal);
        Uri vidUriActual = Uri.fromFile(file);
        intent.putExtra("vidUri",vidUriActual.toString());
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        MainActivity.mBuilder.setContentIntent(pendingIntent);
        MainActivity.mBuilder.setContentText("Processing completed! Click to play.");
        MainActivity.notificationManager.notify(MainActivity.notificationID, MainActivity.mBuilder.build());
    }

    public Bitmap getBitmap(){
        return  mPreview.getBitmap();
    }

    private void updateTextureViewSize(int viewWidth, int viewHeight) {
        float scaleX = 1.0f;
        float scaleY = 1.0f;

        vHOrig = vH;
        vWOrig = vW;

        //vH and vW and viewWidth and viewHeight

        if (vH > viewHeight){
            vH = vH * (viewHeight / vH);
            vW = vW * (viewHeight / vH);
            if (vW > viewWidth){
                vW = vW * (viewWidth / vW);
                vH = vH * (viewWidth / vW);
            }
        }else if (vW > viewWidth){
            vW = vW * (viewWidth / vW);
            vH = vH * (viewWidth / vW);
            if (vH > viewHeight){
                vH = vH * (viewHeight / vH);
                vW = vW * (viewHeight / vH);
            }
        }

        scaleX = vW / viewWidth;
        scaleY = vH / viewHeight;


        // Calculate pivot points, in our case crop from center
        int pivotPointX = viewWidth / 2;
        int pivotPointY = viewHeight / 2;

        Matrix matrix = new Matrix();
        matrix.setScale(scaleX, scaleY, pivotPointX, pivotPointY);

        mPreview.setTransform(matrix);
        mPreview.setLayoutParams(new FrameLayout.LayoutParams(viewWidth, viewHeight));
        imageView.setMaxHeight(viewHeight);
        imageView.setMaxWidth(viewWidth);
    }

    public void playVideo(){
        if (mMediaPlayer != null){
            mMediaPlayer.start();
        }
    }

    public void stopVideo(){
        if (mMediaPlayer != null){
            mMediaPlayer.stop();
        }
    }

    public void beginRecording(View view){
        saveBMPFlag = true;
        beginRecordingAudio();
        beginRecordingBitmap();
    }

    public void endRecording(View view){
        saveBMPFlag = false;
        mediaRecorder.stop();
        mediaRecorder.release();

        DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd'at'HH-mm-ss");
        eMagTime = df2.format(Calendar.getInstance().getTime());
        File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        outPath = directory.getAbsolutePath() + "/Voiceover-" + eMagTime + ".mp4";
        outPathFinal = outPath;
        SeekableByteChannel out = null;
        //determine total time images were being recorded
        Long lastmodified1;
        Long lastmodifiedEnd;
        File f = new File(voiceOverBmpPaths.get(0));
        lastmodified1 = f.lastModified();
        f = new File(voiceOverBmpPaths.get(voiceOverBmpPaths.size()-1));
        lastmodifiedEnd = f.lastModified();
        long recordTime = lastmodifiedEnd - lastmodified1;
        fps = (double)voiceOverBmpPaths.size() / (double)(recordTime / 1000);
        mergeVideoAudio();
        Toast.makeText(this, "Merging audio and captured video, may take a minute or two depending on length", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);

    }


    public class MediaMultiplexer {
        private static final int MAX_SAMPLE_SIZE = 8 * 2880 * 2880;

        public void startMuxing(Context context) {
            MediaMuxer muxer = null;
            MediaFormat VideoFormat = null;
            File d = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
            String Path = d.getAbsolutePath() + "/Voiceover-" + eMagTime + ".mp4";
            String outputVideoFileName = Path;
            try {
                muxer = new MediaMuxer(outputVideoFileName, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            } catch (IOException e) {
                e.printStackTrace();
            }
            MediaExtractor extractorVideo = new MediaExtractor();
            try {
                extractorVideo.setDataSource(outPath);
                int tracks = extractorVideo.getTrackCount();
                for (int i = 0; i < tracks; i++) {
                    MediaFormat mf = extractorVideo.getTrackFormat(i);
                    String mime = mf.getString(MediaFormat.KEY_MIME);
                    if (mime.startsWith("video/")) {
                        extractorVideo.selectTrack(i);
                        VideoFormat = extractorVideo.getTrackFormat(i);
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            MediaExtractor extractorAudio = new MediaExtractor();
            try {
                audioFileName = FileUtils.getPath(getApplicationContext(),Uri.fromFile(mypath));;
                extractorAudio.setDataSource(audioFileName);
                int tracks = extractorAudio.getTrackCount();
                extractorAudio.selectTrack(0);

                MediaFormat AudioFormat = extractorAudio.getTrackFormat(0);
                int audioTrackIndex = muxer.addTrack(AudioFormat);
                int videoTrackIndex = muxer.addTrack(VideoFormat);

                boolean sawEOS = false;
                boolean sawAudioEOS = false;
                int bufferSize = MAX_SAMPLE_SIZE;
                ByteBuffer dstBuf = ByteBuffer.allocate(bufferSize);
                int offset = 100;
                MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                muxer.start();

                while (!sawEOS) {
                    bufferInfo.offset = offset;
                    bufferInfo.size = extractorVideo.readSampleData(dstBuf, offset);
                    if (bufferInfo.size < 0) {
                        sawEOS = true;
                        bufferInfo.size = 0;
                    } else {
                        bufferInfo.presentationTimeUs = extractorVideo.getSampleTime();
                        bufferInfo.flags = extractorVideo.getSampleFlags();
                        int trackIndex = extractorVideo.getSampleTrackIndex();
                        muxer.writeSampleData(videoTrackIndex, dstBuf, bufferInfo);
                        extractorVideo.advance();
                    }
                }
                ByteBuffer audioBuf = ByteBuffer.allocate(bufferSize);
                while (!sawAudioEOS) {
                    bufferInfo.offset = offset;
                    bufferInfo.size = extractorAudio.readSampleData(audioBuf, offset);
                    if (bufferInfo.size < 0) {
                        sawAudioEOS = true;
                        bufferInfo.size = 0;
                    } else {
                        bufferInfo.presentationTimeUs = extractorAudio.getSampleTime();
                        bufferInfo.flags = extractorAudio.getSampleFlags();
                        int trackIndex = extractorAudio.getSampleTrackIndex();
                        muxer.writeSampleData(audioTrackIndex, audioBuf, bufferInfo);
                        extractorAudio.advance();
                    }
                }
                muxer.stop();
                muxer.release();
                tmpVid.delete();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }

    }





}
