package com.henriquenfaria.popularmovies;

// Class containing several Constants used by app
public class Constants {
    // GENERAL
    public static final int PORTRAIT_COLUMN_COUNT = 2;
    public static final int LANDSCAPE_COLUMN_COUNT = 3;
    public static final String EXTRA_MOVIE = "intent_extra_movie";
    public static final String SAVE_LAST_UPDATE_ORDER = "save_last_update_order";

    // THE MOVIE DB API
    public static final String API_POPULAR_MOVIES_BASE_URL = "https://api.themoviedb.org/3/movie/popular?";
    public static final String API_TOP_RATED_MOVIES_BASE_URL = "https://api.themoviedb.org/3/movie/top_rated?";
    public static final String API_POSTER_MOVIES_BASE_URL = "http://image.tmdb.org/t/p/";
    public static final String API_POSTER_SIZE = "w185/";
    public static final String API_KEY_PARAM = "api_key";
    public static final String API_LANGUAGE_PARAM = "language";
    public static final String API_PORTUGUESE_LANGUAGE = "pt-BR";

    // THE MOVIE DB JSON
    public static final String JSON_LIST = "results";
    public static final String JSON_ID = "id";
    public static final String JSON_TITLE = "title";
    public static final String JSON_RELEASE_DATE = "release_date";
    public static final String JSON_VOTE_AVERAGE = "vote_average";
    public static final String JSON_OVERVIEW = "overview";
    public static final String JSON_POSTER_PATH = "poster_path";

}
