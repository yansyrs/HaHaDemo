package com.yan.haha;

import android.Manifest;
import android.animation.Animator;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.yan.haha.adapter.BrainRiddleAdapter;
import com.yan.haha.units.BrainRiddle;
import com.yan.haha.units.Horoscope;
import com.yan.haha.units.HoroscopeInfo;
import com.yan.haha.units.Jokes;
import com.yan.haha.utils.Config;
import com.yan.haha.utils.GetBrainRiddle;
import com.yan.haha.utils.GetHoroscope;
import com.yan.haha.utils.GetJoke;
import com.yan.haha.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;


public class InitFragment extends ContentFragment implements View.OnClickListener{
    private static final String TAG = "InitFragment";
    private static int REQUEST_PAGE = 1;
    private int jokeNum = 10;
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
    private ViewGroup mJokeCardView;
    private TextView mJokeTextView;
    private ViewGroup mBrainRiddleCardView;
    private TextView mBrainRiddleTextView;
    private CardView mHoroscopeCardView;
    private String mHoroscopeName = "水瓶座";
    private ImageView mHoroscopeBgImg = null;
    private ImageView mHoroscopeAvatarImg = null;
    private TextView mHoroscopeTitle = null;
    private TextView mHoroscopeSummary = null;
    private TextView mHoroscopeDate = null;
    private Button mJokeMoreButton = null;
    private Button mJokeShareButton = null;
    private Button mJokeReloadButton = null;
    private Button mHoroscopeMoreButton = null;
    private Button mHoroscopeShareButton = null;
    private Button mHoroscopeReloadButton = null;
    private Button mBrainRiddleMoreButton = null;
    private Button mBrainRiddleShareButton = null;
    private Button mBrainRiddleReloadButton = null;
    private View mPermissionView = null;
    private SmoothProgressBar mLoadingProgressBar = null;

    private int mHoroscopeBgRes = -1;
    private int mHoroscopeAvatarRes = -1;
    private int mRandomJokeNum = 0;
    private boolean mHoroscopeChanged = false;

