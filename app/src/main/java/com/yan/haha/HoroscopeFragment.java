package com.yan.haha;

import android.animation.Animator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.dolphinwang.imagecoverflow.CoverFlowView;
import com.yan.haha.adapter.HoroscopeCoverFlowAdapter;
import com.yan.haha.units.Horoscope;
import com.yan.haha.units.HoroscopeInfo;
import com.yan.haha.utils.GetHoroscope;
import com.yan.haha.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;

public class HoroscopeFragment extends ContentFragment implements OnDataFinishedListener,
        CoverFlowView.CoverFlowListener<HoroscopeCoverFlowAdapter> {
    private Horoscope mHoroscope = null;
    private GetHoroscope mProcess = null;

    private CardView mCard = null;
    private ImageView mBgImg = null;
    private ImageView mAvatarImg = null;
    private TextView mTitle = null;
    private TextView mSummary = null;
    private Button mShareBtn = null;
    private Button mMoreBtn = null;
    private ImageView mLoadingImg = null;
    private FloatingActionButton mLoadingFab = null;
    private Button mReloadBtn = null;
    private TextView mName = null;
    private CoverFlowView mCoverFlow = null;

    private String mHoroscopeName = "双鱼座";

    private final static int MSG_ID_CHANGE_HOROSCOPE = 0;

    private enum LoadState {
        IDLE, LOADING, LOAD_SUCCESS, LOAD_FAIL
    }

    private ArrayList<HoroscopeInfo> mHoroscopeList = new ArrayList<HoroscopeInfo>(){
        {
            add(new HoroscopeInfo("水瓶座", "01.21~02.19"));
            add(new HoroscopeInfo("双鱼座", "02.20~03.20"));
            add(new HoroscopeInfo("白羊座", "03.21~04.20"));
            add(new HoroscopeInfo("金牛座", "04.21~05.21"));
            add(new HoroscopeInfo("双子座", "05.22~06.21"));
            add(new HoroscopeInfo("巨蟹座", "06.22~07.22"));
            add(new HoroscopeInfo("狮子座", "07.23~08.23"));
            add(new HoroscopeInfo("处女座", "08.24~09.23"));
            add(new HoroscopeInfo("天秤座", "09.24~10.23"));
            add(new HoroscopeInfo("天蝎座", "10.24~11.22"));
            add(new HoroscopeInfo("射手座", "11.23~12.21"));
            add(new HoroscopeInfo("摩羯座", "12.22~01.20"));
        }
    };

    private LoadState mLoadState = LoadState.IDLE;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.horoscope_fragment, container, false);
    }

    private HoroscopeInfo getHoroscopeInfo(String name) {
        for (int i = 0; i < mHoroscopeList.size(); i++) {
            if (mHoroscopeList.get(i).getName().equals(name)) {
                return mHoroscopeList.get(i);
            }
        }
        return null;
    }

    private int getHoroscopeInfoIndex(String name) {
        for (int i = 0; i < mHoroscopeList.size(); i++) {
            if (mHoroscopeList.get(i).getName().equals(name)) {
                return i;
            }
        }
        return -1;
    }

    private void showCoverFlow() {
        mCoverFlow.setScaleX(0f);
        mCoverFlow.setScaleY(0f);
        mCoverFlow.setVisibility(View.VISIBLE);
        mCoverFlow.animate().scaleX(1f).scaleY(1f)
                .setInterpolator(new DecelerateInterpolator(3f))
                .setDuration(500)
                .start();
        mName.setAlpha(0f);
        mName.setVisibility(View.VISIBLE);
        mName.animate().alpha(1f)
                .setDuration(1000)
                .start();
    }

    private void showCard() {
        int bgNum = Utils.getRandomNumber(1, 4);
        int avatarNum = Utils.getRandomNumber(1, 2);
        String name = HoroscopeInfo.getLatinName(mHoroscopeName);
        int bgRes = Utils.getResourceIdByName(
                "horoscope_bg_" + bgNum,
                "drawable");
        int avatarRes = Utils.getResourceIdByName(
                "ic_horoscope_" + name + "_" + avatarNum,
                "drawable");
        mBgImg.setImageResource(bgRes);
        mAvatarImg.setImageResource(avatarRes);
        mTitle.setText(String.format("%s今日综合评价", mHoroscopeName));
        mSummary.setText(mHoroscope.getSummary());
        mCard.setTranslationY(Utils.getScreenHeight());
        mCard.setVisibility(View.VISIBLE);
        mCard.animate()
                .translationY(0f)
                .setDuration(1000)
                .setListener(null)
                .setInterpolator(new DecelerateInterpolator(3f))
                .start();
        if (mCoverFlow.getVisibility() == View.GONE) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    showCoverFlow();
                }
            }, 600);
        }
    }

    private void doGetHoroscopeSuccessfully() {
        boolean isFirstLoad = (mCard.getVisibility() == View.GONE);
        mLoadingImg.setVisibility(View.GONE);
        mLoadingFab.setVisibility(View.GONE);

        if (isFirstLoad) {
            showCard();
        } else {
            mCard.animate()
                    .translationY(0 - mCard.getHeight())
                    .setDuration(500)
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            showCard();
                        }

                        public void onAnimationStart(Animator animation) {}
                        public void onAnimationCancel(Animator animation) {}
                        public void onAnimationRepeat(Animator animation) {}
                    })
                    .start();
        }
    }

    private void doGetHoroscopeAnimate() {
        boolean isFirstLoad = (mCard.getVisibility() == View.GONE);
        final View mLoadingView = isFirstLoad ? mLoadingImg : mLoadingFab;
        mLoadingView.setVisibility(View.VISIBLE);
        mLoadingView.setRotation(0f);
        mLoadingView.animate()
                .rotation(360f)
                .setDuration(1000)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mLoadingView.setRotation(0f);
                        if (mLoadState == LoadState.LOADING) {
                            // 网络仍在请求中
                            doGetHoroscopeAnimate();
                        } else if (mLoadState != LoadState.IDLE){
                            if (mLoadState == LoadState.LOAD_SUCCESS) {
                                // 网络请求成功
                                doGetHoroscopeSuccessfully();
                            } else if (mLoadState == LoadState.LOAD_FAIL){
                                // 网络请求失败
                                mLoadingView.setVisibility(View.GONE);
                                mReloadBtn.setVisibility(View.VISIBLE);
                            }
                            mLoadState = LoadState.IDLE;
                        }
                    }

                    public void onAnimationStart(Animator animation) {}
                    public void onAnimationCancel(Animator animation) {}
                    public void onAnimationRepeat(Animator animation) {}
                })
                .start();
    }

    private void doGetHoroscope() {
        if (mLoadState != LoadState.LOADING
                && HoroscopeInfo.getLatinName(mHoroscopeName) != null) {
            mReloadBtn.setVisibility(View.GONE);
            mProcess = new GetHoroscope("today", mHoroscopeName);
            mProcess.setOnDataFinishedListener(this);
            mLoadState = LoadState.LOADING;
            doGetHoroscopeAnimate();
            mProcess.execute();
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MSG_ID_CHANGE_HOROSCOPE:
                    HoroscopeInfo info = (HoroscopeInfo) msg.obj;
                    mHoroscopeName = info.getName();
                    doGetHoroscope();
                    break;
            }
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        if (mHoroscope == null) {
            initViews();
            doGetHoroscope();
        }
    }

    private void changeHoroscope(String name) {
        if (mLoadState != LoadState.LOADING
                && HoroscopeInfo.getLatinName(name) != null) {
            mHoroscopeName = name;
            mTitle.setText(String.format("%s今日综合评价", mHoroscopeName));
            doGetHoroscope();
        }
    }

    private void initViews() {
        mCard = (CardView) getActivity().findViewById(R.id.horoscope_card);
        mBgImg = (ImageView) getActivity().findViewById(R.id.horoscope_bg);
        mAvatarImg = (ImageView) getActivity().findViewById(R.id.horoscope_avatar);
        mTitle = (TextView) getActivity().findViewById(R.id.horoscope_title);
        mSummary = (TextView) getActivity().findViewById(R.id.horoscope_summary);
        mShareBtn = (Button) getActivity().findViewById(R.id.horoscope_share);
        mMoreBtn = (Button) getActivity().findViewById(R.id.horoscope_more);

        mLoadingImg = (ImageView) getActivity().findViewById(R.id.horoscope_loading);
        mLoadingFab = (FloatingActionButton) getActivity().findViewById(R.id.horoscope_loading_fab);
        mReloadBtn = (Button) getActivity().findViewById(R.id.horoscope_reload);

        mName = (TextView) getActivity().findViewById(R.id.horoscope_name);
        mCoverFlow = (CoverFlowView) getActivity().findViewById(R.id.horoscope_coverflow);
        mCoverFlow.setAdapter(new HoroscopeCoverFlowAdapter(mHoroscopeList));
        mCoverFlow.setCoverFlowListener(this);
        mCoverFlow.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mLoadingFab.getVisibility() == View.VISIBLE) {
                    return true;
                }
                return false;
            }
        });

        mReloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doGetHoroscope();
            }
        });

        if (getHoroscopeInfo(mHoroscopeName) != null) {
            mName.setText(getHoroscopeInfo(mHoroscopeName).getDescription());
        }
        if (getHoroscopeInfoIndex(mHoroscopeName) >= 0) {
            mCoverFlow.setSelection(getHoroscopeInfoIndex(mHoroscopeName));
        }

        mCard.setVisibility(View.GONE);
        mLoadingImg.setVisibility(View.GONE);
        mLoadingFab.setVisibility(View.GONE);
        mReloadBtn.setVisibility(View.GONE);
        mName.setVisibility(View.GONE);
        mCoverFlow.setVisibility(View.GONE);
    }

    @Override
    public void onDataSuccessfully(Object data) {
        mHoroscope = ((ArrayList<Horoscope>)data).get(0);
        mLoadState = LoadState.LOAD_SUCCESS;
    }

    @Override
    public void onDataFailed() {
        mLoadState = LoadState.LOAD_FAIL;
    }

    @Override
    public void imageOnTop(CoverFlowView<HoroscopeCoverFlowAdapter> coverFlowView, int position,
                           float left, float top, float right, float bottom) {
        if (position >= 0 && position < mHoroscopeList.size()) {
            mName.setText(mHoroscopeList.get(position).getDescription());
            mHandler.removeMessages(MSG_ID_CHANGE_HOROSCOPE);
            if(!mHoroscopeList.get(position).getName().equals(mHoroscopeName)) {
                Message msg = new Message();
                msg.what = MSG_ID_CHANGE_HOROSCOPE;
                msg.obj = mHoroscopeList.get(position);
                mHandler.sendMessageDelayed(msg, 2000);
            }
        }
    }

    @Override
    public void topImageClicked(CoverFlowView<HoroscopeCoverFlowAdapter> coverFlowView, int position) {
        if (position >= 0 && position < mHoroscopeList.size()) {
            mHandler.removeMessages(MSG_ID_CHANGE_HOROSCOPE);
            if(!mHoroscopeList.get(position).getName().equals(mHoroscopeName)) {
                Message msg = new Message();
                msg.what = MSG_ID_CHANGE_HOROSCOPE;
                msg.obj = mHoroscopeList.get(position);
                mHandler.sendMessage(msg);
            }
        }
    }

    @Override
    public void invalidationCompleted() {
    }
}
