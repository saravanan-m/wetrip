package com.wetrip.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wetrip.R;

/**
 * Created by anupamchugh on 10/12/15.
 */
public class HelpFragment extends Fragment {

    public HelpFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_table, container, false);

        return rootView;
    }

}
