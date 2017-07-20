package com.example.android.movieapp2.utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.android.movieapp2.BuildConfig;
import com.example.android.movieapp2.R;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by dnj on 6/19/17.
 */

public class NetworkUtils {

    private static final String LOG_TAG = NetworkUtils.class.getSimpleName();
    private static final String TAG = "default";

    private static RequestQueue sRequestQueue;

    public static void initRequestQueue(Context context) {
        if (sRequestQueue == null) {
            sRequestQueue = Volley.newRequestQueue(context);
        }
    }

    public static void addToRequestQueue(Request<String> request, String tag) {
        request.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        sRequestQueue.add(request);
    }

    public static void cancelRequest(Request<String> request, String tag) {
        request.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        sRequestQueue.cancelAll(request);
    }

    public static String buildURL(Context context) {
        SharedPreferences pref = android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(context);
        String sort = pref.getString(context.getString(R.string.pref_sort_key), context.getString(R.string.pref_sort_default));

        // build URI using sharedPreferences
        Uri uri = Uri.parse(context.getString(R.string.query_base_url)).buildUpon()
                .appendQueryParameter(context.getString(R.string.pref_sort_key), sort)
                .appendQueryParameter(context.getString(R.string.api_code_key), BuildConfig.MOVIE_API_KEY)
                .build();

        // build Url
        URL url = null;
        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Malformed URL: " + e);
        }
        Log.e(LOG_TAG, "Movies URL: " + url);
        return uri.toString();
    }

    public static String buildMovieDetailUrl(Context context, String id) {
        Uri uri = Uri.parse(context.getString(R.string.trailer_base_url) + id + "/videos?").buildUpon()
                .appendQueryParameter(context.getString(R.string.api_code_key), BuildConfig.MOVIE_API_KEY)
                .build();
        // build Url
        URL url = null;
        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Malformed URL: " + e);
        }
        Log.e(LOG_TAG, "Get Trailers URL: " + url);
        return uri.toString();

    }

    public static String buildReviewUrlString(Context context, String id) {
        Uri uri = Uri.parse("https://api.themoviedb.org/3/movie/" + id + "/reviews?").buildUpon()
                .appendQueryParameter(context.getString(R.string.api_code_key), BuildConfig.MOVIE_API_KEY)
                .build();

        URL url = null;
        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Malformed URL: " + e);
        }
        Log.e(LOG_TAG, "Get Reviews URL: " + url);

        return uri.toString();
    }

}