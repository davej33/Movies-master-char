package com.example.android.movieapp2;

import android.app.ActionBar;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.android.movieapp2.data.MovieContract;

public class DetailsActivity extends AppCompatActivity implements DetailFragment.OnFragmentInteractionListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        ActionBar bar = getActionBar();
        if (bar != null) bar.setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        String localID = intent.getStringExtra(getString(R.string.local_id_key));
        String title = intent.getStringExtra(getString(R.string.movie_title_key));
        getSupportFragmentManager().beginTransaction()
                .add(R.id.detail_container, new DetailFragment().newInstance(localID, title)).commit();
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
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.check_fav_db:
                checkFavDB();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public void onFragmentInteraction(Uri uri) {
    Log.i("DetailActivity", "Fragment Interaction Uri: " + uri);
    }

    void checkFavDB() {
        String selection = MovieContract.MovieEntry.MOVIE_FAVORITE + "=?";
        String[] selectionArgs = {"1"};
        Cursor c = this.getContentResolver().query(MovieContract.MovieEntry.MOVIE_TABLE_URI, null, selection, selectionArgs, null);
        if (c != null && c.getCount() > 0 ) {
            c.moveToFirst();
            Log.i("DetailActivity", "Cursor count: " + c.getCount());

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
                Log.i("DetailActivity", "Favorite ID/Title/poster/rating/date/FAV: " + id + " / " + title + " / " + p + " / " + r + " / " + d + " / " + f);
            } while (c.moveToNext());
            c.close();
        } else {
            Toast.makeText(this, "No Favorited Movies", Toast.LENGTH_SHORT).show();
        }
    }
}
