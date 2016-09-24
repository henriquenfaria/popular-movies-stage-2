package com.henriquenfaria.popularmovies.data;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.henriquenfaria.popularmovies.data.FavoriteMoviesContract.MoviesEntry;
import com.henriquenfaria.popularmovies.data.FavoriteMoviesContract.VideosEntry;
import com.henriquenfaria.popularmovies.data.FavoriteMoviesContract.ReviewsEntry;

public class FavoriteMoviesDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "favorite_movies.db";
    private static final int DATABASE_VERSION = 1;

    private static final String SQL_CREATE_MOVIES_TABLE = "CREATE TABLE " + MoviesEntry
            .TABLE_NAME
            + " (" + MoviesEntry._ID + " TEXT PRIMARY KEY, "
            + MoviesEntry.COLUMN_TITLE + " TEXT NOT NULL, "
            + MoviesEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL, "
            + MoviesEntry.COLUMN_VOTE_AVERAGE + " TEXT NOT NULL, "
            + MoviesEntry.COLUMN_OVERVIEW + " TEXT NOT NULL, "
            + MoviesEntry.COLUMN_PORTER_URI + " TEXT NOT NULL "
            + " );";

    private static final String SQL_CREATE_VIDEOS_TABLE = "CREATE TABLE "
            + VideosEntry.TABLE_NAME
            + " (" + VideosEntry._ID + " TEXT PRIMARY KEY, "
            + VideosEntry.COLUMN_MOVIE_ID + " TEXT NOT NULL, "
            + VideosEntry.COLUMN_KEY + " TEXT NOT NULL, "
            + VideosEntry.COLUMN_NAME + " TEXT NOT NULL, "
            + "FOREIGN KEY (" + VideosEntry.COLUMN_MOVIE_ID + ") REFERENCES " +
            MoviesEntry.TABLE_NAME + " (" + MoviesEntry._ID + ") "
            + " );";

    private static final String SQL_CREATE_REVIEWS_TABLE = "CREATE TABLE "
            + ReviewsEntry.TABLE_NAME
            + " (" + ReviewsEntry._ID + " TEXT PRIMARY KEY, "
            + ReviewsEntry.COLUMN_MOVIE_ID + " TEXT NOT NULL, "
            + ReviewsEntry.COLUMN_AUTHOR + " TEXT NOT NULL, "
            + ReviewsEntry.COLUMN_CONTENT + " TEXT NOT NULL, "
            + "FOREIGN KEY (" + ReviewsEntry.COLUMN_MOVIE_ID + ") REFERENCES " +
            ReviewsEntry.TABLE_NAME + " (" + MoviesEntry._ID + ") "
            + " );";

    private static final String SQL_DROP_MOVIES_TABLE = "DROP TABLE IS EXISTS " +
            MoviesEntry.TABLE_NAME;
    private static final String SQL_DROP_VIDEOS_TABLE = "DROP TABLE IS EXISTS " +
            MoviesEntry.TABLE_NAME;
    private static final String SQL_DROP_REVIEWS_TABLE = "DROP TABLE IS EXISTS " +
            ReviewsEntry.TABLE_NAME;

    FavoriteMoviesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_MOVIES_TABLE);
        db.execSQL(SQL_CREATE_VIDEOS_TABLE);
        db.execSQL(SQL_CREATE_REVIEWS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DROP_MOVIES_TABLE);
        db.execSQL(SQL_DROP_VIDEOS_TABLE);
        db.execSQL(SQL_DROP_REVIEWS_TABLE);
        onCreate(db);
    }
}