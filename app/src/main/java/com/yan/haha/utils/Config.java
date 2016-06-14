package com.yan.haha.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.yan.haha.MainApplication;

public class Config {
    private final static String PREFS_NAME = "haha_prefs";

    public final static String KEY_HOROSCOPE = "key_horoscope";

    private static SharedPreferences getPref() {
        Context context = MainApplication.getContext();
        return context.getSharedPreferences(PREFS_NAME, 0);
    }

    public static String getString(String key, String defValue) {
        return getPref().getString(key, defValue);
    }

    public static int getInt(String key, int defValue) {
        return getPref().getInt(key, defValue);
    }

    public static float getFloat(String key, float defValue) {
        return getPref().getFloat(key, defValue);
    }

    public static boolean getBoolean(String key, boolean defValue) {
        return getPref().getBoolean(key, defValue);
    }

    public static void putString(String key, String value) {
        getPref().edit().putString(key, value).commit();
    }

    public static void putInt(String key, int value) {
        getPref().edit().putInt(key, value).commit();
    }

    public static void putFloat(String key, float value) {
        getPref().edit().putFloat(key, value).commit();
    }

    public static void putBoolean(String key, boolean value) {
        getPref().edit().putBoolean(key, value).commit();
    }
}
