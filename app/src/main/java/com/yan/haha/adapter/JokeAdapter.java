package com.yan.haha.adapter;

import android.animation.Animator;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.yan.haha.R;
import com.yan.haha.units.Jokes;
import com.yan.haha.utils.Utils;

import java.util.ArrayList;

public class JokeAdapter extends RecyclerView.Adapter<JokeAdapter.ViewHolder> implements View.OnTouchListener {
    private ArrayList<Jokes> mJokeData = new ArrayList<Jokes>();
    private ArrayList<ViewHolder> mHolderList = new ArrayList<ViewHolder>();

    private static final int CIRCULAR_REVEAL_DURATION = 400;
    private static final int RUN_UP_ANI_DURATION = 700;
    private static final int RUN_UP_ANI_TIME_GAP = 200;
    private int mGoUpDuration = RUN_UP_ANI_DURATION;

    private final static int MSG_ID_RESET_GO_UP_ANI_STATE = 0;
    private int mLastBindPosition = -1;

    private int mMenuItemHeight = -1;

    private int mDelPosMark = -1;

    private View mPreExpandedView = null;
    private int mCurrExpandedPosition = -1;
    private int positionX;
    private int positionY;
    private Context mContext;
    private SQLiteDatabase db;
    private static final String DB_NAME = "jokes.db";
    public static final String TABLE_NAME = "Jokes";
    private static final String CRLF_REPLACE = ".crlf0.0";


    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewGroup mLayoutView = null;

