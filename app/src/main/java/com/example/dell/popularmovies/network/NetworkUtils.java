package com.example.dell.popularmovies.network;


import android.net.Uri;
import com.example.dell.popularmovies.BuildConfig;
import java.net.MalformedURLException;
import java.net.URL;


public class NetworkUtils {

    private static final String BASE_URL = "http://api.themoviedb.org/3/movie/";
    private static final String POPULARITY_URL = "popular";
    private static final String TOP_RATED_URL = "top_rated";
    private static final String TRAILER_URL = "videos";
    private static final String REVIEWS_URL = "reviews";
    private static final String PARAM_API_KEY = "api_key";
    private static final String API_KEY = BuildConfig.API_KEY;
    private static final String PARAM_PAGE = "page";


    /**
     * This method builds a URL to query the most popular movies.
     *
     * @param page The page we want to get the movies from. In this version of the app we only get movies from page 1
     *             so this parameter is always 1.
     * @return The URL to query the most popular movies.
     */
    public static URL buildPopularityURL(int page){
        Uri uri = Uri.parse(BASE_URL + POPULARITY_URL).buildUpon().
                appendQueryParameter(PARAM_API_KEY, API_KEY).
                appendQueryParameter(PARAM_PAGE, String.valueOf(page)).
                build();
        URL url = null;

        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    /**
     * This method builds a URL to query the top rated movies.
     *
     * @param page The page we want to get the movies from. In this version of the app we only get movies from page 1
     *             so this parameter is always 1.
     * @return The URL to query the top rated movies.
     */
    public static URL buildTopRatedURL(int page){
        Uri uri = Uri.parse(BASE_URL + TOP_RATED_URL).buildUpon().
                appendQueryParameter(PARAM_API_KEY, API_KEY).
                appendQueryParameter(PARAM_PAGE, String.valueOf(page)).
                build();
        URL url = null;

        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }


    public static URL buildTrailerURL(int id){
        Uri uri = Uri.parse(BASE_URL + String.valueOf(id) + "/" + TRAILER_URL).buildUpon().
                appendQueryParameter(PARAM_API_KEY, API_KEY).
                build();
        URL url = null;

        try {
            url = new URL(uri.toString());
        }
        catch (MalformedURLException e){
            e.printStackTrace();
        }

        return url;
    }


    public static URL buildReviewsURL(int id){
        Uri uri = Uri.parse(BASE_URL + String.valueOf(id) + "/" + REVIEWS_URL).buildUpon().
                appendQueryParameter(PARAM_API_KEY, API_KEY).
                build();
        URL url = null;

        try {
            url = new URL(uri.toString());
        }
        catch (MalformedURLException e){
            e.printStackTrace();
        }

        return url;
    }
}