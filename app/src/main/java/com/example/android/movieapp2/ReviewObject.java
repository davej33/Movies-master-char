package com.example.android.movieapp2;

/**
 * Created by charlotte on 7/20/17.
 */

public class ReviewObject {

    private String mAuthor;
    private String mReview;

    public ReviewObject(String author, String review){
        mAuthor = author;
        mReview = review;
    }

    public String getmAuthor() {
        return mAuthor;
    }

    public String getmReview() {
        return mReview;
    }
}
