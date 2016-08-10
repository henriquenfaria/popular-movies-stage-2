package com.henriquenfaria.popularmovies;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

// Fragment displayed when internet connection is not available. It contains a text and a retry
// button.
public class NoInternetFragment extends Fragment {

    private static final String LOG_TAG = NoInternetFragment.class.getSimpleName();

    private OnRetryInteractionListener mListener;

    public NoInternetFragment() {
        // Required empty public constructor
    }

    public static NoInternetFragment newInstance() {
        NoInternetFragment fragment = new NoInternetFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_no_internet, container, false);
        Button retryButton = (Button) view.findViewById(R.id.retry_button);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRetryButtonPressed();
            }
        });
        return view;
    }

    // Calls listener implemented by host Activity
    public void onRetryButtonPressed() {
        if (mListener != null) {
            mListener.onRetryInteraction();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnRetryInteractionListener) {
            mListener = (OnRetryInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnRetryInteractionListener {
        void onRetryInteraction();
    }
}
