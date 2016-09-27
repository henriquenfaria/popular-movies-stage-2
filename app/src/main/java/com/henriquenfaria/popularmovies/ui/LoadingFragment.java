package com.henriquenfaria.popularmovies.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.henriquenfaria.popularmovies.R;

// Fragment that displays a custom loading spinner.
// Background is transparent so it can be used over another Fragment.
public class LoadingFragment extends Fragment {

    public static final String FRAGMENT_TAG = LoadingFragment.class.getSimpleName();
    private static final String LOG_TAG = LoadingFragment.class.getSimpleName();

    public LoadingFragment() {
        // Required empty public constructor
    }

    public static LoadingFragment newInstance() {
        LoadingFragment fragment = new LoadingFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_loading, container, false);
        return view;
    }
}
