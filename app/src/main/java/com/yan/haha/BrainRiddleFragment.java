package com.yan.haha;

import android.animation.Animator;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.Toast;

import com.yan.haha.adapter.BrainRiddleAdapter;
import com.yan.haha.units.BrainRiddle;
import com.yan.haha.utils.GetBrainRiddle;
import com.yan.haha.utils.RiddleDb;

import java.util.ArrayList;
import java.util.List;

public class BrainRiddleFragment extends ContentFragment
        implements OnDataFinishedListener,
        BrainRiddleAdapter.AppBarFavoriteUpdater {
    private RecyclerView mBrainRiddleList = null;
    private BrainRiddleAdapter mAdapter = null;
    private ArrayList<BrainRiddle> mRiddleData = new ArrayList<BrainRiddle>();

    private final static int RANDOM_COUNT = 15;//10;
    private GetBrainRiddle mBrainRiddleRequest = null;

    private ImageView mContentLoadingImg = null;
    private FloatingActionButton mFab = null;

    private enum LoadingState {
        IDLE, LOADING, LOAD_SUCCESS, LOAD_FAIL
    }

    private LoadingState mLoadState = LoadingState.IDLE;
    private boolean mIsFabShown = true;

    private boolean mFirstRun = true;

    private MenuItemAnim mMenuItemAnim;
    private ImageView mMenuButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.brain_riddle_fragment, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mFirstRun) {
            mFirstRun = false;
            mAdapter = new BrainRiddleAdapter(getActivity());
            mAdapter.setOnAppBarFavoriteUpdater(this);

            initViews();

            mBrainRiddleList.setHasFixedSize(true);

            LinearLayoutManager lm = new LinearLayoutManager(getActivity());
            lm.setOrientation(LinearLayoutManager.VERTICAL);
            mBrainRiddleList.setLayoutManager(lm);
            mBrainRiddleList.setAdapter(mAdapter);

            doGetBrainRiddles();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.favorite_menu, menu);
        mMenuButton = (ImageView) menu.findItem(R.id.joke_favorite_action).getActionView();
        setMenuButtonBackground();
        mMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //MainActivity.getInstance().replaceContentFragment(new JokeFavoriteFragment(),true);
                MainActivity.getInstance().replaceContentFragment(new BrainRiddleFavoriteFragment(),true);
            }
        });
    }

    public void setMenuButtonBackground() {
        RiddleDb db = RiddleDb.getInstance();
        if (db != null) {
            db.openDatabase();
            List<BrainRiddle> list = db.getSavedRiddles();
            if (list == null || list.size() == 0) {
                mMenuButton.setBackgroundResource(R.drawable.ic_appbar_favorite_white);
            } else {
                mMenuButton.setBackgroundResource(R.drawable.ic_appbar_favorite_red);
            }
            db.closeDatabase();
        }
    }

    public void startMenuItemAnim() {
        try {
            mMenuItemAnim = new MenuItemAnim();
            mMenuItemAnim.setDuration(1000);
            mMenuButton.startAnimation(mMenuItemAnim);
        }catch (NullPointerException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onAppBarFavoriteUpdate() {
        setMenuButtonBackground();
        startMenuItemAnim();
    }

    private GetBrainRiddle getBrainRiddleProcess() {
        GetBrainRiddle process = new GetBrainRiddle(RANDOM_COUNT);
        process.setOnDataFinishedListener(this);
        return process;
    }

    private void doGetBrainRiddlesAnimate() {
        final View loadingView = (mContentLoadingImg.getVisibility() == View.VISIBLE)
                ? mContentLoadingImg : mFab;
        loadingView.setRotation(0f);
        ViewPropertyAnimator ani = loadingView.animate()
                .rotation(360f)
                .setDuration(1000)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        loadingView.setRotation(0f);
                        if (mLoadState == LoadingState.LOADING) {
                            // 网络仍在请求中
                            doGetBrainRiddlesAnimate();
                            return;
                        } else if (mLoadState != LoadingState.IDLE){
                            // 网络请求成功，加载数据到列表
                            if (mLoadState == LoadingState.LOAD_SUCCESS) {
                                mAdapter.setBrainDataList(mRiddleData, false);
                                //mAdapter.notifyDataSetChanged();
                                mBrainRiddleList.scrollToPosition(0);
                                mAdapter.refresh();
                            }

                            // 隐藏第一次进入时使用的加载图标
                            if (mContentLoadingImg.getVisibility() == View.VISIBLE) {
                                mContentLoadingImg.setVisibility(View.GONE);
                            } else {
                            }

                            // 用动画弹出 FAB 按钮
                            if (mFab.getVisibility() == View.INVISIBLE) {
                                mFab.setScaleX(0f);
                                mFab.setScaleY(0f);
                                mFab.setVisibility(View.VISIBLE);
                                mFab.animate()
                                        .scaleX(1f)
                                        .scaleY(1f)
                                        .setInterpolator(new DecelerateInterpolator(3.0f))
                                        .setDuration(1000)
                                        .start();
                            }
                            Toast.makeText(getActivity(), (mLoadState == LoadingState.LOAD_SUCCESS)
                                    ? R.string.load_success : R.string.load_failed, Toast.LENGTH_SHORT)
                                    .show();
                            mLoadState = LoadingState.IDLE;
                        }
                    }

                    public void onAnimationStart(Animator animation) {}
                    public void onAnimationCancel(Animator animation) {}
                    public void onAnimationRepeat(Animator animation) {}
                });
        ani.start();
    }

    private void doGetBrainRiddles() {
        if (mLoadState != LoadingState.LOADING) {
            mLoadState = LoadingState.LOADING;
            // 创建网络请求
            mBrainRiddleRequest = getBrainRiddleProcess();
            mBrainRiddleRequest.execute();
            // 显示“加载中”动画
            doGetBrainRiddlesAnimate();
        }
    }

    private void hideFAB() {
        if (mIsFabShown) {
            float destance = mFab.getHeight() + getActivity().getResources().getDimensionPixelSize(R.dimen.fab_margin);
            mFab.setTranslationY(0f);
            mFab.animate()
                    .translationY(destance)
                    .setInterpolator(new DecelerateInterpolator(3.0f))
                    .setDuration(500)
                    .start();
            mIsFabShown = false;
        }
    }

    private void showFAB() {
        if (!mIsFabShown) {
            mFab.animate()
                    .translationY(0f)
                    .setInterpolator(new DecelerateInterpolator(3.0f))
                    .setDuration(500)
                    .start();
            mIsFabShown = true;
        }
    }

    /**
     * 初始化 views
     */
    private void initViews() {
        mBrainRiddleList = (RecyclerView) getActivity().findViewById(R.id.brain_riddle_list);

        mFab = (FloatingActionButton) getActivity()
                                        .findViewById(R.id.brain_riddle_fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doGetBrainRiddles();
            }
        });

        mFab.setVisibility(View.INVISIBLE);
        mContentLoadingImg = (ImageView) getActivity().findViewById(R.id.brain_riddle_loading);

        mBrainRiddleList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 5) {
                    hideFAB();
                } else if (dy < -5) {
                    showFAB();
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    /**
     * 脑筋急转弯获取成功
     * @param data 脑筋急转弯数据
     */
    @Override
    public void onDataSuccessfully(Object data) {
        ArrayList<BrainRiddle> result = (ArrayList<BrainRiddle>) data;
        // 保存数据，等动画完毕后加载
        /*
        if (mRiddleData.size() > 0) {
            mRiddleData.add(null); // 分割线
        }
        mRiddleData.addAll(result);
        */
        mRiddleData = result;
        mLoadState = LoadingState.LOAD_SUCCESS;
    }

    /**
     * 脑筋急转弯获取失败
     */
    @Override
    public void onDataFailed() {
        mLoadState = LoadingState.LOAD_FAIL;
    }
}
