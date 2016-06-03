package com.yan.haha;

import android.animation.Animator;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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

import java.util.ArrayList;
import java.util.List;

public class JokeFavoriteFragment extends ContentFragment{
    private RecyclerView mJokeList = null;
    private JokeAdapter mAdapter = new JokeAdapter(MainActivity.getInstance());
    private SQLiteDatabase db;
    private Jokes jokesObject;
    public static List<Jokes> jokesArray = new ArrayList<Jokes>();
    private static final String DB_NAME = "jokes.db";
    public static final String TABLE_NAME = "Jokes";
    private static final String CRLF_REPLACE = ".crlf0.0";
    private static final String TAG = "JokeFavoriteFragment";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.joke_favorite, container, false);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.joke_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.joke_favorite_action) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        initViews();
        if (jokesArray.size() == 0) {
            mJokeList.setHasFixedSize(true);
            LinearLayoutManager lm = new LinearLayoutManager(getActivity());
            lm.setOrientation(LinearLayoutManager.VERTICAL);
            mJokeList.setLayoutManager(lm);
            mJokeList.setAdapter(mAdapter);

            getJokeFromDb();
        }
    }

    private void getJokeFromDb() {
        db = MainActivity.getInstance().openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
        //查询数据库
        Cursor c = db.rawQuery("SELECT * FROM "+ TABLE_NAME +" WHERE title IS NOT NULL", null);
        while (c.moveToNext()) {
            int _id = c.getInt(c.getColumnIndex("_id"));
            String title = c.getString(c.getColumnIndex("title")).replaceAll(CRLF_REPLACE,"\r\n");
            String pubDate = c.getString(c.getColumnIndex("pubDate"));
            String body = c.getString(c.getColumnIndex("body")).replaceAll(CRLF_REPLACE, "\r\n");
            jokesObject = new Jokes(title, pubDate, body);
            jokesArray.add(jokesObject);
            Log.d(TAG, "_id=>" + _id + ", title=>" + title);
        }
        c.close();
        mAdapter.setJokeList((ArrayList<Jokes>)jokesArray);

        //查询end
    }
    private void initViews() {
        mJokeList = (RecyclerView) getActivity().findViewById(R.id.joke_favorite_list);
    }

}
