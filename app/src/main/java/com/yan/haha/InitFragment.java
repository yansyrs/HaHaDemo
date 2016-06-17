package com.yan.haha;

import android.animation.Animator;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.yan.haha.units.BrainRiddle;
import com.yan.haha.units.Horoscope;
import com.yan.haha.units.HoroscopeInfo;
import com.yan.haha.units.Jokes;
import com.yan.haha.utils.GetBrainRiddle;
import com.yan.haha.utils.GetHoroscope;
import com.yan.haha.utils.GetJoke;
import com.yan.haha.utils.Utils;

import java.util.ArrayList;
import java.util.Date;


public class InitFragment extends ContentFragment{
    private static final String TAG = "InitFragment";
    private static int REQUEST_PAGE = 1;
    private int jokeNum = 1;
    private final static int RANDOM_COUNT = 1;//10;
    private ArrayList<Jokes> mJokeData = new ArrayList<Jokes>();
    private ArrayList<BrainRiddle> mRiddleData = new ArrayList<BrainRiddle>();
    private Horoscope mHoroscope = null;
    private enum LoadingState {
        IDLE, LOADING, LOAD_SUCCESS, LOAD_FAIL
    }
    private LoadingState mJokeLoadState = LoadingState.IDLE;
    private LoadingState mBrainRiddleLoadState = LoadingState.IDLE;
    private LoadingState mHoroscopeLoadState = LoadingState.IDLE;
    private GetJoke mJokeRequest = null;
    private GetBrainRiddle mBrainRiddleRequest = null;
    private ImageView mJokeContentLoadingImg = null;
    private ImageView mBrainRiddleContentLoadingImg = null;
    private ImageView mHoroscopeContentLoadingImg = null;
    private CardView mJokeCardView;
    private TextView mJokeTextView;
    private CardView mBrainRiddleCardView;
    private TextView mBrainRiddleTextView;
    private CardView mHoroscopeCardView;
    private String mHoroscopeName = "水瓶座";
    private ImageView mHoroscopeBgImg = null;
    private ImageView mHoroscopeAvatarImg = null;
    private TextView mHoroscopeTitle = null;
    private TextView mHoroscopeSummary = null;
    private TextView mHoroscopeDate = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.init_fragment, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        initViews();
        if (mJokeData.size() == 0) {
            mJokeCardView.setVisibility(View.INVISIBLE);
            doGetJokes();
        }else {
            mJokeTextView.setText(mJokeData.get(0).getBody());
        }
        if(mRiddleData.size() == 0) {
            mBrainRiddleCardView.setVisibility(View.INVISIBLE);
            doGetBrainRiddles();
        }else {
            mBrainRiddleTextView.setText(mRiddleData.get(0).getQuestion());
        }
        if (mHoroscope == null) {
            mHoroscopeCardView.setVisibility(View.INVISIBLE);
            doGetHoroscope();
        }else {
            showHoroscopeCard();
        }

    }


    private void initViews() {
        mJokeCardView = (CardView) getActivity().findViewById(R.id.joke_card);
        mHoroscopeCardView = (CardView) getActivity().findViewById(R.id.horoscope_card);
        mHoroscopeBgImg = (ImageView) getActivity().findViewById(R.id.horoscope_bg);
        mHoroscopeAvatarImg = (ImageView) getActivity().findViewById(R.id.horoscope_avatar);
        mHoroscopeTitle = (TextView) getActivity().findViewById(R.id.horoscope_title);
        mHoroscopeSummary = (TextView) getActivity().findViewById(R.id.horoscope_summary);
        mBrainRiddleCardView = (CardView) getActivity().findViewById(R.id.riddle_card);
        mJokeTextView = (TextView) getActivity().findViewById(R.id.one_joke);
        mBrainRiddleTextView = (TextView) getActivity().findViewById(R.id.one_riddle);
        mJokeContentLoadingImg = (ImageView) getActivity().findViewById(R.id.joke_loading);
        mBrainRiddleContentLoadingImg = (ImageView) getActivity().findViewById(R.id.riddle_loading);
        mHoroscopeContentLoadingImg = (ImageView) getActivity().findViewById(R.id.horoscope_loading) ;
        mHoroscopeDate = (TextView) getActivity().findViewById(R.id.horoscope_date);

        mHoroscopeDate.setEnabled(false);
        Date date = new Date(System.currentTimeMillis());
        mHoroscopeDate.setText(DateFormat.getLongDateFormat(getActivity()).format(date));
        mJokeCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.getInstance().replaceContentFragment(new JokeFragment(),true);
            }
        });

    }

    private GetJoke getJokeProcess() {
        GetJoke process = new GetJoke(REQUEST_PAGE, jokeNum);
        process.setOnDataFinishedListener(new OnDataFinishedListener() {
            @Override
            public void onDataSuccessfully(Object data) {
                ArrayList<Jokes> result = (ArrayList<Jokes>) data;
                mJokeData = result;
                mJokeLoadState = LoadingState.LOAD_SUCCESS;
            }

            @Override
            public void onDataFailed() {
                mJokeLoadState = LoadingState.LOAD_FAIL;
            }
        });
        return process;
    }

    private GetBrainRiddle getBrainRiddleProcess() {
        GetBrainRiddle process = new GetBrainRiddle(RANDOM_COUNT);
        process.setOnDataFinishedListener(new OnDataFinishedListener() {
            @Override
            public void onDataSuccessfully(Object data) {
                ArrayList<BrainRiddle> result = (ArrayList<BrainRiddle>) data;
                mRiddleData = result;
                mBrainRiddleLoadState = LoadingState.LOAD_SUCCESS;
            }

            @Override
            public void onDataFailed() {

            }
        });
        return process;
    }

    private void doGetJokes() {
        if (mJokeLoadState != LoadingState.LOADING) {
            mJokeLoadState = LoadingState.LOADING;
            // 创建网络请求
            mJokeRequest = getJokeProcess();
            mJokeRequest.execute();
            // 显示“加载中”动画
            doGetJokeAnimate();
        }
    }

    private void doGetBrainRiddles() {
        if (mBrainRiddleLoadState != LoadingState.LOADING) {
            mBrainRiddleLoadState = LoadingState.LOADING;
            // 创建网络请求
            mBrainRiddleRequest = getBrainRiddleProcess();
            mBrainRiddleRequest.execute();
            // 显示“加载中”动画
            doGetBrainRiddlesAnimate();
        }
    }

    private void doGetHoroscope() {
        if (mHoroscopeLoadState != LoadingState.LOADING
                && HoroscopeInfo.getLatinName(mHoroscopeName) != null) {
            GetHoroscope mProcess = new GetHoroscope("today", mHoroscopeName);
            mProcess.setOnDataFinishedListener(new OnDataFinishedListener() {
                @Override
                public void onDataSuccessfully(Object data) {
                    mHoroscope = ((ArrayList<Horoscope>)data).get(0);
                    mHoroscopeLoadState = LoadingState.LOAD_SUCCESS;
                }

                @Override
                public void onDataFailed() {

                }
            });
            mHoroscopeLoadState = LoadingState.LOADING;
            mProcess.execute();
            doGetHoroscopeAnimate();
        }
    }

    private void doGetJokeAnimate() {
        final View loadingView = mJokeContentLoadingImg;
        loadingView.setRotation(0f);
        ViewPropertyAnimator ani = loadingView.animate()
                .rotation(360f)
                .setDuration(1000)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        loadingView.setRotation(0f);
                        if (mJokeLoadState == LoadingState.LOADING) {
                            // 网络仍在请求中
                            doGetJokeAnimate();
                            return;
                        } else if (mJokeLoadState != LoadingState.IDLE) {
                            // 网络请求成功，加载数据到列表
                            if (mJokeLoadState == LoadingState.LOAD_SUCCESS) {
                                mJokeCardView.setVisibility(View.VISIBLE);
                                mJokeTextView.setText(mJokeData.get(0).getBody());
                            }

                            // 隐藏第一次进入时使用的加载图标
                            if (mJokeContentLoadingImg.getVisibility() == View.VISIBLE) {
                                mJokeContentLoadingImg.setVisibility(View.GONE);
                            } else {
                            }
                            mJokeLoadState = LoadingState.IDLE;
                        }
                    }

                    public void onAnimationStart(Animator animation) {
                    }

                    public void onAnimationCancel(Animator animation) {
                    }

                    public void onAnimationRepeat(Animator animation) {
                    }
                });
        ani.start();
    }

    private void doGetBrainRiddlesAnimate() {
        final View loadingView = mBrainRiddleContentLoadingImg;
        loadingView.setRotation(0f);
        ViewPropertyAnimator ani = loadingView.animate()
                .rotation(360f)
                .setDuration(1000)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        loadingView.setRotation(0f);
                        if (mBrainRiddleLoadState == LoadingState.LOADING) {
                            // 网络仍在请求中
                            doGetBrainRiddlesAnimate();
                            return;
                        } else if (mBrainRiddleLoadState != LoadingState.IDLE){
                            // 网络请求成功，加载数据到列表
                            if (mBrainRiddleLoadState == LoadingState.LOAD_SUCCESS) {
                                mBrainRiddleCardView.setVisibility(View.VISIBLE);
                                mBrainRiddleTextView.setText(mRiddleData.get(0).getQuestion());
                            }

                            // 隐藏第一次进入时使用的加载图标
                            if (mBrainRiddleContentLoadingImg.getVisibility() == View.VISIBLE) {
                                mBrainRiddleContentLoadingImg.setVisibility(View.GONE);
                            } else {
                            }

                            mBrainRiddleLoadState = LoadingState.IDLE;
                        }
                    }

                    public void onAnimationStart(Animator animation) {}
                    public void onAnimationCancel(Animator animation) {}
                    public void onAnimationRepeat(Animator animation) {}
                });
        ani.start();
    }

    private void doGetHoroscopeAnimate() {
        final View mLoadingView = mHoroscopeContentLoadingImg;
        mLoadingView.setVisibility(View.VISIBLE);
        mLoadingView.setRotation(0f);
        mLoadingView.animate()
                .rotation(360f)
                .setDuration(1000)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mLoadingView.setRotation(0f);
                        if (mHoroscopeLoadState == LoadingState.LOADING) {
                            // 网络仍在请求中
                            doGetHoroscopeAnimate();
                        } else if (mHoroscopeLoadState != LoadingState.IDLE){
                            if (mHoroscopeLoadState == LoadingState.LOAD_SUCCESS) {
                                // 网络请求成功
                                doGetHoroscopeSuccessfully();
                            } else if (mHoroscopeLoadState == LoadingState.LOAD_FAIL){
                                // 网络请求失败
                                mLoadingView.setVisibility(View.INVISIBLE);
                            }
                            mHoroscopeLoadState = LoadingState.IDLE;
                        }
                    }

                    public void onAnimationStart(Animator animation) {}
                    public void onAnimationCancel(Animator animation) {}
                    public void onAnimationRepeat(Animator animation) {}
                })
                .start();
    }

    private void doGetHoroscopeSuccessfully() {
        mHoroscopeContentLoadingImg.setVisibility(View.INVISIBLE);
        showHoroscopeCard();
    }

    private void showHoroscopeCard() {
        int bgNum = Utils.getRandomNumber(1, 4);
        int avatarNum = Utils.getRandomNumber(1, 2);
        String name = HoroscopeInfo.getLatinName(mHoroscopeName);
        int bgRes = Utils.getResourceIdByName(
                "horoscope_bg_" + bgNum,
                "drawable");
        int avatarRes = Utils.getResourceIdByName(
                "ic_horoscope_" + name + "_" + avatarNum,
                "drawable");
        mHoroscopeBgImg.setImageResource(bgRes);
        mHoroscopeAvatarImg.setImageResource(avatarRes);
        mHoroscopeTitle.setText(String.format("%s今日综合评价", mHoroscopeName));
        mHoroscopeSummary.setText(mHoroscope.getSummary());
        mHoroscopeCardView.setVisibility(View.VISIBLE);

        ViewPropertyAnimator animator = mHoroscopeCardView.animate();
        animator.setDuration(1000)
                .setListener(null)
                .setInterpolator(new DecelerateInterpolator(3f));
            mHoroscopeCardView.setAlpha(0f);
            animator.alpha(1f);

        animator.start();
    }

    private HoroscopeInfo getHoroscopeInfo(String name) {
        for (int i = 0; i < HoroscopeFragment.mHoroscopeList.size(); i++) {
            if (HoroscopeFragment.mHoroscopeList.get(i).getName().equals(name)) {
                return HoroscopeFragment.mHoroscopeList.get(i);
            }
        }
        return null;
    }
}
