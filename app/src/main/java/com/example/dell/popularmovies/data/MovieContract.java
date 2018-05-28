package com.example.dell.popularmovies.data;


import android.net.Uri;
import android.provider.BaseColumns;

public class MovieContract {


    static final String AUTHORITY = "com.example.dell.popularmovies";
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    static final String PATH_MOVIES = MovieEntry.TABLE_NAME;


    public static final class MovieEntry implements BaseColumns{
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES).build();

        static final String TABLE_NAME = "favorite_movies";
        public static final String MOVIE_ID = "movieId";
        public static final String MOVIE_TITLE = "movieTitle";
        public static final String MOVIE_IMAGE_PATH = "movieImagePath";
        public static final String MOVIE_RELEASE_DATE = "movieReleaseDate";
        public static final String MOVIE_RATE_AVERAGE = "movieRateAverage";
        public static final String MOVIE_PLOT_SYNOPSIS = "moviePlotSynopsis";
    }
}
