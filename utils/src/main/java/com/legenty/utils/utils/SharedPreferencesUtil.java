package com.legenty.utils.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.content.SharedPreferencesCompat;
import android.text.TextUtils;

import java.util.Map;
import java.util.Set;


public class SharedPreferencesUtil {
    private static final String DEFAULT_NAME = "share_data";
    private static final int DEFAULT_MODE = Context.MODE_PRIVATE;

    private static SharedPreferences getEditor(Context context, String name, int mode) {
        name = TextUtils.isEmpty(name) ? DEFAULT_NAME : name;
        SharedPreferences sp = context.getSharedPreferences(name, mode);
        return sp;
    }

    public static void put(Context context, String key, Object value) {
        SharedPreferences.Editor editor = getEditor(context, null, DEFAULT_MODE).edit();
        if (value instanceof String) {
            editor.putString(key, (String) value);
        } else if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        } else if (value instanceof Float) {
            editor.putFloat(key, (Float) value);
        } else if (value instanceof Integer) {
            editor.putInt(key, (Integer) value);
        } else if (value instanceof Long) {
            editor.putLong(key, (Long) value);
        } else if (value instanceof Set<?>) {
            editor.putStringSet(key, (Set<String>) value);
        }
        SharedPreferencesCompat.EditorCompat.getInstance().apply(editor);
    }

    public static void put(Context context, String name, int model, String key, Object value) {
        SharedPreferences.Editor editor = getEditor(context, name, model).edit();
        if (value instanceof String) {
            editor.putString(key, (String) value);
        } else if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        } else if (value instanceof Float) {
            editor.putFloat(key, (Float) value);
        } else if (value instanceof Integer) {
            editor.putInt(key, (Integer) value);
        } else if (value instanceof Long) {
            editor.putLong(key, (Long) value);
        } else if (value instanceof Set<?>) {
            editor.putStringSet(key, (Set<String>) value);
        }
        SharedPreferencesCompat.EditorCompat.getInstance().apply(editor);
    }


    public static Object get(Context context, String name, int model, String key, Object defaultValue) {
        SharedPreferences sp = getEditor(context, name, model);
        if (defaultValue instanceof String) {
            return sp.getString(key, (String) defaultValue);
        } else if (defaultValue instanceof Boolean) {
            return sp.getBoolean(key, (Boolean) defaultValue);
        } else if (defaultValue instanceof Float) {
            return sp.getFloat(key, (Float) defaultValue);
        } else if (defaultValue instanceof Integer) {
            return sp.getInt(key, (Integer) defaultValue);
        } else if (defaultValue instanceof Long) {
            return sp.getLong(key, (Long) defaultValue);
        } else if (defaultValue instanceof Set) {
            return sp.getStringSet(key, (Set) defaultValue);
        } else {
            return defaultValue;
        }
    }

    public static Object get(Context context, String key, Object defaultValue) {
        SharedPreferences sp = getEditor(context, null, DEFAULT_MODE);
        if (defaultValue instanceof String) {
            return sp.getString(key, (String) defaultValue);
        } else if (defaultValue instanceof Boolean) {
            return sp.getBoolean(key, (Boolean) defaultValue);
        } else if (defaultValue instanceof Float) {
            return sp.getFloat(key, (Float) defaultValue);
        } else if (defaultValue instanceof Integer) {
            return sp.getInt(key, (Integer) defaultValue);
        } else if (defaultValue instanceof Long) {
            return sp.getLong(key, (Long) defaultValue);
        } else if (defaultValue instanceof Set) {
            return sp.getStringSet(key, (Set) defaultValue);
        } else {
            return defaultValue;
        }
    }

    public static void remove(Context context, String name, int model, String key) {
        SharedPreferences.Editor editor = getEditor(context, name, model).edit().remove(key);
        SharedPreferencesCompat.EditorCompat.getInstance().apply(editor);
    }

    public static void remove(Context context, String key) {
        SharedPreferences.Editor editor = getEditor(context, null, DEFAULT_MODE).edit().remove(key);
        SharedPreferencesCompat.EditorCompat.getInstance().apply(editor);
    }

    public static void clear(Context context) {
        SharedPreferences.Editor editor = getEditor(context, null, DEFAULT_MODE).edit().clear();
        SharedPreferencesCompat.EditorCompat.getInstance().apply(editor);
    }

    public static void clear(Context context, String name, int model) {
        SharedPreferences.Editor editor = getEditor(context, name, model).edit().clear();
        SharedPreferencesCompat.EditorCompat.getInstance().apply(editor);
    }


    public static Map<String, ?> getAll(Context context) {
        return getEditor(context, null, DEFAULT_MODE).getAll();
    }


    public static Map<String, ?> getAll(Context context, String name, int model) {
        return getEditor(context, name, model).getAll();
    }
}
