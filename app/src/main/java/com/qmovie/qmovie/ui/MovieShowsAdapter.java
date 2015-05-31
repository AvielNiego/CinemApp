package com.qmovie.qmovie.ui;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.CursorAdapter;

public class MovieShowsAdapter extends CursorAdapter
{

    public MovieShowsAdapter(Context context, Cursor c, int flags)
    {
        super(context, c, flags);
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent)
    {
        return null;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor)
    {

    }
}
