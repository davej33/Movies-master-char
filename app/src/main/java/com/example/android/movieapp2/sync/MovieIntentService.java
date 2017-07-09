package com.example.android.movieapp2.sync;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

/**
 * Created by dnj on 6/19/17.
 */

public final class MovieIntentService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor
     */
    public MovieIntentService() {
        super("MovieIntentService");
//        Log.w("IntentService", "Service run");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        SyncTask.syncData(getApplicationContext());
    }
}
