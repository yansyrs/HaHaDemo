package com.yan.haha;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yan.haha.adapter.BrainRiddleAdapter;
import com.yan.haha.units.BrainRiddle;
import com.yan.haha.utils.GetBrainRiddle;

import java.util.ArrayList;

public class BrainRiddleFragment extends ContentFragment implements OnDataFinishedListener {
    private RecyclerView mBrainRiddleList = null;
    private BrainRiddleAdapter mAdapter = new BrainRiddleAdapter();
    private ArrayList<BrainRiddle> mRiddleData = new ArrayList<BrainRiddle>();

    private final static int RANDOM_COUNT = 10;
    private GetBrainRiddle mBrainRiddleRequest = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.brain_riddle_fragment, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        initViews();
        if (mRiddleData.size() == 0) {
            mBrainRiddleList.setHasFixedSize(true);

            LinearLayoutManager lm = new LinearLayoutManager(getActivity());
            lm.setOrientation(LinearLayoutManager.VERTICAL);
            mBrainRiddleList.setLayoutManager(lm);
            mBrainRiddleList.setAdapter(mAdapter);

            mBrainRiddleRequest.execute();
        }
    }

    private GetBrainRiddle getBrainRiddleProcess() {
        GetBrainRiddle process = new GetBrainRiddle(RANDOM_COUNT);
        process.setOnDataFinishedListener(this);
        return process;
    }

    private void initViews() {
        mBrainRiddleList = (RecyclerView) getActivity().findViewById(R.id.brain_riddle_list);
        mBrainRiddleRequest = getBrainRiddleProcess();

        FloatingActionButton fab = (FloatingActionButton) getActivity()
                                        .findViewById(R.id.brain_riddle_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBrainRiddleRequest = getBrainRiddleProcess();
                mBrainRiddleRequest.execute();
            }
        });
    }

    @Override
    public void onDataSuccessfully(Object data) {
        ArrayList<BrainRiddle> result = (ArrayList<BrainRiddle>) data;
        mRiddleData.addAll(result);
        mAdapter.setBrainDataList(mRiddleData);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDataFailed() {
    }
}
