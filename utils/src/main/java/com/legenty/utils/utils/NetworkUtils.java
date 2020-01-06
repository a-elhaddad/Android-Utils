package com.legenty.utils.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.legenty.utils.App;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetworkUtils {
    public static final int NETWORK_CLASS_UNKNOWN = 0;
    public static final int NETWORK_CLASS_2_G = 1;
    public static final int NETWORK_CLASS_3_G = 2;
    public static final int NETWORK_CLASS_4_G = 3;

    public static long lastTimeInternetConnectionChecked = 0;
    public static boolean internetConnectionAvailable = true;

    private static final int BUFFER_SIZE = 1024;

    private static Proxy proxy = null;

    public static void closeStream(Closeable stream) {
        try {
            if (stream != null) {
                stream.close();
            }
        } catch (IOException e) {
            Log.e("Closing stream warn", e.toString()); //$NON-NLS-1$
        }
    }
    public static void fileCopy(File src, File dst) throws IOException {
        FileOutputStream fout = new FileOutputStream(dst);
        try {
            FileInputStream fin = new FileInputStream(src);
            try {
                streamCopy(fin, fout);
            } finally {
                fin.close();
            }
        } finally {
            fout.close();
        }
    }

    public static void streamCopy(InputStream in, OutputStream out) throws IOException {
        byte[] b = new byte[BUFFER_SIZE];
        int read;
        while ((read = in.read(b)) != -1) {
            out.write(b, 0, read);
        }
    }


    public static void streamCopy(InputStream in, OutputStream out, int bytesDivisor) throws IOException {
        byte[] b = new byte[BUFFER_SIZE];
        int read;
        int cp = 0;
        while ((read = in.read(b)) != -1) {
            out.write(b, 0, read);
            cp += read;
            if (cp > bytesDivisor) {
                cp = cp % bytesDivisor;
            }
        }
    }

    public static String getNetworkTypeName(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null) {
                if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                    return "wifi";
                }
                if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                    return getNetworkClassName(info.getSubtype());
                }
            }
        }
        return null;

    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null) {
                return info.isConnected() && info.isAvailable();
            }
        }
        return false;
    }


    public static int getNetworkClass(int networkType) {
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return NETWORK_CLASS_2_G;
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return NETWORK_CLASS_3_G;
            case TelephonyManager.NETWORK_TYPE_LTE:
                return NETWORK_CLASS_4_G;
            default:
                return NETWORK_CLASS_UNKNOWN;
        }
    }

    private static String getNetworkClassName(int networkType) {
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "2G";
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return "3G";
            case TelephonyManager.NETWORK_TYPE_LTE:
                return "4G";
            default:
                return null;
        }
    }

    public static boolean isOnline(Context con) {
        ConnectivityManager cm = (ConnectivityManager) con.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            try {
                URL url = new URL("http://www.fikraplus.com");
                HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
                urlc.setConnectTimeout(3000);
                urlc.connect();
                if (urlc.getResponseCode() == 200) {
                    return new Boolean(true);
                }
            } catch (MalformedURLException e1) {

                e1.printStackTrace();
            } catch (IOException e) {

                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean isConnectingToInternet(Context _context) {
        ConnectivityManager connectivity = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }

        }
        return false;
    }

    public static boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            try {
                URL url = new URL("http://www.fikraplus.com/");
                HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
                urlc.setRequestProperty("User-Agent", "test");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(7000); // mTimeout is in seconds
                urlc.connect();
                if (urlc.getResponseCode() == 200) {
                    return true;
                } else {
                    return false;
                }
            } catch (IOException e) {
                Log.i("warning", "Error checking internet connection", e);
                return false;
            }
        }

        return false;

    }

    public static void openFacebook(Context con, String facebookUrl) {


        try {
            int versionCode = con.getPackageManager().getPackageInfo("com.facebook.katana", 0).versionCode;
            if (versionCode >= 3002850) {
                Uri uri = Uri.parse("fb://facewebmodal/f?href=" + facebookUrl);
                con.startActivity(new Intent(Intent.ACTION_VIEW, uri).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

            } else {
                Uri uri = Uri.parse("fb://page/<id_here>");
                con.startActivity(new Intent(Intent.ACTION_VIEW, uri).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

            }
        } catch (PackageManager.NameNotFoundException e) {
            con.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(facebookUrl)).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }

    }

    public static void openBrowser(Context context, String url) {
        Uri uri = Uri.parse(url);
        context.startActivity(new Intent(Intent.ACTION_VIEW, uri).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

    }

    public static void sendEmail(Context context, File file) {


        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Store Report ");
        intent.putExtra(Intent.EXTRA_TEXT, "This is default report, do you have any probleme ?");

        if (file != null) {
            Uri path = Uri.fromFile(file);
            intent.putExtra(Intent.EXTRA_STREAM, path); // Include the path
        }


        intent.setData(Uri.parse("mailto:report@camystore.com"));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);


    }

    public static void share(Activity act, String message) {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Camy Store");
        sharingIntent.putExtra(Intent.EXTRA_TEXT, message);
        act.startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }

    public static void shareFacebook(Activity act, String description) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, description);
        intent.setType("text/plain");

        List<ResolveInfo> matches = act.getPackageManager().queryIntentActivities(intent, 0);
        for (ResolveInfo info : matches) {
            if (info.activityInfo.packageName.toLowerCase().contains("facebook")) {
                intent.setPackage(info.activityInfo.packageName);
            }
        }


        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        act.startActivity(intent);

    }

    public static Map<String, List<String>> getQueryParams(String url) {
        try {
            Map<String, List<String>> params = new HashMap<String, List<String>>();
            String[] urlParts = url.split("\\?");
            if (urlParts.length > 1) {
                String query = urlParts[1];
                for (String param : query.split("&")) {
                    String[] pair = param.split("=");
                    String key = URLDecoder.decode(pair[0], "UTF-8");
                    String value = "";
                    if (pair.length > 1) {
                        value = URLDecoder.decode(pair[1], "UTF-8");
                    }

                    List<String> values = params.get(key);
                    if (values == null) {
                        values = new ArrayList<String>();
                        params.put(key, values);
                    }
                    values.add(value);
                }
            }

            return params;
        } catch (UnsupportedEncodingException ex) {
            throw new AssertionError(ex);
        }
    }

    // Check internet connection available every 15 seconds
    static public boolean isInternetConnectionAvailable() {
        return isInternetConnectionAvailable(false);
    }

    static public boolean isInternetConnectionAvailable(boolean update) {
        long delta = System.currentTimeMillis() - lastTimeInternetConnectionChecked;
        if (delta < 0 || delta > 15000 || update) {
            internetConnectionAvailable = isInternetConnected(App.getInstance());
        }
        return internetConnectionAvailable;
    }

    public boolean isWifiConnected(App app) {
        try {
            ConnectivityManager mgr = (ConnectivityManager) app.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo ni = mgr.getActiveNetworkInfo();
            return ni != null && ni.getType() == ConnectivityManager.TYPE_WIFI;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isInternetConnected(App app) {
        try {
            ConnectivityManager mgr = (ConnectivityManager) app.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo active = mgr.getActiveNetworkInfo();
            if (active == null) {
                return false;
            } else {
                NetworkInfo.State state = active.getState();
                return state != NetworkInfo.State.DISCONNECTED && state != NetworkInfo.State.DISCONNECTING;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public static void setProxy(String host, int port) {
        if(host != null && port > 0) {
            InetSocketAddress isa = new InetSocketAddress(host, port);
            proxy = new Proxy(Proxy.Type.HTTP, isa);
        } else {
            proxy = null;
        }
    }

    public static Proxy getProxy() {
        return proxy;
    }

    public static HttpURLConnection getHttpURLConnection(String urlString) throws MalformedURLException, IOException {
        return getHttpURLConnection(new URL(urlString));
    }

    public static HttpURLConnection getHttpURLConnection(URL url) throws IOException {
        if (proxy != null) {
            return (HttpURLConnection) url.openConnection(proxy);
        } else {
            return (HttpURLConnection) url.openConnection();
        }
    }
}
