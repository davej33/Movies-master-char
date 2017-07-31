package com.example.android.movieapp2;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by charlotte on 7/19/17.
 */

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
    private static final String AUTHOR_KEY = "author";
    private static final String REVIEW_KEY = "content";
    private static ContentValues[] sReviews;
    private static int alternateBackgroundColors = 0;


    public ReviewAdapter() {

    }

    @Override
    public ReviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.review_item_layout, parent, false);
        if ((alternateBackgroundColors++ % 2 == 0)) {
            view.setBackgroundColor(Color.parseColor("#ffffff"));
        } else {
            view.setBackgroundColor(Color.parseColor("#edf3ff"));
        }
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ReviewViewHolder holder, int position) {

        ContentValues cv = sReviews[position];
        String author = cv.getAsString(AUTHOR_KEY);
        String review = cv.getAsString(REVIEW_KEY);

        holder.author.setText(author);
        holder.review.setText("\"" + review + "\"");

        Log.i("ReviewAdapter", "Reviews #: " + position + author);
    }

    public static void setReviewArray(ContentValues[] cv) {
        sReviews = cv;
        Log.i("ReviewAdapter", "set from Json: ");
    }

    @Override
    public int getItemCount() {
        if (sReviews == null) {
            return 0;
        } else {
            return sReviews.length;
        }
    }

    class ReviewViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.review_text)
        TextView review;
        @BindView(R.id.author)
        TextView author;


        public ReviewViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

        }
    }
}

