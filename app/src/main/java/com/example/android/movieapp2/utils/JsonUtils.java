package com.example.android.movieapp2.utils;

import android.content.ContentValues;
import android.util.Log;

import com.example.android.movieapp2.data.MovieContract.MovieEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by dnj on 6/19/17.
 */

public final class JsonUtils {

    public static ContentValues[] parseJson(String bufferedString) throws JSONException {

        // Api keys
        final String TITLE_KEY = "title";
        final String RELEASE_DATE_KEY = "release_date";
        final String PLOT_KEY = "overview";
        final String POPULARITY_KEY = "popularity";
        final String RATING_KEY = "vote_average";
        final String POSTER_KEY = "poster_path";
        final String ID_KEY = "id";

        // image url
        final String BASE_IMAGE_URL = "http://image.tmdb.org/t/p/w185";

        // create json object and array from buffered stream
        JSONObject root = new JSONObject(bufferedString);
        JSONArray data = root.getJSONArray("results");

        // declare ContentValues[] object using length of array
        ContentValues[] contentValues = new ContentValues[data.length()];

        // iterate through each movie to get data
        for (int i = 0; i < data.length(); i++) {
            JSONObject element = data.getJSONObject(i);

            String title = element.getString(TITLE_KEY);
            String release_date = element.getString(RELEASE_DATE_KEY);
            String plot = element.getString(PLOT_KEY);
            double popularity = element.getDouble(POPULARITY_KEY);
            double rating = element.getDouble(RATING_KEY);
            String poster = BASE_IMAGE_URL + element.getString(POSTER_KEY);
            int id = element.getInt(ID_KEY);

            // add key/values into ContentValues object
            ContentValues cv = new ContentValues();
            cv.put(MovieEntry.MOVIE_TITLE, title);
            cv.put(MovieEntry.MOVIE_RELEASE_DATE, release_date);
            cv.put(MovieEntry.MOVIE_PLOT, plot);
            cv.put(MovieEntry.MOVIE_POPULARITY, popularity);
            cv.put(MovieEntry.MOVIE_RATING, rating);
            cv.put(MovieEntry.MOVIE_POSTER, poster);
            cv.put(MovieEntry.MOVIE_TMDB_ID, id);

            // add ContentValues object to ContentValues[]
            contentValues[i] = cv;
        }
        String s = contentValues[0].getAsString(MovieEntry.MOVIE_TITLE);
        Log.w("JsonParse", "Title: " + s);
        return contentValues;
    }
}
