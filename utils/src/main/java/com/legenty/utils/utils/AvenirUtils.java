package com.legenty.utils.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import java.util.Locale;

public class AvenirUtils {
    public static final String TAG = AvenirUtils.class.getSimpleName();

    private static Resources res;
    private AnalyticsHelper analyticsHelper;
    private static Context mContext = null;
    private static  String analyticsUploadUrl = null;

    public  void initializeWithDefaults(final Context context) {
        this.mContext = context.getApplicationContext();
    }

    public static synchronized Context getInstance() {
        Context con;
        synchronized (AvenirUtils.class) {
            con = mContext;
        }
        return con;
    }


    public static String getAnalyticsUploadUrl() {
        return analyticsUploadUrl;
    }

    public static void setAnalyticsUploadUrl(final String url) {
       analyticsUploadUrl = url;
    }

    public void logEvent(String view, String event, Integer id) {
        try {
            if (analyticsHelper == null)
                analyticsHelper = new AnalyticsHelper(mContext);
            analyticsHelper.addEvent(view, event, id, AnalyticsHelper.EVENT_TYPE_APP_USAGE);
        } catch (Exception e) {
            Log.d(TAG, "logEvent: " + e);
        }
    }

    public static String getLanguage() {
        String lang;

        lang = Locale.getDefault().getLanguage();

        if (lang != null && lang.length() > 3) {
            lang = lang.substring(0, 2).toLowerCase();
        }
        return lang;
    }


}
