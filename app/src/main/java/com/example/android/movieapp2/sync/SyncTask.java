package com.example.android.movieapp2.sync;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.util.Log;

import com.example.android.movieapp2.data.MovieContract;
import com.example.android.movieapp2.utils.NetworkUtils;

import org.json.JSONException;

/**
 * Created by dnj on 6/19/17.
 */

public final class SyncTask {

    private static final String SORT_QUERY = "sort";
    private static String mTrailerUpdateID;


    static void syncData(Context context, String fetchType) {
        Log.i("SyncTask", "fetchType: " + fetchType);

        // get network data
        ContentValues[] cv = null;
        try {
            cv = NetworkUtils.fetchData(context, fetchType);
            int test = 0;
            Log.i("SyncTask", "Network Utils Run Count: " + ++test);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            if (fetchType.equals(SORT_QUERY) && cv.length > 0) {
                int rowsInserted = context.getContentResolver().bulkInsert(MovieContract.MovieEntry.MOVIE_TABLE_URI, cv);
                Log.i("SyncTask", "inserted: " + rowsInserted);
            } else {
                Uri updateUri = Uri.parse(MovieContract.MovieEntry.MOVIE_TABLE_URI + "/" + mTrailerUpdateID);
                int updateRow = context.getContentResolver().update(updateUri,
                        cv[0], null,null);
                Log.i("SyncTask", "inserted: " + updateRow);
            }
        } catch (SQLException e) {
            Log.i("SyncTask", "SQL error: " + e);
        }


    }

    public static void setmTrailerUpdateID(String id){
        mTrailerUpdateID = id;
    }
}
