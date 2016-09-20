package com.henriquenfaria.popularmovies.ui;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.henriquenfaria.popularmovies.R;
import com.henriquenfaria.popularmovies.common.Constants;
import com.henriquenfaria.popularmovies.common.Utils;
import com.henriquenfaria.popularmovies.data.FavoriteMoviesContract;
import com.henriquenfaria.popularmovies.model.Movie;
import com.henriquenfaria.popularmovies.model.Review;
import com.henriquenfaria.popularmovies.model.Video;
import com.henriquenfaria.popularmovies.service.MoviesIntentService;

// Fragment that displays detailed info about selected movie
public class DetailsFragment extends Fragment {

    private static final String LOG_TAG = DetailsFragment.class.getSimpleName();
    private static final String ARG_MOVIE = "arg_movie";
    private static final String SAVE_MOVIE = "save_movie";
    private static final String SAVE_FAVORITE_MOVIE = "save_favorite_movie";
    private static final String SAVE_FAVORITE_SORT = "save_favorite_sort";
    private static final String SAVE_FULLY_LOADED = "save_fully_loaded";
    private Movie mMovie;
    private boolean mIsFavoriteMovie;
    private boolean mIsFavoriteSort;
    private boolean mIsFullyLoaded;
    private LinearLayout mVideosLayout;
    private LinearLayout mReviewsLayout;

    private ImageView mPosterImageView;

    private ResponseReceiver mReceiver = new ResponseReceiver();

    private OnLoadingInteractionListener mLoadingListener;

    public DetailsFragment() {
        // Required empty public constructor
    }

