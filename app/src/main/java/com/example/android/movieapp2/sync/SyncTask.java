package com.example.android.movieapp2.sync;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.util.Log;

import com.example.android.movieapp2.data.MovieContract;
import com.example.android.movieapp2.utils.NetworkUtils;

import org.json.JSONException;

/**
 * Created by dnj on 6/19/17.
 */

public final class SyncTask {

    static int test;

    static void syncData(Context context) {

        // get network data
        ContentValues[] cv = null;
        try {
            cv = NetworkUtils.fetchData(context);
            test++;
            Log.i("SyncTask","Network Utils Run Count: " + test);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i("SyncTask", "cv size " + cv.length );
        try {
            if (cv.length > 0) {
                int rowsDeleted = context.getContentResolver().delete(MovieContract.MovieEntry.MOVIE_TABLE_URI, null, null);
                int rowsInserted = context.getContentResolver().bulkInsert(MovieContract.MovieEntry.MOVIE_TABLE_URI, cv);
                Log.i("SyncTask", "deleted/inserted: " + rowsDeleted + "/" + rowsInserted);
            }
        }catch (SQLException e) {
            Log.i("SyncTask", "SQL error: " + e );
        }

//        try {
//            Cursor cursor = context.getContentResolver().query(MovieContract.MovieEntry.MOVIE_TABLE_URI,
//                    null, null, null, null);
//            cursor.moveToFirst();
//            Log.w("SyncTask", "Cursor count: " + cursor.getCount());
//            int col = cursor.getColumnIndex(MovieContract.MovieEntry.MOVIE_TITLE);
//            String s;
//            try{
//            s = cursor.getString(col);
//                Log.w("SyncTask", "DB movie #1 = " + s);}
//            catch (Exception e){
//                Log.e("SyncTask", "Error: " + e);
//            }
//
//            cursor.close();
//        }catch(SQLException e){
//            Log.i("SyncTask", "SQL error 2: " + e);
//        }

    }
}
