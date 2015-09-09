package com.mattbozelka.popularmovies;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.mattbozelka.popularmovies.AsyncTasks.FetchMoviesTask;
import com.mattbozelka.popularmovies.DataObjetcs.Movie;
import com.mattbozelka.popularmovies.adapters.ImageAdapter;

import java.util.ArrayList;


/*
*
* the main UI of MainActivity and handles the logic for the view
*
* */

public class MovieListFragment extends Fragment{

    /**
     * Private globals
     */
    private final String LOG_TAG = MovieListFragment.class.getSimpleName();
    private final String STORED_MOVIES = "stored_movies";
    private SharedPreferences prefs;
    private String sortOrder;
    private ImageAdapter mMoviePosterAdapter;
    private ArrayList<Movie> movies = new ArrayList<Movie>();


    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        public void loadItem(Movie movie);
    }

    /**
     * Blank default constructor for fragment
     */
    public MovieListFragment() {
    }


    /**
     * on create get back the saved movie list
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sortOrder = prefs.getString(getString(R.string.display_preferences_sort_order_key),
                getString(R.string.display_preferences_sort_default_value));

        if(savedInstanceState != null){
            if(savedInstanceState.<Movie>getParcelableArrayList(STORED_MOVIES) != null) {
                movies.clear();
                movies.addAll(savedInstanceState.<Movie>getParcelableArrayList(STORED_MOVIES));
            }
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setHasOptionsMenu(true);
        mMoviePosterAdapter = new ImageAdapter(
                getActivity(),
                R.layout.list_item_poster,
                R.id.list_item_poster_imageview,
                new ArrayList<String>());

        View rootView = inflater.inflate(R.layout.fragment_movie_list, container, false);
        GridView gridView = (GridView) rootView.findViewById(R.id.main_movie_grid);
        gridView.setAdapter(mMoviePosterAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                Movie details = movies.get(position);

                ((Callback) getActivity())
                        .loadItem(details);

            }

        });

        return rootView;
    }


    /*
    * call getMovies to kick of async task. Asynk task now handles if it should call
    * the API or not
    * */
    @Override
    public void onStart() {
        super.onStart();

        // get preferences to check sort order
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String checkSortOrder = prefs.getString(getString(R.string.display_preferences_sort_order_key),
                getString(R.string.display_preferences_sort_default_value));


        boolean sortOrderChange = !checkSortOrder.equals(sortOrder);
        sortOrder = checkSortOrder;

        // if no movies or the sort order has changed fetch new movies
        // if sort order is favorites it grabs favorites from DB
        // to ensure it is always up to date
        if(movies.size() == 0 || sortOrderChange || sortOrder.equals("favorites")){
            getMovies();
        }else{
            // else load what's already in memory
            mMoviePosterAdapter.clear();
            for(Movie movie : movies) {
                mMoviePosterAdapter.add(movie.getPoster());
            }
        }

    }


    /*
    * On save instance state. Creates a parcable array of all current received from the API
    * */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(STORED_MOVIES, movies);
    }


    /*
    * kicks off async task to get movies for the main movie list UI
    * */
    private void getMovies() {

        // fetch the movies from the API, or it will get favorites from the DB
        FetchMoviesTask fetchMoviesTask = new FetchMoviesTask(getActivity(),
                movies, mMoviePosterAdapter, sortOrder);

        fetchMoviesTask.execute();

    }

}
