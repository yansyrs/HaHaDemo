package com.yan.haha.utils;

import android.content.Context;
import android.util.DisplayMetrics;

import com.yan.haha.MainApplication;

import java.util.Random;

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

    public static int getResourceIdByName(String name, String type) {
        Context context = MainApplication.getContext();
        if (context != null) {
            return context.getResources().getIdentifier(name, type, context.getPackageName());
        }
        return -1;
    }

    public static int getRandomNumber(int min, int max) {
        int num = new Random().nextInt(max - min + 1) + min;
        if (num > max) {
            num = max;
        }
        return num;
    }
}
