package com.example.android.movieapp2;

import android.content.ContentValues;
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


    public ReviewAdapter(ContentValues[] sReviewArray) {
        sReviews = sReviewArray;

    }

    @Override
    public ReviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.review_item_layout,parent,false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ReviewViewHolder holder, int position) {
        Log.i("ReviewAdapter", "Reviews #: " + sReviews.length);
        ContentValues cv = sReviews[position];
        String author = cv.getAsString(AUTHOR_KEY);
        String review = cv.getAsString(REVIEW_KEY);

        holder.author.setText(author);
        holder.review.setText(review);
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

        @BindView(R.id.review_text) TextView review;
        @BindView(R.id.author) TextView author;


        public ReviewViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

        }
    }
}

