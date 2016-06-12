package com.yan.haha;

import android.animation.Animator;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
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
import com.yan.haha.adapter.JokeFavoriteAdapter;
import com.yan.haha.units.Jokes;
import com.yan.haha.utils.GetJoke;

import java.util.ArrayList;
import java.util.List;

public class JokeFavoriteFragment extends ContentFragment{
    private RecyclerView mJokeList = null;
    private JokeFavoriteAdapter mAdapter = new JokeFavoriteAdapter(MainActivity.getInstance());
    private SQLiteDatabase db;
    private Jokes jokesObject;
    public static List<Jokes> jokesArray = new ArrayList<Jokes>();
    private static final String DB_NAME = "jokes.db";
    public static final String TABLE_NAME = "Jokes";
    private static final String CRLF_REPLACE = ".crlf0.0";
    private static final String TAG = "JokeFavoriteFragment";
    private ActionBarDrawerToggle toggle;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        toggle = MainActivity.getInstance().toggle;
        toggle.setDrawerIndicatorEnabled(false);
        toggle.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);
        toggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.getInstance().onBackPressed();
                toggle.setDrawerIndicatorEnabled(true);
            }
        });
        return inflater.inflate(R.layout.joke_favorite, container, false);
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
        getJokeFromDb();
        Log.d(TAG,"jokeFavoriteFragment onStart");
    }

    private void getJokeFromDb() {
        jokesArray.clear();
        db = MainActivity.getInstance().openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
        //查询数据库
        Cursor c = db.rawQuery("SELECT count(*) FROM sqlite_master WHERE type='table' AND name='"+TABLE_NAME+"'", null);
        if(c.moveToNext() && c.getInt(0) > 0) {
            c = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE title IS NOT NULL", null);
            while (c.moveToNext()) {
                int _id = c.getInt(c.getColumnIndex("_id"));
                String title = c.getString(c.getColumnIndex("title")).replaceAll(CRLF_REPLACE, "\r\n");
                String pubDate = c.getString(c.getColumnIndex("pubDate"));
                String body = c.getString(c.getColumnIndex("body")).replaceAll(CRLF_REPLACE, "\r\n");
                jokesObject = new Jokes(title, pubDate, body);
                jokesArray.add(jokesObject);
                Log.d(TAG, "_id=>" + _id + ", title=>" + title);
            }
        }
        c.close();
        mAdapter.setJokeList((ArrayList<Jokes>) jokesArray);

        //查询end
    }

    private void initViews() {
        mJokeList = (RecyclerView) getActivity().findViewById(R.id.joke_favorite_list);
    }

}