    private boolean mIsProgressBarShown = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.init_fragment, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().setTitle(getString(R.string.app_name));
        String horoscopeName = Config.getString(Config.KEY_HOROSCOPE, mHoroscopeName);
        if (!horoscopeName.equals(mHoroscopeName)) {
            mHoroscopeName = horoscopeName;
            mHoroscopeChanged = true;
        }
        initViews();
        if (mJokeData.size() == 0) {
            mRandomJokeNum = Math.abs(new Random().nextInt())%10;
            mJokeCardView.setVisibility(View.INVISIBLE);
            doGetJokes();
        } else {
            mJokeContentLoadingImg.setVisibility(View.GONE);
            mJokeTextView.setText(mJokeData.get(mRandomJokeNum).getBody());
        }
        if (mRiddleData.size() == 0) {
            mBrainRiddleCardView.setVisibility(View.INVISIBLE);
            doGetBrainRiddles();
        } else {
            mBrainRiddleContentLoadingImg.setVisibility(View.GONE);
            mBrainRiddleTextView.setText(mRiddleData.get(0).getQuestion());
        }
        if (mHoroscope == null || mHoroscopeChanged) {
            mHoroscopeCardView.setVisibility(View.INVISIBLE);
            doGetHoroscope();
            mHoroscopeChanged = false;
        } else {
            showHoroscopeCard();
        }

    }

    private void initViews() {
        mJokeCardView = (ViewGroup) getActivity().findViewById(R.id.joke_card);
        mHoroscopeCardView = (CardView) getActivity().findViewById(R.id.horoscope_card);
        mBrainRiddleCardView = (ViewGroup) getActivity().findViewById(R.id.riddle_card);
        mHoroscopeBgImg = (ImageView) getActivity().findViewById(R.id.horoscope_bg);
        mHoroscopeAvatarImg = (ImageView) getActivity().findViewById(R.id.horoscope_avatar);
        mHoroscopeTitle = (TextView) getActivity().findViewById(R.id.horoscope_title);
        mHoroscopeSummary = (TextView) getActivity().findViewById(R.id.horoscope_summary);
        mJokeTextView = (TextView) getActivity().findViewById(R.id.one_joke);
        mBrainRiddleTextView = (TextView) getActivity().findViewById(R.id.one_riddle);
        mJokeContentLoadingImg = (ImageView) getActivity().findViewById(R.id.joke_loading);
        mBrainRiddleContentLoadingImg = (ImageView) getActivity().findViewById(R.id.riddle_loading);
        mHoroscopeContentLoadingImg = (ImageView) getActivity().findViewById(R.id.horoscope_loading) ;
        mHoroscopeDate = (TextView) getActivity().findViewById(R.id.horoscope_date);
        mJokeMoreButton = (Button) getActivity().findViewById(R.id.joke_more);
        mJokeShareButton = (Button) getActivity().findViewById(R.id.joke_share);
        mJokeReloadButton = (Button) getActivity().findViewById(R.id.joke_reload);
        mHoroscopeMoreButton = (Button) getActivity().findViewById(R.id.horoscope_more);
        mHoroscopeShareButton = (Button) getActivity().findViewById(R.id.horoscope_share);
        mHoroscopeReloadButton = (Button) getActivity().findViewById(R.id.horoscope_reload);
        mBrainRiddleMoreButton = (Button) getActivity().findViewById(R.id.riddle_more);
        mBrainRiddleShareButton = (Button) getActivity().findViewById(R.id.riddle_share);
        mBrainRiddleReloadButton = (Button) getActivity().findViewById(R.id.riddle_reload);
        mLoadingProgressBar = (SmoothProgressBar) getActivity().findViewById(R.id.init_loading);
        setHoroscopeCardView();
        mHoroscopeDate.setEnabled(false);
        Date date = new Date(System.currentTimeMillis());
        mHoroscopeDate.setText(DateFormat.getLongDateFormat(getActivity()).format(date));
        mHoroscopeCardView.setOnClickListener(this);
        mHoroscopeMoreButton.setOnClickListener(this);
        mHoroscopeShareButton.setOnClickListener(this);
        mHoroscopeReloadButton.setOnClickListener(this);
        mBrainRiddleCardView.setOnClickListener(this);
        mBrainRiddleMoreButton.setOnClickListener(this);
        mBrainRiddleShareButton.setOnClickListener(this);
        mBrainRiddleReloadButton.setOnClickListener(this);
        mJokeCardView.setOnClickListener(this);
        mJokeMoreButton.setOnClickListener(this);
        mJokeShareButton.setOnClickListener(this);
        mJokeReloadButton.setOnClickListener(this);
        if (mIsProgressBarShown) {
            mLoadingProgressBar.setVisibility(View.VISIBLE);
        } else {
            mLoadingProgressBar.setVisibility(View.GONE);
        }
    }

    private void hideProgressBarIfNotLoading() {
        if (mHoroscopeLoadState != LoadingState.LOADING
                && mJokeLoadState != LoadingState.LOADING
                && mBrainRiddleLoadState != LoadingState.LOADING) {
            mLoadingProgressBar.setVisibility(View.GONE);
            mIsProgressBarShown = false;
        }
    }

    private boolean isProgressBarShown() {
        return mIsProgressBarShown;
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.horoscope_card:
            case R.id.horoscope_more:
                MainActivity.getInstance().replaceContentFragment(new HoroscopeFragment(),true);
                MainActivity.getInstance().setTitle(getString(R.string.horoscope));
                break;
            case R.id.horoscope_share:
                shareHoroscope();
                break;
            case R.id.horoscope_reload:
                doGetHoroscope();
                break;
            case R.id.riddle_card:
                mBrainRiddleTextView.setAlpha(0);
                if(mBrainRiddleTextView.getText().equals(mRiddleData.get(0).getQuestion()))
                    mBrainRiddleTextView.setText(mRiddleData.get(0).getAnswer());
                else
                    mBrainRiddleTextView.setText(mRiddleData.get(0).getQuestion());
                mBrainRiddleTextView.setAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.alpha));
                mBrainRiddleTextView.setAlpha(1);
                break;
            case R.id.riddle_more:
                MainActivity.getInstance().replaceContentFragment(new BrainRiddleFragment(),true);
                MainActivity.getInstance().setTitle(getString(R.string.brain_riddles));
                break;
            case R.id.riddle_share:
                Utils.share(getActivity(),mRiddleData.get(0).getQuestion());
                break;
            case R.id.riddle_reload:
                doGetBrainRiddles();
                break;
            case R.id.joke_card:
            case R.id.joke_more:
                MainActivity.getInstance().replaceContentFragment(new JokeFragment(),true);
                MainActivity.getInstance().setTitle(getString(R.string.jokes));
                break;
            case R.id.joke_share:
                Utils.share(getActivity(),mJokeData.get(mRandomJokeNum).getBody());
                break;
            case R.id.joke_reload:
                doGetJokes();
                break;
            default:
                break;
        }
    }

    public void setHoroscopeCardView() {
        int bgNum = Utils.getRandomNumber(1, 4);
        int avatarNum = Utils.getRandomNumber(1, 2);
        String name = HoroscopeInfo.getLatinName(mHoroscopeName);
        if (mHoroscopeBgRes == -1 || mHoroscopeChanged) {
            mHoroscopeBgRes = Utils.getResourceIdByName(
                    "horoscope_bg_" + bgNum,
                    "drawable");
        }
        if (mHoroscopeAvatarRes == -1 || mHoroscopeChanged) {
            mHoroscopeAvatarRes = Utils.getResourceIdByName(
                    "ic_horoscope_" + name + "_" + avatarNum,
                    "drawable");
        }
        if (mHoroscopeBgRes != -1) {
            mHoroscopeBgImg.setImageResource(mHoroscopeBgRes);
        }
        if (mHoroscopeAvatarRes != -1) {
            mHoroscopeAvatarImg.setImageResource(mHoroscopeAvatarRes);
        }

        /*
        Bitmap bitmap = ((BitmapDrawable) ContextCompat.getDrawable(getActivity(), bgRes)).getBitmap();
        if (bitmap != null) {
            Utils.getBitmapColor(bitmap, new Utils.BitmapColorCallback(){
                @Override
                public void onGenerated(Palette palette) {
                    Palette.Swatch swatchLight = palette.getLightMutedSwatch();

                    if (swatchLight != null) {
                        mHoroscopeCardView.setCardBackgroundColor(swatchLight.getRgb());
                    }

                }
            });
        }
        */
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
                mBrainRiddleLoadState = LoadingState.LOAD_FAIL;
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
                    mHoroscopeLoadState = LoadingState.LOAD_FAIL;
                }
            });
            mHoroscopeLoadState = LoadingState.LOADING;
            mProcess.execute();
            doGetHoroscopeAnimate();
        }
    }

    private void doGetJokeAnimate() {
        final View loadingView = mJokeContentLoadingImg;
        if (isProgressBarShown()) {
            loadingView.setVisibility(View.GONE);
        } else {
            loadingView.setVisibility(View.VISIBLE);
        }
        mJokeReloadButton.setVisibility(View.GONE);
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
                                mJokeTextView.setText(mJokeData.get(mRandomJokeNum).getBody());
                                mJokeCardView.setAlpha(0);
                                mJokeCardView.animate()
                                        .alpha(1f)
                                        .setInterpolator(new DecelerateInterpolator(3f))
                                        .setDuration(1000).start();
                            } else if (mJokeLoadState == LoadingState.LOAD_FAIL){
                                mJokeReloadButton.setVisibility(View.VISIBLE);
                            }

                            // 隐藏加载图标
                            if (loadingView.getVisibility() == View.VISIBLE) {
                                loadingView.setVisibility(View.GONE);
                            }
                            mJokeLoadState = LoadingState.IDLE;

                            hideProgressBarIfNotLoading();
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
        if (isProgressBarShown()) {
            loadingView.setVisibility(View.GONE);
        } else {
            loadingView.setVisibility(View.VISIBLE);
        }
        mBrainRiddleReloadButton.setVisibility(View.GONE);
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
                                mBrainRiddleCardView.setAlpha(0);
                                mBrainRiddleCardView.animate()
                                        .alpha(1f)
                                        .setInterpolator(new DecelerateInterpolator(3f))
                                        .setDuration(1000).start();
                            } else if (mBrainRiddleLoadState == LoadingState.LOAD_FAIL){
                                mBrainRiddleReloadButton.setVisibility(View.VISIBLE);
                            }

                            // 隐藏加载图标
                            if (loadingView.getVisibility() == View.VISIBLE) {
                                loadingView.setVisibility(View.GONE);
                            }

                            mBrainRiddleLoadState = LoadingState.IDLE;

                            hideProgressBarIfNotLoading();
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
        if (isProgressBarShown()) {
            mLoadingView.setVisibility(View.GONE);
        } else {
            mLoadingView.setVisibility(View.VISIBLE);
        }
        mHoroscopeReloadButton.setVisibility(View.GONE);
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
                                showHoroscopeCard();
                            } else if (mHoroscopeLoadState == LoadingState.LOAD_FAIL){
                                // 网络请求失败
                                mLoadingView.setVisibility(View.GONE);
                                mHoroscopeReloadButton.setVisibility(View.VISIBLE);
                            }
                            mHoroscopeLoadState = LoadingState.IDLE;

                            hideProgressBarIfNotLoading();
                        }
                    }

                    public void onAnimationStart(Animator animation) {}
                    public void onAnimationCancel(Animator animation) {}
                    public void onAnimationRepeat(Animator animation) {}
                })
                .start();
    }

    private void showHoroscopeCard() {
        mHoroscopeContentLoadingImg.setVisibility(View.INVISIBLE);
        mHoroscopeTitle.setText(String.format("%s今日综合评价", mHoroscopeName));
        mHoroscopeSummary.setText(mHoroscope.getSummary());
        mHoroscopeCardView.setVisibility(View.VISIBLE);

        mHoroscopeCardView.setAlpha(0f);
        ViewPropertyAnimator animator = mHoroscopeCardView.animate();
        animator.setDuration(1000)
                .alpha(1f)
                .setListener(null)
                .setInterpolator(new DecelerateInterpolator(3f));
        animator.start();
    }

    /*
    申请外部存储读写权限
    */
    private boolean checkPermissionBeforeClick(String permission, View view) {
        if (ContextCompat.checkSelfPermission(getActivity(), permission)
                != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(getActivity(), new String[]{permission},
                    HoroscopeFragment.PERMISSION_REQ_CODE);
            mPermissionView = view;
            return false;
        }
        return true;
    }

    private void shareHoroscope() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (!checkPermissionBeforeClick(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE, mHoroscopeShareButton)) {
                // 无权限，需要先授权
                return;
            }

            // 先创建目录
            final String dir = Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/Pictures/Haha/Horoscope/";
            File dirFile = new File(dir);
            if (!dirFile.exists()) {
                dirFile.mkdirs();
            }

            // 以星座及时间为文件名
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int min = calendar.get(Calendar.MINUTE);
            int sec = calendar.get(Calendar.SECOND);
            final String fileName = String.format("%s_summary_%d%d%d%d%d%d.png",
                    HoroscopeInfo.getLatinName(mHoroscopeName),
                    year, month, day, hour, min, sec);

            // 将 view 保存为图片
            Utils.saveViewAsPicture(mHoroscopeCardView, dir + fileName,
                    new Runnable() {
                        @Override
                        public void run() {
                            // 保存成功，启动分享功能
                            Utils.share(new File(dir + fileName), Utils.FILE_TYPE_IMAGE);
                        }
                    }, null);
        }
    }

    private void expandView(final View view, int width, int height) {
        if (Build.VERSION.SDK_INT >= 21 && view.isAttachedToWindow()) {
            Animator anim = ViewAnimationUtils.createCircularReveal(
                    view, width / 2, height / 2, 0, width);
            anim.setDuration(BrainRiddleAdapter.CIRCULAR_REVEAL_DURATION);
            anim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    view.setVisibility(View.VISIBLE);
                }

                public void onAnimationEnd(Animator animation) {}
                public void onAnimationCancel(Animator animation) {}
                public void onAnimationRepeat(Animator animation) {}
            });
            anim.start();
        } else {
            view.setVisibility(View.VISIBLE);
        }
    }

}
