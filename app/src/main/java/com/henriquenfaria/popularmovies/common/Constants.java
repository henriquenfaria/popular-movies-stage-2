package com.henriquenfaria.popularmovies.common;

// Class containing several Constants used by app
public class Constants {
    // GENERAL
    public static final String EXTRA_MOVIE = "intent_extra_movie";
    public static final String SAVE_LAST_UPDATE_ORDER = "save_last_update_order";
    public static final String ACTION_MOVIES_REQUEST =
            "com.henriquenfaria.popularmovies.ACTION_MOVIES_REQUEST";
    public static final String ACTION_EXTRA_INFO_REQUEST =
            "com.henriquenfaria.popularmovies.ACTION_EXTRA_INFO_REQUEST";
    public static final String ACTION_MOVIES_RESULT =
            "com.henriquenfaria.popularmovies.ACTION_MOVIES_RESULT";
    public static final String ACTION_EXTRA_INFO_RESULT =
            "com.henriquenfaria.popularmovies.ACTION_EXTRA_INFO_RESULT";
    public static final String YOUTUBE_BASE_URL = "http://www.youtube.com/watch?v=";

    // THE MOVIE DB API
    public static final String API_POPULAR_MOVIES_BASE_URL = "https://api.themoviedb" +
            ".org/3/movie/popular?";
    public static final String API_TOP_RATED_MOVIES_BASE_URL = "https://api.themoviedb" +
            ".org/3/movie/top_rated?";
    public static final String API_VIDEOS_REVIEWS_BASE_URL = "https://api.themoviedb.org/3/movie";
    public static final String API_VIDEOS_PATH = "videos";
    public static final String API_REVIEWS_BASE_URL = "https://api.themoviedb.org/3/movie";
    public static final String API_REVIEWS_PATH = "reviews";
    public static final String API_POSTER_MOVIES_BASE_URL = "http://image.tmdb.org/t/p/";
    public static final String API_POSTER_SIZE = "w185/";
    public static final String API_KEY_PARAM = "api_key";
    public static final String API_LANGUAGE_PARAM = "language";
    public static final String API_PORTUGUESE_LANGUAGE = "pt-BR";

    // THE MOVIE DB JSON
    // Movie info
    public static final String JSON_MOVIE_LIST = "results";
    public static final String JSON_MOVIE_ID = "id";
    public static final String JSON_MOVIE_TITLE = "title";
    public static final String JSON_MOVIE_RELEASE_DATE = "release_date";
    public static final String JSON_MOVIE_VOTE_AVERAGE = "vote_average";
    public static final String JSON_MOVIE_OVERVIEW = "overview";
    public static final String JSON_MOVIE_POSTER_PATH = "poster_path";

    // Reviews info
    public static final String JSON_REVIEW_LIST = "results";
    public static final String JSON_REVIEW_ID = "id";
    public static final String JSON_REVIEW_AUTHOR = "author";
    public static final String JSON_REVIEW_CONTENT = "content";

    // Video info
    public static final String JSON_VIDEOS_LIST = "results";
    public static final String JSON_VIDEO_ID = "id";
    public static final String JSON_VIDEO_KEY = "key";
    public static final String JSON_VIDEO_NAME = "name";
}
