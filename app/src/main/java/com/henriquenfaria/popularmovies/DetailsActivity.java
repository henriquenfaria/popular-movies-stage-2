package com.henriquenfaria.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

// Activity that hosts DetailsFragment
public class DetailsActivity extends AppCompatActivity {

    private static final String LOG_TAG = DetailsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        if (savedInstanceState == null) {
            Intent intent = getIntent();
            if (intent != null && intent.hasExtra(Constants.EXTRA_MOVIE)) {
                DetailsFragment detailsFragment = DetailsFragment.newInstance((Movie) intent
                        .getParcelableExtra(Constants.EXTRA_MOVIE));
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.details_fragment_container, detailsFragment)
                        .commit();
            } else {
                Log.d("LOG_TAG", "Something went wrong. Intent doesn't have Constants.EXTRA_MOVIE extra. Finishing DetailsActivity.");
                finish();
            }
        }
    }
}
