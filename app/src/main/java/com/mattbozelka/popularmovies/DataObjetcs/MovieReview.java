package com.mattbozelka.popularmovies.DataObjetcs;

/*
*
* simple object to store all the information
* for a movie being used in the UI
*
* */

public class MovieReview {

    private String author;
    private String content;

    public MovieReview(String author, String content){
        this.author = author;
        this.content = content;;
    }

    public String getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

}