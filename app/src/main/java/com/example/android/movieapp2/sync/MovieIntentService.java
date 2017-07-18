package com.example.android.movieapp2.sync;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

/**
 * Created by dnj on 6/19/17.
 */

public final class MovieIntentService extends IntentService {
    private static final String FETCH_TYPE = "fetch";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor
     */
    public MovieIntentService() {
        super("MovieIntentService");
//        Log.w("IntentService", "Service run");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String fetchType = intent.getStringExtra(FETCH_TYPE);
        SyncTask.syncData(getApplicationContext(), fetchType);
    }
}
