package com.example.android.movieapp2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class DetailsActivity extends AppCompatActivity implements MovieDetailFragment.OnFragmentInteractionListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Intent intent = getIntent();
        String id = intent.getStringExtra("movieId");
        Log.i("DetailsActivity", "MovieID from Intent: " + id);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.detail_container, new MovieDetailFragment().newInstance(id)).commit();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
    Log.i("DetailActivity", "Fragment Interaction Uri: " + uri);
    }
}
