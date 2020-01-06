package com.legenty.utils.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import static com.legenty.utils.utils.NetworkUtils.closeStream;
import static com.legenty.utils.utils.NetworkUtils.streamCopy;


public class AndroidNetworkUtils {
    public static final String TAG = AndroidNetworkUtils.class.getSimpleName();
    private static final int CONNECTION_TIMEOUT = 15000;

    public interface OnRequestResultListener {
        void onResult(String result);
    }

    public static void sendRequestsAsync(final Context ctx,
                                         final List<Request> requests,
                                         final OnRequestResultListener listener) {

        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                for (Request request : requests) {
                    try {
                        return sendRequest(ctx, request.getUrl(), request.getParameters(),
                                request.getUserOperation(), request.isToastAllowed(), request.isPost());
                    } catch (Exception e) {
                        // ignore
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(String response) {
                if (listener != null) {
                    listener.onResult(response);
                }
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
    }

    public static void sendRequestAsync(final Context ctx,
                                        final String url,
                                        final Map<String, String> parameters,
                                        final String userOperation,
                                        final boolean toastAllowed,
                                        final boolean post,
                                        final OnRequestResultListener listener) {

        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                try {
                    return sendRequest(ctx, url, parameters, userOperation, toastAllowed, post);
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String response) {
                if (listener != null) {
                    listener.onResult(response);
                }
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
    }


    public static String sendRequest(Context ctx, String url, Map<String, String> parameters,
                                     String userOperation, boolean toastAllowed, boolean post) {
        HttpURLConnection connection = null;
        try {

            String params = null;
            if (parameters != null && parameters.size() > 0) {
                StringBuilder sb = new StringBuilder();
                for (Map.Entry<String, String> entry : parameters.entrySet()) {
                    if (sb.length() > 0) {
                        sb.append("&");
                    }
                    sb.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                }
                params = sb.toString();
            }
            String paramsSeparator = url.indexOf('?') == -1 ? "?" : "&";
            connection = NetworkUtils.getHttpURLConnection(params == null || post ? url : url + paramsSeparator + params);
            connection.setRequestProperty("Accept-Charset", "UTF-8");
            connection.setRequestProperty("User-Agent", "");
            connection.setConnectTimeout(15000);
            if (params != null && post) {
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setUseCaches(false);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
                connection.setRequestProperty("Content-Length", String.valueOf(params.getBytes("UTF-8").length));
                connection.setFixedLengthStreamingMode(params.getBytes("UTF-8").length);

                OutputStream output = new BufferedOutputStream(connection.getOutputStream());
                output.write(params.getBytes("UTF-8"));
                output.flush();
                output.close();

            } else {

                connection.setRequestMethod("GET");
                connection.connect();
            }

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                if (toastAllowed) {
                    String msg = userOperation
                            + ": " + connection.getResponseMessage();
                    Log.d(TAG, "sendRequest: " + msg);
                }
            } else {
                StringBuilder responseBody = new StringBuilder();
                responseBody.setLength(0);
                InputStream i = connection.getInputStream();
                if (i != null) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(i, "UTF-8"), 256);
                    String s;
                    boolean f = true;
                    while ((s = in.readLine()) != null) {
                        if (!f) {
                            responseBody.append("\n");
                        } else {
                            f = false;
                        }
                        responseBody.append(s);
                    }
                    try {
                        in.close();
                        i.close();
                    } catch (Exception e) {
                        // ignore exception
                    }
                }
                return responseBody.toString();
            }

        } catch (NullPointerException e) {
            // that's tricky case why NPE is thrown to fix that problem httpClient could be used

        } catch (MalformedURLException e) {

        } catch (IOException e) {

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return null;
    }

    public static Bitmap downloadImage(Context ctx, String url) {
        Bitmap res = null;
        try {
            URLConnection connection = NetworkUtils.getHttpURLConnection(url);
            connection.setRequestProperty("User-Agent", "");
            connection.setConnectTimeout(CONNECTION_TIMEOUT);
            connection.setReadTimeout(CONNECTION_TIMEOUT);
            BufferedInputStream inputStream = new BufferedInputStream(connection.getInputStream(), 8 * 1024);
            try {
                res = BitmapFactory.decodeStream(inputStream);
            } finally {
                closeStream(inputStream);
            }
        } catch (UnknownHostException e) {
            Log.d(TAG, "UnknownHostException, cannot download image " + url + " " + e.getMessage());
        } catch (Exception e) {
            Log.d(TAG, "sendRequest: "+"Cannot download image : " + url, e);
        }
        return res;
    }

    private static final String BOUNDARY = "CowMooCowMooCowCowCow";

    public static String uploadFile(String urlText, File file, boolean gzip, Map<String, String> additionalParams) throws IOException {
        return uploadFile(urlText, new FileInputStream(file), file.getName(), gzip, additionalParams);
    }

    public static String uploadFile(String urlText, InputStream inputStream, String fileName, boolean gzip, Map<String, String> additionalParams) {
        URL url;
        try {
          /*  boolean firstPrm = !urlText.contains("?");
            StringBuilder sb = new StringBuilder(urlText);
            for (String key : additionalParams.keySet()) {
                sb.append(firstPrm ? "?" : "&").append(key).append("=").append(URLEncoder.encode(additionalParams.get(key), "UTF-8"));
                firstPrm = false;
            }
            urlText = sb.toString();*/

            Log.d(TAG, "Start uploading file to " + urlText + " " + fileName);
            url = new URL(urlText);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
            conn.setRequestProperty("User-Agent", "OsmAnd");

            OutputStream ous = conn.getOutputStream();
            ous.write(("--" + BOUNDARY + "\r\n").getBytes());
            if (gzip) {
                fileName += ".gz";
            }
            ous.write(("content-disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"\r\n").getBytes());
            ous.write(("Content-Type: application/octet-stream\r\n\r\n").getBytes());

            BufferedInputStream bis = new BufferedInputStream(inputStream, 20 * 1024);
            ous.flush();
            if (gzip) {
                GZIPOutputStream gous = new GZIPOutputStream(ous, 1024);
                streamCopy(bis, gous);
                gous.flush();
                gous.finish();
            } else {
                streamCopy(bis, ous);
            }

            ous.write(("\r\n--" + BOUNDARY + "--\r\n").getBytes());
            ous.flush();
            closeStream(bis);
            closeStream(ous);

            Log.d(TAG, "Finish uploading file " + fileName);
            Log.d(TAG, "Finish uploading file " + ("\r\n--" + BOUNDARY + "--\r\n").getBytes());
            Log.d(TAG, "Response code and message : " + conn.getResponseCode() + " " + conn.getResponseMessage()+" ");
            if (conn.getResponseCode() != 200) {
                return conn.getResponseMessage();
            }
            InputStream is = conn.getInputStream();
            StringBuilder responseBody = new StringBuilder();
            if (is != null) {
                BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                String s;
                boolean first = true;
                while ((s = in.readLine()) != null) {
                    if (first) {
                        first = false;
                    } else {
                        responseBody.append("\n");
                    }
                    responseBody.append(s);

                }
                is.close();
            }
            String response = responseBody.toString();
            Log.d(TAG, "Response : " + response);
            return null;
        } catch (IOException e) {
            Log.d(TAG, e.getMessage(), e);
            return e.getMessage();
        }
    }


    public static class Request {
        private String url;
        private Map<String, String> parameters;
        private String userOperation;
        private boolean toastAllowed;
        private boolean post;

        public Request(String url, Map<String, String> parameters, String userOperation, boolean toastAllowed, boolean post) {
            this.url = url;
            this.parameters = parameters;
            this.userOperation = userOperation;
            this.toastAllowed = toastAllowed;
            this.post = post;
        }

        public String getUrl() {
            return url;
        }

        public Map<String, String> getParameters() {
            return parameters;
        }

        public String getUserOperation() {
            return userOperation;
        }

        public boolean isToastAllowed() {
            return toastAllowed;
        }

        public boolean isPost() {
            return post;
        }
    }
}
