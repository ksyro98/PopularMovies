package com.example.dell.popularmovies.data;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MoviesDBHelper extends SQLiteOpenHelper{

    private static final String DATABASE_NAME ="movieDatabase.db";

    private static final int DATABASE_VERSION = 7;

    MoviesDBHelper(Context context){
        super(context, DATABASE_NAME, null,  DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " + MovieContract.MovieEntry.TABLE_NAME + " (" +
                MovieContract.MovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                MovieContract.MovieEntry.MOVIE_ID + " INTEGER NOY NULL," +
                MovieContract.MovieEntry.MOVIE_TITLE + " TEXT NOT NULL," +
                MovieContract.MovieEntry.MOVIE_IMAGE_PATH + " TEXT NOT NULL," +
                MovieContract.MovieEntry.MOVIE_RELEASE_DATE + " TEXT NOT NULL," +
                MovieContract.MovieEntry.MOVIE_RATE_AVERAGE + " TEXT NOT NULL," +
                MovieContract.MovieEntry.MOVIE_PLOT_SYNOPSIS + " TEXT NOT NULL" +
                ");";

        sqLiteDatabase.execSQL(SQL_CREATE_MOVIE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieContract.MovieEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
