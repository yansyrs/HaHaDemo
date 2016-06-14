package com.yan.haha;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class InitFragment extends ContentFragment{
    private static final String TAG = "InitFragment";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.init_fragment, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
    }


}
