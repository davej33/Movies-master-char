package com.example.android.movieapp2.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.movieapp2.data.MovieContract.MovieEntry;

/**
 * Created by dnj on 6/19/17.
 */

public final class MovieDbHelper extends SQLiteOpenHelper {

    // vars
    public static final String MOVIE_DB_NAME = "movies.db";
    public static final int DB_VERSION = 1;


    public MovieDbHelper(Context context) {
        super(context, MOVIE_DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_MOVIE_TABLE = "CREATE TABLE " + MovieEntry.MOVIE_TABLE + " (" +
                MovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MovieEntry.MOVIE_TMDB_ID + " INTEGER NOT NULL, " +
                MovieEntry.MOVIE_TITLE + " TEXT NOT NULL, " +
                MovieEntry.MOVIE_RELEASE_DATE + " TEXT NOT NULL, " +
                MovieEntry.MOVIE_PLOT + " TEXT NOT NULL, " +
                MovieEntry.MOVIE_POPULARITY + " FLOAT NOT NULL, " +
                MovieEntry.MOVIE_RATING + " FLOAT NOT NULL, " +
                MovieEntry.MOVIE_PREFERENCE_TYPE + " INTEGER, " +
                MovieEntry.MOVIE_FAVORITE + " INTEGER DEFAULT 0, " +
                MovieEntry.MOVIE_POSTER + " TEXT, " +
                MovieEntry.MOVIE_TRAILER_1 + " TEXT, " +
                MovieEntry.MOVIE_TRAILER_2 + " TEXT, " +
                MovieEntry.MOVIE_TRAILER_3 + " TEXT, " +
                "UNIQUE (" + MovieEntry.MOVIE_TMDB_ID + ") ON CONFLICT IGNORE);";

        db.execSQL(CREATE_MOVIE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + MovieEntry.MOVIE_TABLE);
        onCreate(db);
    }
}
