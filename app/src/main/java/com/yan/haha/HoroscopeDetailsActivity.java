package com.yan.haha;

import android.Manifest;
import android.animation.Animator;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.andexert.expandablelayout.library.ExpandableLayout;
import com.yan.haha.units.Horoscope;
import com.yan.haha.units.HoroscopeInfo;
import com.yan.haha.utils.GetHoroscope;
import com.yan.haha.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HoroscopeDetailsActivity extends Activity
        implements OnDataFinishedListener, ExpandableLayout.ToggleListener {
    private ImageView mHeaderBg = null;
    private ImageView mHeaderAvatar = null;
    private TextView mHeaderTitle = null;

    private ViewGroup mTodayHeader = null;
    private ViewGroup mWeekHeader = null;
    private ViewGroup mMonthHeader = null;
    private ViewGroup mYearHeader = null;

    private ExpandableLayout mTodayLayout = null;
    private ExpandableLayout mWeekLayout = null;
    private ExpandableLayout mMonthLayout = null;
    private ExpandableLayout mYearLayout = null;

    private ScrollView mScrollView = null;

    private String mHoroscopeName = null;

    private String mHeaderBgName = null;
    private String mHeaderAvatarName = null;

    private ViewGroup mContent = null;
    private ViewGroup mLoadContainer = null;
    private ImageView mLoading = null;
    private Button mReload = null;

    private String mSavedFileName = null;
    private String mFabState = "save";
    private FloatingActionButton mFab = null;

    private Horoscope mTodayInfo = null;
    private Horoscope mWeekInfo = null;
    private Horoscope mMonthInfo = null;
    private Horoscope mYearInfo = null;

    private GetHoroscope mProcess = null;

    private static int PERMISSION_REQ_CODE = 0;
    private View mPermissionView = null;

    private boolean mDataLoaded = false;
    private boolean mFabShown = true;
    private boolean mFabAnimationRunning = false;

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
        setStatusBarTransparent(true);
        setContentView(R.layout.activity_horoscope_details);
        initViews();
    }

    private void setStatusBarTransparent(boolean enable) {
        if (Build.VERSION.SDK_INT >= 19) {
            if (enable) {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                if (Build.VERSION.SDK_INT >= 21) {
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    getWindow().setStatusBarColor(Color.TRANSPARENT);
                }
            } else {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                getWindow().getDecorView().setSystemUiVisibility(0);
            }
        }
    }

    private class DescriptionItem {
        public String mLabel;
        public String mText;

        public DescriptionItem(String label, String text) {
            mLabel = label;
            mText = text;
        }
    }

    private DescriptionItem getDescriptionItem(String text) {
        DescriptionItem item = null;

        Pattern p = Pattern.compile("(.{2,4})：(.*)");
        Matcher m = p.matcher(text);
        if (m.matches()) {
            item = new DescriptionItem(m.group(1), m.group(2));
        } else {
            item = new DescriptionItem(null, text);
        }
        return item;
    }

    private void initViews() {
        mHeaderBg = (ImageView) findViewById(R.id.horoscope_details_bg);
        mHeaderAvatar = (ImageView) findViewById(R.id.horoscope_details_avatar);
        mHeaderTitle = (TextView) findViewById(R.id.horoscope_details_title);

        mTodayHeader = (ViewGroup) findViewById(R.id.today_header);
        mWeekHeader = (ViewGroup) findViewById(R.id.week_header);
        mMonthHeader = (ViewGroup) findViewById(R.id.month_header);
        mYearHeader = (ViewGroup) findViewById(R.id.year_header);

        mTodayLayout = (ExpandableLayout) findViewById(R.id.horoscope_details_today);
        mWeekLayout = (ExpandableLayout) findViewById(R.id.horoscope_details_week);
        mMonthLayout = (ExpandableLayout) findViewById(R.id.horoscope_details_month);
        mYearLayout = (ExpandableLayout) findViewById(R.id.horoscope_details_year);

        mScrollView = (ScrollView) findViewById(R.id.horoscope_details_scroll_view);
        mFab = (FloatingActionButton) findViewById(R.id.horoscope_details_save_fab);

        /* 获取 header 的背景和头像图片名字 */
        mHeaderBgName = "horoscope_bg_" + Utils.getRandomNumber(1, 4);
        mHeaderAvatarName = "ic_horoscope_" + HoroscopeInfo.getLatinName(mHoroscopeName)
                + "_" + Utils.getRandomNumber(1, 2);

        /* 设置头像和标题 */
        mHeaderAvatar.setImageResource(Utils.getResourceIdByName(mHeaderAvatarName, "drawable"));
        String dateString = "";
        Date date = new Date(System.currentTimeMillis());
        dateString = DateFormat.getLongDateFormat(this).format(date);
        mHeaderTitle.setText(mHoroscopeName + "  " + dateString);

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

        mTodayLayout.setOnToggleListener(this);
        mWeekLayout.setOnToggleListener(this);
        mMonthLayout.setOnToggleListener(this);
        mYearLayout.setOnToggleListener(this);

        /* 添加滚动监听 */
        mScrollView.getViewTreeObserver().addOnScrollChangedListener(
                new ViewTreeObserver.OnScrollChangedListener() {
                    @Override
                    public void onScrollChanged() {
                        onScroll();
                    }
                });

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFabState.equals("save")) {
                    saveToFile();
                } else if (mFabState.equals("share") && mSavedFileName != null) {
                    Utils.share(new File(mSavedFileName), Utils.FILE_TYPE_IMAGE);
                }
            }
        });

        doLoadData();
    }

    private void hideFab() {
        if (!mFabAnimationRunning && mFabShown) {
            mFabShown = false;
            mFab.setScaleY(1f);
            mFab.setScaleX(1f);
            mFab.animate()
                    .scaleX(0.01f).scaleY(0.01f) // 此处设成 0 会崩溃，原因未知。。
                    .setInterpolator(new DecelerateInterpolator(3f))
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            mFabAnimationRunning = true;
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mFabAnimationRunning = false;
                            mFab.setVisibility(View.GONE);
                        }

                        public void onAnimationCancel(Animator animation) {}
                        public void onAnimationRepeat(Animator animation) {}
                    })
                    .setDuration(500)
                    .start();
        }
    }

    private void showFab() {
        if (!mFabAnimationRunning && !mFabShown) {
            mFabShown = true;
            mFab.setScaleY(0f);
            mFab.setScaleX(0f);
            mFab.animate()
                    .scaleX(1f).scaleY(1f)
                    .setInterpolator(new DecelerateInterpolator(3f))
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            mFab.setVisibility(View.VISIBLE);
                            mFabAnimationRunning = true;
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mFabAnimationRunning = false;
                        }

                        public void onAnimationCancel(Animator animation) {}
                        public void onAnimationRepeat(Animator animation) {}
                    })
                    .setDuration(500)
                    .start();
        }
    }

    private void onScroll() {
        if (!mDataLoaded) {
            return;
        }
        int scrollY = mScrollView.getScrollY();
        int headerHeight = mHeaderBg.getHeight();
        int fabHeight = mFab.getHeight();

        /* 状态栏透明 */
        if (scrollY <= headerHeight) {
            setStatusBarTransparent(true);
        } else {
            setStatusBarTransparent(false);
        }

        /* 隐藏 fab 按钮 */
        if (scrollY <= (headerHeight - (fabHeight / 2))) {
            showFab();
        } else {
            hideFab();
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
        if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(this, new String[]{ permission },
                    PERMISSION_REQ_CODE);
            mPermissionView = view;
            return false;
        }
        return true;
    }

    private void saveToFile() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (!checkPermissionBeforeClick(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE, mFab)) {
                // 无权限，需要先授权
                return;
            }
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
        final String fileName = String.format("%s_%d%d%d%d%d%d.png",
                            HoroscopeInfo.getLatinName(mHoroscopeName),
                            year, month, day, hour, min, sec);

        // 开始保存
        Utils.saveViewAsPicture(
                findViewById(R.id.horoscope_details_container), dir + fileName,
                new Runnable() {
                    @Override
                    public void run() {
                        mSavedFileName = dir + fileName;
                        String hint = getResources().getString(R.string.complete) + ": " + mSavedFileName;
                        Toast.makeText(HoroscopeDetailsActivity.this,
                                hint, Toast.LENGTH_SHORT).show();

                        // 将 fab 按钮改为分享
                        mFabState = "share";
                        mFab.setImageResource(R.drawable.ic_share);
                    }
                }, null);
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

        /* 本周 */
        Pattern p = Pattern.compile("(.*)(作者：.*$)");
        Matcher m = p.matcher(mWeekInfo.getHealth());
        if (m.matches()) {
            DescriptionItem weekAuthor = getDescriptionItem(m.group(2));
            DescriptionItem weekHealth = getDescriptionItem(m.group(1));
            if (weekAuthor.mLabel != null) {
                ((TextView) findViewById(R.id.week_author_label)).setText(weekAuthor.mLabel);
            }
            ((TextView) findViewById(R.id.week_author)).setText(weekAuthor.mText);
            if (weekHealth.mLabel != null) {
                ((TextView) findViewById(R.id.week_health_label)).setText(weekHealth.mLabel);
            }
            ((TextView) findViewById(R.id.week_health)).setText(weekHealth.mText);
        } else {
            findViewById(R.id.week_author_label).setVisibility(View.GONE);
            findViewById(R.id.week_author).setVisibility(View.GONE);

            DescriptionItem weekHealth = getDescriptionItem(mWeekInfo.getHealth());
            if (weekHealth.mLabel != null) {
                ((TextView) findViewById(R.id.week_health_label)).setText(weekHealth.mLabel);
            }
            ((TextView) findViewById(R.id.week_health)).setText(weekHealth.mText);
        }
        DescriptionItem weekJob = getDescriptionItem(mWeekInfo.getJob());
        DescriptionItem weekLove = getDescriptionItem(mWeekInfo.getLove());
        DescriptionItem weekMoney = getDescriptionItem(mWeekInfo.getMoney());
        DescriptionItem weekWork = getDescriptionItem(mWeekInfo.getWork());
        if (weekJob.mLabel != null) {
            ((TextView) findViewById(R.id.week_job_label)).setText(weekJob.mLabel);
        }
        ((TextView) findViewById(R.id.week_job)).setText(weekJob.mText);
        if (weekLove.mLabel != null) {
            ((TextView) findViewById(R.id.week_love_label)).setText(weekLove.mLabel);
        }
        ((TextView) findViewById(R.id.week_love)).setText(weekLove.mText);
        if (weekMoney.mLabel != null) {
            ((TextView) findViewById(R.id.week_money_label)).setText(weekMoney.mLabel);
        }
        ((TextView) findViewById(R.id.week_money)).setText(weekMoney.mText);
        if (weekWork.mLabel != null) {
            ((TextView) findViewById(R.id.week_work_label)).setText(weekWork.mLabel);
        }
        ((TextView) findViewById(R.id.week_work)).setText(weekWork.mText);

        /* 本月 */
        ((TextView) findViewById(R.id.month_overall)).setText(mMonthInfo.getSummary());
        ((TextView) findViewById(R.id.month_health)).setText(mMonthInfo.getHealth());
        ((TextView) findViewById(R.id.month_love)).setText(mMonthInfo.getLove());
        ((TextView) findViewById(R.id.month_money)).setText(mMonthInfo.getMoney());
        ((TextView) findViewById(R.id.month_work)).setText(mMonthInfo.getWork());

        /* 今年 */
        ((TextView) findViewById(R.id.year_info)).setText(mYearInfo.getSummaryTitle());
        ((TextView) findViewById(R.id.year_overall)).setText(mYearInfo.getSummary());
        ((TextView) findViewById(R.id.year_work)).setText(mYearInfo.getWork());
        ((TextView) findViewById(R.id.year_love)).setText(mYearInfo.getLove());
        ((TextView) findViewById(R.id.year_health)).setText(mYearInfo.getHealth());
        ((TextView) findViewById(R.id.year_money)).setText(mYearInfo.getMoney());
        ((TextView) findViewById(R.id.year_lucky_stone)).setText(mYearInfo.getLuckyStone());

        /* 默认展开今日和本周 */
        if (!mTodayLayout.isOpened()) {
            mTodayLayout.show();
        }
        if (!mWeekLayout.isOpened()) {
            mWeekLayout.show();
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mDataLoaded = true;
            }
        }, 500);
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

    @Override
    public void onExpandableLayoutShow(ViewGroup parent) {
        ImageView icon = (ImageView) parent.findViewWithTag("expandable_toggle_icon");
        if (icon != null) {
            icon.setRotation(0f);
            icon.animate().rotation(90f).setDuration(250).start();
        }
    }

    @Override
    public void onExpandableLayoutHide(ViewGroup parent) {
        ImageView icon = (ImageView) parent.findViewWithTag("expandable_toggle_icon");
        if (icon != null) {
            icon.setRotation(90f);
            icon.animate().rotation(0f).setDuration(250).start();
        }
    }
}
