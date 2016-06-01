package com.yan.haha.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.dolphinwang.imagecoverflow.CoverFlowAdapter;
import com.yan.haha.MainApplication;
import com.yan.haha.units.HoroscopeInfo;
import com.yan.haha.utils.Utils;

import java.util.ArrayList;

public class HoroscopeCoverFlowAdapter extends CoverFlowAdapter{
    private ArrayList<HoroscopeInfo> mList = null;

    public HoroscopeCoverFlowAdapter(ArrayList<HoroscopeInfo> list) {
        mList = list;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Bitmap getImage(int position) {
        Context ctx = MainApplication.getContext();
        if (ctx != null) {
            if (position < 0) {
                position = 0;
            } else if (position >= mList.size()) {
                position = mList.size() - 1;
            }
            int resId = Utils.getResourceIdByName(
                    "horoscope_overflow_" + mList.get(position).getLatinName(), "drawable");
            Bitmap bitmap = ((BitmapDrawable) ContextCompat.getDrawable(ctx, resId)).getBitmap();
            return bitmap;
        }
        return null;
    }
}
