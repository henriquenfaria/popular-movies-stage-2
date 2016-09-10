package com.henriquenfaria.popularmovies.ui;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.henriquenfaria.popularmovies.R;
import com.henriquenfaria.popularmovies.data.MoviesContract;
import com.henriquenfaria.popularmovies.model.Movie;

// Fragment that displays detailed info about selected movie
public class DetailsFragment extends Fragment {

    private static final String ARG_MOVIE = "arg_movie";
    private static final String SAVE_MOVIE = "save_movie";
    private static final String SAVE_FAVORITE_MOVIE = "save_favorit_movie";
    private static final String SAVE_FAVORITE_SORT = "save_favorite_sort";
    private static final String LOG_TAG = DetailsFragment.class.getSimpleName();
    private Movie mMovie;
    private boolean mIsFavoriteMovie;
    private boolean mIsFavoriteSort;

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
            mIsFavoriteMovie = isFavoriteMovie(getActivity(), mMovie);
            mIsFavoriteSort = isFavoriteMovie(getActivity(), mMovie);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SAVE_MOVIE, mMovie);
        outState.putBoolean(SAVE_FAVORITE_MOVIE, mIsFavoriteMovie);
        outState.putBoolean(SAVE_FAVORITE_SORT, mIsFavoriteSort);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Restore objects value
        if (savedInstanceState != null) {
            mMovie = savedInstanceState.getParcelable(SAVE_MOVIE);
            mIsFavoriteMovie = savedInstanceState.getBoolean(SAVE_FAVORITE_MOVIE);
            mIsFavoriteSort = savedInstanceState.getBoolean(SAVE_FAVORITE_SORT);
        }

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

            ImageButton startButton = (ImageButton) view.findViewById(R.id.star_button);
            startButton.setOnClickListener(starButtonOnClickListener);
            if (mIsFavoriteMovie) {
                startButton.setImageResource(R.drawable.ic_star);
            } else {
                startButton.setImageResource(R.drawable.ic_star_border);
            }
            startButton.setVisibility(View.VISIBLE);

            FrameLayout detailFrame = (FrameLayout) view.findViewById(R.id.detail_frame);
            detailFrame.setVisibility(View.VISIBLE);
        }

        return view;
    }

    private View.OnClickListener starButtonOnClickListener = new View.OnClickListener() {
        public void onClick(View view) {
            // TODO: Move placeholder string to strings.xml
            if (mIsFavoriteMovie) {
                if (removeFavoriteMovie(mMovie) > 0) {
                    Toast.makeText(getActivity(), "Successfully removed from Favorites", Toast
                            .LENGTH_LONG)
                            .show();
                    ((ImageButton) view).setImageResource(R.drawable.ic_star_border);
                    mIsFavoriteMovie = false;
                } else {
                    Toast.makeText(getActivity(), "Fail to remove from Favorites",
                            Toast.LENGTH_LONG)
                            .show();
                }

            } else {
                Uri returnUri = addFavoriteMovie(mMovie);
                if (returnUri != null) {
                    Toast.makeText(getActivity(), "Successfully added to Favorites", Toast
                            .LENGTH_LONG)
                            .show();
                    ((ImageButton) view).setImageResource(R.drawable.ic_star);
                    mIsFavoriteMovie = true;
                } else {
                    Toast.makeText(getActivity(), "Fail to add to Favorites", Toast
                            .LENGTH_LONG).show();
                }
            }
        }
    };

    private Uri addFavoriteMovie(Movie movie) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(MoviesContract.MovieEntry._ID, Integer.parseInt(mMovie.getId()));
        contentValues.put(MoviesContract.MovieEntry.COLUMN_OVERVIEW, mMovie.getOverview());
        contentValues.put(MoviesContract.MovieEntry.COLUMN_POSTER, mMovie.getPosterUri().toString
                ());
        contentValues.put(MoviesContract.MovieEntry.COLUMN_RELEASE_DATE, mMovie.getReleaseDate());
        contentValues.put(MoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE, mMovie.getVoteAverage());
        contentValues.put(MoviesContract.MovieEntry.COLUMN_TITLE, mMovie.getTitle());
        return getActivity().getContentResolver().insert(MoviesContract.MovieEntry.CONTENT_URI,
                contentValues);


    }

    private int removeFavoriteMovie(Movie movie) {
        return getActivity().getContentResolver().delete(MoviesContract.MovieEntry.CONTENT_URI,
                MoviesContract.MovieEntry._ID + " = ?", new String[]{movie.getId()});
    }

    private boolean isFavorteSort(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        String sortOrder = prefs.getString(getString(R.string.pref_sort_order_key),
                getString(R.string.pref_popular_value));

        if (sortOrder.equals(getString(R.string.pref_favorite_value))) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isFavoriteMovie(Context ctx, Movie movie) {

        int movieID = Integer.parseInt(movie.getId());
        Cursor cursor = ctx.getContentResolver().query(MoviesContract.MovieEntry
                        .CONTENT_URI, null,
                MoviesContract.MovieEntry._ID + " = " + movieID, null, null);
        if (cursor != null && cursor.moveToNext()) {
            // TODO: Index is hardcoded. Fix it
            int movieIdColumnIndex = cursor.getColumnIndex(MoviesContract.MovieEntry._ID);
            if (movieID == cursor.getInt(movieIdColumnIndex)) {
                return true;
            }

        }
        return false;
    }
}
