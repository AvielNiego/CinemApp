package com.qmovie.qmovie.ui.movieDetail;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.qmovie.qmovie.R;
import com.qmovie.qmovie.Utilities;
import com.qmovie.qmovie.data.MovieContract;
import com.qmovie.qmovie.ui.RemainderReceiver;
import com.squareup.picasso.Picasso;

public class MovieShowsAdapter extends android.support.v7.widget.RecyclerView.Adapter<MovieShowsAdapter.MovieViewHolder>
{
    public static final int MOVIE_DETAILS_VIEW_TYPE = 0;
    public static final int MOVIE_SHOWS_VIEW_TYPE   = 1;

    private Context context;
    @Nullable
    private Cursor  showsCursor, detailCursor;

    public static final String[] PROJECTION = {
            MovieContract.ShowEntry.TABLE_NAME + "." + MovieContract.ShowEntry._ID,
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_MOVIE_NAME,
            MovieContract.TheaterEntry.TABLE_NAME + "." + MovieContract.TheaterEntry.COLUMN_THEATER_NAME,
            MovieContract.ShowEntry.TABLE_NAME + "." + MovieContract.ShowEntry.COLUMN_SHOW_DATE};

    public static final int SHOW_ID_COLUMN_INDEX = 0;

    public static final int MOVIE_NAME_COLUMN_INDEX   = 1;
    public static final int THEATER_NAME_COLUMN_INDEX = 2;
    public static final int SHOW_DATE_COLUMN_INDEX    = 3;


    public MovieShowsAdapter(Context context, @Nullable Cursor showsCursor, @Nullable Cursor detailCursor)
    {
        this.context = context;
        this.showsCursor = showsCursor;
        this.detailCursor = detailCursor;
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
            case MOVIE_SHOWS_VIEW_TYPE:
                view = LayoutInflater.from(context).inflate(R.layout.movie_show_list_item, parent, false);
                return new MovieShowViewHolder(view);
            case MOVIE_DETAILS_VIEW_TYPE:
                view = LayoutInflater.from(context).inflate(R.layout.movie_details_card, parent, false);
                return new MovieDetailsViewHolder(view);
            default: return null;
        }
    }

    @Override
    public void onBindViewHolder(MovieViewHolder viewHolder, int position)
    {
        if (viewHolder instanceof MovieShowViewHolder)
        {
            bindShowsViewHolder((MovieShowViewHolder) viewHolder, position);
        }

        if (viewHolder instanceof MovieDetailsViewHolder)
        {
            if (detailCursor == null)
            {
                return;
            }

            MovieDetailsViewHolder movieDetailsViewHolder = (MovieDetailsViewHolder) viewHolder;

            String movieName = detailCursor
                    .getString(detailCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_NAME));
            movieDetailsViewHolder.movieNameTextView.setText(movieName);
            ((Activity) context).setTitle(movieName);
            movieDetailsViewHolder.movieGenreTextView.setText(detailCursor.getString(detailCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_GENRE)));
            movieDetailsViewHolder.movieLimitAgeTextView.setText(detailCursor.getString(detailCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_LIMIT_AGE)));
            movieDetailsViewHolder.summaryTextView.setText(detailCursor.getString(detailCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_SUMMARY)));

            Picasso.with(context).load(detailCursor.getString(detailCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_PICTURE)))
                    .into(movieDetailsViewHolder.moviePosterImageView);
        }
    }

    public void bindShowsViewHolder(MovieShowViewHolder viewHolder, int position)
    {
        if (!(showsCursor != null && showsCursor.moveToFirst() && showsCursor.move(position)))
        {
            return;
        }

        viewHolder.showTheaterName.setText(showsCursor.getString(THEATER_NAME_COLUMN_INDEX));
        final long showDate = showsCursor.getLong(SHOW_DATE_COLUMN_INDEX);
        final String friendlyDayString = Utilities.getFriendlyDayString(context, showDate);
        viewHolder.showDate.setText(friendlyDayString);

        viewHolder.remainderButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final AlarmManager alarmMgr;
                final PendingIntent alarmIntent;

                alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                Activity rootActivity = (Activity) context;
                Intent intent = new Intent(context, RemainderReceiver.class).setData(rootActivity.getIntent().getData());
                intent.putExtra(RemainderReceiver.MOVIE_NAME_EXTRA_KEY, showsCursor.getString(MOVIE_NAME_COLUMN_INDEX));
                intent.putExtra(RemainderReceiver.ROOT_ACTIVITY, rootActivity.getClass());
                alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

                alarmMgr.set(AlarmManager.RTC_WAKEUP, showDate - 15 * 60 * 1000, alarmIntent);

                Snackbar.make(v,
                              context.getString(R.string.snackbar_remainder_set_text, friendlyDayString),
                              Snackbar.LENGTH_LONG).setAction(R.string.cancel, new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        alarmMgr.cancel(alarmIntent);
                    }
                }).show();
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return showsCursor == null ? 0 : showsCursor.getCount();
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
        final TextView showTheaterName;
        final Button   remainderButton;
        final TextView showDate;

        public MovieShowViewHolder(View view)
        {
            super(view);
            this.showTheaterName = (TextView) view.findViewById(R.id.showTheaterName);
            this.remainderButton = (Button) view.findViewById(R.id.remainderButton);
            this.showDate = (TextView) view.findViewById(R.id.showDate);
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
