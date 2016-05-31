package com.yan.haha.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.dolphinwang.imagecoverflow.CoverFlowAdapter;
import com.yan.haha.MainApplication;
import com.yan.haha.utils.Utils;

public class HoroscopeCoverFlowAdapter extends CoverFlowAdapter {

    @Override
    public int getCount() {
        return 12;
    }

    @Override
    public Bitmap getImage(int position) {
        Context ctx = MainApplication.getContext();
        if (ctx != null) {
            int resId = Utils.getResourceIdByName(String.format("horoscope_test_%02d", (position+1)), "drawable");
            Bitmap bitmap = ((BitmapDrawable) ContextCompat.getDrawable(ctx, resId)).getBitmap();
            Log.i("yan", "bitmap: " + bitmap);
            return bitmap;
        }
        return null;
    }
}
