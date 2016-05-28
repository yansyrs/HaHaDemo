package com.yan.haha;

import android.app.Application;
import android.content.Context;

public class MainApplication extends Application {
    private static MainApplication mApp = null;

    public static MainApplication getInstance() {
        return mApp;
    }

    public static Context getContext() {
        if (mApp != null) {
            return mApp.getApplicationContext();
        } else {
            return null;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mApp = this;
    }
}
