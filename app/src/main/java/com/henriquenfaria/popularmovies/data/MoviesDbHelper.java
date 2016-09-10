package com.henriquenfaria.popularmovies.data;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.henriquenfaria.popularmovies.data.MoviesContract.MovieEntry;

public class MoviesDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "movies.db";
    private static final int DATABASE_VERSION = 2;

    private static final String SQL_CREATE_MOVIES_TABLE = "CREATE TABLE " + MovieEntry.TABLE_NAME
            + " (" + MovieEntry._ID + " INTEGER PRIMARY KEY, "
            + MovieEntry.COLUMN_TITLE + " TEXT NOT NULL, "
            + MovieEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL, "
            + MovieEntry.COLUMN_VOTE_AVERAGE + " TEXT NOT NULL, "
            + MovieEntry.COLUMN_OVERVIEW + " TEXT NOT NULL, "
            + MovieEntry.COLUMN_POSTER + " TEXT NOT NULL "
            + " );";

    private static final String SQL_DROP_TABLE = "DROP TABLE IS EXISTS " + MovieEntry.TABLE_NAME;

    MoviesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_MOVIES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DROP_TABLE);
        onCreate(db);
    }
}