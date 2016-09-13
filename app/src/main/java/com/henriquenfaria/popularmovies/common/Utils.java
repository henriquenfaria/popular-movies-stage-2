package com.henriquenfaria.popularmovies.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.henriquenfaria.popularmovies.R;
import com.henriquenfaria.popularmovies.data.FavoriteMoviesContract;
import com.henriquenfaria.popularmovies.model.Movie;

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
        String id = cursor.getString(cursor.getColumnIndex(FavoriteMoviesContract.MovieEntry._ID));
        String title = cursor.getString(cursor.getColumnIndex(FavoriteMoviesContract.MovieEntry.COLUMN_TITLE));
        String releaseDate = cursor.getString(cursor.getColumnIndex(FavoriteMoviesContract.MovieEntry.COLUMN_RELEASE_DATE));
        String voteAverage = cursor.getString(cursor.getColumnIndex(FavoriteMoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE));
        String overview = cursor.getString(cursor.getColumnIndex(FavoriteMoviesContract.MovieEntry.COLUMN_OVERVIEW));
        Uri posterUri = Uri.parse(cursor.getString(cursor.getColumnIndex(FavoriteMoviesContract.MovieEntry.COLUMN_PORTER_URI)));

        return new Movie(id, title, releaseDate, voteAverage, overview, posterUri);
    }

    public static String getSortPref(Context ctx) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        return preferences.getString(ctx.getString(R.string.pref_sort_order_key), ctx.getString(R.string.pref_popular_name));
    }

    public static boolean isFavoriteSort(Context ctx, String currentSort) {
        return TextUtils.equals(currentSort, ctx.getString(R.string.pref_favorites_value));

    }
}