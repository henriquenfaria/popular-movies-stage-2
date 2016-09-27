package com.henriquenfaria.popularmovies.listener;


import com.henriquenfaria.popularmovies.model.Movie;

public interface OnMoviesListFragmentListener {
    void onMoviesSelected(Movie movie);
    void onFavoriteMovieSelected(Movie movie);
    void onUpdateMoviesListVisibility();
    void onUpdateMovieDetails();
}

