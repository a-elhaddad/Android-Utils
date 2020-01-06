package com.legenty.utils.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Patterns;

import com.legenty.utils.App;

import java.util.Locale;
import java.util.UUID;


public class StringUtils {
    private StringUtils() {}

    public static boolean isEmail(final @NonNull CharSequence str) {
        return Patterns.EMAIL_ADDRESS.matcher(str).matches();
    }

    public static boolean isEmpty(final @Nullable String str) {
        return str == null || str.trim().length() == 0;
    }

    public static boolean isPresent(final @Nullable String str) {
        return !isEmpty(str);
    }


    public static @NonNull
    String sentenceCase(final @NonNull String str) {
        return str.length() <= 1
                ? str.toUpperCase(Locale.getDefault())
                : str.substring(0, 1).toUpperCase(Locale.getDefault()) + str.substring(1);
    }

    public static @NonNull
    String wrapInParentheses(final @NonNull String str) {
        return "(" + str + ")";
    }


    public static @NonNull
    String safeSentence(final @NonNull String str) {

        return isEmpty(str)
                ? ""
                : str;
    }

    public static @NonNull
    String getUUID() {
        return UUID.randomUUID().toString();
    }

    public static String resolve(int id){
        return App.getResourses().getString(id);
    }

}
