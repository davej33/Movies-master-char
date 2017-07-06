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
        String localID = intent.getStringExtra(getString(R.string.local_id_key));
        String sourceID = intent.getStringExtra(getString(R.string.source_id_key));
        getSupportFragmentManager().beginTransaction()
                .add(R.id.detail_container, new MovieDetailFragment().newInstance(localID, sourceID)).commit();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        MainActivity.onDestroy();
    Log.i("DetailActivity", "Fragment Interaction Uri: " + uri);
    }
}
