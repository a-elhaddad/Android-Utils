package com.legenty.utils.utils;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.legenty.utils.App;
import com.legenty.utils.AppConstant;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AnalyticsHelper extends SQLiteOpenHelper {

    public static final String TAG = AnalyticsHelper.class.getSimpleName();

    private final static String ANALYTICS_UPLOAD_URL = AppConstant.PATH_END_POINT + "analytics/submit";
    private final static String ANALYTICS_FILE_NAME = "analytics.json";

    private final static int DATA_PARCEL_SIZE = 500; // 500 events
    private final static int SUBMIT_DATA_INTERVAL = 60 * 60 * 1000; // 1 hour

    private final static String PARAM_OS = "os";
    private final static String PARAM_START_DATE = "startDate";
    private final static String PARAM_FINISH_DATE = "finishDate";
    private final static String PARAM_FIRST_INSTALL_DAYS = "nd";
    private final static String PARAM_NUMBER_OF_STARTS = "ns";
    private final static String PARAM_USER_ID = "aid";
    private final static String PARAM_VERSION = "version";
    private final static String PARAM_LANG = "lang";
    private final static String PARAM_EVENTS = "events";


    private final static String DATABASE_NAME = "analytics";
    private final static int DATABASE_VERSION = 2;

    private final static String TABLE_NAME = "app_events";
    private final static String COL_DATE = "event_date";
    private final static String COL_EVENT = "event";
    private final static String COL_EVENT_TYPE = "event_type";
    private final static String COL_EVENT_VIEW = "event_view";
    private final static String COL_EVENT_META_ID = "event_meta_id";

    public final static int EVENT_TYPE_APP_USAGE = 1;
    public final static int EVENT_TYPE_APP_SEARCH = 2;

    private String insertEventScript;
    private long lastSubmittedTime;
    private App app;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Future<?> submittingTask;

    private static class AnalyticsItem {
        long date;
        int type;
        String view;
        String event;
        int id;

    }

    private static class AnalyticsData {
        long startDate;
        long finishDate;
        List<AnalyticsItem> items;
    }

    public AnalyticsHelper(App ctx) {
        super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
        this.app = ctx;
        insertEventScript = "INSERT INTO " + TABLE_NAME + " VALUES (?, ?, ?, ?, ?)";
        submitCollectedDataAsync();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTable(db);
    }

    private void createTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (" + COL_DATE + " long, " + COL_EVENT_TYPE + " int, " + COL_EVENT_VIEW + " text, " + COL_EVENT + " text, " + COL_EVENT_META_ID + " int) ");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    private long getCollectedRowsCount() {
        long res = -1;
        try {
            SQLiteDatabase db = getWritableDatabase();
            if (db != null && db.isOpen()) {
                try {
                    res = DatabaseUtils.queryNumEntries(db, TABLE_NAME);
                } finally {
                    db.close();
                }
            }
        } catch (RuntimeException e) {
            // ignore
        }
        return res;
    }

    private void clearDB(long finishDate) {
        SQLiteDatabase db = getWritableDatabase();
        if (db != null && db.isOpen()) {
            try {
                db.execSQL("DELETE FROM " + TABLE_NAME + " WHERE " + COL_DATE + " <= ?", new Object[]{finishDate});
            } finally {
                db.close();
            }
        }
    }

    public boolean submitCollectedDataAsync() {
        if (NetworkUtils.isInternetConnectionAvailable()) {
            long collectedRowsCount = getCollectedRowsCount();
            if (collectedRowsCount > DATA_PARCEL_SIZE) {
                if ((submittingTask == null || submittingTask.isDone())) {
                    submittingTask = executor.submit(new Runnable() {
                        @Override
                        public void run() {
                            submitCollectedData();
                        }
                    });
                    return true;
                }
            }
        }
        return false;
    }


    @SuppressLint("HardwareIds")
    private void submitCollectedData() {
        List<AnalyticsData> data = collectRecordedData();
        for (AnalyticsData d : data) {
            if (d.items != null && d.items.size() > 0) {
                try {
                    JSONArray jsonItemsArray = new JSONArray();
                    for (AnalyticsItem item : d.items) {
                        JSONObject jsonItem = new JSONObject();
                        jsonItem.put(COL_DATE, item.date);
                        jsonItem.put(COL_EVENT_VIEW, item.view);
                        jsonItem.put(COL_EVENT_TYPE, item.type);
                        jsonItem.put(COL_EVENT_META_ID, item.id);
                        jsonItem.put(COL_EVENT, item.event);
                        jsonItemsArray.put(jsonItem);
                    }

                    Map<String, String> additionalData = new LinkedHashMap<String, String>();
                    additionalData.put(PARAM_OS, "android");
                    additionalData.put(PARAM_START_DATE, String.valueOf(d.startDate));
                    additionalData.put(PARAM_FINISH_DATE, String.valueOf(d.finishDate));
                    additionalData.put(PARAM_VERSION, VersionUtils.getFullVersion(App.getInstance()));
                    additionalData.put(PARAM_LANG, App.getInstance().getLanguage() + "");
                    //additionalData.put(PARAM_FIRST_INSTALL_DAYS, String.valueOf(App.getInstance().getAppInitializer().getFirstInstalledDays()));
                    //additionalData.put(PARAM_NUMBER_OF_STARTS, String.valueOf(ctx.getAppInitializer().getNumberOfStarts()));
                    //additionalData.put(PARAM_USER_ID, ctx.getUserAndroidId());

                    JSONObject json = new JSONObject();
                    for (Map.Entry<String, String> entry : additionalData.entrySet()) {
                        json.put(entry.getKey(), entry.getValue());
                    }
                    json.put(PARAM_EVENTS, jsonItemsArray);

                    String jsonStr = json.toString();
                    Log.d(TAG, "submitCollectedData: " + jsonStr);
                    InputStream inputStream = new ByteArrayInputStream(jsonStr.getBytes());
                    String res = AndroidNetworkUtils.uploadFile(ANALYTICS_UPLOAD_URL, inputStream, ANALYTICS_FILE_NAME, true, additionalData);
                    if (res != null) {
                        return;
                    }
                } catch (Exception e) {
                    Log.d(TAG, "submitCollectedData: ");
                    return;
                }
                clearDB(d.finishDate);
            }
        }
    }

    private List<AnalyticsData> collectRecordedData() {
        List<AnalyticsData> data = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        if (db != null && db.isOpen()) {
            try {
                collectDBData(db, data);
            } finally {
                db.close();
            }
        }
        return data;
    }

    private void collectDBData(SQLiteDatabase db, List<AnalyticsData> data) {
        Cursor query = db.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY " + COL_DATE + " ASC", null);
        List<AnalyticsItem> items = new ArrayList<>();
        int itemsCounter = 0;
        long startDate = Long.MAX_VALUE;
        long finishDate = 0;
        if (query.moveToFirst()) {
            do {
                AnalyticsItem item = new AnalyticsItem();
                long date = query.getLong(0);
                item.date = query.getLong(query.getColumnIndex(COL_DATE));
                item.type = query.getInt(query.getColumnIndex(COL_EVENT_TYPE));
                item.event = query.getString(query.getColumnIndex(COL_EVENT));
                item.view = query.getString(query.getColumnIndex(COL_EVENT_VIEW));
                item.id = query.getInt(query.getColumnIndex(COL_EVENT_META_ID));
                items.add(item);
                itemsCounter++;

                if (startDate > date) {
                    startDate = date;
                }
                if (finishDate < date) {
                    finishDate = date;
                }

                if (itemsCounter >= DATA_PARCEL_SIZE) {
                    AnalyticsData d = new AnalyticsData();
                    d.startDate = startDate;
                    d.finishDate = finishDate;
                    d.items = items;
                    data.add(d);
                    items = new ArrayList<>();
                    itemsCounter = 0;
                    startDate = Long.MAX_VALUE;
                    finishDate = 0;
                }

            } while (query.moveToNext());

            if (itemsCounter > 0) {
                AnalyticsData d = new AnalyticsData();
                d.startDate = startDate;
                d.finishDate = finishDate;
                d.items = items;
                data.add(d);
            }
        }
        query.close();
    }

    public void addEvent(String view, String event, Integer id, Integer type) {
        SQLiteDatabase db = getWritableDatabase();
        if (db != null && db.isOpen()) {
            try {
                db.execSQL(insertEventScript, new Object[]{System.currentTimeMillis(), type, view, event, id});
            } finally {
                db.close();
            }
        }
        long currentTimeMillis = System.currentTimeMillis();
        if (lastSubmittedTime + SUBMIT_DATA_INTERVAL < currentTimeMillis) {
            if (!submitCollectedDataAsync()) {
                lastSubmittedTime = currentTimeMillis - SUBMIT_DATA_INTERVAL / 4;
            } else {
                lastSubmittedTime = currentTimeMillis;
            }
        }
    }
}
