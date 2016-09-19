package com.henriquenfaria.popularmovies.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.henriquenfaria.popularmovies.R;
import com.henriquenfaria.popularmovies.common.Constants;
import com.henriquenfaria.popularmovies.common.Utils;
import com.henriquenfaria.popularmovies.model.Movie;

// Class that can host MoviesListFragment or NoInternetFragment
public class MoviesActivity extends AppCompatActivity implements MoviesListFragment
        .OnMoviesListInteractionListener, MoviesListFragment.OnLoadingInteractionListener,
        MoviesListFragment.OnFavoriteMoviesListInteractionListener, NoInternetFragment.OnRetryInteractionListener {

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

        if (mDetailsFragmentContainer != null) {
            mIsTwoPane = true;
        } else {
            mIsTwoPane = false;
        }

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
    public void changeNoInternetVisibility(boolean isInternetConnected) {
        String currentSortOrder = Utils.getSortPref(this);

        if (isInternetConnected || Utils.isFavoriteSort(this, currentSortOrder)) {
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
    public void onMoviesListInteraction(Movie movieItem) {
        if (mIsTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.

            DetailsFragment detailsFragment = DetailsFragment.newInstance(movieItem);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.details_fragment_container, detailsFragment).commit();
        } else {

            Intent intent = new Intent(this, DetailsActivity.class).putExtra(Constants.EXTRA_MOVIE,
                    movieItem);
            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.movies_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //changeNoInternetVisibility(Utils.isInternetConnected(this));
    }

    // Method called after pressing RETRY button. It checks Internet connection again.
    @Override
    public void onRetryInteraction() {
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
    public void onFavoriteMoviesListInteraction(Movie movieItem) {
        if (mIsTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.

            DetailsFragment detailsFragment = DetailsFragment.newInstance(movieItem);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.details_fragment_container, detailsFragment).commit();
        } else {

            Intent intent = new Intent(this, DetailsActivity.class).putExtra(Constants.EXTRA_MOVIE,
                    movieItem);
            startActivity(intent);
        }
    }

    @Override
    public void onLoadingInteraction(boolean display) {
        Fragment loadingFragment = getSupportFragmentManager()
                .findFragmentById(R.id.loading_fragment_container);
        if (display && loadingFragment == null) {
            loadingFragment = LoadingFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.loading_fragment_container, loadingFragment).commit();
        } else if (!display && loadingFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .remove(loadingFragment).commit();
        }
    }
}
