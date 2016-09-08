package com.henriquenfaria.popularmovies.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.henriquenfaria.popularmovies.R;
import com.henriquenfaria.popularmovies.model.Movie;

// Fragment that displays detailed info about selected movie
public class DetailsFragment extends Fragment {

    private static final String ARG_MOVIE = "arg_movie";
    private static final String LOG_TAG = DetailsFragment.class.getSimpleName();
    private Movie mMovie;

    public DetailsFragment() {
        // Required empty public constructor
    }

    // Create new Fragment instance
    public static DetailsFragment newInstance(Movie movieSelected) {
        DetailsFragment fragment = new DetailsFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_MOVIE, movieSelected);
        fragment.setArguments(args);
        return fragment;
    }

    // TODO: No args method just for testing
    public static DetailsFragment newInstance() {
        DetailsFragment fragment = new DetailsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mMovie = getArguments().getParcelable(ARG_MOVIE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_details, container, false);

        if (mMovie != null) {


            ImageView posterView = (ImageView) view.findViewById(R.id.poster);
            Glide.with(getActivity()).load(mMovie.getPosterUri())
                    .diskCacheStrategy(DiskCacheStrategy.ALL).dontAnimate().into(posterView);

            TextView titleView = (TextView) view.findViewById(R.id.title_content);
            titleView.setText(mMovie.getTitle());

            TextView releaseDateView = (TextView) view.findViewById(R.id.release_date_content);
            releaseDateView.setText(mMovie.getReleaseDate());

            TextView averageView = (TextView) view.findViewById(R.id.vote_average_content);
            averageView.setText(mMovie.getVoteAverage());

            TextView overviewView = (TextView) view.findViewById(R.id.overview_content);

            // In portuguese, some movies does not contain overview data. In that case, displays
            // default text: @string/overview_not_available
            if (!TextUtils.isEmpty(mMovie.getOverview())) {
                overviewView.setText(mMovie.getOverview());
            }

            FrameLayout detailFrame = (FrameLayout) view.findViewById(R.id.detail_frame);
            detailFrame.setVisibility(View.VISIBLE);
        }

        return view;
    }
}
