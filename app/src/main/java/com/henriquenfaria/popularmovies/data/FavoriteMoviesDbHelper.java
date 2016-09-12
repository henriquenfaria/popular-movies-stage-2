package com.henriquenfaria.popularmovies.data;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.henriquenfaria.popularmovies.data.FavoriteMoviesContract.MovieEntry;

public class FavoriteMoviesDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "favorite_movies.db";
    private static final int DATABASE_VERSION = 1;

    private static final String SQL_CREATE_FAVORITE_MOVIES_TABLE = "CREATE TABLE " + MovieEntry
            .TABLE_NAME
            + " (" + MovieEntry._ID + " INTEGER PRIMARY KEY, "
            + MovieEntry.COLUMN_TITLE + " TEXT NOT NULL, "
            + MovieEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL, "
            + MovieEntry.COLUMN_VOTE_AVERAGE + " TEXT NOT NULL, "
            + MovieEntry.COLUMN_OVERVIEW + " TEXT NOT NULL, "
            + MovieEntry.COLUMN_PORTER_URI + " TEXT NOT NULL "
            + " );";

    private static final String SQL_DROP_FAVORITE_MOVIES_TABLE = "DROP TABLE IS EXISTS " +
            MovieEntry.TABLE_NAME;

    FavoriteMoviesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_FAVORITE_MOVIES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DROP_FAVORITE_MOVIES_TABLE);
        onCreate(db);
    }
}