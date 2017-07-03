package com.example.android.movieapp2;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.example.android.movieapp2.data.MovieContract;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

/**
 * Created by dnj on 6/19/17.
 */

public final class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    private static final String LOG_TAG = MovieAdapter.class.getSimpleName();
    private static Cursor sCursor;
    private Context mContext;
    private static int mImageWidth;
    private static int mImageHeight;
    private ListItemClickListener mOnClickListener;

    public interface ListItemClickListener{
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
        MovieViewHolder holder = new MovieViewHolder(view);

        return holder;
    }

    public static int getmImageWidth(){
        return mImageWidth;
    }
    public static int getmImageHeight(){
        return mImageHeight;
    }
    @Override
    public void onBindViewHolder(final MovieViewHolder holder, int position) {
        sCursor.moveToPosition(position);

        // get poster image url
        int posterColId = sCursor.getColumnIndex(MovieContract.MovieEntry.MOVIE_POSTER);
        String posterUrl = sCursor.getString(posterColId);

        // get title
        int titleColId = sCursor.getColumnIndex(MovieContract.MovieEntry.MOVIE_TITLE);
        final String title = sCursor.getString(titleColId);
        Log.i(LOG_TAG, "Title: " + title);

        int tmdbIdCol = sCursor.getColumnIndex(MovieContract.MovieEntry.MOVIE_TMDB_ID);
        int tmdbId = sCursor.getInt(tmdbIdCol);


        int idCol = sCursor.getColumnIndex(MovieContract.MovieEntry.MOVIE_ID);
        final int id = sCursor.getInt(idCol);
        final String[] idArg = {String.valueOf(id)};



        // update DB with state of favorite
//        holder.favoriteCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                ContentValues cv = new ContentValues();
//                Uri itemUri = ContentUris.withAppendedId(MovieContract.MovieEntry.MOVIE_TABLE_URI, id);
//                int check = 0;
//                if (isChecked) {
//                        cv.put(MovieContract.MovieEntry.MOVIE_FAVORITE, 1);
//                        check = mContext.getContentResolver().update(itemUri,
//                                cv, null, null);
//                    if (check == 1) {
//                        Toast.makeText(mContext, "\"" + title + "\"" + " added to Favorites", Toast.LENGTH_SHORT).show();
//                    } else {
//                        Toast.makeText(mContext, "Failed to add " + "\"" + title + "\"" + " to Favorites", Toast.LENGTH_SHORT).show();
//                    }
//
//                } else {
//                    cv.put(MovieContract.MovieEntry.MOVIE_FAVORITE, 0);
//                    check = mContext.getContentResolver().update(itemUri,
//                            cv, MovieContract.MovieEntry.MOVIE_TMDB_ID, idArg);
//                    if (check == 1) {
//                        Toast.makeText(mContext, "\"" + title + "\"" + " removed from Favorites", Toast.LENGTH_SHORT).show();
//                    } else {
//                        Toast.makeText(mContext, "Failed to remove " + "\"" + title + "\"" + " from Favorites", Toast.LENGTH_SHORT).show();
//                    }
//
//                }
//            }
//        });

        // get favorite state and set display
        int favoriteCol = sCursor.getColumnIndex(MovieContract.MovieEntry.MOVIE_FAVORITE);
        Integer isFavorite = sCursor.getInt(favoriteCol);
        if (isFavorite == 1) {
            holder.favoriteCheckBox.setChecked(true);
        } else {
            holder.favoriteCheckBox.setChecked(false);
        }


        // picasso library to fetch and display images
        Picasso.with(mContext)
                .load(posterUrl)
                .error(R.drawable.error)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .resize(mImageWidth, mImageHeight)
                .placeholder(R.drawable.placeholder)
                .centerCrop()
                .into(holder.poster);
    }


    @Override
    public int getItemCount() {
        if (sCursor == null) {
//            Log.w(LOG_TAG, "Count = " + 0);
            return 0;
        }
//        Log.w(LOG_TAG, "Count = " + sCursor.getCount());
        return sCursor.getCount();
    }

    public void swapCursor(Cursor cursor) {
        Log.w(LOG_TAG, "Swap Cursor run");
        sCursor = cursor;
        notifyDataSetChanged();
    }

    public String getSelectedMovieDbID(int cursorIndexNum){
        sCursor.moveToPosition(cursorIndexNum);

        int idCol = sCursor.getColumnIndex(MovieContract.MovieEntry._ID);
        return sCursor.getString(idCol);
    }

    public class MovieViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

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
