package com.yan.haha;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yan.haha.adapter.JokeAdapter;
import com.yan.haha.units.Jokes;
import com.yan.haha.utils.GetJoke;

import java.util.ArrayList;

public class JokeFragment extends ContentFragment implements OnDataFinishedListener {
    private RecyclerView mJokeList = null;
    private JokeAdapter mAdapter = new JokeAdapter();
    private ArrayList<Jokes> mJokeData = new ArrayList<Jokes>();

    private static int REQUEST_PAGE = 1;
    private GetJoke mJokeRequest = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.joke_fragment, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        initViews();
        if (mJokeData.size() == 0) {
            mJokeList.setHasFixedSize(true);

            LinearLayoutManager lm = new LinearLayoutManager(getActivity());
            lm.setOrientation(LinearLayoutManager.VERTICAL);
            mJokeList.setLayoutManager(lm);
            mJokeList.setAdapter(mAdapter);

            mJokeRequest.execute();
        }
    }

    private GetJoke getJokeProcess() {
        GetJoke process = new GetJoke(REQUEST_PAGE);
        process.setOnDataFinishedListener(this);
        return process;
    }

    private void initViews() {
        mJokeList = (RecyclerView) getActivity().findViewById(R.id.joke_list);
        mJokeRequest = getJokeProcess();

        FloatingActionButton fab = (FloatingActionButton) getActivity()
                                        .findViewById(R.id.joke_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                REQUEST_PAGE += 1;
                mJokeRequest = getJokeProcess();
                mJokeRequest.execute();
            }
        });
    }

    @Override
    public void onDataSuccessfully(Object data) {
        ArrayList<Jokes> result = (ArrayList<Jokes>) data;
        mJokeData.clear();
        mJokeData.addAll(result);
        mAdapter.setJokeList(mJokeData);
        mAdapter.notifyDataSetChanged();
        mJokeList.scrollToPosition(0);
    }

    @Override
    public void onDataFailed() {

    }
}
