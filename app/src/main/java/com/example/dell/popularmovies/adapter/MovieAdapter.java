package com.example.dell.popularmovies.adapter;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.dell.popularmovies.Movie;
import com.example.dell.popularmovies.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder>{


    private int numberItems;
    private ArrayList<Movie> movies;
    private Context context;
    final private ClickListener clickListener;

    public MovieAdapter(int numberItems, ArrayList<Movie> movies, Context context, ClickListener clickListener){
        super();
        this.numberItems = numberItems;
        this.movies = movies;
        this.context = context;
        this.clickListener = clickListener;
    }

    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForGridItem = R.layout.image_item;
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        View view = layoutInflater.inflate(layoutIdForGridItem, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MovieViewHolder holder, int position) {
        final int currentPosition = position;

        /*code was taken from stackoverflow post:
        https://stackoverflow.com/questions/23391523/load-images-from-disk-cache-with-picasso-if-offline

        With this code the i   mages are loaded from cache first and only if they aren't store in cache they are loaded
        through Internet. If there is no Internet connection they are replaced with a placeholder.
         */
        Picasso.with(context).load(movies.get(position).getAbsoluteImagePath()).networkPolicy(NetworkPolicy.OFFLINE).
                into(holder.imageView, new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError() {
                /*images were taken from:
                 https://www.simplifiedcoding.net/picasso-android-tutorial-picasso-image-loader-library/
                  */
                Picasso.with(context).load(movies.get(currentPosition).getAbsoluteImagePath()).
                        placeholder(R.drawable.placeholder).into(holder.imageView);
            }
        });
    }

    @Override
    public int getItemCount() {
        return numberItems;
    }


    public interface ClickListener{
        void onItemClicked(int movieIndex);
    }


    class MovieViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        @BindView(R.id.image_view) ImageView imageView;


        MovieViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            ButterKnife.bind(this, itemView);

        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            clickListener.onItemClicked(position);
        }
    }

}