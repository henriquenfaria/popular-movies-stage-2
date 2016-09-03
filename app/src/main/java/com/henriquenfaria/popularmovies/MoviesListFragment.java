package com.henriquenfaria.popularmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

// Class containing a list of movies
public class MoviesListFragment extends Fragment implements FetchMoviesTask.OnPostExecuteListener {

    private static final String LOG_TAG = MoviesActivity.class.getSimpleName();
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
    public static MoviesListFragment newInstance() {
        MoviesListFragment fragment = new MoviesListFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        FetchMoviesTask moviesTask = new FetchMoviesTask(getActivity().getApplicationContext(),
                this);
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
        if (view instanceof DynamicSpanCountRecyclerView) {
            Context context = view.getContext();
            DynamicSpanCountRecyclerView recyclerView = (DynamicSpanCountRecyclerView) view;
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

    @Override
    public void onPostExecuteInteraction(Movie[] result) {
        mMoviesRecyclerViewAdapter.clearRecyclerViewData();
        for (Movie movieObj : result) {
            mMoviesList.add(movieObj);

        }
        //mMoviesRecyclerViewAdapter.notifyItemInserted(mMoviesList.size()-1);
        mMoviesRecyclerViewAdapter.notifyItemRangeInserted(0, result.length);
    }

    public interface OnMoviesListInteractionListener {
        void onMoviesListInteraction(Movie item);
    }

}
