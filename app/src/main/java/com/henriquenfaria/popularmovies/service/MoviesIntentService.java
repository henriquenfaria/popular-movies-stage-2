package com.henriquenfaria.popularmovies.service;


import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.henriquenfaria.popularmovies.BuildConfig;
import com.henriquenfaria.popularmovies.R;
import com.henriquenfaria.popularmovies.common.Constants;
import com.henriquenfaria.popularmovies.model.Movie;
import com.henriquenfaria.popularmovies.model.Review;
import com.henriquenfaria.popularmovies.model.Video;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class MoviesIntentService extends IntentService {

    private static final String LOG_TAG = MoviesIntentService.class.getSimpleName();

    public static final String EXTRA_MOVIES_RESULT = "extra_movies_request";
    public static final String EXTRA_INFO_VIDEOS_RESULT = "extra_info_videos_request";
    public static final String EXTRA_INFO_REVIEWS_RESULT = "extra_info_reviews_request";

    public static final String EXTRA_MOVIES_SORT = "extra_movies_sort";
    public static final String EXTRA_INFO_MOVIE_ID = "extra_info_movie_id";

    public MoviesIntentService() {
        super("MoviesIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {


        if (intent == null || intent.getAction() == null) {
            return;
        }

        // TODO: Remove me. This is only for debugging!
        /*try {
            Thread.sleep(11000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/

        if (intent.getAction().equals(Constants.ACTION_MOVIES_REQUEST) && intent.hasExtra
                (EXTRA_MOVIES_SORT)) {
            String sort = intent.getStringExtra(EXTRA_MOVIES_SORT);
            if (!TextUtils.isEmpty(sort)) {
                Movie[] movies = null;
                try {
                    Uri moviesUri = createMoviesUri(sort);
                    String moviesJsonStr = getJsonString(moviesUri);
                    movies = getMoviesDataFromJson(moviesJsonStr);
                } catch (JSONException e) {
                    Log.d(LOG_TAG, "JSONException while parsing result from server");
                    e.printStackTrace();
                } catch (IOException e) {
                    Log.d(LOG_TAG, "IOException while parsing result from server");
                    e.printStackTrace();
                } finally {
                    Intent broadcastIntent = new Intent();
                    broadcastIntent.setAction(Constants.ACTION_MOVIES_RESULT);
                    if (movies != null) {
                        broadcastIntent.putExtra(EXTRA_MOVIES_RESULT, movies);
                    }
                    LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
                }
            }
        } else if (intent.getAction().equals(Constants.ACTION_EXTRA_INFO_REQUEST)
                && intent.hasExtra(EXTRA_INFO_MOVIE_ID)) {
            String movieId = intent.getStringExtra(EXTRA_INFO_MOVIE_ID);
            Video[] videos = null;
            Review[] reviews = null;
            if (!TextUtils.isEmpty(movieId)) {
                try {
                    Uri videosUri = createVideosUri(movieId);
                    String videosJsonString = getJsonString(videosUri);
                    videos = getVideosDataFromJson(videosJsonString);

                    Uri reviewsUri = createReviewsUri(movieId);
                    String reviewsJsonString = getJsonString(reviewsUri);
                    reviews = getReviewsDataFromJson(reviewsJsonString);

                } catch (JSONException e) {
                    Log.d(LOG_TAG, "JSONException while parsing result from server");
                    e.printStackTrace();
                } catch (IOException e) {
                    Log.d(LOG_TAG, "IOException while parsing result from server");
                    e.printStackTrace();
                } finally {
                    Intent broadcastIntent = new Intent();
                    broadcastIntent.setAction(Constants.ACTION_EXTRA_INFO_RESULT);
                    if (videos != null) {
                    broadcastIntent.putExtra(EXTRA_INFO_VIDEOS_RESULT, videos);
                     }
                    if (reviews != null) {
                    broadcastIntent.putExtra(EXTRA_INFO_REVIEWS_RESULT, reviews);
                     }
                    LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
                }
            }
        }
    }


    private String getJsonString(Uri requestUri) throws IOException {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        try {

            URL url = new URL(requestUri.toString());

            // Create the request to The Movide DB, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;


            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            return buffer.toString();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                reader.close();
            }
        }
    }

    private Movie[] getMoviesDataFromJson(String moviesJsonStr)
            throws JSONException {

        JSONObject moviesJson = new JSONObject(moviesJsonStr);
        JSONArray jsonMoviesArray = moviesJson.getJSONArray(Constants.JSON_MOVIE_LIST);
        Movie[] moviesArray = null;

        if (jsonMoviesArray != null) {
            moviesArray = new Movie[jsonMoviesArray.length()];

            for (int i = 0; i < jsonMoviesArray.length(); i++) {
                String id = jsonMoviesArray.getJSONObject(i).getString(Constants.JSON_MOVIE_ID);
                String title = jsonMoviesArray.getJSONObject(i).getString(Constants
                        .JSON_MOVIE_TITLE);
                String releaseDate = jsonMoviesArray.getJSONObject(i).getString(Constants
                        .JSON_MOVIE_RELEASE_DATE);
                String voteAverage = jsonMoviesArray.getJSONObject(i).getString(Constants
                        .JSON_MOVIE_VOTE_AVERAGE);
                String overview = jsonMoviesArray.getJSONObject(i).getString(Constants
                        .JSON_MOVIE_OVERVIEW);
                Uri posterUri = createPosterUri(jsonMoviesArray.getJSONObject(i).getString
                        (Constants.JSON_MOVIE_POSTER_PATH));

                moviesArray[i] = new Movie(id, title, releaseDate, voteAverage, overview,
                        posterUri);
            }
        }

        return moviesArray;
    }

    private Video[] getVideosDataFromJson(String videosJsonStr)
            throws JSONException {

        if (TextUtils.isEmpty(videosJsonStr)) {
            return null;
        }

        JSONObject videosJson = new JSONObject(videosJsonStr);
        JSONArray jsonVideosArray = videosJson.getJSONArray(Constants.JSON_VIDEOS_LIST);

        Video[] videosArray = null;

        if (jsonVideosArray != null) {
            videosArray = new Video[jsonVideosArray.length()];
            for (int i = 0; i < jsonVideosArray.length(); i++) {
                String id = jsonVideosArray.getJSONObject(i).getString(Constants.JSON_VIDEO_ID);
                String key = jsonVideosArray.getJSONObject(i).getString(Constants.JSON_VIDEO_KEY);
                String name = jsonVideosArray.getJSONObject(i).getString(Constants.JSON_VIDEO_NAME);
                videosArray[i] = new Video(id, key, name);
            }
        }

        return videosArray;
    }

    private Review[] getReviewsDataFromJson(String reviewsJsonStr)
            throws JSONException {

        if (TextUtils.isEmpty(reviewsJsonStr)) {
            return null;
        }

        JSONObject reviewsJson = new JSONObject(reviewsJsonStr);
        JSONArray jsonReviewsArray = reviewsJson.getJSONArray(Constants.JSON_REVIEW_LIST);

        Review[] reviewsArray = null;

        if (jsonReviewsArray != null) {
            reviewsArray = new Review[jsonReviewsArray.length()];
            for (int i = 0; i < jsonReviewsArray.length(); i++) {
                String id = jsonReviewsArray.getJSONObject(i).getString(Constants.JSON_REVIEW_ID);
                String author = jsonReviewsArray.getJSONObject(i).getString(Constants
                        .JSON_REVIEW_AUTHOR);
                String content = jsonReviewsArray.getJSONObject(i).getString(Constants
                        .JSON_REVIEW_CONTENT);
                reviewsArray[i] = new Review(id, author, content);
            }
        }

        return reviewsArray;
    }

    // Creates Uri based on sort order, language, etc
    private Uri createMoviesUri(String sortOrder) {

        Uri builtUri;
        if (sortOrder.equals(getString(R.string.pref_popular_value))) {
            builtUri = Uri.parse(Constants.API_POPULAR_MOVIES_BASE_URL);
        } else if (sortOrder.equals(getString(R.string.pref_top_rated_value))) {
            builtUri = Uri.parse(Constants.API_TOP_RATED_MOVIES_BASE_URL);
        } else {
            builtUri = Uri.parse(Constants.API_POPULAR_MOVIES_BASE_URL);

        }

        Uri apiUri;

        // This app supports English and Brazilian Portuguese
        // How to get system's current country: http://stackoverflow
        // .com/questions/4212320/get-the-current-language-in-device
        if (Constants.API_PORTUGUESE_LANGUAGE.startsWith(Locale.getDefault().getLanguage())) {
            apiUri = builtUri.buildUpon()
                    .appendQueryParameter(Constants.API_KEY_PARAM, BuildConfig
                            .THE_MOVIE_DB_MAP_API_KEY)
                    .appendQueryParameter(Constants.API_LANGUAGE_PARAM, Constants
                            .API_PORTUGUESE_LANGUAGE)
                    .build();
        } else {

            apiUri = builtUri.buildUpon()
                    .appendQueryParameter(Constants.API_KEY_PARAM, BuildConfig
                            .THE_MOVIE_DB_MAP_API_KEY)
                    .build();
        }

        return apiUri;
    }

    private Uri createVideosUri(String videoId) {

        Uri builtUri = Uri.parse(Constants.API_VIDEOS_REVIEWS_BASE_URL);
        Uri apiUri;

        // This app supports English and Brazilian Portuguese
        // How to get system's current country: http://stackoverflow
        // .com/questions/4212320/get-the-current-language-in-device
        if (Constants.API_PORTUGUESE_LANGUAGE.startsWith(Locale.getDefault().getLanguage())) {
            apiUri = builtUri.buildUpon()
                    .appendPath(videoId)
                    .appendPath(Constants.API_VIDEOS_PATH)
                    .appendQueryParameter(Constants.API_KEY_PARAM, BuildConfig
                            .THE_MOVIE_DB_MAP_API_KEY)
                    .appendQueryParameter(Constants.API_LANGUAGE_PARAM, Constants
                            .API_PORTUGUESE_LANGUAGE)
                    .build();
        } else {

            apiUri = builtUri.buildUpon()
                    .appendPath(videoId)
                    .appendPath(Constants.API_VIDEOS_PATH)
                    .appendQueryParameter(Constants.API_KEY_PARAM, BuildConfig
                            .THE_MOVIE_DB_MAP_API_KEY)
                    .build();
        }

        return apiUri;
    }


    private Uri createReviewsUri(String reviewId) {

        Uri builtUri = Uri.parse(Constants.API_VIDEOS_REVIEWS_BASE_URL);
        Uri apiUri;

        // This app supports English and Brazilian Portuguese
        // How to get system's current country: http://stackoverflow
        // .com/questions/4212320/get-the-current-language-in-device
        if (Constants.API_PORTUGUESE_LANGUAGE.startsWith(Locale.getDefault().getLanguage())) {
            apiUri = builtUri.buildUpon()
                    .appendPath(reviewId)
                    .appendPath(Constants.API_REVIEWS_PATH)
                    .appendQueryParameter(Constants.API_KEY_PARAM, BuildConfig
                            .THE_MOVIE_DB_MAP_API_KEY)
                    .appendQueryParameter(Constants.API_LANGUAGE_PARAM, Constants
                            .API_PORTUGUESE_LANGUAGE)
                    .build();
        } else {

            apiUri = builtUri.buildUpon()
                    .appendPath(reviewId)
                    .appendPath(Constants.API_REVIEWS_PATH)
                    .appendQueryParameter(Constants.API_KEY_PARAM, BuildConfig
                            .THE_MOVIE_DB_MAP_API_KEY)
                    .build();
        }

        return apiUri;
    }


    // Method to create poster thumbnail Uri
    private Uri createPosterUri(String posterPath) {
        Uri builtUri = Uri.parse(Constants.API_POSTER_MOVIES_BASE_URL).buildUpon()
                .appendEncodedPath(Constants.API_POSTER_SIZE).appendEncodedPath(posterPath)
                .build();
        return builtUri;
    }

}
