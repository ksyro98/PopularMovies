package com.example.dell.popularmovies;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


public class Movie {

    private int id;
    private String title;
    private String imagePath;
    private String releaseDate;
    private double rateAverage;
    private String plotSynopsis;
    private boolean favorite;
    private HashMap<String, String> reviewMap = new HashMap<>();
    private ArrayList<String> trailerList = new ArrayList<>();

    private static final String BASE_URL = "http://image.tmdb.org/t/p/";
    private static final String SIZE = "w185";

    Movie(int id, String title, String imagePath, String releaseDate, double rateAverage, String plotSynopsis){
        this.id = id;
        this.title = title;
        this.imagePath = imagePath;
        this.releaseDate = releaseDate;
        this.rateAverage = rateAverage;
        this.plotSynopsis = plotSynopsis;
        this.favorite = false;
    }


    /**
     * A private constructor to get a movie using a specific JSONObject from a JSONArray.
     * @param jsonArray The JSONArray containing the JSONObject we want to use.
     * @param i The position of the JSONObject in the JSONArray.
     * @throws JSONException Thrown when we cannot convert the JSONObject to a movie.
     */
    private Movie(JSONArray jsonArray, int i) throws JSONException {
        JSONObject jsonMovieObject = jsonArray.getJSONObject(i);
        id = jsonMovieObject.getInt("id");
        title = jsonMovieObject.getString("title");
        imagePath = jsonMovieObject.getString("poster_path");
        releaseDate = jsonMovieObject.getString("release_date");
        rateAverage = jsonMovieObject.getDouble("vote_average");
        plotSynopsis = jsonMovieObject.getString("overview");
        favorite = false;
    }

    /**
     *A public constructor to get a movie using a String which is created from the toString method of the Movie class.
     *
     * @param movieString the String created from the toString method
     */
    Movie (String movieString){
        String[] movieDetails = movieString.split("_");
        id = Integer.parseInt(movieDetails[0]);
        title = movieDetails[1];
        imagePath = movieDetails[2];
        releaseDate = movieDetails[3];
        rateAverage = Double.parseDouble(movieDetails[4]);
        plotSynopsis = movieDetails[5];
        favorite = false;
    }

    /**
     * Returns all the movies in an ArrayList from a String in JSON form that contains a JSONArray.
     *
     * @param JSONString The String in JSON form.
     * @return An ArrayList with all the movies in the String.
     * @throws JSONException Thrown when we cannot convert the JSONObject to a movie.
     */
    static ArrayList<Movie> getAllMovies(String JSONString) throws JSONException {
        ArrayList<Movie> movieList = new ArrayList<>();

        JSONObject jsonObject = new JSONObject(JSONString);
        JSONArray jsonArray = jsonObject.getJSONArray("results");
        for(int i=0; i<jsonArray.length(); i++){
            movieList.add(new Movie(jsonArray, i));
        }

        return movieList;
    }


    /**
     * Gets the absolute image path for a movie, because the imagePath variable stores a relative image path.
     *
     * @return A String that represents the absolute image path.
     */
    public String getAbsoluteImagePath(){
        return BASE_URL + SIZE + imagePath;
    }

    String getRelativeImagePath(){
        return imagePath;
    }


    /**
     * Creates a String that contains the values from all the variables in a Movie Object.
     *
     * @return A String containing the values from all the variables in a Movie Object.
     */
    @Override
    public String toString() {
        return id + "_" +
                title + "_" +
                imagePath + "_" +
                releaseDate + "_" +
                String.valueOf(rateAverage) + "_" +
                plotSynopsis;
    }




    /**
     *
     * @return The movie's id.
     */
    public int getId(){
        return id;
    }

    /**
     * @return The movie's title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return The movie's release date
     */
    String getReleaseDate() {
        return releaseDate;
    }

    /**
     * @return The movie's average rating.
     */
    double getRateAverage() {
        return rateAverage;
    }

    /**
     * @return The movie's plot synopsis.
     */
    String getPlotSynopsis() {
        return plotSynopsis;
    }


    /**
     *
     * @return If the movie is marked as favorite.
     */
    boolean getFavorite(){
        return favorite;
    }


    /**
     * Changes a movie favorite status.
     *
     * @param favorite true if the user sets the movie to favorite and false if he unsets it
     */
    void setFavorite(boolean favorite){
        this.favorite = favorite;
    }


    /**
     * Adds a review to the reviewMap.
     *
     * @param key the author of the review
     * @param value the text of the review
     */
    void addReviewToMap(String key, String value){
        reviewMap.put(key, value);
    }

    /**
     * Returns one review from the reviewMap.
     *
     * @param key the author of the review that we want to return
     * @return the review written by the author whose name is given as input
     */
    String getReviewFromMap(String key){
        return reviewMap.get(key);
    }


    /**
     *
     * @return the reviewMap (a map that contains all the reviews)
     */
    HashMap<String, String> getReviewMap(){
        return reviewMap;
    }


    /**
     * Sets a new map as the reviewMap.
     *
     * @param reviewMap the map we want to set as the new reviewMap
     */
    void setReviewMap(HashMap<String, String> reviewMap){
        this.reviewMap = reviewMap;
    }


    /**
     * Adds a new trailer to the trailerList.
     *
     * @param value the trailer link that we want to add
     */
    void addTrailerToList(String value){
        trailerList.add(value);
    }


    /**
     * Returns one trailer's link based on the index we enter
     *
     * @param i the index we enter
     * @return the trailer's link that corresponds to index i
     */
    String getTrailerFromList(int i){
        return trailerList.get(i);
    }


    /**
     *
     * @return the trailerList (a list that contains all the trailers' links)
     */
    ArrayList<String> getTrailerList(){
        return trailerList;
    }


    /**
     * Sets a new list as the trailerList.
     *
     * @param trailerList the trailer we want to set as the new trailerList
     */
    void setTrailerList(ArrayList<String> trailerList){
        this.trailerList = trailerList;
    }


}
