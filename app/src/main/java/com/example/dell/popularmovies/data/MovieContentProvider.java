package com.example.dell.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;



public class MovieContentProvider extends ContentProvider {

    private MoviesDBHelper moviesDBHelper;

    private static final int MOVIES = 100;
    private static final int MOVIES_WITH_ID = 101;

    private static final UriMatcher uriMatcher = buildUriMatcher();


    private static UriMatcher buildUriMatcher(){
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(MovieContract.AUTHORITY, MovieContract.PATH_MOVIES, MOVIES);
        uriMatcher.addURI(MovieContract.AUTHORITY, MovieContract.PATH_MOVIES + "/#", MOVIES_WITH_ID);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        moviesDBHelper = new MoviesDBHelper(getContext());

        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        final SQLiteDatabase sqLiteDatabase = moviesDBHelper.getReadableDatabase();
        int match = uriMatcher.match(uri);
        Cursor returnCursor;

        switch (match){
            case(MOVIES):
                returnCursor = sqLiteDatabase.query(MovieContract.MovieEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        return returnCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final SQLiteDatabase sqLiteDatabase = moviesDBHelper.getWritableDatabase();
        int match = uriMatcher.match(uri);
        Uri returnUri;

        switch (match){
            case MOVIES:
                long id = sqLiteDatabase.insert(MovieContract.MovieEntry.TABLE_NAME, null, contentValues);
                if(id > 0)
                    returnUri = ContentUris.withAppendedId(MovieContract.MovieEntry.CONTENT_URI, id);
                else
                    throw new SQLException("Failed to insert new intro " + uri);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String whereClause, @Nullable String[] selectionArgs) {
        final SQLiteDatabase sqLiteDatabase = moviesDBHelper.getWritableDatabase();
        int match = uriMatcher.match(uri);
        int returnInt;

        switch (match){
            case MOVIES:
                returnInt = sqLiteDatabase.delete(MovieContract.MovieEntry.TABLE_NAME, whereClause, selectionArgs);
                if(returnInt <= 0)
                    throw new SQLException("Failed to delete " + uri);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri" + uri);
        }

        return returnInt;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s,
                      @Nullable String[] strings) {
        return 0;
    }
}
