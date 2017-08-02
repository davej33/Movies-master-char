package com.example.android.movieapp2.utils;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.example.android.movieapp2.DetailFragment;
import com.example.android.movieapp2.ReviewAdapter;
import com.example.android.movieapp2.data.MovieContract.MovieEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by dnj on 6/19/17.
 */

public final class JsonUtils {

    private static final String POPULAR_VALUE = "popular";
    private static final String RATING_VALUE = "top_rated";
    private static final String FETCH_TRAILERS_VALUE = "trailers";
    private static final String FETCH_REVIEWS_VALUE = "reviews";
    private static String SEARCH_TYPE;

    private static ArrayList<String> sTrailerList = new ArrayList<>();
    private static ContentValues[] sCvReview;


    public static ContentValues[] parseJson(String bufferedString, String type) throws JSONException {


        // Query Api keys
        final String TITLE_KEY = "title";
        final String RELEASE_DATE_KEY = "release_date";
        final String PLOT_KEY = "overview";
        final String POPULARITY_KEY = "popularity";
        final String RATING_KEY = "vote_average";
        final String POSTER_KEY = "poster_path";
        final String ID_KEY = "id";
        final String AUTHOR_KEY = "author";
        final String REVIEW_KEY = "content";
        final String BASE_IMAGE_URL = "http://image.tmdb.org/t/p/w185";

        // Trailer Api keys
        final String VIDEO_ID = "key";
        final String TRAILER_VALUE = "Trailer";
        final String TYPE_KEY = "type";

        // set search type class var
        SEARCH_TYPE = type;

        // create json object and array from buffered stream
        JSONObject root = new JSONObject(bufferedString);
        JSONArray data = root.getJSONArray("results");

        // declare ContentValues[] object using length of array
        ContentValues[] contentValues = new ContentValues[data.length()];
        sTrailerList.clear();

        // iterate through each movie to get data
        for (int i = 0; i < data.length(); i++) {
            JSONObject element = data.getJSONObject(i);
            switch (type) {
                case POPULAR_VALUE:
                case RATING_VALUE:

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
                    contentValues[i] = cv; // add ContentValues to ContentValues[]
                    break;
                case FETCH_TRAILERS_VALUE:
                    Log.i("JsonUtils", "trailer fetch run check: ");
                    String videoType = element.getString(TYPE_KEY); // get the String value at key "type"
                    if (videoType.equals(TRAILER_VALUE)) { // if value match "Trailer"
                        String videoID = element.getString(VIDEO_ID); // get the youtube trailer id
                        addIdToArrayList(videoID); // add id to array list using helper
                    }
                    break;
                case FETCH_REVIEWS_VALUE:
                    String author = element.getString(AUTHOR_KEY);
                    String review = element.getString(REVIEW_KEY);

                    ContentValues cvReview = new ContentValues();
                    cvReview.put(AUTHOR_KEY, author);
                    cvReview.put(REVIEW_KEY, review);

                    contentValues[i] = cvReview;

            }

        }

        // return ContentValues[] if array list is empty
        if (SEARCH_TYPE.equals(POPULAR_VALUE) || SEARCH_TYPE.equals(RATING_VALUE)) {
            return contentValues;
        } else if (SEARCH_TYPE.equals(FETCH_REVIEWS_VALUE)) {
            ReviewAdapter.setReviewArray(contentValues);
            return contentValues;
        } else {
            DetailFragment.setDetailFragTrailerList(sTrailerList);
            return convertArrayListToContentValue(); // convert array list to a ContentValue[] and return
        }


}

    public static ArrayList getTrailers() {
        Log.i("JsonUtils", "trailerList count: " + sTrailerList.size());
        return sTrailerList;
    }

    private static ContentValues[] convertArrayListToContentValue() {

        ContentValues cv = new ContentValues();
        for (int i = 0; i < sTrailerList.size(); i++) {
            String s = sTrailerList.get(i); // get String at each iteration
            switch (i) {
                case 0:
                    cv.put(MovieEntry.MOVIE_TRAILER_1, s); // put value in ContentValue with matching local DB column name
                    break;
                case 1:
                    cv.put(MovieEntry.MOVIE_TRAILER_2, s);
                    break;
                case 2:
                    cv.put(MovieEntry.MOVIE_TRAILER_3, s);
                    break;
                default:
                    Log.i("Tag", "Only store up to three trailers");
            }
        }

        ContentValues[] cvArray = new ContentValues[1]; // instantiate ContentValue[] with single ContentValue
        cvArray[0] = cv; // add ContentValue to ContentValue[]
        return cvArray;
    }

    private static void addIdToArrayList(String videoID) {
        Log.i("JsonUtils", "Added trailer ID to list: " + videoID);
        sTrailerList.add(videoID);
    }

    public static ContentValues[] getReviews() {
        return sCvReview;
    }
}

