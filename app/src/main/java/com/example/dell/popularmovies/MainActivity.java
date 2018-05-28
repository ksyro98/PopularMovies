package com.example.dell.popularmovies;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.dell.popularmovies.adapter.MovieAdapter;
import com.example.dell.popularmovies.data.MovieContract;
import com.example.dell.popularmovies.network.NetworkUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


import butterknife.BindView;
import butterknife.ButterKnife;

import static android.content.res.Configuration.ORIENTATION_PORTRAIT;


public class MainActivity extends AppCompatActivity implements MovieAdapter.ClickListener,
        LoaderManager.LoaderCallbacks<Cursor>{


    public enum Order {
        TOP_RATED, POPULARITY, FAVORITE
    }


    @BindView(R.id.recycler_view) RecyclerView recyclerView;
    @BindView(R.id.progressBar) ProgressBar progressBar;
    @BindView(R.id.textViewError) TextView errorTextView;
    @BindView(R.id.errorButton) Button errorButton;
    @BindView(R.id.noFavoriteMovies) TextView noFavoriteMoviesTextView;

    private static ArrayList<Movie> movieArrayList = new ArrayList<>();
    private MainActivity clickListener = this;
    public static final String INTENT_EXTRA_NAME = "Movie Selected";
    public static final int MOVIE_LOADER = 0;
    private static final int STARTING_PAGE = 1;
    private static int currentPageTopRated = STARTING_PAGE; //in case we need to load more pages
    private static int currentPagePopular = STARTING_PAGE;  //in case we need to load more pages
    private static final int COLUMNS_IN_PORTRAIT = 4;
    private static final int COLUMNS_IN_LANDSCAPE = 6;
    private RequestQueue requestQueue;
    private static Order sortBy = Order.TOP_RATED;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        requestQueue = Volley.newRequestQueue(this);

        errorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (sortBy){
                    case TOP_RATED:
                        loadMovies(NetworkUtils.buildTopRatedURL(currentPageTopRated).toString());
                        break;
                    case POPULARITY:
                        loadMovies(NetworkUtils.buildPopularityURL(currentPagePopular).toString());
                        break;
                    case FAVORITE:
                        startLoader();
                        break;
                }
            }
        });


        switch (sortBy){
            case TOP_RATED:
                loadMovies(NetworkUtils.buildTopRatedURL(currentPageTopRated).toString());
                break;
            case POPULARITY:
                loadMovies(NetworkUtils.buildPopularityURL(currentPagePopular).toString());
                break;
            case FAVORITE:
                startLoader();
                break;
        }
    }


    @SuppressLint("StaticFieldLeak")
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        return new AsyncTaskLoader<Cursor>(this) {
            Cursor movieCursor = null;

            @Override
            protected void onStartLoading() {
                forceLoad();
                super.onStartLoading();
            }

            @Override
            public Cursor loadInBackground() {
                return getContentResolver().query(MovieContract.MovieEntry.CONTENT_URI,
                        null, null, null, null);
            }

            @Override
            public void deliverResult(Cursor data) {
                movieCursor = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data == null)
            return;

        movieArrayList.clear();

        while(data.moveToNext()) {
            movieArrayList.add(new Movie(data.getInt(1), data.getString(2), data.getString(3),
                    data.getString(4), Double.parseDouble(data.getString(5)), data.getString(6)));
        }

        if(!movieArrayList.isEmpty()) {
            showNothing();
            loadRecyclerViewWithMovies(movieArrayList);
            showMovies();
        }
        else
            showNoFavoriteMoviesTextView();

        data.close();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onItemClicked(int movieIndex) {
        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra(INTENT_EXTRA_NAME, movieArrayList.get(movieIndex).toString());
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sort_by_rating, menu);
        getMenuInflater().inflate(R.menu.sort_by_popularity, menu);
        getMenuInflater().inflate(R.menu.display_favorites, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        requestQueue = Volley.newRequestQueue(getApplicationContext());

        switch (item.getItemId()){
            case R.id.sort_by_top_rated:
                sortBy = Order.TOP_RATED;
                loadMovies(NetworkUtils.buildTopRatedURL(currentPageTopRated).toString());
                break;
            case R.id.sort_by_popularity:
                sortBy = Order.POPULARITY;
                loadMovies(NetworkUtils.buildPopularityURL(currentPagePopular).toString());
                break;
            case R.id.display_favorites:
                sortBy = Order.FAVORITE;
                startLoader();
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * This method shows an error message with a button to retry loading the movies.
     * It also hides everything else in the UI.
     */
    private void showError() {
        errorTextView.setVisibility(View.VISIBLE);
        errorButton.setVisibility(View.VISIBLE);
        errorButton.setEnabled(true);
        recyclerView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
        noFavoriteMoviesTextView.setVisibility(View.INVISIBLE);
    }

    /**
     * This shows the RecyclerView with the movies' images.
     * It also hides everything else in the UI.
     */
    private void showMovies(){
        errorTextView.setVisibility(View.INVISIBLE);
        errorButton.setVisibility(View.INVISIBLE);
        errorButton.setEnabled(true);
        recyclerView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
        noFavoriteMoviesTextView.setVisibility(View.INVISIBLE);
    }


    /**
     * This method shows a TextView to inform the user that he hasn't selected any movie to be displayed in the
     * "favorite movies" section.
     * It also hides everything else in the UI.
     */
    private void showNoFavoriteMoviesTextView(){
        errorTextView.setVisibility(View.INVISIBLE);
        errorButton.setVisibility(View.INVISIBLE);
        errorButton.setEnabled(false);
        recyclerView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
        noFavoriteMoviesTextView.setVisibility(View.VISIBLE);
    }


    /**
     * This method hides everything in the UI.
     */
    private void showNothing(){
        errorTextView.setVisibility(View.INVISIBLE);
        errorButton.setVisibility(View.INVISIBLE);
        errorButton.setEnabled(false);
        recyclerView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
        noFavoriteMoviesTextView.setVisibility(View.INVISIBLE);
    }


    /**
     * This method restarts the Loader (if it already existed) or initialises it (if it didn't already exist).
     */
    private void startLoader(){
        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<Cursor> movieLoader = loaderManager.getLoader(MOVIE_LOADER);
        if(movieLoader == null)
            loaderManager.initLoader(MOVIE_LOADER, null, this);
        else
            loaderManager.restartLoader(MOVIE_LOADER, null, this);
    }


    /**
     * This method uses the ArrayList that is passed as an input to load the RecyclerView with movie images.
     *
     * @param recyclerViewList the list that contains the movies which will be used to load the RecyclerView
     */
    private void loadRecyclerViewWithMovies(ArrayList<Movie> recyclerViewList){
        GridLayoutManager gridLayoutManager;
        if(getResources().getConfiguration().orientation == ORIENTATION_PORTRAIT)
            gridLayoutManager = new GridLayoutManager(getApplicationContext(), COLUMNS_IN_PORTRAIT);
        else
            gridLayoutManager = new GridLayoutManager(getApplicationContext(), COLUMNS_IN_LANDSCAPE);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setHasFixedSize(true);

        MovieAdapter movieAdapter = new MovieAdapter(recyclerViewList.size(), recyclerViewList,
                getApplicationContext(), clickListener);
        movieAdapter.notifyDataSetChanged();

        recyclerView.setAdapter(movieAdapter);
    }



    /**
     * This method loads the movies' data from the Internet using the Volley library with the uri that is provided
     * as an input.
     *
     * @param uri the uri that is used to load the movies from the Internet
     */
    private void loadMovies(String uri){
        showNothing();
        progressBar.setVisibility(View.VISIBLE);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, uri,
                null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    progressBar.setVisibility(View.INVISIBLE);

                    movieArrayList = Movie.getAllMovies(response.toString());

                    loadRecyclerViewWithMovies(movieArrayList);

                    showMovies();
                } catch (JSONException | NullPointerException e) {
                    if(sortBy == Order.TOP_RATED || sortBy == Order.POPULARITY)
                        showError();
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(sortBy == Order.TOP_RATED || sortBy == Order.POPULARITY)
                    showError();
            }
        });

        requestQueue.add(jsonObjectRequest);
    }

}