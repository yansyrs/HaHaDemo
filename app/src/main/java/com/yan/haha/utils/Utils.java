package com.yan.haha.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ScrollView;

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

    public static boolean canScroll(ScrollView scrollView) {
        View child = scrollView.getChildAt(0);
        if (child != null) {
            int childHeight = child.getHeight();
            return scrollView.getHeight() <
                    childHeight + scrollView.getPaddingTop() + scrollView.getPaddingBottom();
        }
        return false;
    }

    public static boolean isScreenLandscape() {
        Context ctx = MainApplication.getContext();
        if (ctx != null) {
            Configuration cfg = ctx.getResources().getConfiguration();
            return (cfg.orientation == Configuration.ORIENTATION_LANDSCAPE);
        } else {
            return false;
        }
    }
}
