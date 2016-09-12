package com.henriquenfaria.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;

public class FavoriteMoviesProvider extends ContentProvider {

    private static final int MOVIES = 1;
    private static final int MOVIE_ID = 2;
    private static final UriMatcher mUriMatcher = getUriMatcher();
    private FavoriteMoviesDbHelper mFavoriteMoviesDBHelper;

    @Override
    public boolean onCreate() {
        mFavoriteMoviesDBHelper = new FavoriteMoviesDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        Cursor cursor;
        String id = null;
        switch (mUriMatcher.match(uri)) {
            case MOVIES: {
                cursor = mFavoriteMoviesDBHelper.getReadableDatabase().query(
                        FavoriteMoviesContract.MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case MOVIE_ID: {
                id = uri.getPathSegments().get(1);
                cursor = getSpecificMovie(id, projection, selection, selectionArgs, sortOrder);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        //TODO: Not used yet. It will be used for notifying CursorLoader.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    private Cursor getSpecificMovie(String id, String[] projection, String selection, String[]
            selectionArgs, String sortOrder) {
        SQLiteQueryBuilder sqliteQueryBuilder = new SQLiteQueryBuilder();
        sqliteQueryBuilder.setTables(FavoriteMoviesContract.MovieEntry.TABLE_NAME);

        if (id != null) {
            sqliteQueryBuilder.appendWhere("_id" + " = " + id);
        }

        Cursor cursor = sqliteQueryBuilder.query(mFavoriteMoviesDBHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        switch (mUriMatcher.match(uri)) {
            case MOVIES:
                return FavoriteMoviesContract.MovieEntry.CONTENT_TYPE;
            case MOVIE_ID:
                return FavoriteMoviesContract.MovieEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {

        final SQLiteDatabase db = mFavoriteMoviesDBHelper.getWritableDatabase();
        final int match = mUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case MOVIES: {
                long _id = db.insert(FavoriteMoviesContract.MovieEntry.TABLE_NAME, null, contentValues);
                if (_id > 0)
                    returnUri = FavoriteMoviesContract.MovieEntry.buildFavoriteMoviesUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mFavoriteMoviesDBHelper.getWritableDatabase();
        final int match = mUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if (null == selection) selection = "1";
        switch (match) {
            case MOVIES:
                rowsDeleted = db.delete(FavoriteMoviesContract.MovieEntry.TABLE_NAME, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mFavoriteMoviesDBHelper.getWritableDatabase();
        final int match = mUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case MOVIES:
                rowsUpdated = db.update(FavoriteMoviesContract.MovieEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    private static UriMatcher getUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(FavoriteMoviesContract.CONTENT_AUTHORITY, FavoriteMoviesContract.PATH_FAVORITE_MOVIES, MOVIES);
        uriMatcher.addURI(FavoriteMoviesContract.CONTENT_AUTHORITY, FavoriteMoviesContract.PATH_FAVORITE_MOVIES + "/#",
                MOVIE_ID);
        return uriMatcher;
    }
}
