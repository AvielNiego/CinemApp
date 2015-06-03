package com.qmovie.qmovie.ui;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.qmovie.qmovie.R;
import com.qmovie.qmovie.Utilities;
import com.qmovie.qmovie.data.MovieContract;

public class MovieShowsAdapter extends CursorAdapter
{

    public MovieShowsAdapter(Context context, Cursor c, int flags)
    {
        super(context, c, flags);
    }

    public static final String[] PROJECTION = {
            MovieContract.ShowEntry.TABLE_NAME + "." + MovieContract.ShowEntry._ID,
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_MOVIE_NAME,
            MovieContract.TheaterEntry.TABLE_NAME + "." + MovieContract.TheaterEntry.COLUMN_THEATER_NAME,
            MovieContract.ShowEntry.TABLE_NAME + "." + MovieContract.ShowEntry.COLUMN_SHOW_DATE};

    public static final int SHOW_ID_COLUMN_INDEX      = 0;
    public static final int MOVIE_NAME_COLUMN_INDEX   = 1;
    public static final int THEATER_NAME_COLUMN_INDEX = 2;
    public static final int SHOW_DATE_COLUMN_INDEX    = 3;

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.movie_show_list_item, parent, false);
        view.setTag(new ViewHolder(view));
        return view;
    }

    @Override
    public void bindView(View view, final Context context,final Cursor cursor)
    {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        viewHolder.showTheaterName.setText(cursor.getString(THEATER_NAME_COLUMN_INDEX));
        final long showDate = cursor.getLong(SHOW_DATE_COLUMN_INDEX);
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
                Intent intent = new Intent(context, RemainderReceiver.class).setData(((Activity) context).getIntent().getData());
                intent.putExtra(RemainderReceiver.MOVIE_NAME_EXTRA_KEY, cursor.getString(MOVIE_NAME_COLUMN_INDEX));
                alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

                alarmMgr.set(AlarmManager.RTC_WAKEUP, showDate - 15 * 1000, alarmIntent);

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



    private class ViewHolder
    {
        final TextView showTheaterName;
        final Button   remainderButton;
        final TextView showDate;

        public ViewHolder(View view)
        {
            this.showTheaterName = (TextView) view.findViewById(R.id.showTheaterName);
            this.remainderButton = (Button) view.findViewById(R.id.remainderButton);
            this.showDate = (TextView) view.findViewById(R.id.showDate);
        }
    }
}
