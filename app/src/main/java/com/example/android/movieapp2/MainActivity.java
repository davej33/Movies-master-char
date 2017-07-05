package com.example.android.movieapp2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
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
        SharedPreferences.OnSharedPreferenceChangeListener,
MovieAdapter.ListItemClickListener{

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private MovieAdapter mAdapter;
    private static final int LANDSCAPE_COLUMNS = 3;
    private static final int PORTRAIT_COLUMNS = 2;
    private static final int LOADER_ID = 300;

    // TODO: fetch more results when scrolled to end

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);

    }

    private void displayData() {
        if(getSupportLoaderManager().getLoader(LOADER_ID)!=null){
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
        switch(id){
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
        if(getResources().getConfiguration().orientation  == Configuration.ORIENTATION_PORTRAIT) {
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
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.w(LOG_TAG, "SP-Changed");
        SyncUtils.syncImmediately(this);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                displayData();
            }
        },1000);
    }

    @Override
    public void onListItemClick(int clickedItemIndex) {
        Intent intent = new Intent(this, DetailsActivity.class);
        String movieID = mAdapter.getSelectedMovieDbID(clickedItemIndex);
        intent.putExtra("movieId", movieID);
        startActivity(intent);
    }

}
