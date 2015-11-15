package com.qmovie.qmovie.ui.movieDetail;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.qmovie.qmovie.R;
import com.qmovie.qmovie.data.MovieContract;
import com.qmovie.qmovie.entities.showsPerTheater;
import com.qmovie.qmovie.ui.customComponents.RecycleViewWrapContentEnableLinearLayout;
import com.qmovie.qmovie.ui.movieDetail.movieDetailshowsPerTheater.MovieShowsPerTheaterAdapter;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MovieShowsAdapter extends android.support.v7.widget.RecyclerView.Adapter<MovieShowsAdapter.MovieViewHolder>
{
    public static final int MOVIE_DETAILS_VIEW_TYPE = 0;
    public static final int MOVIE_SHOWS_VIEW_TYPE   = 1;

    public static final String[] PROJECTION = {
            MovieContract.ShowEntry.TABLE_NAME + "." + MovieContract.ShowEntry._ID,
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_MOVIE_NAME,
            MovieContract.TheaterEntry.TABLE_NAME + "." + MovieContract.TheaterEntry.COLUMN_THEATER_NAME,
            MovieContract.ShowEntry.TABLE_NAME + "." + MovieContract.ShowEntry.COLUMN_SHOW_DATE};

    public static final String SORT_ORDER = MovieContract.TheaterEntry.TABLE_NAME + "." +
            MovieContract.TheaterEntry.COLUMN_THEATER_NAME + " DESC," +
            MovieContract.ShowEntry.COLUMN_SHOW_DATE;

    public static final int SHOW_ID_COLUMN_INDEX = 0;

    public static final int MOVIE_NAME_COLUMN_INDEX = 1;

    public static final int THEATER_NAME_COLUMN_INDEX = 2;
    public static final int SHOW_DATE_COLUMN_INDEX    = 3;

    private Context context;
    @Nullable
    private Cursor  showsCursor, detailCursor;
    private List<showsPerTheater> showsPerTheaters;


    public MovieShowsAdapter(Context context, @Nullable Cursor showsCursor, @Nullable Cursor detailCursor)
    {
        this.context = context;
        this.showsCursor = showsCursor;
        readShowsCursor(showsCursor);
        this.detailCursor = detailCursor;
    }

    public void readShowsCursor(@Nullable Cursor showsCursor)
    {
        this.showsPerTheaters = new ArrayList<>();

        if (showsCursor != null && showsCursor.moveToFirst())
        {
            String lastTheaterName = showsCursor.getString(THEATER_NAME_COLUMN_INDEX);
            showsPerTheater showsPerTheater = new showsPerTheater(new ArrayList<Long>(), lastTheaterName);
            showsPerTheater.getShowDates().add(showsCursor.getLong(SHOW_DATE_COLUMN_INDEX));
            while (showsCursor.moveToNext())
            {
                if (!lastTheaterName.equals(showsCursor.getString(THEATER_NAME_COLUMN_INDEX)))
                {
                    showsPerTheaters.add(showsPerTheater);
                    showsPerTheater = new showsPerTheater(new ArrayList<Long>(),
                                                          showsCursor.getString(THEATER_NAME_COLUMN_INDEX));
                }
                showsPerTheater.getShowDates().add(showsCursor.getLong(SHOW_DATE_COLUMN_INDEX));
                lastTheaterName = showsCursor.getString(THEATER_NAME_COLUMN_INDEX);
            }
            showsPerTheaters.add(showsPerTheater);
        }
    }

    @Nullable
    public Cursor getShowsCursor()
    {
        return showsCursor;
    }

    @Nullable
    public Cursor getDetailCursor()
    {
        return detailCursor;
    }

    @Override
    public int getItemViewType(int position)
    {
        if (position == 0)
        {
            return MOVIE_DETAILS_VIEW_TYPE;
        }
        return MOVIE_SHOWS_VIEW_TYPE;
    }

    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view;
        switch (viewType)
        {
            case MOVIE_DETAILS_VIEW_TYPE:
                view = LayoutInflater.from(context).inflate(R.layout.movie_details_card, parent, false);
                return new MovieDetailsViewHolder(view);
            case MOVIE_SHOWS_VIEW_TYPE:
                view = LayoutInflater.from(context).inflate(R.layout.movie_show_list_item, parent, false);
                return new MovieShowViewHolder(view);
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(MovieViewHolder viewHolder, int position)
    {
        if (viewHolder instanceof MovieDetailsViewHolder)
        {
            bindMovieDetailsViewHolder((MovieDetailsViewHolder) viewHolder);
        }
        else if (viewHolder instanceof MovieShowViewHolder)
        {
            bindShowsViewHolder((MovieShowViewHolder) viewHolder, position);
        }
    }

    public void bindMovieDetailsViewHolder(MovieDetailsViewHolder viewHolder)
    {
        if (detailCursor == null)
        {
            return;
        }

        String movieName = detailCursor.getString(detailCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_NAME));
        viewHolder.movieNameTextView.setText(movieName);
        ((Activity) context).setTitle(movieName);
        viewHolder.movieGenreTextView
                .setText(detailCursor.getString(detailCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_GENRE)));
        viewHolder.movieLimitAgeTextView
                .setText(detailCursor.getString(detailCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_AGE_LIMIT)));
        viewHolder.summaryTextView
                .setText(detailCursor.getString(detailCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_SUMMARY)));

        Picasso.with(context)
                .load(detailCursor.getString(detailCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_PICTURE)))
                .into(viewHolder.moviePosterImageView);

        viewHolder.moviePosterImageView.setContentDescription(movieName.concat(" ")
                                                                      .concat(context.getString(R.string.moviePosterContentDescription)));
    }

    public void bindShowsViewHolder(MovieShowViewHolder viewHolder, int position)
    {
        if (showsPerTheaters.isEmpty())
        {
            return;
        }

        // the position counts the details view too, so we need to subtract it
        position--;

        viewHolder.showTheaterName.setText(showsPerTheaters.get(position).getTheaterName());

        viewHolder.showsDatePerTheaterRV
                .setLayoutManager(new RecycleViewWrapContentEnableLinearLayout(viewHolder.showsDatePerTheaterRV.getContext(),
                                                                               LinearLayoutManager.VERTICAL,
                                                                               false));
        MovieShowsPerTheaterAdapter movieShowsPerTheaterAdapter = new MovieShowsPerTheaterAdapter(viewHolder.showsDatePerTheaterRV.getContext(),
                                                                                                  showsPerTheaters.get(position).getShowDates());
        viewHolder.showsDatePerTheaterRV.setAdapter(movieShowsPerTheaterAdapter);
    }

    @Override
    public int getItemCount()
    {
        // Adds 1 for the details view
        return showsPerTheaters.size() + 1;
    }

    abstract class MovieViewHolder extends RecyclerView.ViewHolder
    {
        public MovieViewHolder(View itemView)
        {
            super(itemView);
        }
    }

    class MovieShowViewHolder extends MovieViewHolder
    {
        final TextView     showTheaterName;
        final RecyclerView showsDatePerTheaterRV;

        public MovieShowViewHolder(View view)
        {
            super(view);
            this.showTheaterName = (TextView) view.findViewById(R.id.showTheaterName);
            this.showsDatePerTheaterRV = (RecyclerView) view.findViewById(R.id.showsPerTheaterRecycleView);
        }
    }

    class MovieDetailsViewHolder extends MovieViewHolder
    {
        public final TextView  movieNameTextView;
        public final TextView  movieGenreTextView;
        public final TextView  movieLimitAgeTextView;
        public final TextView  summaryTextView;
        public       ImageView moviePosterImageView;

        public MovieDetailsViewHolder(View itemView)
        {
            super(itemView);

            movieNameTextView = (TextView) itemView.findViewById(R.id.movieNameTextViewDetail);
            movieGenreTextView = (TextView) itemView.findViewById(R.id.movieGenreTextViewDetail);
            movieLimitAgeTextView = (TextView) itemView.findViewById(R.id.movieLimitAgeTextViewDetail);
            moviePosterImageView = (ImageView) itemView.findViewById(R.id.moviePosterImageViewDetail);
            summaryTextView = (TextView) itemView.findViewById(R.id.movieSummaryTextViewDetail);
            summaryTextView.setMovementMethod(new ScrollingMovementMethod());
        }
    }
}
