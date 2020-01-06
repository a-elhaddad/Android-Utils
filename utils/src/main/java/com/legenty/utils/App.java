package com.legenty.utils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.legenty.utils.utils.AnalyticsHelper;
import com.legenty.utils.utils.DateUtils;

import java.util.Locale;


public class App extends Application  {
    private static final String TAG = "App";
    private static App sInstance;
    private AnalyticsHelper analyticsHelper;
    private static Resources res;

    public static synchronized App getInstance() {
        App app;
        synchronized (App.class) {
            app = sInstance;
        }
        return app;
    }

    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: app created");
        sInstance = this;
        res = getResources();
        Log.d(TAG, "onCreate: "+ DateUtils.getDateTimeSql());

/*        NeverCrash.init(new NeverCrash.CrashHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                Log.d("Jenly", Log.getStackTraceString(e));
//                e.printStackTrace();
                showToast(e.getMessage());


            }
        });*/
    }


    public void logEvent(String view, String event, Integer id) {
        try {
            if (analyticsHelper == null)
                analyticsHelper = new AnalyticsHelper(this);
            analyticsHelper.addEvent(view, event, id, AnalyticsHelper.EVENT_TYPE_APP_USAGE);
        } catch (Exception e) {
            Log.d(TAG, "logEvent: " + e);
        }
    }


    public static Resources getResourses() {
        return res;
    }

    public String getLanguage() {
        String lang;

        lang = Locale.getDefault().getLanguage();

        if (lang != null && lang.length() > 3) {
            lang = lang.substring(0, 2).toLowerCase();
        }
        return lang;
    }
}