    // Create new Fragment instance
    public static DetailsFragment newInstance(Movie movieSelected) {
        DetailsFragment fragment = new DetailsFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_MOVIE, movieSelected);
        fragment.setArguments(args);
        return fragment;
    }

    public static DetailsFragment newInstance() {
        DetailsFragment fragment = new DetailsFragment();
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
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
        mLoadingListener = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mMovie = getArguments().getParcelable(ARG_MOVIE);
            mIsFavoriteMovie = isFavoriteMovie(getActivity(), mMovie);
            mIsFavoriteSort = Utils.isFavoriteSort(getActivity());
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SAVE_MOVIE, mMovie);
        outState.putBoolean(SAVE_FAVORITE_MOVIE, mIsFavoriteMovie);
        outState.putBoolean(SAVE_FAVORITE_SORT, mIsFavoriteSort);
        outState.putBoolean(SAVE_FULLY_LOADED, mIsFullyLoaded);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Restore objects value
        if (savedInstanceState != null) {
            mMovie = savedInstanceState.getParcelable(SAVE_MOVIE);
            mIsFavoriteMovie = savedInstanceState.getBoolean(SAVE_FAVORITE_MOVIE);
            mIsFavoriteSort = savedInstanceState.getBoolean(SAVE_FAVORITE_SORT);
            mIsFullyLoaded = savedInstanceState.getBoolean(SAVE_FULLY_LOADED);
        }

        View view = inflater.inflate(R.layout.fragment_details, container, false);

        if (mMovie != null) {
            mPosterImageView = (ImageView) view.findViewById(R.id.poster);

            mVideosLayout = (LinearLayout) view.findViewById(R.id.videos);
            mReviewsLayout = (LinearLayout) view.findViewById(R.id.reviews);


            Glide.with(getActivity()).load(mMovie.getPosterUri())
                    .dontAnimate().into(mPosterImageView);

            TextView titleView = (TextView) view.findViewById(R.id.title_content);
            titleView.setText(mMovie.getTitle());

            TextView releaseDateView = (TextView) view.findViewById(R.id.release_date_content);
            releaseDateView.setText(mMovie.getReleaseDate());

            TextView averageView = (TextView) view.findViewById(R.id.vote_average_content);
            averageView.setText(mMovie.getVoteAverage());

            TextView overviewView = (TextView) view.findViewById(R.id.overview_content);

            // In portuguese, some movies does not contain overview data. In that case, displays
            // default text: @string/overview_not_available
            if (!TextUtils.isEmpty(mMovie.getOverview())) {
                overviewView.setText(mMovie.getOverview());
            }

            ImageButton starButton = (ImageButton) view.findViewById(R.id.star_button);
            starButton.setOnClickListener(starButtonOnClickListener);
            if (mIsFavoriteMovie) {
                starButton.setImageResource(R.drawable.ic_star);
            } else {
                starButton.setImageResource(R.drawable.ic_star_border);
            }
            starButton.setVisibility(View.VISIBLE);


            FrameLayout detailFrame = (FrameLayout) view.findViewById(R.id.detail_frame);
            detailFrame.setVisibility(View.VISIBLE);

            populateVideosLayout(getActivity());
            populateReviewsLayout(getActivity());
        }

        return view;
    }

    private View.OnClickListener starButtonOnClickListener = new View.OnClickListener() {
        public void onClick(View view) {

            // Can't save it to favorites db if movie poster is not ready yet
            if (mPosterImageView != null && !Utils.hasImage(mPosterImageView)) {
                Toast.makeText(getActivity(), R.string.please_wait_poster_download,
                        Toast.LENGTH_SHORT)
                        .show();
                return;
            }

            if (mIsFavoriteMovie) {
                if (removeFavoriteMovie(mMovie) > 0) {
                    Toast.makeText(getActivity(), R.string.success_remove_favorites, Toast
                            .LENGTH_SHORT)
                            .show();
                    ((ImageButton) view).setImageResource(R.drawable.ic_star_border);

                    // Delete poster image stored in internal storage
                    Utils.deleteFileFromInternalStorage(getActivity(), mMovie.getId());

                    mIsFavoriteMovie = false;
                } else {
                    Toast.makeText(getActivity(), R.string.fail_remove_favorites,
                            Toast.LENGTH_SHORT)
                            .show();
                }

            } else {
                Uri returnUri = addFavoriteMovie(mMovie);
                if (returnUri != null) {
                    Toast.makeText(getActivity(), R.string.success_add_favorites, Toast
                            .LENGTH_SHORT)
                            .show();
                    ((ImageButton) view).setImageResource(R.drawable.ic_star);

                    // Save poster image to internal storage
                    Bitmap posterBitmap = Utils.getBitmapFromImageView(mPosterImageView);
                    Utils.saveBitmapToInternalStorage(getActivity(), posterBitmap, mMovie.getId());

                    mIsFavoriteMovie = true;
                } else {

                    Toast.makeText(getActivity(), R.string.fail_add_favorites, Toast
                            .LENGTH_SHORT).show();
                }
            }
        }
    };

    private Uri addFavoriteMovie(Movie movie) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(FavoriteMoviesContract.MovieEntry._ID, Integer.parseInt(movie.getId()));
        contentValues.put(FavoriteMoviesContract.MovieEntry.COLUMN_TITLE, movie.getTitle());
        contentValues.put(FavoriteMoviesContract.MovieEntry.COLUMN_RELEASE_DATE, movie
                .getReleaseDate());
        contentValues.put(FavoriteMoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE, movie
                .getVoteAverage());
        contentValues.put(FavoriteMoviesContract.MovieEntry.COLUMN_OVERVIEW, movie.getOverview());
        contentValues.put(FavoriteMoviesContract.MovieEntry.COLUMN_PORTER_URI, movie.getPosterUri()
                .toString());

        Uri returnUri = null;

        try {
            returnUri = getActivity().getContentResolver().insert(FavoriteMoviesContract.MovieEntry
                    .CONTENT_URI, contentValues);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return returnUri;
    }

    private int removeFavoriteMovie(Movie movie) {
        return getActivity().getContentResolver().delete(FavoriteMoviesContract.MovieEntry
                        .CONTENT_URI,
                FavoriteMoviesContract.MovieEntry._ID + " = ?", new String[]{movie.getId()});
    }

    private boolean isFavoriteMovie(Context ctx, Movie movie) {

        int movieID = Integer.parseInt(movie.getId());
        Cursor cursor = ctx.getContentResolver().query(FavoriteMoviesContract.MovieEntry
                        .CONTENT_URI, null,
                FavoriteMoviesContract.MovieEntry._ID + " = " + movieID, null, null);
        if (cursor != null && cursor.moveToNext()) {
            int movieIdColumnIndex = cursor.getColumnIndex(FavoriteMoviesContract.MovieEntry._ID);
            if (movieID == cursor.getInt(movieIdColumnIndex)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mReceiver != null) {
            LocalBroadcastManager.getInstance(getActivity())
                    .registerReceiver(mReceiver, new IntentFilter(Constants.ACTION_EXTRA_INFO_RESULT));
        }

        if (mMovie != null && !mIsFullyLoaded && !Utils.isFavoriteSort(getActivity())) {
            Intent intent = new Intent(getActivity(), MoviesIntentService.class);
            intent.setAction(Constants.ACTION_EXTRA_INFO_REQUEST);
            intent.putExtra(MoviesIntentService.EXTRA_INFO_MOVIE_ID, mMovie.getId());
            getActivity().startService(intent);

            if (mLoadingListener != null) {
                mLoadingListener.onLoadingInteraction(true);
            }
        }
    }
        @Override
    public void onPause() {
        super.onPause();
        if (mReceiver != null) {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
        }
    }


    public class ResponseReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent == null || intent.getAction() == null) {
                return;
            }

            if (intent.getAction().equals(Constants.ACTION_EXTRA_INFO_RESULT)
                    && intent.hasExtra(MoviesIntentService.EXTRA_INFO_VIDEOS_RESULT)
                    && intent.hasExtra(MoviesIntentService.EXTRA_INFO_REVIEWS_RESULT)) {

                Video[] videos = (Video[]) intent.getParcelableArrayExtra(MoviesIntentService
                        .EXTRA_INFO_VIDEOS_RESULT);
                Review[] reviews = (Review[]) intent.getParcelableArrayExtra(MoviesIntentService
                        .EXTRA_INFO_REVIEWS_RESULT);

                if (mMovie != null) {
                    mMovie.setVideos(videos);
                    mMovie.setReviews(reviews);
                }

                if (mLoadingListener != null) {
                    mLoadingListener.onLoadingInteraction(false);
                }

                populateVideosLayout(getActivity());
                populateReviewsLayout(getActivity());

                mIsFullyLoaded = true;
            }
        }
    }

    private void populateVideosLayout(Context ctx) {
        if (mMovie != null && mVideosLayout != null && mMovie.getVideos() != null) {
            if (mVideosLayout.getChildCount() > 0) {
                mVideosLayout.removeAllViews();
            }

            LayoutInflater layoutInflater = (LayoutInflater)
                    ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            Video[] videos = mMovie.getVideos();
            for (int i = 0; i < videos.length; i++) {
                LinearLayout videoLayout = (LinearLayout) layoutInflater.inflate(R.layout.video_item, null);
                TextView nameTextView = (TextView) videoLayout.findViewById(R.id.name);
                int trailerIndex = i + 1;
                nameTextView.setText(ctx.getString(R.string.trailer_item) + " " + trailerIndex);
                mVideosLayout.addView(videoLayout);
            }

            if (videos != null) {
                mVideosLayout.setVisibility(View.VISIBLE);
            }

        }
    }

    private void populateReviewsLayout(Context ctx) {
        if (mMovie != null && mReviewsLayout != null && mMovie.getReviews() != null) {
            if (mReviewsLayout.getChildCount() > 0) {
                mReviewsLayout.removeAllViews();
            }

            LayoutInflater layoutInflater = (LayoutInflater)
                    ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            Review[] reviews = mMovie.getReviews();
            for (int i = 0; i < reviews.length; i++) {
                LinearLayout reviewLayout = (LinearLayout) layoutInflater.inflate(R.layout.review_item, null);
                TextView authorTextView = (TextView) reviewLayout.findViewById(R.id.author_name);
                TextView contentTextView = (TextView) reviewLayout.findViewById(R.id.content);
                authorTextView.setText(" " + reviews[i].getAuthor());
                contentTextView.setText(reviews[i].getContent());
                mReviewsLayout.addView(reviewLayout);
            }

            if (reviews != null) {
                mReviewsLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    public interface OnLoadingInteractionListener {
        void onLoadingInteraction(boolean display);
    }
}

