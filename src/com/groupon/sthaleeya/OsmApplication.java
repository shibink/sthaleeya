package com.groupon.sthaleeya;

import android.app.Application;
import android.content.Context;
import android.util.Log;

/**
 * Starting point of the application
 *
 */
public class OsmApplication extends Application {
    private static final String TAG = "OsmApplication";
    private static OsmApplication instance = null;

    public OsmApplication() {
        instance = this;
    }

    public static Context getAppContext() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Application onCreate");
    }
}