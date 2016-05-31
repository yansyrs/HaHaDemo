package com.yan.haha;

import android.animation.Animator;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.yan.haha.units.Horoscope;
import com.yan.haha.utils.GetHoroscope;
import com.yan.haha.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;

public class HoroscopeFragment extends ContentFragment implements OnDataFinishedListener {
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
    private Button mReloadBtn = null;

    private String mHoroscopeName = "双鱼座";

    private enum LoadState {
        IDLE, LOADING, LOAD_SUCCESS, LOAD_FAIL
    }

    private HashMap<String, String> mNameMap = new HashMap<String, String>(){
        {
            put("水瓶座", "aquarius");
            put("白羊座", "aries");
            put("巨蟹座", "cancer");
            put("摩羯座", "capricorn");
            put("双子座", "gemini");
            put("狮子座", "leo");
            put("天秤座", "libra");
            put("双鱼座", "pisces");
            put("射手座", "sagittarius");
            put("天蝎座", "scorpius");
            put("金牛座", "taurus");
            put("处女座", "virgo");
        }
    };

    private LoadState mLoadState = LoadState.IDLE;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.horoscope_fragment, container, false);
    }

    private void doGetHoroscopeAnimate() {
        mLoadingImg.setVisibility(View.VISIBLE);
        mLoadingImg.setRotation(0f);
        mLoadingImg.animate()
                .rotation(360f)
                .setDuration(1000)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mLoadingImg.setRotation(0f);
                        if (mLoadState == LoadState.LOADING) {
                            // 网络仍在请求中
                            doGetHoroscopeAnimate();
                        } else if (mLoadState != LoadState.IDLE){
                            if (mLoadState == LoadState.LOAD_SUCCESS) {
                                // 网络请求成功
                                mLoadingImg.setVisibility(View.GONE);
                                int bgNum = Utils.getRandomNumber(1, 4);
                                int avatarNum = Utils.getRandomNumber(1, 2);
                                String name = mNameMap.get(mHoroscopeName);
                                int bgRes = Utils.getResourceIdByName(
                                                    "horoscope_bg_" + bgNum,
                                                    "drawable");
                                int avatarRes = Utils.getResourceIdByName(
                                        "ic_horoscope_" + name + "_" + avatarNum,
                                        "drawable");
                                mBgImg.setImageResource(bgRes);
                                mAvatarImg.setImageResource(avatarRes);
                                mSummary.setText(mHoroscope.getSummary());
                                mCard.setTranslationY(Utils.getScreenHeight());
                                mCard.animate()
                                        .translationY(0f)
                                        .setDuration(1000)
                                        .setInterpolator(new DecelerateInterpolator(3f))
                                        .setListener(new Animator.AnimatorListener() {
                                            @Override
                                            public void onAnimationStart(Animator animation) {
                                                mCard.setVisibility(View.VISIBLE);
                                            }

                                            public void onAnimationEnd(Animator animation) {}
                                            public void onAnimationCancel(Animator animation) {}
                                            public void onAnimationRepeat(Animator animation) {}
                                        })
                                        .start();
                            } else if (mLoadState == LoadState.LOAD_FAIL){
                                // 网络请求失败
                                mLoadingImg.setVisibility(View.GONE);
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
        if (mLoadState != LoadState.LOADING && mNameMap.get(mHoroscopeName) != null) {
            mReloadBtn.setVisibility(View.GONE);
            mProcess = new GetHoroscope("today", mHoroscopeName);
            mProcess.setOnDataFinishedListener(this);
            mLoadState = LoadState.LOADING;
            doGetHoroscopeAnimate();
            mProcess.execute();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mHoroscope == null) {
            initViews();
            doGetHoroscope();
        }
    }

    private void changeHoroscope(String name) {
        if (mLoadState != LoadState.LOADING && mNameMap.get(name) != null) {
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
        mReloadBtn = (Button) getActivity().findViewById(R.id.horoscope_reload);

        mReloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doGetHoroscope();
            }
        });

        mTitle.setText(String.format("%s今日综合评价", mHoroscopeName));

        mCard.setVisibility(View.GONE);
        mLoadingImg.setVisibility(View.GONE);
        mReloadBtn.setVisibility(View.GONE);
    }

    @Override
    public void onDataSuccessfully(Object data) {
        Log.i("yan", "success");
        mHoroscope = ((ArrayList<Horoscope>)data).get(0);
        mLoadState = LoadState.LOAD_SUCCESS;
    }

    @Override
    public void onDataFailed() {
        Log.i("yan", "failed");
        mLoadState = LoadState.LOAD_FAIL;
    }
}
