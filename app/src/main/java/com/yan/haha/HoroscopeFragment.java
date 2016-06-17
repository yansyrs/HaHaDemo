package com.yan.haha;

import android.Manifest;
import android.animation.Animator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.dolphinwang.imagecoverflow.CoverFlowView;
import com.yan.haha.adapter.HoroscopeCoverFlowAdapter;
import com.yan.haha.units.Horoscope;
import com.yan.haha.units.HoroscopeInfo;
import com.yan.haha.utils.Config;
import com.yan.haha.utils.GetHoroscope;
import com.yan.haha.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class HoroscopeFragment extends ContentFragment implements OnDataFinishedListener,
        CoverFlowView.CoverFlowListener<HoroscopeCoverFlowAdapter> {
    private Horoscope mHoroscope = null;
    private GetHoroscope mProcess = null;

    private boolean mIsFirstLoad = true;
    private boolean mIsSlimMode = false;

    private CardView mCard = null;
    private ImageView mBgImg = null;
    private ImageView mAvatarImg = null;
    private TextView mTitle = null;
    private TextView mSummary = null;
    private Button mShareBtn = null;
    private Button mMoreBtn = null;
    private TextView mDate = null;
    private ImageView mLoadingImg = null;
    private FloatingActionButton mLoadingFab = null;
    private Button mReloadBtn = null;
    private TextView mName = null;
    private CoverFlowView mCoverFlow = null;
    private ScrollView mScrollView = null;
    private ViewGroup mScrollSelector = null;

    private String mHoroscopeName = "水瓶座";

    private final static int MSG_ID_CHANGE_HOROSCOPE = 0;

    public static int PERMISSION_REQ_CODE = 0;
    private View mPermissionView = null;

    private enum LoadState {
        IDLE, LOADING, LOAD_SUCCESS, LOAD_FAIL
    }

    public static ArrayList<HoroscopeInfo> mHoroscopeList = new ArrayList<HoroscopeInfo>(){
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

    /**
     * 设置精简模式，需要在 Fragment 启动前设置
     * @param enable 是否开启精简模式
     */
    public void setSlimMode(boolean enable) {
        mIsSlimMode = enable;
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
        mCard.setVisibility(View.VISIBLE);

        ViewPropertyAnimator animator = mCard.animate();
        animator.setDuration(1000)
                .setListener(null)
                .setInterpolator(new DecelerateInterpolator(3f));
        if (mIsSlimMode) {
            mCard.setAlpha(0f);
            animator.alpha(1f);
        } else {
            mCard.setTranslationY(Utils.getScreenHeight());
            animator.translationY(0f);
        }
        animator.start();
        if (mCoverFlow.getVisibility() == View.INVISIBLE) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    showCoverFlow();
                }
            }, 600);
        }

        // 判断是否有滚动条，如果没滚动条则将 mCoverFlow 靠底显示
        if (!Utils.canScroll(mScrollView) && !Utils.isScreenLandscape()) {
            //RelativeLayout rl = new RelativeLayout(getActivity());
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            mScrollSelector.setLayoutParams(params);
        } else {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.BELOW, R.id.horoscope_card);
            mScrollSelector.setLayoutParams(params);
        }
    }

    private void doGetHoroscopeSuccessfully() {
        boolean isFirstLoad = (mCard.getVisibility() == View.INVISIBLE);
        mLoadingImg.setVisibility(View.INVISIBLE);
        mLoadingFab.setVisibility(View.INVISIBLE);

        if (isFirstLoad) {
            showCard();
        } else {
            mCard.animate()
                    .translationY(0 - mCard.getHeight() - 10)
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
        boolean isFirstLoad = (mCard.getVisibility() == View.INVISIBLE);
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
                                mLoadingView.setVisibility(View.INVISIBLE);
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
            mReloadBtn.setVisibility(View.INVISIBLE);
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
        if (mIsFirstLoad) {
            mIsFirstLoad = false;
            // 从配置中取出保存的星座名
            String horoscope = Config.getString(Config.KEY_HOROSCOPE, null);
            if (horoscope != null) {
                mHoroscopeName = horoscope;
            }
            initViews();
            doGetHoroscope();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        // 将当前星座保存进配置
        Config.putString(Config.KEY_HOROSCOPE, mHoroscopeName);
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
        mDate = (TextView) getActivity().findViewById(R.id.horoscope_date);

        mScrollView = (ScrollView) getActivity().findViewById(R.id.horoscope_scrollview);
        mScrollSelector = (ViewGroup) getActivity().findViewById(R.id.horoscope_selector);

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

        mDate.setEnabled(false);
        Date date = new Date(System.currentTimeMillis());
        mDate.setText(DateFormat.getLongDateFormat(getActivity()).format(date));

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

        mMoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), HoroscopeDetailsActivity.class);
                intent.putExtra("name", mHoroscopeName);
                startActivity(intent);
            }
        });

        mShareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                share();
            }
        });

        mCard.setVisibility(View.INVISIBLE);
        mLoadingImg.setVisibility(View.INVISIBLE);
        mLoadingFab.setVisibility(View.INVISIBLE);
        mReloadBtn.setVisibility(View.INVISIBLE);
        mName.setVisibility(View.INVISIBLE);
        mCoverFlow.setVisibility(View.INVISIBLE);

        // 紧凑模式
        if (mIsSlimMode) {
            Utils.setViewSize(mBgImg, Utils.SIZE_NOT_CHANGE,
                    Utils.getDimen(R.dimen.horoscope_card_img_height_slim));

            Utils.setViewMargin(getActivity().findViewById(R.id.horoscope_info_container),
                    Utils.getDimen(R.dimen.horoscope_card_img_height_slim),
                    Utils.SIZE_NOT_CHANGE, Utils.SIZE_NOT_CHANGE, Utils.SIZE_NOT_CHANGE);

            Utils.setViewSize(mAvatarImg, Utils.getDimen(R.dimen.horoscope_card_avatar_size_slim),
                    Utils.getDimen(R.dimen.horoscope_card_avatar_size_slim));

            Utils.setViewMargin(mAvatarImg,
                    Utils.getDimen(R.dimen.horoscope_card_avatar_margin_top_slim),
                    Utils.SIZE_NOT_CHANGE, Utils.SIZE_NOT_CHANGE, Utils.SIZE_NOT_CHANGE);

            mTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    Utils.getDimen(R.dimen.horoscope_card_title_size_slim));

            Utils.setViewMargin(mTitle,
                    Utils.getDimen(R.dimen.horoscope_card_title_margin_top_slim),
                    Utils.SIZE_NOT_CHANGE, Utils.SIZE_NOT_CHANGE,
                    Utils.getDimen(R.dimen.horoscope_card_title_margin_left_slim));

            View mainContainer = getActivity().findViewById(R.id.horoscope_container);
            Utils.setViewSize(mainContainer,
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            mainContainer.setPadding(0, 0, 0, 0);

            mScrollSelector.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQ_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mPermissionView != null) {
                    mPermissionView.performClick();
                    mPermissionView = null;
                }
            } else {
                mPermissionView = null;
            }
        }
    }

    /*
    申请外部存储读写权限
    */
    private boolean checkPermissionBeforeClick(String permission, View view) {
        if (ContextCompat.checkSelfPermission(getActivity(), permission)
                != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(getActivity(), new String[]{permission},
                    PERMISSION_REQ_CODE);
            mPermissionView = view;
            return false;
        }
        return true;
    }

    private void share() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (!checkPermissionBeforeClick(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE, mShareBtn)) {
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
            Utils.saveViewAsPicture(mCard, dir + fileName,
                    new Runnable() {
                        @Override
                        public void run() {
                            // 保存成功，启动分享功能
                            Utils.share(new File(dir + fileName), Utils.FILE_TYPE_IMAGE);
                        }
                    }, null);
        }
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
                mHandler.sendMessageDelayed(msg, 1500);
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
