package com.yan.haha.utils;

import android.content.Context;
import android.util.DisplayMetrics;

import com.yan.haha.MainApplication;

public class Utils {

    public static int getScreenHeight() {
        Context context = MainApplication.getContext();
        if (context != null) {
            DisplayMetrics dm = context.getResources().getDisplayMetrics();
            return dm.heightPixels;
        } else {
            return -1;
        }
    }

    public static int getScreenWidth() {
        Context context = MainApplication.getContext();
        if (context != null) {
            DisplayMetrics dm = context.getResources().getDisplayMetrics();
            return dm.widthPixels;
        } else {
            return -1;
        }
    }
}
