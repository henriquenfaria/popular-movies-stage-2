package com.henriquenfaria.popularmovies.ui;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.henriquenfaria.popularmovies.R;
import com.henriquenfaria.popularmovies.common.Constants;
import com.henriquenfaria.popularmovies.common.Utils;
import com.henriquenfaria.popularmovies.data.FavoriteMoviesContract;
import com.henriquenfaria.popularmovies.listener.OnMoviesListFragmentListener;
import com.henriquenfaria.popularmovies.listener.OnNoInternetFragmentListener;
import com.henriquenfaria.popularmovies.listener.OnLoadingFragmentListener;
import com.henriquenfaria.popularmovies.model.Movie;

// Class that can host MoviesListFragment, NoInternetFragment and LoadingFragment
public class MoviesActivity extends AppCompatActivity implements
        OnMoviesListFragmentListener,
        OnLoadingFragmentListener,
        OnNoInternetFragmentListener {

    private static final String LOG_TAG = MoviesActivity.class.getSimpleName();
    private boolean mIsTwoPane;
    private View mMoviesFragmentContainer;
    private View mDetailsFragmentContainer;
    private View mNoInternetConnectionFragmentContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movies);

        mMoviesFragmentContainer = findViewById(R.id.movies_fragment_container);
        mDetailsFragmentContainer = findViewById(R.id.details_fragment_container);
        mNoInternetConnectionFragmentContainer = findViewById(R.id.no_internet_container);

        mIsTwoPane = mDetailsFragmentContainer != null;

        if (savedInstanceState == null) {

            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            MoviesListFragment moviesListFragment = MoviesListFragment.newInstance();
            NoInternetFragment noInternetFragment = NoInternetFragment.newInstance();
            fragmentTransaction.add(R.id.movies_fragment_container, moviesListFragment)
                    .add(R.id.no_internet_container, noInternetFragment);

            if (mIsTwoPane) {
                DetailsFragment detailFragment = DetailsFragment.newInstance();
                fragmentTransaction.add(R.id.details_fragment_container, detailFragment);
            }

            fragmentTransaction.commit();
        }
    }

    // Change visibility of fragment according to current internet connection state
    private void changeNoInternetVisibility(boolean isInternetConnected) {

        if (isInternetConnected || Utils.isFavoriteSort(this)) {
            mNoInternetConnectionFragmentContainer.setVisibility(View.GONE);
            mMoviesFragmentContainer.setVisibility(View.VISIBLE);

            if (mIsTwoPane) {
                mDetailsFragmentContainer.setVisibility(View.VISIBLE);
            }
        } else {
            mNoInternetConnectionFragmentContainer.setVisibility(View.VISIBLE);
            mMoviesFragmentContainer.setVisibility(View.GONE);

            if (mIsTwoPane) {
                mDetailsFragmentContainer.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onRetry() {
        boolean isInternetConnected = Utils.isInternetConnected(this);

        changeNoInternetVisibility(Utils.isInternetConnected(this));

        if (!isInternetConnected) {
            Toast.makeText(this, R.string.toast_no_internet_connection, Toast.LENGTH_SHORT).show();

        } else {
            MoviesListFragment moviesListFragment = (MoviesListFragment)
                    getSupportFragmentManager().findFragmentById(R.id.movies_fragment_container);
            moviesListFragment.updateMoviesList();
        }
    }

    @Override
    public void onMoviesSelected(Movie movieItem) {
        if (movieItem != null) {
            if (!Utils.isInternetConnected(this)) {
                Toast.makeText(this, R.string.toast_check_your_internet_connection,
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (mIsTwoPane) {
                // In two-pane mode, show the detail view in this activity by
                // adding or replacing the detail fragment using a
                // fragment transaction.
                DetailsFragment detailsFragment = DetailsFragment.newInstance(movieItem);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.details_fragment_container, detailsFragment).commit();
            } else {
                Intent intent = new Intent(this, DetailsActivity.class).putExtra(Constants
                                .EXTRA_MOVIE,
                        movieItem);
                startActivity(intent);
            }
        }
    }

    @Override
    public void onFavoriteMovieSelected(Movie movieItem) {
        if (movieItem != null) {
            int movieID = Integer.parseInt(movieItem.getId());

            // Videos query
            Cursor videosCursor = getContentResolver().query(FavoriteMoviesContract.VideosEntry
                            .CONTENT_URI, null,
                    FavoriteMoviesContract.VideosEntry.COLUMN_MOVIE_ID + " = " + movieID, null,
                    null);
            if (videosCursor != null) {
                try {
                    movieItem.setVideos(Utils.createVideosFromCursor(videosCursor));
                } finally {
                    if (videosCursor != null) {
                        videosCursor.close();
                    }
                }
            }

            // Reviews query
            Cursor reviewsCursor = getContentResolver().query(FavoriteMoviesContract.ReviewsEntry
                            .CONTENT_URI, null,
                    FavoriteMoviesContract.ReviewsEntry.COLUMN_MOVIE_ID + " = " + movieID, null,
                    null);
            if (reviewsCursor != null) {
                try {
                    movieItem.setReviews(Utils.createReviewsFromCursor(reviewsCursor));
                } finally {
                    if (reviewsCursor != null) {
                        reviewsCursor.close();
                    }
                }
            }

            if (mIsTwoPane) {
                // In two-pane mode, show the detail view in this activity by
                // adding or replacing the detail fragment using a
                // fragment transaction.
                DetailsFragment detailsFragment = DetailsFragment.newInstance(movieItem);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.details_fragment_container, detailsFragment).commit();
            } else {
                Intent intent = new Intent(this, DetailsActivity.class).putExtra(Constants
                        .EXTRA_MOVIE, movieItem);
                startActivity(intent);
            }
        }
    }

    @Override
    public void onUpdateMoviesListVisibility() {
        changeNoInternetVisibility(Utils.isInternetConnected(this));
    }

    @Override
    public void onUpdateMovieDetails() {
        if (mIsTwoPane) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            DetailsFragment detailFragment = DetailsFragment.newInstance();
            fragmentTransaction.replace(R.id.details_fragment_container, detailFragment);
            fragmentTransaction.commit();
        }
    }

    @Override
    public void onLoadingDisplay(boolean fromDetails, boolean display) {
        Fragment loadingFragment = getSupportFragmentManager()
                .findFragmentByTag(LoadingFragment.FRAGMENT_TAG);
        if (display && loadingFragment == null) {
            loadingFragment = LoadingFragment.newInstance();
            if (fromDetails) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.details_fragment_container,
                                loadingFragment, LoadingFragment.FRAGMENT_TAG).commit();
            } else {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.movies_fragment_container,
                                loadingFragment, LoadingFragment.FRAGMENT_TAG).commit();
            }
        } else if (!display && loadingFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .remove(loadingFragment).commit();
        }
    }
}
