package com.example.android.movieapp2;

import android.app.ActionBar;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.android.movieapp2.data.MovieContract;
import com.example.android.movieapp2.utils.FavoriteUtils;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DetailFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    // Log tag
    private static final String LOG_TAG = DetailFragment.class.getSimpleName();

    // the fragment initialization parameters
    private final String ARG_LOCAL_ID = "localID";
    private final String ARG_MOVIE_TITLE = "title";
    private String mLocalID;
    private String mTitle; // also used to create ContentValue

    // loader ID
    private static final int DETAIL_LOADER_ID = 111;

    // views
    private TextView mTitleV;
    private TextView mReleaseV;
    private TextView mPlotV;
    private RatingBar mRatingBar;
    private ImageView mPosterV;
    private ToggleButton mFavorite_button;

    // db favorite-state constants
    private static final int FAVORITED = 1;
    private static final int NOT_FAVORITED = 0;

    private OnFragmentInteractionListener mListener;

    public DetailFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment DetailFragment.
     */

    public DetailFragment newInstance(String localDbID, String title) {
        DetailFragment fragment = new DetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_LOCAL_ID, localDbID);
        args.putString(ARG_MOVIE_TITLE, title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mLocalID = getArguments().getString(ARG_LOCAL_ID);
            mTitle = getArguments().getString(ARG_MOVIE_TITLE);
        }

        // SharedPreferences and Listener
//        mPref = PreferenceManager.getDefaultSharedPreferences(getContext()); // instantiate SharedPreferences
//        mPreferenceListener = new SharedPreferences.OnSharedPreferenceChangeListener() { // instantiate SharePrefListener
//            @Override
//            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
//                Log.i(LOG_TAG, "DetFrag SP Changed %%%%%%%%%%%%%%%%: ");
//            }
//        };
//        mPref.registerOnSharedPreferenceChangeListener(mPreferenceListener); // set Listener on SharedPref
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movie_detail, container, false); // inflate detail view

        // get views
        mTitleV = (TextView) view.findViewById(R.id.movie_title);
        mReleaseV = (TextView) view.findViewById(R.id.release_year);
        mPlotV = (TextView) view.findViewById(R.id.plot);
        mRatingBar = (RatingBar) view.findViewById(R.id.ratingBar);
        mPosterV = (ImageView) view.findViewById(R.id.poster_detail_view);
        mFavorite_button = (ToggleButton) view.findViewById(R.id.favorite_checkbox_view);

        // load data from DB
        runLoader();

        // set onClickListener on favorite button
        mFavorite_button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, final boolean b) {
                if(b){
                    ContentValues cv = new ContentValues();
                    cv.put(MovieContract.MovieEntry.MOVIE_FAVORITE, FAVORITED);
                    FavoriteUtils.addFavorite(getContext(), mTitle, mLocalID, cv);
                } else {
                    try {
                        ContentValues cv = new ContentValues();
                        cv.put(MovieContract.MovieEntry.MOVIE_FAVORITE, NOT_FAVORITED);
                        FavoriteUtils.removeFavorite(getContext(), mTitle, mLocalID, cv);
                    } catch (Exception e) {
                        Log.e(LOG_TAG,"error removing favorite: " + mTitle);
                    }
                }
            }
        });
        // Inflate the layout for this fragment
        return view;
    }




    // initialize or restart loader
    private void runLoader() {
        if (getLoaderManager().getLoader(DETAIL_LOADER_ID) != null) {
            getLoaderManager().restartLoader(DETAIL_LOADER_ID, null, this);
        } else {
            getLoaderManager().initLoader(DETAIL_LOADER_ID, null, this);
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] selArgs = {mLocalID};
        Uri singleMovieUri = Uri.parse(MovieContract.MovieEntry.MOVIE_TABLE_URI.toString() + "/" + mLocalID);
        // TODO: how to seach for
        return new CursorLoader(getContext(), singleMovieUri, null, MovieContract.MovieEntry._ID + "=?",
                selArgs, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        data.moveToFirst();
//        mCursor = data;

        // title
        mTitleV.setText(mTitle);

        // release date
        int releaseDateCol = data.getColumnIndex(MovieContract.MovieEntry.MOVIE_RELEASE_DATE);
        int releaseDate = data.getInt(releaseDateCol);
        String mReleaseDate = "(" + String.valueOf(releaseDate) + ")";
        mReleaseV.setText(mReleaseDate);

        // plot
        int plotCol = data.getColumnIndex(MovieContract.MovieEntry.MOVIE_PLOT);
        String mPlot = data.getString(plotCol);
        mPlotV.setText(mPlot);

        // rating bar
        int rateCol = data.getColumnIndex(MovieContract.MovieEntry.MOVIE_RATING);
        float mRating = (float) (data.getDouble(rateCol) / 2);
        mRatingBar.setRating(mRating);

        // favorite state
        if (FavoriteUtils.checkFavorite(getContext(), mTitle)) {
            mFavorite_button.setChecked(true);
        } else {
            mFavorite_button.setChecked(false);
        }

        // poster
        int posterColId = data.getColumnIndex(MovieContract.MovieEntry.MOVIE_POSTER);
        String mPoster = data.getString(posterColId);
        Picasso.with(getContext())
                .load(mPoster)
                .error(R.drawable.error)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .resize(MovieAdapter.getmImageWidth(), MovieAdapter.getmImageHeight())
                .placeholder(R.drawable.placeholder_detail_view)
                .centerCrop()
                .into(mPosterV);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onDestroy() {

        super.onDestroy();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
