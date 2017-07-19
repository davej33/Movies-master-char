package com.example.android.movieapp2;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 * Created by charlotte on 7/19/17.
 */

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {


    @Override
    public ReviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(ReviewViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    class ReviewViewHolder extends RecyclerView.ViewHolder {

        TextView reviewText;

        public ReviewViewHolder(View itemView) {
            super(itemView);

            reviewText = (TextView) itemView.findViewById(R.id.review_text);
        }
    }
}

