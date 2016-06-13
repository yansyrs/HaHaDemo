package com.yan.haha.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.yan.haha.R;
import com.yan.haha.units.Jokes;

import java.util.ArrayList;

/**
 * Created by Leung on 2016/6/6.
 */
public class JokeFavoriteAdapter extends JokeAdapter {

    private static final String TAG = "JokeFavoriteAdapter";

    public JokeFavoriteAdapter(Context context) {
        mContext = context;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        super.onBindViewHolder(holder, position);
        ViewGroup container = holder.mLayoutView;

        // 设置 menu item 内容
        final TextView title = (TextView) container.findViewById(R.id.joke_title);
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
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, mJokeData.get(position).getBody());
                    sendIntent.setType("text/plain");
                    mContext.startActivity(sendIntent);
                }
            });

            db = mContext.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
            Cursor tmpCursor = db.rawQuery("SELECT count(*) FROM sqlite_master WHERE type='table' AND name='" + TABLE_NAME + "'", null);
            if (tmpCursor.moveToNext() && tmpCursor.getInt(0) > 0) {
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
            tmpCursor.close();
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
                    if (joke_favorite.getText().equals(mContext.getText(R.string.favorite_capital))) {
                        db.insert(TABLE_NAME, null, cv);
                        joke_favorite.setText(mContext.getText(R.string.unfavorite_capital));
                        joke_favorite.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent));
                    } else {
                        db.delete(TABLE_NAME, "title=?", new String[]{mTitle});
                        deleteItem(position, null);
                    }

                    //查询
                    Cursor c = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE title IS NOT NULL", null);
                    while (c.moveToNext()) {
                        int _id = c.getInt(c.getColumnIndex("_id"));
                        String title = c.getString(c.getColumnIndex("title"));
                        Log.d(TAG, "_id=>" + _id + ", title=>" + title);
                    }
                    c.close();
                    //查询end
                    db.close();
                    Log.d(TAG, "db... success");
                }
            });
        }
    }


}
