package com.home.buffa.movementscience;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class gaitStability extends Activity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mSensor;
    int mDelay;

    int sampEnFlag = 1;
    int lyeFlag = 0;

    int collectTimeInSeconds = 10;

    double eMag;
    ArrayList<Double> eMagList = new ArrayList<Double>();

    ArrayList<Float> accelerometerSamplesX = new ArrayList<Float>();
    ArrayList<Float> accelerometerSamplesY = new ArrayList<Float>();
    ArrayList<Float> accelerometerSamplesZ = new ArrayList<Float>();
    ArrayList<Float> accelerometerSamplesMag = new ArrayList<Float>();
    ArrayList<Float> accelerometerSampleTimes = new ArrayList<Float>();

    ArrayList<Float> x = new ArrayList<Float>();
    ArrayList<Float> y = new ArrayList<Float>();
    ArrayList<Float> z = new ArrayList<Float>();
    ArrayList<Float> mag = new ArrayList<Float>();
    ArrayList<Float> time = new ArrayList<Float>();
    ArrayList<Float> m = new ArrayList<Float>();

    File FILES_DIR = Environment.getExternalStorageDirectory();
    String SIGNAL_FILE = "accelSignal.txt";
    String SEN_FILE = "accelSEn.txt";
    String LYE_FILE = "accelLYE.txt";
    String USED_FILE = "usedData.txt";
    String CHANGED_FILE = "senChangeLog.txt";
    File signalFile;
    File senFile;
    File lyeFile;
    File usedFile;
    File senChangeFile;
    FileWriter signalWriter;
    FileWriter senWriter;
    FileWriter lyeWriter;
    FileWriter usedWriter;
    FileWriter changeWriter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){
            // Success! There's a magnetometer.
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

//            mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_FASTEST);


        }
        else {
            // Failure! No linear accelerometer.
        }
        setContentView(R.layout.activity_gait_stability);
    }

    private class accelerometerAnalysisSEn extends AsyncTask<Void, Void, Void> {

        protected Void doInBackground(Void... params) {

//            double eX = new nonlinearMath().sampen(x);
//            double eY = new nonlinearMath().sampen(y);
//            double eZ = new nonlinearMath().sampen(z);
            m.clear();
            m.addAll(mag);
            DateFormat df2 = new SimpleDateFormat("HH:mm:ss");
            String eMagTime = df2.format(Calendar.getInstance().getTime());

            eMag = new nonlinearMath().sampen(m);
            eMagList.add(eMag);
            if (eMagList.size() > 24){
                double eMagStart = 0;
                for(int i = 16; i < 22; i++){
                    eMagStart = eMagStart + eMagList.get(i);
                }
                eMagStart = eMagStart / 6;

                double eMagEnd = 0;
                for(int i =0; i < 6; i++){
                    eMagEnd = eMagEnd + eMagList.get(eMagList.size()-i-1);
                }
                eMagEnd = eMagEnd / 6;

                double eMagDiff = eMagStart - eMagEnd;
                try {
                    changeWriter.append(Double.toString(eMagDiff) + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            try{
                senWriter.append(eMagTime);
                senWriter.append("\t");
                senWriter.append(Double.toString(eMag));
                senWriter.append("\n");
                int ct=0;
                for (Float fl : m) {
                    if(ct < m.size()) {
                        usedWriter.append(Float.toString(fl) + "\t");
                        ct++;
                    }else{
                        usedWriter.append(Float.toString(fl) + "\n");
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onProgressUpdate(Void... progress) {

        }

        protected void onPostExecute(Void result) {

        }
    }

    private class accelerometerAnalysisLyE extends AsyncTask<Void, Void, Void> {

        protected Void doInBackground(Void... params) {

            return null;
        }

        protected void onProgressUpdate(Void... progress) {

        }

        protected void onPostExecute(Void result) {

        }
    }



    @Override
    public final void onSensorChanged(SensorEvent event) {
        accelerometerSamplesX.add(event.values[0]);
        accelerometerSamplesY.add(event.values[1]);
        accelerometerSamplesZ.add(event.values[2]);
        accelerometerSamplesMag.add((float) (Math.sqrt( ((double)event.values[0]*(double)event.values[0]) + ((double)event.values[1]*((double)event.values[1]) + ((double)event.values[2]- 9.81)*((double)event.values[2]- 9.81) ) ) ));
        accelerometerSampleTimes.add((float) event.timestamp);
        float timeCollected = accelerometerSampleTimes.get(accelerometerSampleTimes.size()-1) - accelerometerSampleTimes.get(0);
        // Do something with this sensor values.
        if ((timeCollected / (float) 1000000000) > collectTimeInSeconds){
            x.addAll(accelerometerSamplesX);
            y.addAll(accelerometerSamplesY);
            z.addAll(accelerometerSamplesZ);
            time.addAll(accelerometerSampleTimes);
            mag.addAll(accelerometerSamplesMag);
            if (sampEnFlag == 1) {
                new accelerometerAnalysisSEn().execute(null, null, null);
            }
            if (sampEnFlag == 0){
                eMag = 0;
                DateFormat df2 = new SimpleDateFormat("HH:mm:ss");
                String eMagTime = df2.format(Calendar.getInstance().getTime());
                try{
                    senWriter.append(eMagTime);
                    senWriter.append("\t");
                    senWriter.append(Double.toString(eMag));
                    senWriter.append("\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            new accelerometerAnalysisLyE().execute(null,null,null);

            TextView textViewCSEn = (TextView) findViewById(R.id.currentSEn);

            GraphView graph = (GraphView) findViewById(R.id.graph);
            //plot accel data
            //clone current data set


            for (int i = 0; i < mag.size(); i++){
                try {
                    signalWriter.append( Float.toString( (time.get(i) - time.get(1))/ (float) 1000000000 ) );
                    signalWriter.append("\t");
                    signalWriter.append(Float.toString(mag.get(i)));
                    signalWriter.append("\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


            graph.removeAllSeries();
            //create DataPoint objects for serieses

            DataPoint[] dpmag = new DataPoint[mag.size()];
            for (int i = 0; i < x.size(); i++){
                dpmag[i] = new DataPoint(((time.get(i)-time.get(1)) / 1000000000),mag.get(i));
            }
            LineGraphSeries<DataPoint> seriesMag = new LineGraphSeries<>(dpmag);
            seriesMag.setColor(Color.RED);
            String title = "Acceleration Magnitude";
            graph.setTitle(title);
            graph.getViewport().setMinX(0);
            graph.getViewport().setMaxX(((time.get(time.size()-1)-time.get(1)) / 1000000000));
            graph.getViewport().setXAxisBoundsManual(true);
            graph.addSeries(seriesMag);
            DecimalFormat df = new DecimalFormat("#.####");
            String num = df.format(eMag);
            textViewCSEn.setText(num);


            accelerometerSamplesX.clear();
            accelerometerSamplesY.clear();
            accelerometerSamplesZ.clear();
            accelerometerSamplesMag.clear();
            accelerometerSampleTimes.clear();

            x.clear();
            y.clear();
            z.clear();
            time.clear();
            mag.clear();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {
        super.onResume();
//        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause() {
        super.onPause();
//        mSensorManager.unregisterListener(this);
    }

    public void stopAccelerometer(View v){
        mSensorManager.unregisterListener(this, mSensor);
        try {
            signalWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            senWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            usedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            changeWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Button startAccelButt = (Button) findViewById(R.id.buttonStartAccelerometer);
        startAccelButt.setBackgroundColor(Color.RED);
        Button stopAccelButt = (Button) findViewById(R.id.buttonStopAccelerometer);
        stopAccelButt.setBackgroundColor(Color.GREEN);
    }

    public void startAccelerometer(View v){
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
        mDelay = 100;
        DateFormat df = new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z");
        String date = df.format(Calendar.getInstance().getTime());
        signalFile = new File(FILES_DIR,date + "-" + SIGNAL_FILE);
        senFile = new File(FILES_DIR,date + "-" + SEN_FILE);
        usedFile = new File(FILES_DIR,date + "-" + USED_FILE);
        senChangeFile = new File(FILES_DIR,date + "-" + CHANGED_FILE);
        try {
            signalWriter = new FileWriter(signalFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            senWriter = new FileWriter(senFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            usedWriter = new FileWriter(usedFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            changeWriter = new FileWriter(senChangeFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Button startAccelButt = (Button) findViewById(R.id.buttonStartAccelerometer);
        startAccelButt.setBackgroundColor(Color.GREEN);
        Button stopAccelButt = (Button) findViewById(R.id.buttonStopAccelerometer);
        stopAccelButt.setBackgroundColor(Color.RED);

    }






}
