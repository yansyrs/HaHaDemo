package com.yan.haha;

import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yan.haha.adapter.BrainRiddleAdapter;
import com.yan.haha.units.BrainRiddle;
import com.yan.haha.utils.RiddleDb;

import java.util.ArrayList;

public class BrainRiddleFavoriteFragment extends ContentFragment {

    private boolean mFirstRun = true;
    private BrainRiddleAdapter mAdapter = null;

    private ArrayList<BrainRiddle> mRiddleList = null;
    private RecyclerView mBrainRiddleList = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final ActionBarDrawerToggle toggle = MainActivity.getInstance().toggle;
        toggle.setDrawerIndicatorEnabled(false);
        toggle.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);
        toggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.getInstance().onBackPressed();
                toggle.setDrawerIndicatorEnabled(true);
            }
        });
        return inflater.inflate(R.layout.brain_riddle_favorite_fragment, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mFirstRun) {
            RiddleDb db = RiddleDb.getInstance();
            if (db != null) {
                db.openDatabase();
                mRiddleList = db.getSavedRiddles();
                db.closeDatabase();
                if (mRiddleList != null && mRiddleList.size() > 0) {
                    mAdapter = new BrainRiddleAdapter(getActivity(), mRiddleList, true);
                    initView();
                    mBrainRiddleList.setHasFixedSize(true);

                    LinearLayoutManager lm = new LinearLayoutManager(getActivity());
                    lm.setOrientation(LinearLayoutManager.VERTICAL);
                    mBrainRiddleList.setLayoutManager(lm);
                    mBrainRiddleList.setAdapter(mAdapter);
                }
            }
        }
    }

    private void initView() {
        mBrainRiddleList = (RecyclerView) getActivity().findViewById(R.id.brain_riddle_favorite_list);
    }
}
