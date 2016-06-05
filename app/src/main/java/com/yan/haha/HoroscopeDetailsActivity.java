package com.yan.haha;

import android.animation.Animator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.yan.haha.units.Horoscope;
import com.yan.haha.units.HoroscopeInfo;
import com.yan.haha.utils.GetHoroscope;
import com.yan.haha.utils.Utils;

import java.util.ArrayList;

public class HoroscopeDetailsActivity extends Activity implements OnDataFinishedListener {
    private ImageView mHeaderBg = null;
    private ImageView mHeaderAvatar = null;

    private ViewGroup mTodayHeader = null;
    private ViewGroup mWeekHeader = null;
    private ViewGroup mMonthHeader = null;
    private ViewGroup mYearHeader = null;

    private String mHoroscopeName = null;

    private String mHeaderBgName = null;
    private String mHeaderAvatarName = null;

    private ViewGroup mContent = null;
    private ViewGroup mLoadContainer = null;
    private ImageView mLoading = null;
    private Button mReload = null;

    private Horoscope mTodayInfo = null;
    private Horoscope mWeekInfo = null;
    private Horoscope mMonthInfo = null;
    private Horoscope mYearInfo = null;

    private GetHoroscope mProcess = null;

    private enum LoadState {
        IDLE, LOAD_TODAY, LOAD_WEEK, LOAD_MONTH, LOAD_YEAR, LOAD_SUCCESS, LOAD_FAIL
    }

