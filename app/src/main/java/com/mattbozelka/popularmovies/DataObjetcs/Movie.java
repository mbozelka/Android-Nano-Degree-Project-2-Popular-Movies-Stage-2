package com.mattbozelka.popularmovies.DataObjetcs;

/*
*
* simple object to store all the information
* for a movie being used in the UI
*
* Takes a string for the movie title, poster, overview, voteAverage
* and releaseDate
*
* Implements parcelable in order to easily pass between intents
*
* */

import android.os.Parcel;
import android.os.Parcelable;

public class Movie implements Parcelable {
    private int movieId;
    private String title;
    private String poster;
    private String overview;
    private String voteAverage;
    private String releaseDate;
    private String movieReviews;
    private String moviePreviews = "";

    public Movie(int movieId, String title, String poster, String overview,
                 String voteAverage, String releaseDate){
        this.movieId = movieId;
        this.title = title;
        this.poster = poster;
        this.overview = overview;
        this.voteAverage = voteAverage;
        this.releaseDate = releaseDate;
    }

    public int getMovieId() {
        return movieId;
    }

    public String getTitle() {
        return title;
    }

    public String getPoster() {
        return poster;
    }

    public String getOverview() {
        return overview;
    }

    public String getVoteAverage() {
        return voteAverage;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public String getReviews() {
        return movieReviews;
    }

    public void setReviews(String reviews) {
        movieReviews = reviews;
    }

    public String getMoviePreviews() {
        return moviePreviews;
    }

    public void setMoviePreviews(String previews) {
        moviePreviews = previews;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(movieId);
        out.writeString(title);
        out.writeString(poster);
        out.writeString(overview);
        out.writeString(voteAverage);
        out.writeString(releaseDate);
        out.writeString(movieReviews);
        out.writeString(moviePreviews);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    private Movie(Parcel in) {
        movieId = in.readInt();
        title = in.readString();
        poster = in.readString();
        overview = in.readString();
        voteAverage = in.readString();
        releaseDate = in.readString();
        movieReviews = in.readString();
        moviePreviews = in.readString();
    }

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };
}