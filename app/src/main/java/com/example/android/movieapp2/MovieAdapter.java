package com.example.android.movieapp2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import com.example.android.movieapp2.data.MovieContract;
import com.example.android.movieapp2.utils.FavoriteUtils;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

/**
 * Created by dnj on 6/19/17.
 */

public final class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    private static final String LOG_TAG = MovieAdapter.class.getSimpleName();
    private Cursor mCursor;
    private Context mContext;
    private static int mImageWidth;
    private static int mImageHeight;
    private ListItemClickListener mOnClickListener;
    private String mTitleFav;


    public interface ListItemClickListener {
        void onListItemClick(int clickedItemIndex);
    }

    public MovieAdapter(Context context, int width, int height, ListItemClickListener listener) {
        mContext = context;
        mImageHeight = height;
        mImageWidth = width;
        mOnClickListener = listener;

    }

    //    private void setAdapterItemSize(int imageWidth, int imageHeight){
//        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.item_relative_layout);
//        relativeLayout.getLayoutParams().height = imageHeight;
//        relativeLayout.getLayoutParams().width = imageWidth;
//    }
//
    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // inflate item layout in a view
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.movie_item_layout, parent, false);
        view.getLayoutParams().height = mImageHeight;
        view.getLayoutParams().width = mImageWidth;

        // create holder using view
        return new MovieViewHolder(view);
    }

    public static int getmImageWidth() {
        return mImageWidth;
    }

    public static int getmImageHeight() {
        return mImageHeight;
    }

    @Override
    public void onBindViewHolder(final MovieViewHolder holder, int position) {
        mCursor.moveToPosition(position);

        // poster
        final int posterColId = mCursor.getColumnIndex(MovieContract.MovieEntry.MOVIE_POSTER);
        String poster = mCursor.getString(posterColId);
        Picasso.with(mContext)
                .load(poster)
                .error(R.drawable.error)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .resize(mImageWidth, mImageHeight)
                .placeholder(R.drawable.placeholder)
                .centerCrop()
                .into(holder.poster);

        final int titleColId = mCursor.getColumnIndex(MovieContract.MovieEntry.MOVIE_TITLE);
        String title = mCursor.getString(titleColId);

        // get favorite state and set display
        if (FavoriteUtils.checkFavorite(mContext, title)) {
            holder.favoriteCheckBox.setChecked(true);
        } else {
            holder.favoriteCheckBox.setChecked(false);
        }


        holder.favoriteCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int idCol = 0;
                String id = null;
                if (holder.favoriteCheckBox.isChecked()) {
                    mCursor.moveToPosition(holder.getAdapterPosition());
                    mTitleFav = mCursor.getString(titleColId);
                    idCol = mCursor.getColumnIndex(MovieContract.MovieEntry._ID);
                    id = mCursor.getString(idCol);
                    ContentValues cv = new ContentValues();
                    cv.put(MovieContract.MovieEntry.MOVIE_FAVORITE, 1); // TODO: fix
                    FavoriteUtils.addFavorite(mContext, mTitleFav, id, cv);
                } else {
                    mCursor.moveToPosition(holder.getAdapterPosition());
                    mTitleFav = mCursor.getString(titleColId);
                    idCol = mCursor.getColumnIndex(MovieContract.MovieEntry._ID);
                    id = mCursor.getString(idCol);
                    try {
                        ContentValues cv = new ContentValues();
                        cv.put(MovieContract.MovieEntry.MOVIE_FAVORITE, 0); // TODO: fix
                        FavoriteUtils.removeFavorite(mContext, mTitleFav, id, cv);
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Movie not in Favorites");
                    }
                }
            }
        });
    }

//    private ContentValues adapterGetContentValues(String title, int position) {
//        mCursor.moveToPosition(position);
//
//        // release date
//        int releaseDateCol = mCursor.getColumnIndex(MovieContract.MovieEntry.MOVIE_RELEASE_DATE);
//        int releaseDate = mCursor.getInt(releaseDateCol);
//        String releaseDateString = String.valueOf(releaseDate);
//
//        // plot
//        int plotCol = mCursor.getColumnIndex(MovieContract.MovieEntry.MOVIE_PLOT);
//        String plot = mCursor.getString(plotCol);
//
//        // poster
//        int posterColId = mCursor.getColumnIndex(MovieContract.MovieEntry.MOVIE_POSTER);
//        String poster = mCursor.getString(posterColId);
//
//        // rating bar
//        int rateCol = mCursor.getColumnIndex(MovieContract.MovieEntry.MOVIE_RATING);
//        float rating = (float) (mCursor.getDouble(rateCol) / 2);
//
//
//        ContentValues cv = new ContentValues();
//        cv.put(MovieContract.MovieFavorites.FAVORITES_TITLE, title);
//        cv.put(MovieContract.MovieFavorites.FAVORITES_PLOT, plot);
//        cv.put(MovieContract.MovieFavorites.FAVORITES_RELEASE_DATE, releaseDateString);
//        cv.put(MovieContract.MovieFavorites.FAVORITES_POSTER, poster);
//        cv.put(MovieContract.MovieFavorites.FAVORITES_RATING, rating);
//
//        return cv;
//
//    }


    @Override
    public int getItemCount() {
        if (mCursor == null) {
            return 0;
        }
        return mCursor.getCount();
    }

    public void swapCursor(Cursor cursor) {
        Log.w(LOG_TAG, "Swap Cursor run");
        mCursor = cursor;
        notifyDataSetChanged();
    }

    public String getSelectedMovieLocalID(int cursorIndexNum) {
        mCursor.moveToPosition(cursorIndexNum);
        int idCol = mCursor.getColumnIndex(MovieContract.MovieEntry._ID);
        return mCursor.getString(idCol);
    }

    public String getSelectedMovieTitle(int cursorIndexNum) {
        mCursor.moveToPosition(cursorIndexNum);
        int titleCol = mCursor.getColumnIndex(MovieContract.MovieEntry.MOVIE_TITLE);
        return mCursor.getString(titleCol);
    }

    public class MovieViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView poster;
        CheckBox favoriteCheckBox;

        public MovieViewHolder(View itemView) {
            super(itemView);

            poster = (ImageView) itemView.findViewById(R.id.cover_image);
            favoriteCheckBox = (CheckBox) itemView.findViewById(R.id.favorite_button);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();
            mOnClickListener.onListItemClick(clickedPosition);
        }
    }
}
