package com.example.android.movieapp2;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.android.movieapp2.data.MovieContract;
import com.example.android.movieapp2.data.MovieDbHelper;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

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
    private static final String ARG_MOVIE_ID = "movieId";
    private static final int LOADER_ID = 111;
    private String mMovieID;
    private Cursor mCursor;

    // views
    TextView titleV;
    TextView releaseV;
    TextView plotV;
    RatingBar ratingBar;
    ImageView posterV;


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
    // TODO: Rename and change types and number of parameters
    public static MovieDetailFragment newInstance(String movieID) {
        MovieDetailFragment fragment = new MovieDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MOVIE_ID, movieID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mMovieID = getArguments().getString(ARG_MOVIE_ID);
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
        if (getLoaderManager().getLoader(LOADER_ID) != null) {
            getLoaderManager().restartLoader(LOADER_ID, null, this);
        } else {
            getLoaderManager().initLoader(LOADER_ID, null, this);
        }

        titleV = (TextView) view.findViewById(R.id.movie_title);
        releaseV = (TextView) view.findViewById(R.id.release_year);
        plotV = (TextView) view.findViewById(R.id.plot);
        ratingBar = (RatingBar) view.findViewById(R.id.ratingBar);
        posterV = (ImageView) view.findViewById(R.id.poster_detail_view);

        // Inflate the layout for this fragment
        return view;
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
        String[] selArgs = {mMovieID};
        Log.i(LOG_TAG, "Loader movie ID: " + mMovieID);
        Uri singleMovieUri = Uri.parse(MovieContract.MovieEntry.MOVIE_TABLE_URI.toString() + "/" + mMovieID);

        return new CursorLoader(getContext(), singleMovieUri, null, MovieContract.MovieEntry._ID + "=?",
                selArgs, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.i(LOG_TAG, "Details Cursor Count: " + data.getCount());
        data.moveToFirst();

        // title
        int titleColId = data.getColumnIndex(MovieContract.MovieEntry.MOVIE_TITLE);
        String title = data.getString(titleColId);

        titleV.setText(title);
        Log.i("DetailFrag", "Title: " + title);

        // release date
        int releaseDateCol = data.getColumnIndex(MovieContract.MovieEntry.MOVIE_RELEASE_DATE);
        int releaseDate = data.getInt(releaseDateCol);
        String relDateString = "(" + String.valueOf(releaseDate) + ")";
        releaseV.setText(relDateString);

        // plot
        int plotCol = data.getColumnIndex(MovieContract.MovieEntry.MOVIE_PLOT);
        String plot = data.getString(plotCol);
        plotV.setText(plot);

        // rating bar
        int rateCol = data.getColumnIndex(MovieContract.MovieEntry.MOVIE_RATING);
        float rating = (float) (data.getDouble(rateCol) / 2);
        Log.i(LOG_TAG, "Rating: " + rating);
        ratingBar.setRating(rating);


        // poster
        int posterColId = data.getColumnIndex(MovieContract.MovieEntry.MOVIE_POSTER);
        String posterUrl = data.getString(posterColId);
        Log.i("DetailFrag", "PosterURL: " + posterUrl);
        Picasso.with(getContext())
                .load(posterUrl)
                .error(R.drawable.error)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .resize(MovieAdapter.getmImageWidth(), MovieAdapter.getmImageHeight())
                .placeholder(R.drawable.placeholder)
                .centerCrop()
                .into(posterV);
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
