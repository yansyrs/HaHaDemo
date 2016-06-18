package com.yan.haha;

import android.animation.Animator;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.Toast;

import com.yan.haha.adapter.JokeAdapter;
import com.yan.haha.units.Jokes;
import com.yan.haha.utils.GetJoke;
import com.yan.haha.utils.Utils;

import java.util.ArrayList;

public class JokeFragment extends ContentFragment implements OnDataFinishedListener {
    private RecyclerView mJokeList = null;
    private JokeAdapter mAdapter = new JokeAdapter(MainActivity.getInstance(), JokeFragment.this);
    private ArrayList<Jokes> mJokeData = new ArrayList<Jokes>();

    private static int REQUEST_PAGE = 1;
    private int jokeNum = 10;
    private GetJoke mJokeRequest = null;

    private ImageView mContentLoadingImg = null;
    private FloatingActionButton mFab = null;

    private enum LoadingState {
        IDLE, LOADING, LOAD_SUCCESS, LOAD_FAIL
    }

    private LoadingState mLoadState = LoadingState.IDLE;
    private boolean mIsFabShown = true;
    private SQLiteDatabase db;
    protected static final String DB_NAME = "jokes.db";
    protected static final String TABLE_NAME = "Jokes";
    private MenuItemAnim mMenuItemAnim;
    private ImageView mMenuButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.joke_fragment, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.favorite_menu, menu);
        mMenuButton = (ImageView) menu.findItem(R.id.joke_favorite_action).getActionView();
        setJokeMenuButtonBackground();
        mMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.getInstance().replaceContentFragment(new JokeFavoriteFragment(),true);
            }
        });
    }

    public void setJokeMenuButtonBackground() {
        db = MainActivity.getInstance().openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
        Cursor tmpCursor = db.rawQuery("SELECT count(*) FROM sqlite_master WHERE type='table' AND name='"+TABLE_NAME+"'", null);
        //上面先判断表是否存在
        if(tmpCursor.moveToNext() && tmpCursor.getInt(0) > 0) {
            tmpCursor = db.rawQuery("SELECT count(*) FROM "+TABLE_NAME, null);
            if(tmpCursor.moveToNext() && tmpCursor.getInt(0) > 0)
                mMenuButton.setBackgroundResource(R.drawable.ic_appbar_favorite_red);
            else
                mMenuButton.setBackgroundResource(R.drawable.ic_appbar_favorite_white);
        }else {
            mMenuButton.setBackgroundResource(R.drawable.ic_appbar_favorite_white);
        }
        tmpCursor.close();
        db.close();
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
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.joke_favorite_action) {
            //被onCreateOptionsMenu中的imageView的click事件替代
            //MainActivity.getInstance().replaceContentFragment(new JokeFavoriteFragment(),true);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        initViews();
        mJokeList.setHasFixedSize(true);
        LinearLayoutManager lm = new LinearLayoutManager(getActivity());
        lm.setOrientation(LinearLayoutManager.VERTICAL);
        mJokeList.setLayoutManager(lm);
        mJokeList.setAdapter(mAdapter);
        if (mJokeData.size() == 0) {
            doGetJokes();
        }else {
            mContentLoadingImg.setVisibility(View.GONE);
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
        mAdapter.refresh();
    }

    private void doGetJokeAnimate() {
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
                            doGetJokeAnimate();
                            return;
                        } else if (mLoadState != LoadingState.IDLE) {
                            // 网络请求成功，加载数据到列表
                            if (mLoadState == LoadingState.LOAD_SUCCESS) {
                                mAdapter.setJokeList(mJokeData);
                                mJokeList.scrollToPosition(0);
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

                    public void onAnimationStart(Animator animation) {
                    }

                    public void onAnimationCancel(Animator animation) {
                    }

                    public void onAnimationRepeat(Animator animation) {
                    }
                });
        ani.start();
    }

    private void doGetJokes() {
        if (mLoadState != LoadingState.LOADING) {
            mLoadState = LoadingState.LOADING;
            // 创建网络请求
            mJokeRequest = getJokeProcess();
            mJokeRequest.execute();
            // 显示“加载中”动画
            doGetJokeAnimate();
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

    private GetJoke getJokeProcess() {
        GetJoke process = new GetJoke(REQUEST_PAGE, jokeNum);
        process.setOnDataFinishedListener(this);
        return process;
    }

    private void initViews() {
        mJokeList = (RecyclerView) getActivity().findViewById(R.id.joke_list);

        mFab = (FloatingActionButton) getActivity()
                .findViewById(R.id.joke_fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                REQUEST_PAGE += 1;
                doGetJokes();
            }
        });
       mFab.setVisibility(View.INVISIBLE);
        mContentLoadingImg = (ImageView) getActivity().findViewById(R.id.joke_loading);

        mJokeList.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

    @Override
    public void onDataSuccessfully(Object data) {
        ArrayList<Jokes> result = (ArrayList<Jokes>) data;
        mJokeData = result;
        mLoadState = LoadingState.LOAD_SUCCESS;
    }

    @Override
    public void onDataFailed() {
        mLoadState = LoadingState.LOAD_FAIL;
    }
}
