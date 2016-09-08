package com.henriquenfaria.popularmovies.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.henriquenfaria.popularmovies.R;
import com.henriquenfaria.popularmovies.model.Movie;
import com.henriquenfaria.popularmovies.net.FetchMoviesTask;

import java.util.ArrayList;

// Class containing a list of movies
public class MoviesListFragment extends Fragment implements FetchMoviesTask.OnPostExecuteListener {

    private static final String LOG_TAG = MoviesActivity.class.getSimpleName();
    private OnMoviesListInteractionListener mListener;
    private MoviesRecyclerViewAdapter mMoviesRecyclerViewAdapter;
    private ArrayList<Movie> mMoviesList;
    private String mLastUpdateOrder;
    private FetchMoviesTask mMoviesTask;
    private DynamicSpanCountRecyclerView mRecyclerView;
    private Parcelable mLayoutManager;

    private static final String STATE_MOVIES_TASK_RUNNING = "state_movies_task_running";
    private static final String STATE_LAYOUT_MANAGER = "state_recycler_view";
    private static final String STATE_MOVIES_LIST = "state_movies_list";
    private static final String SAVE_LAST_UPDATE_ORDER = "save_last_update_order";

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

        if (savedInstanceState == null) {
            updateMoviesList();
        } else {
            if (savedInstanceState.getBoolean(STATE_MOVIES_TASK_RUNNING, false)) {
                updateMoviesList();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SAVE_LAST_UPDATE_ORDER, mLastUpdateOrder);

        if (isMoviesTaskRunning()) {
            outState.putBoolean(STATE_MOVIES_TASK_RUNNING, true);
        }

        if (mRecyclerView != null) {
            outState.putParcelable(STATE_LAYOUT_MANAGER, mRecyclerView.getLayoutManager()
                    .onSaveInstanceState());
        }
        if (mMoviesList != null) {
            outState.putParcelableArrayList(STATE_MOVIES_LIST, mMoviesList);
        }

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {



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
    public void updateMoviesList() {
        mMoviesTask = new FetchMoviesTask(getActivity().getApplicationContext(),
                this);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortOrder = prefs.getString(getString(R.string.pref_sort_order_key),
                getString(R.string.pref_popular_value));
        mLastUpdateOrder = sortOrder;

        mMoviesTask.execute(sortOrder);
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

        if (view instanceof DynamicSpanCountRecyclerView) {
            Context context = view.getContext();
            mRecyclerView = (DynamicSpanCountRecyclerView) view;
            mMoviesList = new ArrayList<Movie>();

            if (savedInstanceState != null) {
                mLastUpdateOrder = savedInstanceState.getString(SAVE_LAST_UPDATE_ORDER);
                mLayoutManager = savedInstanceState.getParcelable(STATE_LAYOUT_MANAGER);
                mMoviesList = savedInstanceState.getParcelableArrayList(STATE_MOVIES_LIST);

                mRecyclerView.getLayoutManager().onRestoreInstanceState(mLayoutManager);
            }

            mMoviesRecyclerViewAdapter = new MoviesRecyclerViewAdapter(mMoviesList, mListener);
            mRecyclerView.setAdapter(mMoviesRecyclerViewAdapter);
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

    // Code based on http://code.hootsuite.com/orientation-changes-on-android/
    private boolean isMoviesTaskRunning() {
        return (mMoviesTask != null) && (mMoviesTask.getStatus() == AsyncTask.Status.RUNNING);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isMoviesTaskRunning()) {
            mMoviesTask.cancel(true);
        }
    }
}