        public ViewHolder(ViewGroup v) {
            super(v);
            mLayoutView = v;
        }
    }

    public JokeAdapter() {
        super();
    }

    public JokeAdapter(ArrayList<Jokes> jokeList) {
        this();
        mJokeData = jokeList;
    }

    public JokeAdapter(Context context) {
        this.mContext = context;
    }

    public void setJokeList(ArrayList<Jokes> jokeList) {
        mJokeData = jokeList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.joke_list_item, parent, false);

        ViewHolder vh = new ViewHolder((ViewGroup) v);
        v.setOnTouchListener(this);
        return vh;
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MSG_ID_RESET_GO_UP_ANI_STATE:
                    mGoUpDuration = RUN_UP_ANI_DURATION;
                    mDelPosMark = -1;
                    break;
            }
        }
    };

    private void runUpAnimation(final View view, int position) {
        if (position > mLastBindPosition) {
            mLastBindPosition = position;
            if (mDelPosMark >= 0) {
                view.setTranslationY(mMenuItemHeight);
            } else {
                view.setTranslationY(Utils.getScreenHeight());
            }
            view.animate()
                    .translationY(0)
                    .setInterpolator(new DecelerateInterpolator(3.0f))
                    .setDuration(mGoUpDuration)
                    .start();
            mGoUpDuration += 100;
            if (mGoUpDuration >= RUN_UP_ANI_DURATION + RUN_UP_ANI_TIME_GAP * 10) {
                mGoUpDuration = RUN_UP_ANI_DURATION + RUN_UP_ANI_TIME_GAP * 10;
            }
            mHandler.removeMessages(MSG_ID_RESET_GO_UP_ANI_STATE);
            mHandler.sendEmptyMessageDelayed(MSG_ID_RESET_GO_UP_ANI_STATE, mGoUpDuration + 50);
        }
    }

    private void sweepAnimation(View view, final Runnable finishCallback) {
        view.setTranslationX(0);
        view.animate()
                .translationX(Utils.getScreenWidth())
                .setInterpolator(new DecelerateInterpolator(3.f))
                .setDuration(RUN_UP_ANI_DURATION)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        notifyDataSetChanged();
                        if (finishCallback != null) {
                            finishCallback.run();
                        }
                    }

                    public void onAnimationStart(Animator animation) {}
                    public void onAnimationCancel(Animator animation) {}
                    public void onAnimationRepeat(Animator animation) {}
                })
                .start();
    }

    private void expandView(final View view, int width, int height) {
        if (Build.VERSION.SDK_INT >= 21 && view.isAttachedToWindow()) {
            Animator anim = ViewAnimationUtils.createCircularReveal(
                    view, positionX, positionY, 0, width);
            anim.setDuration(CIRCULAR_REVEAL_DURATION);
            anim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    view.setVisibility(View.VISIBLE);
                }

                public void onAnimationEnd(Animator animation) {}
                public void onAnimationCancel(Animator animation) {}
                public void onAnimationRepeat(Animator animation) {}
            });


            view.measure(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT);
            final int targetHeight = view.getMeasuredHeight();

            // Older versions of android (pre API 21) cancel animations for views with a height of 0.
            view.getLayoutParams().height = 1;
            view.setVisibility(View.VISIBLE);
            Animation a = new Animation()
            {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    view.getLayoutParams().height = interpolatedTime == 1
                            ? RecyclerView.LayoutParams.WRAP_CONTENT
                            : (int)(targetHeight * interpolatedTime);
                    view.requestLayout();
                }

                @Override
                public boolean willChangeBounds() {
                    return true;
                }
            };

            // 1dp/ms
            a.setDuration((int)(targetHeight / view.getContext().getResources().getDisplayMetrics().density));
            anim.start();
            view.startAnimation(a);
        } else {
            view.setVisibility(View.VISIBLE);
        }
    }

    private void collapseView(final View view, int width, int height, int radius) {
        if (Build.VERSION.SDK_INT >= 21 && view.isAttachedToWindow()) {
            Animator anim = ViewAnimationUtils.createCircularReveal(
                    view, width, height, radius, 0);
            anim.setDuration(CIRCULAR_REVEAL_DURATION);
            anim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    view.setVisibility(View.GONE);
                }
                public void onAnimationStart(Animator animation) {}
                public void onAnimationCancel(Animator animation) {}
                public void onAnimationRepeat(Animator animation) {}
            });

            final int initialHeight = view.getMeasuredHeight();
            Animation a = new Animation()
            {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    if(interpolatedTime == 1){
                        view.setVisibility(View.GONE);
                    }else{
                        view.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                        view.requestLayout();
                    }
                }

                @Override
                public boolean willChangeBounds() {
                    return true;
                }
            };
            // 1dp/ms
            a.setDuration((int)(initialHeight / view.getContext().getResources().getDisplayMetrics().density));

            anim.start();
            view.startAnimation(a);
        } else {
            view.setVisibility(View.GONE);
        }
    }

    private void onMenuItemClick(View item, int position) {
        View contentView = item.findViewById(R.id.joke_unexpanded);
        final View ExpandedJokeView = item.findViewById(R.id.joke_expanded);
        int height = contentView.getHeight();
        int width = contentView.getWidth();
        if (mPreExpandedView != null) {
            collapseView(mPreExpandedView, width/2, height/2, width);
        }
        if (ExpandedJokeView.getVisibility() == View.GONE) {
            // 显示笑话详情
            expandView(ExpandedJokeView, width, height);
            mPreExpandedView = ExpandedJokeView;
            mCurrExpandedPosition = position;
        } else {
            // 收起详情
            collapseView(ExpandedJokeView, positionX, height/2, width);
            mPreExpandedView = null;
            mCurrExpandedPosition = -1;
        }
    }

    @Override
    public boolean onTouch(View v,MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            positionX = (int)event.getX();
            positionY = (int)event.getY();
        }
        return false;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        ViewGroup container = holder.mLayoutView;

        // 设置 menu item 内容
        TextView title = (TextView) container.findViewById(R.id.joke_title);
        TextView detail = (TextView) container.findViewById(R.id.joke_detail);
        TextView date = (TextView) container.findViewById(R.id.joke_date);
        View expandView = container.findViewById(R.id.joke_expanded);
        View unexpandView = container.findViewById(R.id.joke_unexpanded);
        View sep = container.findViewById(R.id.joke_sep);
        Button share = (Button) container.findViewById(R.id.joke_share);
        final Button joke_favorite = (Button) container.findViewById(R.id.joke_favorite);

        if (mJokeData.get(position) == null) {
            unexpandView.setVisibility(View.GONE);
            expandView.setVisibility(View.GONE);
            sep.setVisibility(View.VISIBLE);
            sep.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        } else {
            unexpandView.setVisibility(View.VISIBLE);
            sep.setVisibility(View.GONE);

            if (position == mCurrExpandedPosition) {
                expandView.setVisibility(View.VISIBLE);
                mPreExpandedView = expandView;
            } else {
                expandView.setVisibility(View.GONE);
            }
            title.setText(mJokeData.get(position).getTitle());
            detail.setText(mJokeData.get(position).getBody());
            date.setText(mJokeData.get(position).getPubDate());

            share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Utils.share(mJokeData.get(position).getBody());
                }
            });

            db = mContext.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
            Cursor tmpCursor = db.rawQuery("SELECT count(*) FROM sqlite_master WHERE type='table' AND name='"+TABLE_NAME+"'", null);
            if(tmpCursor.moveToNext() && tmpCursor.getInt(0) > 0) {
                tmpCursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE title=?",
                        new String[]{mJokeData.get(position).getTitle().replaceAll("\\r\\n", CRLF_REPLACE)});
                if (tmpCursor.getCount() == 0) {
                    joke_favorite.setText(mContext.getText(R.string.favorite_capital));
                    joke_favorite.setTextColor(ContextCompat.getColor(mContext, R.color.gray));
                } else {
                    joke_favorite.setText(mContext.getText(R.string.unfavorite_capital));
                    joke_favorite.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent));
                }
            }
            joke_favorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String mTitle = mJokeData.get(position).getTitle().replaceAll("\\r\\n", CRLF_REPLACE);
                    String mPubDate = mJokeData.get(position).getPubDate();
                    String mBody = mJokeData.get(position).getBody().replaceAll("\\r\\n", CRLF_REPLACE);
                    ContentValues cv = new ContentValues();
                    cv.put("title", mTitle);
                    cv.put("pubDate", mPubDate);
                    cv.put("body", mBody);

                    String sql = "create table if not exists " + TABLE_NAME + " (_id integer primary key," +
                            " title text, pubDate text, body text)";
                    db = mContext.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
                    db.execSQL(sql);
                    if(joke_favorite.getText().equals(mContext.getText(R.string.favorite_capital))) {
                        db.insert(TABLE_NAME, null, cv);
                        joke_favorite.setText(mContext.getText(R.string.unfavorite_capital));
                        joke_favorite.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent));
                    }else {
                        db.delete(TABLE_NAME, "title=?", new String[]{mTitle});
                        joke_favorite.setText(mContext.getText(R.string.favorite_capital));
                        joke_favorite.setTextColor(ContextCompat.getColor(mContext, R.color.gray));
                    }

                    //查询
                    Cursor c = db.rawQuery("SELECT * FROM "+ TABLE_NAME +" WHERE title IS NOT NULL", null);
                    while (c.moveToNext()) {
                        int _id = c.getInt(c.getColumnIndex("_id"));
                        String title = c.getString(c.getColumnIndex("title"));
                        Log.d("leungadd", "_id=>" + _id + ", title=>" + title);
                    }
                    c.close();
                    //查询end
                    db.close();
                    Log.d("leungadd", "db... success");
                }
            });
        }


        // 恢复删除 item 时用到的 tran x
        container.setTranslationX(0);

        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onMenuItemClick(view, position);
            }
        });

        // 保存 holder
        if (position >=0 && position < mHolderList.size()) {
            mHolderList.set(position, holder);
        } else {
            mHolderList.add(position, holder);
        }

        // 获取菜单项高度
        if (mMenuItemHeight <= 0) {
            mMenuItemHeight = container.getHeight();
        }

        // 动画显示
        runUpAnimation(container, position);
    }

    @Override
    public int getItemCount() {
        if (mJokeData != null) {
            return mJokeData.size();
        } else {
            return 0;
        }
    }

    public void deleteItem(int position, Runnable finishCallback) {
        mLastBindPosition = position - 1;
        mDelPosMark = position;
        ViewHolder holder = mHolderList.get(position);
        sweepAnimation(holder.mLayoutView, finishCallback);
    }

    public void refresh() {
        mLastBindPosition = -1;
        mCurrExpandedPosition = -1;
        notifyDataSetChanged();
    }
}
