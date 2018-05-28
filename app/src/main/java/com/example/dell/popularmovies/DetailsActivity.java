package com.example.dell.popularmovies;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.dell.popularmovies.data.MovieContract;
import com.example.dell.popularmovies.network.NetworkUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;


public class DetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{


    @BindView(R.id.text_view_title) TextView textViewTitle;
    @BindView(R.id.text_view_rating) TextView textViewRating;
    @BindView(R.id.text_view_release_date) TextView textViewReleaseDate;
    @BindView(R.id.text_view_plot) TextView textViewPlot;
    @BindView(R.id.image_view) ImageView imageView;
    @BindView(R.id.review_layout) LinearLayout reviewLayout;
    @BindView(R.id.trailer_layout) LinearLayout trailerLayout;
    @BindView(R.id.trailer_progress_bar) ProgressBar trailerProgressBar;
    @BindView(R.id.reviews_progress_bar) ProgressBar reviewsProgressBar;

    private Movie selectedMovie;
    private RequestQueue requestQueue;
    private static final String TRAILER_TAG = "trailer_tag";
    private static final String REVIEWS_TAG = "reviews_tag";
    private static final String YOUTUBE_APP_URI = "vnd.youtube:";
    private static final String YOUTUBE_WEB_URI = "https://www.youtube.com/watch?v=";
    private static final String BUNDLE_MAP_KEY = "review map";
    private static final String BUNDLE_LIST_KEY = "trailer list";
    private Menu menu;
    public static final int DETAILS_LOADER = 1;
    private static final String MIME_TYPE = "text/plain";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        ButterKnife.bind(this);


        selectedMovie = new Movie(getIntent().getStringExtra(MainActivity.INTENT_EXTRA_NAME));


        textViewTitle.setText(selectedMovie.getTitle());

        String ratingString = getString(R.string.rating) + " " + selectedMovie.getRateAverage();
        textViewRating.setText(ratingString);

        final String releaseDateString = getString(R.string.release_date) + "\n" + selectedMovie.getReleaseDate();
        textViewReleaseDate.setText(releaseDateString);

        textViewPlot.setText(selectedMovie.getPlotSynopsis());



