package com.henriquenfaria.popularmovies;


import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

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

// AsyncTask to fetch The Movie DB data
public class FetchMoviesTask extends AsyncTask<String, Void, Movie[]> {

    private static final String LOG_TAG = MoviesActivity.class.getSimpleName();

    //TODO: Possible context leak here?
    private Context mContext;

    private OnPostExecuteListener mDelegate;

    public FetchMoviesTask(Context context, OnPostExecuteListener delegate) {
        mContext = context;
        mDelegate = delegate;
    }

    private Movie[] getMoviesDataFromJson(String moviesJsonStr)
            throws JSONException {

        JSONObject moviesJson = new JSONObject(moviesJsonStr);
        JSONArray jsonMoviesArray = moviesJson.getJSONArray(Constants.JSON_LIST);

        Movie[] moviesArray = new Movie[jsonMoviesArray.length()];

        for (int i = 0; i < jsonMoviesArray.length(); i++) {
            String id = jsonMoviesArray.getJSONObject(i).getString(Constants.JSON_ID);
            String title = jsonMoviesArray.getJSONObject(i).getString(Constants.JSON_TITLE);
            String releaseDate = jsonMoviesArray.getJSONObject(i).getString(Constants
                    .JSON_RELEASE_DATE);
            String voteAverage = jsonMoviesArray.getJSONObject(i).getString(Constants
                    .JSON_VOTE_AVERAGE);
            String overview = jsonMoviesArray.getJSONObject(i).getString(Constants
                    .JSON_OVERVIEW);
            Uri posterUri = createPosterUri(jsonMoviesArray.getJSONObject(i).getString
                    (Constants.JSON_POSTER_PATH));

            moviesArray[i] = new Movie(id, title, releaseDate, voteAverage, overview,
                    posterUri);
        }
        return moviesArray;
    }

    // Creates Uri based on sort order, language, etc
    private Uri createMoviesUri(String sortOrder) {
        Uri builtUri = null;
        if (sortOrder.equals(mContext.getString(R.string.pref_popular_value))) {
            builtUri = Uri.parse(Constants.API_POPULAR_MOVIES_BASE_URL);
        } else if (sortOrder.equals(mContext.getString(R.string.pref_top_rated_value))) {
            builtUri = Uri.parse(Constants.API_TOP_RATED_MOVIES_BASE_URL);
        } else {
            builtUri = Uri.parse(Constants.API_POPULAR_MOVIES_BASE_URL);

        }

        Uri apiUri = null;

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

    // Method to create poster thumbnail Uri
    private Uri createPosterUri(String posterPath) {
        Uri builtUri = Uri.parse(Constants.API_POSTER_MOVIES_BASE_URL).buildUpon()
                .appendEncodedPath(Constants.API_POSTER_SIZE).appendEncodedPath(posterPath)
                .build();
        return builtUri;
    }


    @Override
    protected Movie[] doInBackground(String... params) {

        if (params.length == 0) {
            return null;
        }

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String moviesJsonStr = null;

        try {
            Uri moviesUri = createMoviesUri(params[0]);
            URL url = new URL(moviesUri.toString());

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
            moviesJsonStr = buffer.toString();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the movies data, there's no point in
            // attemping
            // to parse it.
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        try {
            return getMoviesDataFromJson(moviesJsonStr);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        // This will only happen if there was an error getting or parsing the movies.
        return null;
    }

    @Override
    protected void onPostExecute(Movie[] result) {
        if (mDelegate != null && result != null) {
            mDelegate.onPostExecuteInteraction(result);
        }
    }

    public interface OnPostExecuteListener {
        void onPostExecuteInteraction(Movie[] result);
    }
}