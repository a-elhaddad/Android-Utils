package com.legenty.utils.utils;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import com.legenty.utils.App;
import com.legenty.utils.BuildConfig;
import com.legenty.utils.R;

import java.io.File;

public class LogUtil {
    public static boolean DEBUG = BuildConfig.DEBUG || !BuildConfig.BUILD_TYPE.equals("release");

    public static void d(String tag, String msg) {
        if (DEBUG) {
            Log.d(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (DEBUG) {
            Log.e(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (DEBUG) {
            Log.w(tag, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (DEBUG) {
            Log.i(tag, msg);
        }
    }


    public static String getDeviceInfo(String p_seperator) {
        String m_data = "";
        StringBuilder m_builder = new StringBuilder();
        m_builder.append("RELEASE " + Build.VERSION.RELEASE + p_seperator);
        m_builder.append("DEVICE " + Build.DEVICE + p_seperator);
        m_builder.append("MODEL " + Build.MODEL + p_seperator);
        m_builder.append("PRODUCT " + Build.PRODUCT + p_seperator);
        m_builder.append("BRAND " + Build.BRAND + p_seperator);
        m_builder.append("DISPLAY " + Build.DISPLAY + p_seperator);
        m_builder.append("CPU_ABI " + Build.CPU_ABI + p_seperator);
        m_builder.append("CPU_ABI2 " + Build.CPU_ABI2 + p_seperator);
        m_builder.append("UNKNOWN " + Build.UNKNOWN + p_seperator);
        m_builder.append("HARDWARE " + Build.HARDWARE + p_seperator);
        m_builder.append("ID " + Build.ID + p_seperator);
        m_builder.append("MANUFACTURER " + Build.MANUFACTURER + p_seperator);
        m_builder.append("SERIAL " + Build.SERIAL + p_seperator);
        m_builder.append("USER " + Build.USER + p_seperator);
        m_builder.append("HOST " + Build.HOST + p_seperator);
        m_builder.append("VERSION.SDK " + Build.VERSION.SDK + p_seperator);
        m_builder.append("BOARD " + Build.BOARD + p_seperator);
        m_builder.append("ROOTED " + RootUtil.isDeviceRooted() + p_seperator);


        m_data = m_builder.toString();
        return m_data;
    }

    public void sendCrashLog(Activity activity, File file,String email) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
        intent.putExtra(Intent.EXTRA_STREAM, FileUtils.getUriForFile(activity, file));
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType("vnd.android.cursor.dir/email");
        intent.putExtra(Intent.EXTRA_SUBJECT, "OsmAnd bug");
        StringBuilder text = new StringBuilder();
        text.append("\nDevice : ").append(Build.DEVICE);
        text.append("\nBrand : ").append(Build.BRAND);
        text.append("\nModel : ").append(Build.MODEL);
        text.append("\nProduct : ").append(Build.PRODUCT);
        text.append("\nBuild : ").append(Build.DISPLAY);
        text.append("\nVersion : ").append(Build.VERSION.RELEASE);
        text.append("\nApp Version : ").append(VersionUtils.getAppName(App.getInstance()));
        try {
            PackageInfo info = App.getInstance().getPackageManager().getPackageInfo(App.getInstance().getPackageName(), 0);
            if (info != null) {
                text.append("\nApk Version : ").append(info.versionName).append(" ").append(info.versionCode);
            }
        } catch (PackageManager.NameNotFoundException e) {
        }
        intent.putExtra(Intent.EXTRA_TEXT, text.toString());
        Intent chooserIntent = Intent.createChooser(intent, activity.getResources().getString(R.string.send_report));
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(chooserIntent);
    }
}
