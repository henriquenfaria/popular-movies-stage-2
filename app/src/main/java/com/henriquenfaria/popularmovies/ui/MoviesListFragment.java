package com.henriquenfaria.popularmovies.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.henriquenfaria.popularmovies.R;
import com.henriquenfaria.popularmovies.common.Constants;
import com.henriquenfaria.popularmovies.common.Utils;
import com.henriquenfaria.popularmovies.data.FavoriteMoviesContract;
import com.henriquenfaria.popularmovies.model.Movie;
import com.henriquenfaria.popularmovies.service.MoviesIntentService;

import java.util.ArrayList;

// Class containing a list of movies
public class MoviesListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = MoviesActivity.class.getSimpleName();
    private static final int LOADER_FAVORITE_MOVIES = 1;
    private static final String STATE_LAYOUT_MANAGER = "state_recycler_view";
    private static final String STATE_MOVIES_LIST = "state_movies_list";
    private static final String SAVE_LAST_UPDATE_ORDER = "save_last_update_order";

    private OnMoviesListInteractionListener mMoviesListener;
    private OnFavoriteMoviesListInteractionListener mFavoriteListener;
    private FavoriteMoviesRecyclerViewAdapter mFavoriteMoviesRecyclerViewAdapter;
    private MoviesRecyclerViewAdapter mMoviesRecyclerViewAdapter;
    private ArrayList<Movie> mMoviesList;
    private String mLastUpdateOrder;
    private DynamicSpanCountRecyclerView mRecyclerView;
    private Parcelable mLayoutManager;

    private OnLoadingInteractionListener mLoadingListener;


    private ResponseReceiver mReceiver = new ResponseReceiver();

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
        String currentSortOrder = Utils.getSortPref(getActivity());

        if (!Utils.isFavoriteSort(getActivity(), currentSortOrder)) {
            if (savedInstanceState == null) {
                updateMoviesList();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SAVE_LAST_UPDATE_ORDER, mLastUpdateOrder);

        if (mRecyclerView != null) {
            outState.putParcelable(STATE_LAYOUT_MANAGER, mRecyclerView.getLayoutManager()
                    .onSaveInstanceState());
        }
        if (mMoviesList != null) {
            outState.putParcelableArrayList(STATE_MOVIES_LIST, mMoviesList);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        chooseAdapter();

        if (needToUpdateMoviesList()) {
            updateMoviesList();
        }

    }

    // Starts AsyncTask to fetch The Movie DB API
    public void updateMoviesList() {

        Activity activity = getActivity();
        boolean isInternetConnected = Utils.isInternetConnected(activity);

        if (activity instanceof MoviesActivity) {
            ((MoviesActivity) getActivity()).changeNoInternetVisibility(isInternetConnected);
        }

        if (isInternetConnected) {
            String currentSortOrder = Utils.getSortPref(getActivity());
            mLastUpdateOrder = currentSortOrder;
            Intent intent = new Intent(getActivity(), MoviesIntentService.class);
            intent.setAction(Constants.ACTION_MOVIES_REQUEST);
            intent.putExtra(MoviesIntentService.EXTRA_MOVIES_SORT, currentSortOrder);
            getActivity().startService(intent);

            if (mLoadingListener != null) {
                mLoadingListener.onLoadingInteraction(true);
            }
        }

    }

    // Method to decide if movie info should be updated based on sort order
    private boolean needToUpdateMoviesList() {
        if (TextUtils.isEmpty(mLastUpdateOrder)) {
            return true;
        }

        String currentSortOrder = Utils.getSortPref(getActivity());

        if (Utils.isFavoriteSort(getActivity(), currentSortOrder)) {
            return false;
        } else if (!TextUtils.equals(mLastUpdateOrder, currentSortOrder)) {
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
            mRecyclerView = (DynamicSpanCountRecyclerView) view;
            mMoviesList = new ArrayList<Movie>();

            if (savedInstanceState != null) {
                mLastUpdateOrder = savedInstanceState.getString(SAVE_LAST_UPDATE_ORDER);
                mLayoutManager = savedInstanceState.getParcelable(STATE_LAYOUT_MANAGER);
                mMoviesList = savedInstanceState.getParcelableArrayList(STATE_MOVIES_LIST);

                mRecyclerView.getLayoutManager().onRestoreInstanceState(mLayoutManager);
            }

            chooseAdapter();
        }
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnMoviesListInteractionListener) {
            mMoviesListener = (OnMoviesListInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnMoviesListFragmentInteractionListener");
        }

        if (context instanceof OnFavoriteMoviesListInteractionListener) {
            mFavoriteListener = (OnFavoriteMoviesListInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFavoriteMoviesListInteractionListener");
        }

        if (context instanceof OnLoadingInteractionListener) {
            mLoadingListener = (OnLoadingInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnLoadingInteractionListener");
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mMoviesListener = null;
        mFavoriteListener = null;
        mLoadingListener = null;
    }


    public class ResponseReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) {
                return;
            }

            if (intent.getAction().equals(Constants.ACTION_MOVIES_RESULT) && intent.hasExtra
                    (MoviesIntentService.EXTRA_MOVIES_RESULT)) {
                Movie[] movies = (Movie[]) intent.getParcelableArrayExtra(MoviesIntentService
                        .EXTRA_MOVIES_RESULT);

                if (mMoviesRecyclerViewAdapter != null && mMoviesList != null) {
                    mMoviesRecyclerViewAdapter.clearRecyclerViewData();
                    for (Movie movieObj : movies) {
                        mMoviesList.add(movieObj);
                    }
                    mMoviesRecyclerViewAdapter.notifyItemRangeInserted(0, movies.length);
                }

            }

            if (mLoadingListener != null) {
                mLoadingListener.onLoadingInteraction(false);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mReceiver != null) {
            LocalBroadcastManager.getInstance(getActivity())
                    .registerReceiver(mReceiver, new IntentFilter(Constants.ACTION_MOVIES_RESULT));
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        if (mReceiver != null) {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
        }
    }

    public interface OnMoviesListInteractionListener {
        void onMoviesListInteraction(Movie movie);
    }

    public interface OnFavoriteMoviesListInteractionListener {
        void onFavoriteMoviesListInteraction(Movie movie);
    }

    public interface OnLoadingInteractionListener {
        void onLoadingInteraction(boolean display);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_FAVORITE_MOVIES:
                return new CursorLoader(getActivity(), FavoriteMoviesContract.MovieEntry
                        .CONTENT_URI,
                        null, null, null, null);
            default:
                Log.d(LOG_TAG, "Couldn't find loader");
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (mFavoriteMoviesRecyclerViewAdapter != null) {
            mFavoriteMoviesRecyclerViewAdapter.swapCursor(data);
            if (data != null && data.getCount() <= 0) {
                Toast.makeText(getActivity(), R.string.favorites_empty, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (mFavoriteMoviesRecyclerViewAdapter != null) {
            mFavoriteMoviesRecyclerViewAdapter.swapCursor(null);
        }
    }

    private void chooseAdapter() {
        String currentSortOrder = Utils.getSortPref(getActivity());

        if (!TextUtils.equals(mLastUpdateOrder, currentSortOrder) && mMoviesRecyclerViewAdapter
                != null) {
            mMoviesRecyclerViewAdapter.clearRecyclerViewData();
        }

        RecyclerView.Adapter currentAdapter = mRecyclerView.getAdapter();

        if (Utils.isFavoriteSort(getActivity(), currentSortOrder)
                && !(currentAdapter instanceof FavoriteMoviesRecyclerViewAdapter)) {
            mLastUpdateOrder = currentSortOrder;
            mFavoriteMoviesRecyclerViewAdapter = new FavoriteMoviesRecyclerViewAdapter
                    (mFavoriteListener);
            mRecyclerView.setAdapter(mFavoriteMoviesRecyclerViewAdapter);

            getLoaderManager().initLoader(LOADER_FAVORITE_MOVIES, null, this);

        } else if (!Utils.isFavoriteSort(getActivity(), currentSortOrder)
                && !(currentAdapter instanceof MoviesRecyclerViewAdapter)) {

            mMoviesRecyclerViewAdapter = new MoviesRecyclerViewAdapter(mMoviesList,
                    mMoviesListener);
            mRecyclerView.setAdapter(mMoviesRecyclerViewAdapter);
        }
    }
}