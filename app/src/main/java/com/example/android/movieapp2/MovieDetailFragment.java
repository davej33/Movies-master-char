package com.example.android.movieapp2;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
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
import com.example.android.movieapp2.data.MovieDbHelper;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Set;

import butterknife.BindView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MovieDetailFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MovieDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MovieDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = MovieDetailFragment.class.getSimpleName();
    // the fragment initialization parameters
    private final String ARG_LOCAL_ID = "localID";
    private final String ARG_SOURCE_ID = "sourceID";
    private static final int DETAIL_LOADER_ID = 111;
    private String mLocalID;
    private String mSourceID;
    private Uri mSingleMovieUri;



    // views
    private TextView mTitleV;
    private TextView mReleaseV;
    private TextView mPlotV;
    private RatingBar mRatingBar;
    private ImageView mPosterV;
    private ToggleButton mFavorite_button;
    private int mUpdatedFavoriteState;


    private OnFragmentInteractionListener mListener;

    public MovieDetailFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MovieDetailFragment.
     */

    public MovieDetailFragment newInstance(String localDbID, String sourceID) {
        MovieDetailFragment fragment = new MovieDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_LOCAL_ID, localDbID);
        args.putString(ARG_SOURCE_ID, sourceID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mLocalID = getArguments().getString(ARG_LOCAL_ID);
            mSourceID = getArguments().getString(ARG_SOURCE_ID);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movie_detail, container, false);

        runLoader();

        mTitleV = (TextView) view.findViewById(R.id.movie_title);
        mReleaseV = (TextView) view.findViewById(R.id.release_year);
        mPlotV = (TextView) view.findViewById(R.id.plot);
        mRatingBar = (RatingBar) view.findViewById(R.id.ratingBar);
        mPosterV = (ImageView) view.findViewById(R.id.poster_detail_view);
        mFavorite_button = (ToggleButton) view.findViewById(R.id.favorite_checkbox_view);

        // set onClickListener on favorite button
        mFavorite_button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, final boolean b) {
                mUpdatedFavoriteState = (b) ? 1 : 0;
                final Thread updateFavState = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String[] selArgs = {mLocalID};
                        ContentValues cv = new ContentValues();
                        cv.put(MovieContract.MovieEntry.MOVIE_FAVORITE, mUpdatedFavoriteState);
                        int check = getContext().getContentResolver().update(mSingleMovieUri, cv, MovieContract.MovieEntry._ID + "=?",
                                selArgs);
                        if (check == 1) {
                            editFavoritesArray(b);
                            onButtonPressed(mSingleMovieUri);
                            Toast.makeText(getContext(), "Favorites updated", Toast.LENGTH_SHORT).show();
                            //TODO: Doesn't update MainActivity
                        } else {
                            Toast.makeText(getContext(), "Error updating favorites", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                updateFavState.run();
            }
        });
        // Inflate the layout for this fragment
        return view;
    }

    private void editFavoritesArray(boolean state) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext()); // get SharedPreference object
        Set<String> setList = pref.getStringSet(getString(R.string.pref_fav_key), null); // get Favorites Set<> from SP
        Log.i(LOG_TAG, "SourceID: " + mSourceID);
        if(setList == null){
            setList = new HashSet<>();
            setList.add(mSourceID);
            Log.i(LOG_TAG, "T 1: " + setList.size());
        } else if (state) {
            setList.add(mSourceID); // add new movie to favorite ArrayList
            Log.i(LOG_TAG, "T 2: " + setList.size());
        } else {
            Iterator<String> iterator = setList.iterator();
            while (iterator.hasNext()) {
                if (iterator.next().equals(mSourceID)) {
                    iterator.remove();
                }
            }
            Log.i(LOG_TAG, "T 3: " + setList.size());
        }
        SharedPreferences.Editor editor = pref.edit(); // get SharedPreference Editor object
        editor.remove(getString(R.string.pref_fav_key)); // remove SharedPreference favorite Set
        editor.putStringSet(getString(R.string.pref_fav_key), setList); // put updated ArrayList in SharedPreferences as Set
        editor.apply();
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
        mSingleMovieUri = Uri.parse(MovieContract.MovieEntry.MOVIE_TABLE_URI.toString() + "/" + mLocalID);
        return new CursorLoader(getContext(), mSingleMovieUri, null, MovieContract.MovieEntry._ID + "=?",
                selArgs, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.i(LOG_TAG, "Details Cursor Count: " + data.getCount());
        data.moveToFirst();

        // title
        int titleColId = data.getColumnIndex(MovieContract.MovieEntry.MOVIE_TITLE);
        String title = data.getString(titleColId);
        mTitleV.setText(title);
        Log.i("DetailFrag", "Title: " + title);

        // release date
        int releaseDateCol = data.getColumnIndex(MovieContract.MovieEntry.MOVIE_RELEASE_DATE);
        int releaseDate = data.getInt(releaseDateCol);
        String relDateString = "(" + String.valueOf(releaseDate) + ")";
        mReleaseV.setText(relDateString);

        // plot
        int plotCol = data.getColumnIndex(MovieContract.MovieEntry.MOVIE_PLOT);
        String plot = data.getString(plotCol);
        mPlotV.setText(plot);

        // rating bar
        int rateCol = data.getColumnIndex(MovieContract.MovieEntry.MOVIE_RATING);
        float rating = (float) (data.getDouble(rateCol) / 2);
        mRatingBar.setRating(rating);

        // favorite state
        int favCol = data.getColumnIndex(MovieContract.MovieEntry.MOVIE_FAVORITE);
        int favState = data.getInt(favCol);
        if (favState == 0) {
            mFavorite_button.setChecked(false);
        } else {
            mFavorite_button.setChecked(true);
        }

        // poster
        int posterColId = data.getColumnIndex(MovieContract.MovieEntry.MOVIE_POSTER);
        String posterUrl = data.getString(posterColId);
        Log.i("DetailFrag", "PosterURL: " + posterUrl);
        Picasso.with(getContext())
                .load(posterUrl)
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
