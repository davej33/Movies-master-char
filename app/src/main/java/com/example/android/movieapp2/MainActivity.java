package com.example.android.movieapp2;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.SQLException;
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

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.android.movieapp2.data.MovieContract;
import com.example.android.movieapp2.utils.JsonUtils;
import com.example.android.movieapp2.utils.NetworkUtils;

import org.json.JSONException;


public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        MovieAdapter.ListItemClickListener {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String POPULAR_VALUE = "popularity.desc";
    private static final String RATING_VALUE = "vote_average.desc";
    private static final String FAVORITES_VALUE = "favorites";
    private static final String FAVORITED_DB_VALUE = "1";
    private static final String FETCH_TRAILERS_VALUE = "trailers";
    private static final String FETCH_REVIEWS_VALUE = "reviews";

    private MovieAdapter mAdapter;
    private static final int LANDSCAPE_COLUMNS = 3;
    private static final int PORTRAIT_COLUMNS = 2;
    private static final int LOADER_ID = 100;
    private boolean mSortPrefChanged = false;
    private static boolean mFavoriteChanged = false;
    private static boolean sIsInitialed;
    private SharedPreferences.OnSharedPreferenceChangeListener mListener;
    private SharedPreferences mPref;
    private String mSortValue = "popularity.desc";

    private ContentValues[] mCvTrailers;
    private ContentValues[] mCvReviews;
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
        if (sIsInitialed) {
            displayData();
        } else {
            NetworkUtils.initRequestQueue(this);
            fetchMovies();
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

        // if sort pref has changed, take action on new sort value
        if (mSortPrefChanged) {
            fetchMovies();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    displayData();
                }
            }, 1000);
        }

        // refresh display data if SP-Favorites changed
        if (mFavoriteChanged) {
            displayData();
            mFavoriteChanged = false;
        }
        super.onStart();
    }

    private void fetchMovies() {
        switch (mSortValue) {
            case POPULAR_VALUE:
            case RATING_VALUE:
                StringRequest dataRequest = new StringRequest(NetworkUtils.buildURL(this), new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i(LOG_TAG, "### respsonse ###: " + response);

                        // parse
                        ContentValues[] cv = null;
                        try {
                            cv = JsonUtils.parseJson(response, mSortValue);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        int rowsInserted;
                        try{
                            rowsInserted = getContentResolver().bulkInsert(MovieContract.MovieEntry.MOVIE_TABLE_URI, cv);
                        } catch (SQLException e){
                            Log.e(LOG_TAG, "SQL builInsert error: " + e);
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        checkVolleyError(error);
                        error.printStackTrace();
                    }
                });
                NetworkUtils.addToRequestQueue(dataRequest, mSortValue);
                break;
            default:
                Log.i(LOG_TAG, "fetchMovies() no match");

        }
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
            case R.id.check_fav_db:
                checkFavDB();
                return true;
            case R.id.action_refresh:
                displayData();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    void checkFavDB() {
        String selection = MovieContract.MovieEntry.MOVIE_FAVORITE + "=?";
        String[] selectionArgs = {"1"};
        Cursor c = this.getContentResolver().query(MovieContract.MovieEntry.MOVIE_TABLE_URI, null, selection, selectionArgs, null);
        if (c != null && c.getCount() > 0) {
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
                int col6 = c.getColumnIndex(MovieContract.MovieEntry.MOVIE_TRAILER_1);
                String t1 = c.getString(col6);
                int col7 = c.getColumnIndex(MovieContract.MovieEntry.MOVIE_TRAILER_2);
                String t2 = c.getString(col7);
                int col8 = c.getColumnIndex(MovieContract.MovieEntry.MOVIE_TRAILER_3);
                String t3 = c.getString(col8);
                Log.i(LOG_TAG, "Favorite ID/Title/poster/rating/date/FAV/1/2/3: " + id + " / " + title + " / " + p + " / " + r + " / " + d + " / " + f + " / " + t1 + " / " + t2 + " / " + t3);
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
        String localDbRowID = mAdapter.getSelectedMovieLocalID(clickedItemIndex);
        String movieID = mAdapter.getSelectedMovieSourceID(clickedItemIndex);
        String title = mAdapter.getSelectedMovieTitle(clickedItemIndex);

        fetchTrailersAndReviews(movieID);
        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra(getString(R.string.local_id_key), localDbRowID);
        intent.putExtra(getString(R.string.movie_title_key), title);
        startActivity(intent);
    }

    private void fetchTrailersAndReviews(String movieID) {


        StringRequest fetchTrailers = new StringRequest(NetworkUtils.buildMovieDetailUrl(this, movieID), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                ContentValues[] cv = new ContentValues[1];
                try {
                    cv = JsonUtils.parseJson(response, FETCH_TRAILERS_VALUE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mCvTrailers = cv;
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                checkVolleyError(error);
                Log.e(LOG_TAG, "ErrorListener error" + error);
            }
        });

        NetworkUtils.addToRequestQueue(fetchTrailers, FETCH_TRAILERS_VALUE);

        StringRequest reviewRequest = new StringRequest(NetworkUtils.buildReviewUrlString(this, movieID), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                ContentValues[] cv = null;
                try {
                    cv = JsonUtils.parseJson(response, FETCH_REVIEWS_VALUE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                mCvReviews = cv;
                Log.i(LOG_TAG, "Review Size: " + mCvReviews.length);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                checkVolleyError(error);
                Log.e(LOG_TAG, "Error fetching reviews" + error);
            }
        });

        NetworkUtils.addToRequestQueue(reviewRequest, FETCH_REVIEWS_VALUE);

    }

    public static void setDbIsInitialized(boolean b){
        sIsInitialed = b;
    }


    private void checkVolleyError(VolleyError error){
        if (error instanceof TimeoutError || error instanceof NoConnectionError) {
            Toast.makeText(getApplicationContext(), "No Network Connection", Toast.LENGTH_SHORT).show();

        } else if (error instanceof AuthFailureError) {
            Toast.makeText(getApplicationContext(), "Authentication Error!", Toast.LENGTH_SHORT).show();
        } else if (error instanceof ServerError) {
            Toast.makeText(getApplicationContext(), "Server Side Error!", Toast.LENGTH_SHORT).show();
        } else if (error instanceof NetworkError) {
            Toast.makeText(getApplicationContext(), "Network Error!", Toast.LENGTH_SHORT).show();
        } else if (error instanceof ParseError) {
            Toast.makeText(getApplicationContext(), "Parse Error!", Toast.LENGTH_SHORT).show();
        }
    }

}
