package com.home.buffa.movementscience;

import android.app.Application;
import android.content.Context;

/**
 * Created by buffa on 6/21/2017.
 */


public class MyApplication extends Application {

    private static Context context;

    public void onCreate() {
        super.onCreate();
        MyApplication.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return MyApplication.context;
    }
}

