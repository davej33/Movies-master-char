package com.example.android.movieapp2.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by dnj on 6/19/17.
 */

public final class MovieContract {

    // set content authority, path, base uri
    public static final String CONTENT_AUTHORITY = "com.example.android.movieapp2";
    public static final String MOVIE_PATH = "movie";
    public static final String FAVORITES_PATH = "favorites";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final class MovieEntry implements BaseColumns{

        // table uri
        public static final Uri MOVIE_TABLE_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(MOVIE_PATH)
                .build();

        // table columns
        public static final String MOVIE_TABLE = "movie";
        public static final String _ID = BaseColumns._ID;
        public static final String MOVIE_TMDB_ID = "tmdbid";
        public static final String MOVIE_TITLE = "title";
        public static final String MOVIE_RELEASE_DATE = "date";
        public static final String MOVIE_PLOT = "plot";
        public static final String MOVIE_POSTER = "poster";
        public static final String MOVIE_RATING = "rating";
        public static final String MOVIE_PREFERENCE_TYPE = "type";
        public static final String MOVIE_FAVORITE = "favorite";
        public static final String MOVIE_POPULARITY = "popularity";
        public static final String MOVIE_TRAILER_1 = "trailer1";
        public static final String MOVIE_TRAILER_2 = "trailer2";
        public static final String MOVIE_TRAILER_3 = "trailer3";

    }

}
