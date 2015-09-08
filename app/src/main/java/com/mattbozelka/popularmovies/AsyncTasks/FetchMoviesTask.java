package com.mattbozelka.popularmovies.AsyncTasks;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.mattbozelka.popularmovies.Config.Config;
import com.mattbozelka.popularmovies.DataObjetcs.Movie;
import com.mattbozelka.popularmovies.adapters.ImageAdapter;
import com.mattbozelka.popularmovies.data.PopularMovieContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/*
*
* Async class for fetching the
* movie information from the database
*
* Also handles logic if it will load movies in save instance state
*
* */

public class FetchMoviesTask extends AsyncTask<String, Void, ArrayList<Movie>> {

    private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();
    private final String MOVIE_POSTER_BASE = "http://image.tmdb.org/t/p/";
    private final String MOVIE_POSTER_SIZE = "w185";
    private final Context mContext;
    private ImageAdapter mMoviePosterAdapter;
    private ArrayList<Movie> movies;
    private String sortBy;
    private boolean sortByChanged;

    public FetchMoviesTask(Context context, ArrayList<Movie> movies,
                           ImageAdapter mMoviePosterAdapter, String sortBy, boolean sortByChanged){
        this.mContext = context;
        this.movies = movies;
        this.mMoviePosterAdapter = mMoviePosterAdapter;
        this.sortBy = sortBy;
        this.sortByChanged = sortByChanged;
    }

    @Override
    protected ArrayList<Movie> doInBackground(String... params) {

        // get favorites from the database
        if(sortBy.equals("favorites")){
            getFavorites();
            return movies;
        }

        // check if the sort order has changed, if so grab more data.
        // if not return the current movies set.
        if(movies.size() > 0 && !sortByChanged){
            return movies;
        }

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String moviesJsonStr = null;

        try {

            final String BASE_URL = "http://api.themoviedb.org/3/discover/movie?";
            final String SORT_BY = "sort_by";
            final String KEY = "api_key";

            Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                    .appendQueryParameter(SORT_BY, sortBy)
                    .appendQueryParameter(KEY, Config.API_KEY)
                    .build();

            URL url = new URL(builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                return null;
            }
            moviesJsonStr = buffer.toString();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        try {
            return extractData(moviesJsonStr);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        return null;
    }

    private String getYear(String date){

        final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        final Calendar cal = Calendar.getInstance();

        try {
            cal.setTime(df.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return Integer.toString(cal.get(Calendar.YEAR));

    }

    private ArrayList<Movie> extractData(String moviesJsonStr) throws JSONException {

        // Items to extract
        final String ARRAY_OF_MOVIES = "results";
        final String ORIGINAL_ID = "id";
        final String ORIGINAL_TITLE = "original_title";
        final String POSTER_PATH = "poster_path";
        final String OVERVIEW = "overview";
        final String VOTE_AVERAGE = "vote_average";
        final String RELEASE_DATE = "release_date";

        JSONObject moviesJson = new JSONObject(moviesJsonStr);
        JSONArray moviesArray = moviesJson.getJSONArray(ARRAY_OF_MOVIES);
        int moviesLength =  moviesArray.length();

        // clear the list before adding more
        movies.clear();

        for(int i = 0; i < moviesLength; ++i) {

            // for each movie in the JSON object create a new
            // movie object with all the required data
            JSONObject movie = moviesArray.getJSONObject(i);
            int id = movie.getInt(ORIGINAL_ID);
            String title = movie.getString(ORIGINAL_TITLE);
            String poster = MOVIE_POSTER_BASE + MOVIE_POSTER_SIZE + movie.getString(POSTER_PATH);
            String overview = movie.getString(OVERVIEW);
            String voteAverage = movie.getString(VOTE_AVERAGE);
            String releaseDate = getYear(movie.getString(RELEASE_DATE));
            Movie newMovie = new Movie(id, title, poster, overview, voteAverage, releaseDate);

            // fetch reviews which will be stored as a JSON string
            // and add it to the new movie in new asyncTask
            FetchMoviesComponents fetchReviews = new FetchMoviesComponents(newMovie,
                    "reviews");
            fetchReviews.execute();

            // fetch previews which will be stored as a JSON string
            // and add it to the new movie in new asyncTask
            FetchMoviesComponents fetchPreviews = new FetchMoviesComponents(newMovie,
                    "videos");
            fetchPreviews.execute();

            movies.add(newMovie);

        }

        return movies;

    }

    private void getFavorites(){

        Uri uri = PopularMovieContract.MovieEntry.CONTENT_URI;
        ContentResolver resolver = mContext.getContentResolver();
        Cursor cursor = null;

        try {

            cursor = resolver.query(uri, null, null, null, null);

            if (cursor.moveToFirst()){
                movies.clear();
                do {
                    Movie movie = new Movie(cursor.getInt(1), cursor.getString(3),
                            cursor.getString(4), cursor.getString(5), cursor.getString(6),
                            cursor.getString(7));

                    movie.setReviews(cursor.getString(8));
                    movie.setMoviePreviews(cursor.getString(9));
                    movies.add(movie);
                } while (cursor.moveToNext());
            }

        } finally {

            if(cursor != null)
                cursor.close();

        }

    }

    @Override
    protected void onPostExecute(ArrayList<Movie> results) {
        if (results != null && mMoviePosterAdapter != null) {

            mMoviePosterAdapter.clear();
            for(Movie movie : results) {
                mMoviePosterAdapter.add(movie.getPoster());
            }

        }

    }
}