    private LoadState mLoadState = LoadState.IDLE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        mHoroscopeName = intent.getStringExtra("name");
        if (HoroscopeInfo.getLatinName(mHoroscopeName) == null) {
            finish();
        }

        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= 19) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            if (Build.VERSION.SDK_INT >= 21) {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.TRANSPARENT);
            }
        }
        setContentView(R.layout.activity_horoscope_details);
        initViews();
    }

    private void initViews() {
        mHeaderBg = (ImageView) findViewById(R.id.horoscope_details_bg);
        mHeaderAvatar = (ImageView) findViewById(R.id.horoscope_details_avatar);

        mTodayHeader = (ViewGroup) findViewById(R.id.today_header);
        mWeekHeader = (ViewGroup) findViewById(R.id.week_header);
        mMonthHeader = (ViewGroup) findViewById(R.id.month_header);
        mYearHeader = (ViewGroup) findViewById(R.id.year_header);

        /* 获取 header 的背景和头像图片名字 */
        mHeaderBgName = "horoscope_bg_" + Utils.getRandomNumber(1, 4);
        mHeaderAvatarName = "ic_horoscope_" + HoroscopeInfo.getLatinName(mHoroscopeName)
                + "_" + Utils.getRandomNumber(1, 2);

        /* 设置头像 */
        mHeaderAvatar.setImageResource(Utils.getResourceIdByName(mHeaderAvatarName, "drawable"));

        /* 设置 header 背景并根据背景颜色设置列表各项颜色 */
        Bitmap bitmap = ((BitmapDrawable) ContextCompat.getDrawable(this,
                Utils.getResourceIdByName(mHeaderBgName, "drawable"))).getBitmap();
        if (bitmap != null) {
            mHeaderBg.setImageBitmap(bitmap);
            Utils.getBitmapColor(bitmap, new Utils.BitmapColorCallback(){
                @Override
                public void onGenerated(Palette palette) {
                    Palette.Swatch swatchLight = palette.getLightMutedSwatch();
                    Palette.Swatch swatch = palette.getMutedSwatch();

                    if (swatchLight != null) {
                        mTodayHeader.setBackgroundColor(swatchLight.getRgb());
                        mMonthHeader.setBackgroundColor(swatchLight.getRgb());
                    }

                    if (swatch != null) {
                        mWeekHeader.setBackgroundColor(swatch.getRgb());
                        mYearHeader.setBackgroundColor(swatch.getRgb());
                    }
                }
            });
        }

        mContent = (ViewGroup) findViewById(R.id.horoscope_details_content);
        mLoadContainer = (ViewGroup) findViewById(R.id.horoscope_details_load_container);
        mLoading = (ImageView) findViewById(R.id.horoscope_details_loading);
        mReload = (Button) findViewById(R.id.horoscope_details_reload);

        /* 隐藏布局，等待网络请求完成 */
        mContent.setVisibility(View.GONE);
        mLoadContainer.setVisibility(View.GONE);
        mLoading.setVisibility(View.GONE);
        mReload.setVisibility(View.GONE);

        mReload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doLoadData();
            }
        });

        doLoadData();
    }

    private void onLoadDataFinish() {
        mContent.setVisibility(View.VISIBLE);
        mLoadContainer.setVisibility(View.GONE);

        /* 今日 */
        ((TextView) findViewById(R.id.today_overall)).setText(mTodayInfo.getcomplexPoints());
        ((TextView) findViewById(R.id.today_work)).setText(mTodayInfo.getWorkPoints());
        ((TextView) findViewById(R.id.today_love)).setText(mTodayInfo.getLovePoints());
        ((TextView) findViewById(R.id.today_health)).setText(mTodayInfo.getHealthPoints());
        ((TextView) findViewById(R.id.today_money)).setText(mTodayInfo.getMoneyPoints());
        ((TextView) findViewById(R.id.today_lucky_color)).setText(mTodayInfo.getColor());
        ((TextView) findViewById(R.id.today_lucky_number)).setText("" + mTodayInfo.getLuckyNum());
        ((TextView) findViewById(R.id.today_qfriend)).setText(mTodayInfo.getQFriend());
        ((TextView) findViewById(R.id.today_summary)).setText(mTodayInfo.getSummary());

        /* 默认展开今日和本周 */
        mTodayHeader.performClick();
        mWeekHeader.performClick();
    }

    private void doLoadDataAnimate() {
        mContent.setVisibility(View.GONE);
        mReload.setVisibility(View.GONE);
        mLoadContainer.setVisibility(View.VISIBLE);
        mLoading.setVisibility(View.VISIBLE);
        mLoading.setRotation(0f);
        mLoading.animate()
                .rotation(360f)
                .setDuration(1000)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (mLoadState == LoadState.LOAD_TODAY
                                || mLoadState == LoadState.LOAD_WEEK
                                || mLoadState == LoadState.LOAD_MONTH
                                || mLoadState == LoadState.LOAD_YEAR) {
                            // 仍在加载中
                            doLoadDataAnimate();
                        } else if (mLoadState == LoadState.LOAD_SUCCESS) {
                            onLoadDataFinish();
                        } else if (mLoadState == LoadState.LOAD_FAIL) {
                            mLoading.setVisibility(View.GONE);
                            mReload.setVisibility(View.VISIBLE);
                        }
                    }

                    public void onAnimationStart(Animator animation) {
                    }

                    public void onAnimationCancel(Animator animation) {
                    }

                    public void onAnimationRepeat(Animator animation) {
                    }
                })
                .start();
    }

    private void doLoadDataInt(String type) {
        mProcess = new GetHoroscope(type, mHoroscopeName);
        mProcess.setOnDataFinishedListener(this);
        mProcess.execute();
    }

    private void doLoadData() {
        if (mLoadState != LoadState.IDLE) {
            return;
        }
        mLoadState = LoadState.LOAD_TODAY;
        doLoadDataInt("today");
        doLoadDataAnimate();
    }

    @Override
    public void onDataSuccessfully(Object data) {
        Horoscope horoscope = ((ArrayList<Horoscope>) data).get(0);
        if (mLoadState == LoadState.LOAD_TODAY) {
            mTodayInfo = horoscope;
            mLoadState = LoadState.LOAD_WEEK;
            doLoadDataInt("week");
        } else if (mLoadState == LoadState.LOAD_WEEK) {
            mWeekInfo = horoscope;
            mLoadState = LoadState.LOAD_MONTH;
            doLoadDataInt("month");
        } else if (mLoadState == LoadState.LOAD_MONTH) {
            mMonthInfo = horoscope;
            mLoadState = LoadState.LOAD_YEAR;
            doLoadDataInt("year");
        } else if (mLoadState == LoadState.LOAD_YEAR) {
            mYearInfo = horoscope;
            mLoadState = LoadState.LOAD_SUCCESS;
        }
    }

    @Override
    public void onDataFailed() {
        mLoadState = LoadState.LOAD_FAIL;
    }
}
