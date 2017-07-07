package com.example.android.movieapp2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.android.movieapp2.data.MovieContract;
import com.example.android.movieapp2.sync.SyncUtils;


public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        MovieAdapter.ListItemClickListener {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private MovieAdapter mAdapter;
    private static final int LANDSCAPE_COLUMNS = 3;
    private static final int PORTRAIT_COLUMNS = 2;
    private static final int LOADER_ID = 300;
    private boolean mSortPrefChanged = false;
    private static boolean mFavoriteChanged = false;
    private String mSortValue;
    private SharedPreferences.OnSharedPreferenceChangeListener mListener;
    // TODO: fetch more results when scrolled to end

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            mSortValue = savedInstanceState.getString(getString(R.string.pref_sort_key));
        }
        // get image dimensions
        int imageWidth = getWindowWidth();
        int imageHeight = (int) (imageWidth * 1.5);
        Log.w(LOG_TAG, "1. h / w " + imageHeight + " / " + imageWidth);

        // setup shared preferences
        setupSharedPreferences();

        // intitiate and/or display data
        if (SyncUtils.isInitialized()) {
            displayData();
        } else {
            SyncUtils.initialize(this);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    displayData();
                }
            }, 1000);
        }

        // set adapter and gridLayoutManager to recyclerview
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            recyclerView.setLayoutManager(new GridLayoutManager(this, PORTRAIT_COLUMNS));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(this, LANDSCAPE_COLUMNS));
        }

        mAdapter = new MovieAdapter(this, imageWidth, imageHeight, this);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setHasFixedSize(true);
    }

    private void setupSharedPreferences() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        mListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                Log.i(LOG_TAG,"MA-SP run");
                // attempt to compare default value to current value to determine if SP actually changed.
                if (key.equals(getString(R.string.pref_sort_key))) {
                    String prefValue = sharedPreferences.getString(key, null); // get value of changed SP
                    Log.i(LOG_TAG, "MA-SP value: " + prefValue);
                    if (prefValue.equals(mSortValue)) {
                        Log.w(LOG_TAG, "Listener run. SP-Unchanged");
                    } else {
                        Log.i(LOG_TAG, "MA-SP sort changed: " + prefValue);
                        mSortValue = prefValue;
                        mSortPrefChanged = true; // set test variable to true so activity will sync onStart()
                    }
                } else {
                    Log.i(LOG_TAG, "MA-SP key changed %%%%%%%%%%%%: " + key);
                    mFavoriteChanged = true;
                }
            }
        };
        pref.registerOnSharedPreferenceChangeListener(mListener);
        if(SharedPreferences.OnSharedPreferenceChangeListener.class.getSimpleName() == null) {
            Log.i(LOG_TAG, "Listener null");
        } else {
            Log.i(LOG_TAG, "Listener not null" + SharedPreferences.OnSharedPreferenceChangeListener.class.getSimpleName());
        }
        mSortValue = pref.getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_default)); // set current value of sort pref
        Log.i(LOG_TAG, "setupSP() mSortValue = " + mSortValue);
    }

    private void displayData() {
        if (getSupportLoaderManager().getLoader(LOADER_ID) != null) {
            getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
        } else {
            getSupportLoaderManager().initLoader(LOADER_ID, null, this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                MovieContract.MovieEntry.MOVIE_TABLE_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        data.moveToFirst();
        mAdapter.swapCursor(data);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    public int getWindowWidth() {

        int width;
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            width = (metrics.widthPixels) / 2;
        } else {
            width = 1 + (metrics.widthPixels) / 3;
        }
        return width;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(mListener);


    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(LOG_TAG,"onPause run");
    }

    public static void setmFavoriteChanged(boolean s) {
        mFavoriteChanged = s;
    }

    //    @Override
//    protected void onResume() {
//        super.onResume();
//        if(mFavoriteChanged){
//            displayData();
//            mFavoriteChanged = false;
//        }
//
//    }

    @Override
    protected void onStart() {
        if(SharedPreferences.OnSharedPreferenceChangeListener.class.getSimpleName() == null) Log.i(LOG_TAG, "Listener null");
        if (mSortPrefChanged) {
            SyncUtils.syncImmediately(this);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    displayData();
                }
            }, 1000);

            mSortPrefChanged = false;
        }
        if (mFavoriteChanged) {
            displayData();
            mFavoriteChanged = false;
        }
        super.onStart();
    }

//    @Override
//    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
//        Log.i(LOG_TAG,"MA-SP run");
//        // attempt to compare default value to current value to determine if SP actually changed.
//        if (key.equals(getString(R.string.pref_sort_key))) {
//            String prefValue = sharedPreferences.getString(key, null); // get value of changed SP
//            Log.i(LOG_TAG, "MA-SP value: " + prefValue);
//            if (prefValue.equals(mSortValue)) {
//                Log.w(LOG_TAG, "Listener run. SP-Unchanged");
//            } else {
//                Log.i(LOG_TAG, "MA-SP sort changed: " + prefValue);
//                mSortValue = prefValue;
//                mSortPrefChanged = true; // set test variable to true so activity will sync onStart()
//            }
//        } else {
//            Log.i(LOG_TAG, "MA-SP key changed %%%%%%%%%%%%: " + key);
//            mFavoriteChanged = true;
//        }
//    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(getString(R.string.pref_sort_key), mSortValue);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onListItemClick(int clickedItemIndex) {
        Intent intent = new Intent(this, DetailsActivity.class);
        String localID = mAdapter.getSelectedMovieLocalID(clickedItemIndex);
        String sourceID = mAdapter.getSelectedMovieSourceID(clickedItemIndex);
        intent.putExtra(getString(R.string.local_id_key), localID);
        intent.putExtra(getString(R.string.source_id_key), sourceID);
        startActivity(intent);
    }


}
