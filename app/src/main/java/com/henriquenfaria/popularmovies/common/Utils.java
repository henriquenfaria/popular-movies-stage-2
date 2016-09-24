package com.henriquenfaria.popularmovies.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.henriquenfaria.popularmovies.R;
import com.henriquenfaria.popularmovies.data.FavoriteMoviesContract;
import com.henriquenfaria.popularmovies.model.Movie;
import com.henriquenfaria.popularmovies.model.Review;
import com.henriquenfaria.popularmovies.model.Video;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

// Utility calls
public class Utils {

    private static final String LOG_TAG = Utils.class.getSimpleName();

    // Method based on http://stackoverflow.com/
    // questions/9113895/how-to-check-if-an-imageview-is-attached-with-image-in-android
    public static boolean hasImage(ImageView view) {
        Drawable drawable = view.getDrawable();
        boolean hasImage = (drawable != null);

        if (hasImage && (drawable instanceof BitmapDrawable)) {
            hasImage = ((BitmapDrawable) drawable).getBitmap() != null;
        }

        return hasImage;
    }

    public static Bitmap getBitmapFromImageView(ImageView view) {
        Drawable drawable = view.getDrawable();
        boolean hasImage = (drawable != null);
        Bitmap bitmap = null;

        if (hasImage) {
            if (drawable instanceof GlideBitmapDrawable) {
                bitmap = ((GlideBitmapDrawable) drawable).getBitmap();
            } else if (drawable instanceof BitmapDrawable) {
                bitmap = ((BitmapDrawable) drawable).getBitmap();
            }
        }

        return bitmap;
    }

    // Method based on http://stackoverflow.com
    // /questions/17674634/saving-and-reading-bitmaps-images-from-internal-memory-in-android
    public static void saveBitmapToInternalStorage(Context ctx, Bitmap bitmapImage,
                                                   String fileName) {
        FileOutputStream fos = null;
        try {
            fos = ctx.openFileOutput(fileName, ctx.MODE_PRIVATE);
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Method based on http://stackoverflow.com
    // /questions/17674634/saving-and-reading-bitmaps-images-from-internal-memory-in-android
    public static Bitmap loadBitmapFromInternalStorage(Context ctx, String fileName) {
        Bitmap bitmap = null;
        FileInputStream fis = null;
        try {
            fis = ctx.openFileInput(fileName);
            bitmap = BitmapFactory.decodeStream(fis);
            //ImageView img=(ImageView) findViewById(R.id.imgPicker);
            // img.setImageBitmap(b);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    public static boolean deleteFileFromInternalStorage(Context ctx, String fileName) {
        File dir = ctx.getFilesDir();
        File file = new File(dir, fileName);
        return file.delete();
    }

    public static Movie createMovieFromCursor(Cursor cursor) {
        String id = cursor.getString(cursor.getColumnIndex(FavoriteMoviesContract.MoviesEntry._ID));
        String title = cursor.getString(cursor.getColumnIndex(FavoriteMoviesContract.MoviesEntry
                .COLUMN_TITLE));
        String releaseDate = cursor.getString(cursor.getColumnIndex(FavoriteMoviesContract
                .MoviesEntry.COLUMN_RELEASE_DATE));
        String voteAverage = cursor.getString(cursor.getColumnIndex(FavoriteMoviesContract
                .MoviesEntry.COLUMN_VOTE_AVERAGE));
        String overview = cursor.getString(cursor.getColumnIndex(FavoriteMoviesContract
                .MoviesEntry.COLUMN_OVERVIEW));
        Uri posterUri = Uri.parse(cursor.getString(cursor.getColumnIndex(FavoriteMoviesContract
                .MoviesEntry.COLUMN_PORTER_URI)));

        return new Movie(id, title, releaseDate, voteAverage, overview, posterUri);
    }


    public static Video[] createVideosFromCursor(Cursor cursor) {
        Video[] videos = null;
        if (cursor != null) {
            videos = new Video[cursor.getCount()];
            int videoIdColumnIndex = cursor.getColumnIndex(FavoriteMoviesContract.VideosEntry._ID);
            int videoKeyColumnIndex = cursor.getColumnIndex(FavoriteMoviesContract.VideosEntry
                    .COLUMN_KEY);
            int videoNameColumnIndex = cursor.getColumnIndex(FavoriteMoviesContract.VideosEntry
                    .COLUMN_NAME);

            while (cursor.moveToNext()) {
                videos[cursor.getPosition()] =
                        new Video(cursor.getString(videoIdColumnIndex),
                                cursor.getString(videoKeyColumnIndex),
                                cursor.getString(videoNameColumnIndex));
            }
        }
        return videos;
    }

    public static Review[] createReviewsFromCursor(Cursor cursor) {
        Review[] reviews = null;
        if (cursor != null) {
            reviews = new Review[cursor.getCount()];
            int reviewIdColumnIndex = cursor.getColumnIndex(FavoriteMoviesContract.ReviewsEntry
                    ._ID);
            int reviewAuthorColumnIndex = cursor.getColumnIndex(FavoriteMoviesContract
                    .ReviewsEntry.COLUMN_AUTHOR);
            int reviewContentColumnIndex = cursor.getColumnIndex(FavoriteMoviesContract
                    .ReviewsEntry.COLUMN_CONTENT);

            while (cursor.moveToNext()) {
                reviews[cursor.getPosition()] =
                        new Review(cursor.getString(reviewIdColumnIndex),
                                cursor.getString(reviewAuthorColumnIndex),
                                cursor.getString(reviewContentColumnIndex));
            }
        }
        return reviews;
    }

    public static String getSortPref(Context ctx) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        return preferences.getString(ctx.getString(R.string.pref_sort_order_key),
                ctx.getString(R.string.pref_popular_value));
    }

    public static boolean isFavoriteSort(Context ctx, String currentSort) {
        return TextUtils.equals(currentSort, ctx.getString(R.string.pref_favorites_value));

    }

    public static boolean isFavoriteSort(Context ctx) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        String currentSort = preferences.getString(ctx.getString(R.string.pref_sort_order_key),
                ctx.getString(R.string.pref_popular_value));
        return TextUtils.equals(currentSort, ctx.getString(R.string.pref_favorites_value));

    }

    /*
   * Method to check if internet connection is available or not.
   * Method from http://stackoverflow.com/questions/16481334/check-network-connection-in-fragment
    */
    public static boolean isInternetConnected(Context ctx) {

        ConnectivityManager connMgr = (ConnectivityManager)
                ctx.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

}