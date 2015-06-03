package com.qmovie.qmovie.ui.movieList;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.qmovie.qmovie.R;
import com.qmovie.qmovie.Utilities;
import com.qmovie.qmovie.data.MovieContract;
import com.squareup.picasso.Picasso;

public class MovieAdapter extends CursorAdapter
{
    public static final String[] PROJECTION = {
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_MOVIE_NAME,
            MovieContract.MovieEntry.COLUMN_PUBLISHED_YEAR,
            MovieContract.MovieEntry.COLUMN_GENRE,
            MovieContract.MovieEntry.COLUMN_MOVIE_LENGTH,
            MovieContract.MovieEntry.COLUMN_PICTURE};

    public static final int MOVIE_ID_COLUMN_INDEX             = 0;
    public static final int MOVIE_NAME_COLUMN_INDEX           = 1;
    public static final int MOVIE_PUBLISHED_YEAR_COLUMN_INDEX = 2;
    public static final int MOVIE_GENRE_COLUMN_INDEX          = 3;
    public static final int MOVIE_LENGTH_COLUMN_INDEX         = 4;
    public static final int MOVIE_POSTER_COLUMN_INDEX         = 5;


    public MovieAdapter(Context context, Cursor c, int flags)
    {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.movie_list_item, parent, false);
        view.setTag(new ViewHolder(view));
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor)
    {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        String movieName = cursor.getString(MOVIE_NAME_COLUMN_INDEX);
        viewHolder.nameTextView.setText(movieName);
        viewHolder.genreTextView.setText(cursor.getString(MOVIE_GENRE_COLUMN_INDEX));
        viewHolder.movieShowLength.setText(Utilities.getReadableLength(context, cursor.getInt(MOVIE_LENGTH_COLUMN_INDEX)));

        Picasso.with(context).load(cursor.getString(MOVIE_POSTER_COLUMN_INDEX)).placeholder(R.mipmap.ic_launcher)
                .into(viewHolder.moviePoster);
    }

    private class ViewHolder
    {
        final TextView  nameTextView;
        final TextView  genreTextView;
        final TextView  movieShowLength;
        final ImageView moviePoster;

        public ViewHolder(View view)
        {
            this.nameTextView = (TextView) view.findViewById(R.id.movieNameTextViewListItem);
            this.genreTextView = (TextView) view.findViewById(R.id.movieGenreTextViewListItem);
            this.movieShowLength = (TextView) view.findViewById(R.id.movieShowMinutesTextViewListItem);
            this.moviePoster = (ImageView) view.findViewById(R.id.moviePosterImageViewListItem);
        }
    }
}