        /*code was taken from stackoverflow post:
        https://stackoverflow.com/questions/23391523/load-images-from-disk-cache-with-picasso-if-offline

        With this code the images are loaded from cache first and only if they aren't store in cache they are loaded
        through Internet. If there is no Internet connection they are replaced with a placeholder.
         */
        Picasso.with(getApplicationContext()).load(selectedMovie.getAbsoluteImagePath()).
                networkPolicy(NetworkPolicy.OFFLINE).into(imageView, new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError() {
                /*images were taken from:
                https://www.simplifiedcoding.net/picasso-android-tutorial-picasso-image-loader-library/
                 */
                Picasso.with(getApplicationContext()).load(selectedMovie.getAbsoluteImagePath()).
                        placeholder(R.drawable.placeholder).into(imageView);
            }
        });

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);


        requestQueue = Volley.newRequestQueue(this);


        /* If the movie's trailers were already loaded then just use the trailers loaded to display the trailers' links
        in the UI. If not then use the Volley library to load the movie's trailers from the Internet.
         */
        if(savedInstanceState == null || savedInstanceState.getStringArrayList(BUNDLE_LIST_KEY) == null) {
            trailerProgressBar.setVisibility(View.VISIBLE);
            String trailerUrl = NetworkUtils.buildTrailerURL(selectedMovie.getId()).toString();
            JsonObjectRequest trailerJsonObjectRequest = new JsonObjectRequest(Request.Method.GET, trailerUrl,
                    null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        JSONArray jsonArray = response.getJSONArray("results");
                        if(jsonArray.length() == 0) {
                            trailerLayout.addView(createNoTrailersTextView());
                            trailerProgressBar.setVisibility(View.INVISIBLE);
                        }
                        else {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                selectedMovie.addTrailerToList(jsonArray.getJSONObject(i).getString("key"));
                                trailerLayout.addView(createTrailerLayout(i));
                                trailerProgressBar.setVisibility(View.INVISIBLE);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    trailerLayout.addView(createNoInternetTrailerTextView());
                    trailerProgressBar.setVisibility(View.INVISIBLE);
                }
            });
            trailerJsonObjectRequest.setTag(TRAILER_TAG);
            requestQueue.add(trailerJsonObjectRequest);
        }
        else {
            selectedMovie.setTrailerList(savedInstanceState.getStringArrayList(BUNDLE_LIST_KEY));
            for(int i = 0; i < selectedMovie.getTrailerList().size(); i++)
                trailerLayout.addView(createTrailerLayout(i));
        }


        /* If the movie's reviews were already loaded then just use the reviews loaded to display the reviews
        in the UI. If not then use the Volley library to load the movie's reviews from the Internet.
         */
        if(savedInstanceState == null || savedInstanceState.getSerializable(BUNDLE_MAP_KEY) == null) {
            reviewsProgressBar.setVisibility(View.VISIBLE);
            String reviewsUrl = NetworkUtils.buildReviewsURL(selectedMovie.getId()).toString();
            JsonObjectRequest reviewsJsonObjectRequest = new JsonObjectRequest(Request.Method.GET, reviewsUrl,
                    null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        JSONArray jsonArray = response.getJSONArray("results");
                        if(jsonArray.length() == 0) {
                            reviewLayout.addView(createNoReviewsTextView());
                            reviewsProgressBar.setVisibility(View.INVISIBLE);
                        }
                        else {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                selectedMovie.addReviewToMap(jsonArray.getJSONObject(i).getString("author"),
                                        jsonArray.getJSONObject(i).getString("content"));
                                reviewLayout.addView(createReviewLayout(jsonArray.getJSONObject(i).getString("author")));
                                reviewsProgressBar.setVisibility(View.INVISIBLE);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    reviewLayout.addView(createNoInternetReviewsTextView());
                    reviewsProgressBar.setVisibility(View.INVISIBLE);
                }
            });
            reviewsJsonObjectRequest.setTag(REVIEWS_TAG);
            requestQueue.add(reviewsJsonObjectRequest);
        }
        else {
            //noinspection unchecked
            selectedMovie.setReviewMap((HashMap<String, String>) savedInstanceState.getSerializable(BUNDLE_MAP_KEY));
            for(String key : selectedMovie.getReviewMap().keySet())
                reviewLayout.addView(createReviewLayout(key));
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.favorite, menu);

        //The loader is used to query the database and check if the movie is a favorite movie.
        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<Cursor> movieLoader = loaderManager.getLoader(DETAILS_LOADER);
        if(movieLoader == null)
            loaderManager.initLoader(DETAILS_LOADER, null, this);
        else
            loaderManager.restartLoader(DETAILS_LOADER, null, this);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case (android.R.id.home):
                NavUtils.navigateUpFromSameTask(this) ;
                break;
            case (R.id.favorite):
                if(!selectedMovie.getFavorite()) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(MovieContract.MovieEntry.MOVIE_ID,
                            selectedMovie.getId());
                    contentValues.put(MovieContract.MovieEntry.MOVIE_TITLE,
                            selectedMovie.getTitle());
                    contentValues.put(MovieContract.MovieEntry.MOVIE_IMAGE_PATH,
                            selectedMovie.getRelativeImagePath());
                    contentValues.put(MovieContract.MovieEntry.MOVIE_RELEASE_DATE,
                            selectedMovie.getReleaseDate());
                    contentValues.put(MovieContract.MovieEntry.MOVIE_RATE_AVERAGE,
                            Double.toString(selectedMovie.getRateAverage()));
                    contentValues.put(MovieContract.MovieEntry.MOVIE_PLOT_SYNOPSIS,
                            selectedMovie.getPlotSynopsis());

                    Uri uri = getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI, contentValues);

                    if(uri != null) {
                        item.setIcon(R.drawable.star_white);
                        selectedMovie.setFavorite(true);
                    }
                    else
                        Toast.makeText(this, getString(R.string.error), Toast.LENGTH_LONG).show();
                }
                else {
                    int deleted = getContentResolver().delete(MovieContract.MovieEntry.CONTENT_URI,
                            MovieContract.MovieEntry.MOVIE_TITLE + "=?", new String[]{selectedMovie.getTitle()});
                    if(deleted > 0) {
                        item.setIcon(R.drawable.star_yellow);
                        selectedMovie.setFavorite(false);
                    }
                    else
                        Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show();
                }
                break;
            case (R.id.share):
                String title = "Share 1st trailer.";
                String text = "Hey, check this new movie trailer, it looks amazing.\n" +
                        YOUTUBE_WEB_URI + selectedMovie.getTrailerFromList(0);

                ShareCompat.IntentBuilder
                        .from(this)
                        .setType(MIME_TYPE)
                        .setChooserTitle(title)
                        .setText(text)
                        .startChooser();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(requestQueue != null) {
            requestQueue.cancelAll(TRAILER_TAG);
            requestQueue.cancelAll(REVIEWS_TAG);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putStringArrayList(BUNDLE_LIST_KEY, selectedMovie.getTrailerList());
        outState.putSerializable(BUNDLE_MAP_KEY, selectedMovie.getReviewMap());
        super.onSaveInstanceState(outState);
    }


    /**
     * Creates a LinearLayout that contains the buttons (which will be used by the user to watch the movie trailer)
     * and some text.
     *
     * @param trailerNumber number of the trailer for this button
     * @return a LinearLayout that contains the button and a textView
     */
    private LinearLayout createTrailerLayout(final int trailerNumber){
        LinearLayout trailerLinearLayout = new LinearLayout(getApplicationContext());
        trailerLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        trailerLinearLayout.setPadding(5, 10, 5, 20);
        trailerLinearLayout.setOrientation(LinearLayout.HORIZONTAL);


        ImageView playImage = new ImageView(getApplicationContext());
        playImage.setBackgroundResource(R.drawable.triangle);
        playImage.setLayoutParams(new LinearLayout.LayoutParams(35, 35));
        playImage.setPadding(5, 0, 10, 0);
        trailerLinearLayout.addView(playImage);


        TextView trailerTextView = new TextView(getApplicationContext());
        trailerTextView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        trailerTextView.setText(getString(R.string.watch_trailer, trailerNumber+1));
        trailerTextView.setPadding(20, 5, 10, 5);
        trailerTextView.setTextColor(Color.parseColor("#808080"));
        trailerLinearLayout.addView(trailerTextView);
        trailerLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*code was taken from stackoverflow post:
                https://stackoverflow.com/questions/574195/android-youtube-app-play-video-intent

                With this code the the app tries to open the youtube app and if there is no youtube app installed
                it opens the trailer using the youtube site.
                 */
                Intent appIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(YOUTUBE_APP_URI + selectedMovie.getTrailerFromList(trailerNumber)));
                appIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Intent webIntent =  new Intent(Intent.ACTION_VIEW,
                        Uri.parse(YOUTUBE_WEB_URI + selectedMovie.getTrailerFromList(trailerNumber)));
                webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try{
                    getApplicationContext().startActivity(appIntent);
                }
                catch (ActivityNotFoundException e){
                    getApplicationContext().startActivity(webIntent);
                }
            }
        });

        return trailerLinearLayout;
    }

    /**
     * Creates a TextView to inform the user that the trailers cannot be displayed,
     * because there is no Internet connection.
     *
     * @return the TextView that was created
     */
    private TextView createNoInternetTrailerTextView(){
        TextView noInternetTrailerTextView = new TextView(this);
        noInternetTrailerTextView.setText(R.string.no_internet_for_trailers);
        noInternetTrailerTextView.setPadding(20, 0, 5, 15);

        return noInternetTrailerTextView;
    }


    /**
     * Creates a TextView to inform the user that there are no trailers to display.
     *
     * @return the TextView that was created
     */
    private TextView createNoTrailersTextView(){
        TextView noInternetTrailerTextView = new TextView(this);
        noInternetTrailerTextView.setText(R.string.no_trailers);
        noInternetTrailerTextView.setPadding(20, 0, 5, 15);

        return noInternetTrailerTextView;
    }

    /**
     * Creates a LinearLayout that contains 2 textView to display the author of the review and the review
     *
     * @param key a key to identify the review in the reviewMap (this key is the name of the author)
     * @return a LinearLayout that contains 2 textViews
     */
    private LinearLayout createReviewLayout(String key){
        LinearLayout reviewLayout = new LinearLayout(getApplicationContext());
        reviewLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        reviewLayout.setPadding(20, 15, 5, 15);
        reviewLayout.setOrientation(LinearLayout.VERTICAL);

        TextView authorTextView = new TextView(getApplicationContext());
        authorTextView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        authorTextView.setText(key);
        authorTextView.setTextColor(Color.parseColor("#808080"));
        authorTextView.setTypeface(null, Typeface.BOLD);
        reviewLayout.addView(authorTextView);

        TextView contentTextView = new TextView(getApplicationContext());
        contentTextView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        contentTextView.setText(selectedMovie.getReviewFromMap(key));
        contentTextView.setTextColor(Color.parseColor("#808080"));
        reviewLayout.addView(contentTextView);

        return reviewLayout;
    }


    /**
     * Creates a TextView to inform the user that the reviews cannot be displayed,
     * because there is no Internet connection.
     *
     * @return the TextView that was created
     */
    private TextView createNoInternetReviewsTextView(){
        TextView noInternetReviewsTextView = new TextView(this);
        noInternetReviewsTextView.setText(getString(R.string.no_internet_for_reviews));
        noInternetReviewsTextView.setPadding(20, 0, 5, 15);

        return noInternetReviewsTextView;
    }


    /**
     * Creates a TextView to inform the user that there are no reviews to display.
     *
     * @return the TextView that was created
     */
    private TextView createNoReviewsTextView(){
        TextView noInternetReviewsTextView = new TextView(this);
        noInternetReviewsTextView.setText(R.string.no_reviews);
        noInternetReviewsTextView.setPadding(20, 0, 5, 15);

        return noInternetReviewsTextView;
    }


    @SuppressLint("StaticFieldLeak")
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        return new AsyncTaskLoader<Cursor>(this) {
            Cursor detailsCursor = null;

            @Override
            protected void onStartLoading() {
                forceLoad();
                super.onStartLoading();
            }

            @Override
            public Cursor loadInBackground() {
                return getContentResolver().query(MovieContract.MovieEntry.CONTENT_URI, null,
                        MovieContract.MovieEntry.MOVIE_TITLE + " = ?", new String[]{selectedMovie.getTitle()},
                        null);
            }

            @Override
            public void deliverResult(Cursor data) {
                detailsCursor = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data == null)
            return;

        if(!data.moveToFirst())
            return;


        menu.getItem(1).setIcon(R.drawable.star_white);

        selectedMovie.setFavorite(true);

        data.close();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}