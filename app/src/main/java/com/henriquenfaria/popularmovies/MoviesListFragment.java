package com.henriquenfaria.popularmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Class containing a list of movies
public class MoviesListFragment extends Fragment {

    private static final String LOG_TAG = MoviesActivity.class.getSimpleName();
    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = Constants.PORTRAIT_COLUMN_COUNT;
    private OnMoviesListInteractionListener mListener;
    private MoviesRecyclerViewAdapter mMoviesRecyclerViewAdapter;
    private List<Movie> mMoviesList;
    private String mLastUpdateOrder;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MoviesListFragment() {
    }

    // Create new Fragment instance
    public static MoviesListFragment newInstance(int columnCount) {
        MoviesListFragment fragment = new MoviesListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }

        updateMoviesList();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(Constants.SAVE_LAST_UPDATE_ORDER, mLastUpdateOrder);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mLastUpdateOrder = savedInstanceState.getString(Constants.SAVE_LAST_UPDATE_ORDER);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (needToUpdateMoviesList()) {
            updateMoviesList();
        }
    }

    // Starts AsyncTask to fetch The Movie DB API
    private void updateMoviesList() {
        FetchMoviesTask moviesTask = new FetchMoviesTask();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortOrder = prefs.getString(getString(R.string.pref_sort_order_key),
                getString(R.string.pref_popular_value));
        mLastUpdateOrder = sortOrder;

        moviesTask.execute(sortOrder);
    }

    // Method to decide if movie info should be updated based on sort order
    private boolean needToUpdateMoviesList() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (!mLastUpdateOrder.equals(prefs.getString(getString(R.string.pref_sort_order_key),
                getString(R.string.pref_popular_value)))) {
            return true;

        } else {
            return false;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movies_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;

            if (getActivity().getResources().getConfiguration().orientation == Configuration
                    .ORIENTATION_PORTRAIT) {
                mColumnCount = Constants.PORTRAIT_COLUMN_COUNT;
            } else {
                mColumnCount = Constants.LANDSCAPE_COLUMN_COUNT;
            }

            if (mColumnCount <= 1) {
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
                recyclerView.setLayoutManager(linearLayoutManager);
            } else {
                GridLayoutManager gridLayoutManager = new GridLayoutManager(context, mColumnCount);
                recyclerView.setLayoutManager(gridLayoutManager);
            }

            mMoviesList = new ArrayList<Movie>();

            mMoviesRecyclerViewAdapter = new MoviesRecyclerViewAdapter(mMoviesList, mListener);
            recyclerView.setAdapter(mMoviesRecyclerViewAdapter);
        }
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnMoviesListInteractionListener) {
            mListener = (OnMoviesListInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnMoviesListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnMoviesListInteractionListener {
        void onMoviesListInteraction(Movie item);
    }

    // AsyncTask to fetch The Movie DB data
    public class FetchMoviesTask extends AsyncTask<String, Void, Movie[]> {

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

            if (sortOrder.equals(getString(R.string.pref_popular_value))) {
                builtUri = Uri.parse(Constants.API_POPULAR_MOVIES_BASE_URL);
            } else if (sortOrder.equals(getString(R.string.pref_top_rated_value))) {
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
            if (result != null) {
                mMoviesRecyclerViewAdapter.clearRecyclerViewData();
                for (Movie movieObj : result) {
                    mMoviesList.add(movieObj);

                }
                //mMoviesRecyclerViewAdapter.notifyItemInserted(mMoviesList.size()-1);
                mMoviesRecyclerViewAdapter.notifyItemRangeInserted(0, result.length);

            }
        }
    }
}
