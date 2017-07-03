package com.example.android.movieapp2.utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

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

public final class NetworkUtils {

    private static final String LOG_TAG = NetworkUtils.class.getSimpleName();

    public static ContentValues[] fetchData(Context context) throws JSONException {

        SharedPreferences pref = android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(context);
        String sort = pref.getString(context.getString(R.string.pref_sort_key), context.getString(R.string.pref_sort_default));
        Log.i(LOG_TAG, "Sort: " + sort);
        
        // build URI using sharedPreferences
        Uri uri = Uri.parse(context.getString(R.string.network_query_base_url)).buildUpon()
                .appendQueryParameter(context.getString(R.string.pref_sort_key), sort)
                .appendQueryParameter(context.getString(R.string.api_code_key), BuildConfig.MOVIE_API_KEY)
                .build();

        Log.i(LOG_TAG, "URL: " + uri);

        // connect to movie db
        URL url = null;
        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Malformed URL: " + e);
        }

        // capture and buffer network response
        String bufferedString = null;
        if (url != null) {
            try {
                HttpURLConnection connect = (HttpURLConnection) url.openConnection();
                InputStream inputStream = connect.getInputStream();
                Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
                if (scanner.hasNext()) {
                    bufferedString = scanner.next();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // parse response as json
        ContentValues[] cv = null;
        try {
            cv = JsonUtils.parseJson(bufferedString);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "JSON parse error: " + e);
        }

        return cv;

    }
}
