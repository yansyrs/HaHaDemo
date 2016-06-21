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
import com.yan.haha.utils.GetBrainRiddle;
import com.yan.haha.utils.GetHoroscope;
import com.yan.haha.utils.GetJoke;
import com.yan.haha.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class InitFragment extends ContentFragment implements View.OnClickListener{
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
    private Button mHoroscopeMoreButton = null;
    private Button mHoroscopeShareButton = null;
    private Button mBrainRiddleMoreButton = null;
    private Button mBrainRiddleShareButton = null;
    private View mPermissionView = null;

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
        mHoroscopeMoreButton = (Button) getActivity().findViewById(R.id.horoscope_more);
        mHoroscopeShareButton = (Button) getActivity().findViewById(R.id.horoscope_share);
        mBrainRiddleMoreButton = (Button) getActivity().findViewById(R.id.riddle_more);
        mBrainRiddleShareButton = (Button) getActivity().findViewById(R.id.riddle_share);
        setHoroscopeCardView();
        mHoroscopeDate.setEnabled(false);
        Date date = new Date(System.currentTimeMillis());
        mHoroscopeDate.setText(DateFormat.getLongDateFormat(getActivity()).format(date));
        mHoroscopeCardView.setOnClickListener(this);
        mHoroscopeMoreButton.setOnClickListener(this);
        mHoroscopeShareButton.setOnClickListener(this);
        mBrainRiddleCardView.setOnClickListener(this);
        mBrainRiddleMoreButton.setOnClickListener(this);
        mBrainRiddleShareButton.setOnClickListener(this);
        mJokeCardView.setOnClickListener(this);
        mJokeMoreButton.setOnClickListener(this);
        mJokeShareButton.setOnClickListener(this);

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
            case R.id.riddle_card:
            case R.id.riddle_more:
                MainActivity.getInstance().replaceContentFragment(new BrainRiddleFragment(),true);
                MainActivity.getInstance().setTitle(getString(R.string.brain_riddles));
                break;
            case R.id.riddle_share:
                Utils.share(getActivity(),mRiddleData.get(0).getQuestion());
                break;
            case R.id.joke_card:
            case R.id.joke_more:
                MainActivity.getInstance().replaceContentFragment(new JokeFragment(),true);
                MainActivity.getInstance().setTitle(getString(R.string.jokes));
                break;
            case R.id.joke_share:
                Utils.share(getActivity(),mJokeData.get(0).getBody());
                break;
            default:
                break;
        }
    }

    public void setHoroscopeCardView() {
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

        Bitmap bitmap = ((BitmapDrawable) ContextCompat.getDrawable(getActivity(), bgRes)).getBitmap();
        /*
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
                                showHoroscopeCard();
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

    private void showHoroscopeCard() {
        mHoroscopeContentLoadingImg.setVisibility(View.INVISIBLE);
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
