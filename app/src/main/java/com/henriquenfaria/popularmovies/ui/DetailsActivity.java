package com.henriquenfaria.popularmovies.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.henriquenfaria.popularmovies.R;
import com.henriquenfaria.popularmovies.common.Constants;
import com.henriquenfaria.popularmovies.model.Movie;

// Activity that hosts DetailsFragment
public class DetailsActivity extends AppCompatActivity implements DetailsFragment
        .OnLoadingInteractionListener {

    private static final String LOG_TAG = DetailsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        if (savedInstanceState == null) {
            Intent intent = getIntent();
            if (intent != null && intent.hasExtra(Constants.EXTRA_MOVIE)) {
                Movie movie = intent
                        .getParcelableExtra(Constants.EXTRA_MOVIE);

                DetailsFragment detailsFragment = DetailsFragment.newInstance(movie);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.details_fragment_container, detailsFragment).commit();
            } else {
                Log.d("LOG_TAG", "Something went wrong. Intent doesn't have Constants.EXTRA_MOVIE" +
                        " extra. Finishing DetailsActivity.");
                finish();
            }
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
