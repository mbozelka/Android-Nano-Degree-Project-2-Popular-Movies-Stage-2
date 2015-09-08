package com.mattbozelka.popularmovies.adapters;

/*
*
* Experiemental adapter. Not currently being used.
*
* */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.mattbozelka.popularmovies.DataObjetcs.MovieReview;
import com.mattbozelka.popularmovies.R;

import java.util.ArrayList;

public class ReviewListAdapter extends ArrayAdapter<MovieReview> {

    private LayoutInflater mLayoutInflater;
    private int layoutResourceId;
    private ArrayList<MovieReview> reviews;
    private Context context;

    public ReviewListAdapter(Context context, int layoutResourceId, ArrayList<MovieReview> reviews) {
        super(context, layoutResourceId, reviews);
        this.mLayoutInflater = LayoutInflater.from(context);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.reviews = reviews;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        if(v == null){
            // inflate the layout
            v = mLayoutInflater.inflate(layoutResourceId, parent, false);
        }

        MovieReview review = reviews.get(position);

        TextView author = (TextView) v.findViewById(R.id.review_author);
        TextView content = (TextView) v.findViewById(R.id.review_content);

        author.setText(review.getAuthor());
        content.setText(review.getContent());

        return v;
    }

}
