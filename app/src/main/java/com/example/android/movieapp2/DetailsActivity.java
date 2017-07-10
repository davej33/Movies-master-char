package com.example.android.movieapp2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class DetailsActivity extends AppCompatActivity implements DetailFragment.OnFragmentInteractionListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Intent intent = getIntent();
        String localID = intent.getStringExtra(getString(R.string.local_id_key));
        String title = intent.getStringExtra(getString(R.string.movie_title_key));
        getSupportFragmentManager().beginTransaction()
                .add(R.id.detail_container, new DetailFragment().newInstance(localID, title)).commit();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
    Log.i("DetailActivity", "Fragment Interaction Uri: " + uri);
    }
}
