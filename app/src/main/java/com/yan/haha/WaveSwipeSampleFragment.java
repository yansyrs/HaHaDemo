package com.yan.haha;

import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.AppBarLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;

public class WaveSwipeSampleFragment extends ContentFragment
    implements WaveSwipeRefreshLayout.DragListener {

    private WaveSwipeRefreshLayout mWaveSwipe = null;
    private AppBarLayout mAppBar = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.wave_swipe_sample, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        init();
    }

    private void init() {
        mWaveSwipe = (WaveSwipeRefreshLayout) getActivity().findViewById(R.id.wave_swipe_refresh);
        if (Build.VERSION.SDK_INT >= 21) {
            mWaveSwipe.setOnDragListener(this);
        }

        mAppBar = (AppBarLayout) getActivity().findViewById(R.id.appbar);
    }

    @Override
    public void onDragBegin() {
        if (Build.VERSION.SDK_INT >= 21) {
            mAppBar.setElevation(0f);
        }
    }

    private Handler mDragFinishHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case 0:
                    if (Build.VERSION.SDK_INT >= 21) {
                        mAppBar.setElevation(12f);
                    }
                    break;
            }
        }
    };

    @Override
    public void onDragFinish() {
        if (Build.VERSION.SDK_INT >= 21) {
            mDragFinishHandler.removeMessages(0);
            mDragFinishHandler.sendEmptyMessageDelayed(0, 500);
        }
    }
}
