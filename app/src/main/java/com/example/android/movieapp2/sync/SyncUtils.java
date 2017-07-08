package com.example.android.movieapp2.sync;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import com.example.android.movieapp2.data.MovieContract;

/**
 * Created by dnj on 6/19/17.
 */

public final class SyncUtils {

    public static boolean sIsInitialed;

    public static void initialize(final Context context){

        // return to main activity if already initialized
        if(sIsInitialed) return;
        sIsInitialed = true;


        // check provider for entries in DB
        Thread providerCheck = new Thread(new Runnable() {
            @Override
            public void run() {
                String[] projection = {MovieContract.MovieEntry._ID};
                Cursor cursor = context.getContentResolver().query(MovieContract.MovieEntry.MOVIE_TABLE_URI,
                        projection,null,null,null);
                if(cursor == null || cursor.getCount() == 0){
                    syncImmediately(context);
                }
                if(cursor != null ) cursor.close();
            }
        });
        providerCheck.run();
    }

    public static void syncImmediately(Context context) {
        Log.w("SyncUtils", "syncImm run");
        Intent intent = new Intent(context, MovieIntentService.class);
        context.startService(intent);
    }


    public static boolean isInitialized(){
        return sIsInitialed;
    }
}
