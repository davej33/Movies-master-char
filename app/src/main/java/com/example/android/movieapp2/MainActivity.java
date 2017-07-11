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
    private static final String POPULAR_VALUE = "popularity.desc";
    private static final String RATING_VALUE = "vote_average.desc";
    private static final String FAVORITES_VALUE = "favorites";
    private static final String FAVORITED_DB_VALUE = "1";

    private MovieAdapter mAdapter;
    private static final int LANDSCAPE_COLUMNS = 3;
    private static final int PORTRAIT_COLUMNS = 2;
    private static final int LOADER_ID = 100;
    private boolean mSortPrefChanged = false;
    private static boolean mFavoriteChanged = false;
    private SharedPreferences.OnSharedPreferenceChangeListener mListener;
    private SharedPreferences mPref;
    private String mSortValue = "popularity.desc";
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
        mPref = PreferenceManager.getDefaultSharedPreferences(this);
        mListener = new SharedPreferences.OnSharedPreferenceChangeListener() { // instantiated listener to try to prevent garbage collection
            // because listener not being triggered after first change of preference. Still doesn't, so added mSortPrefChanged and mFavoriteChanged
            // to provide listening function.
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                Log.i(LOG_TAG, "MA-SP run");
                // compare default value to current value to determine if SP actually changed. This is to prevent
                // the syncImmediately method from running when calling Settings for first time
                if (key.equals(getString(R.string.pref_sort_key))) {
                    String prefValue = sharedPreferences.getString(key, null); // get value of changed SP
                    if (prefValue != null && !prefValue.equals(mSortValue)) {
                        mSortValue = prefValue;
                        mSortPrefChanged = true; // set test variable to true so activity will sync onStart()
                    }

                } else {
                    mFavoriteChanged = true;
                }
            }
        };
        mPref.registerOnSharedPreferenceChangeListener(mListener);
    }

    @Override
    protected void onStart() {

        // check if SP-ChangeListener is null
        if (SharedPreferences.OnSharedPreferenceChangeListener.class.getSimpleName() == null) {
            Log.i(LOG_TAG, "Listener null");
        } else {
            Log.i(LOG_TAG, "MA - onStart(): Listener NOT null");
        }

        // if sort pref has changed, take action on new sort value
        if (mSortPrefChanged) {
            switch (mSortValue) {
                case POPULAR_VALUE:
                    SyncUtils.syncImmediately(this);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            displayData();
                        }
                    }, 1000);
                    mSortPrefChanged = false;
                    mSortValue = getString(R.string.pref_sort_popularity_value);
                    break;
                case RATING_VALUE:
                    SyncUtils.syncImmediately(this);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            displayData();
                        }
                    }, 1000);
                    mSortPrefChanged = false;
                    mSortValue = getString(R.string.pref_sort_rating_value); // TODO: better manage SP change, fix listener.
                    break;
                case FAVORITES_VALUE:
                    if (getSupportLoaderManager().getLoader(LOADER_ID) != null) {
                        getSupportLoaderManager().destroyLoader(LOADER_ID);
                        getSupportLoaderManager().initLoader(LOADER_ID, null, this);
                    } else {
                        getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
                    }
                    mSortPrefChanged = false;
                    mSortValue = getString(R.string.pref_sort_fav_value);
            }
        }

        // refresh display data if SP-Favorites changed
        if (mFavoriteChanged) {
            displayData();
            mFavoriteChanged = false;
        }
        super.onStart();
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

    void checkFavDB() {
        String selection = MovieContract.MovieEntry.MOVIE_FAVORITE + "=?";
        String[] selectionArgs = {"1"};
        Cursor c = this.getContentResolver().query(MovieContract.MovieEntry.MOVIE_TABLE_URI, null, selection, selectionArgs, null);
        if (c != null && c.getCount() > 0 ) {
            c.moveToFirst();
            Log.i(LOG_TAG, "Cursor count: " + c.getCount());

            do {
                int colId = c.getColumnIndex(MovieContract.MovieEntry._ID);
                String id = c.getString(colId);
                int col = c.getColumnIndex(MovieContract.MovieEntry.MOVIE_TITLE);
                String title = c.getString(col);
                int col2 = c.getColumnIndex(MovieContract.MovieEntry.MOVIE_POSTER);
                String p = c.getString(col2);
                int col3 = c.getColumnIndex(MovieContract.MovieEntry.MOVIE_RATING);
                String r = c.getString(col3);
                int col4 = c.getColumnIndex(MovieContract.MovieEntry.MOVIE_RELEASE_DATE);
                String d = c.getString(col4);
                int col5 = c.getColumnIndex(MovieContract.MovieEntry.MOVIE_FAVORITE);
                String f = c.getString(col5);
                Log.i(LOG_TAG, "Favorite ID/Title/poster/rating/date/FAV: " + id + " / " + title + " / " + p + " / " + r + " / " + d + " / " + f);
            } while (c.moveToNext());
            c.close();
        } else {
            Toast.makeText(this, "No Favorited Movies", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {MovieContract.MovieEntry._ID, MovieContract.MovieEntry.MOVIE_TITLE, MovieContract.MovieEntry.MOVIE_POSTER, MovieContract.MovieEntry.MOVIE_FAVORITE};

        switch (mSortValue) {
            case RATING_VALUE:
                String ratingSortOrder = MovieContract.MovieEntry.MOVIE_RATING + " DESC";
                return new CursorLoader(this,
                        MovieContract.MovieEntry.MOVIE_TABLE_URI, projection, null, null, ratingSortOrder);
            case POPULAR_VALUE:
                String popSortOrder = MovieContract.MovieEntry.MOVIE_POPULARITY + " DESC";
                return new CursorLoader(this,
                        MovieContract.MovieEntry.MOVIE_TABLE_URI, projection, null, null, popSortOrder);
            case FAVORITES_VALUE:
                String selection = MovieContract.MovieEntry.MOVIE_FAVORITE + "=?";
                String[] selectionArgs = {FAVORITED_DB_VALUE};
                return new CursorLoader(this, MovieContract.MovieEntry.MOVIE_TABLE_URI, projection, selection, selectionArgs, null);
            default:
                return null;
        }
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
        Log.i(LOG_TAG, "onPause run");
    }

    public static void setmFavoriteChanged(boolean b) {
        mFavoriteChanged = b;
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
        String sourceID = mAdapter.getSelectedMovieTitle(clickedItemIndex);
        intent.putExtra(getString(R.string.local_id_key), localID);
        intent.putExtra(getString(R.string.movie_title_key), sourceID);
        startActivity(intent);
    }


}
