package com.qmovie.qmovie.ui.movieList;

import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.qmovie.qmovie.R;
import com.qmovie.qmovie.data.MovieContract;

public class MovieListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>
{
    private static final int    HANDLER_WHAT = 1;
    private static final int    MOVIE_LOADER = 10;
    private static final String SELECTED_KEY = "ITEM_SELECTED_POSITION";

    private int itemSelectedPosition = ListView.INVALID_POSITION;
    private MovieAdapter movieAdapter;
    private ListView     movieListView;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(MOVIE_LOADER, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        movieAdapter = new MovieAdapter(getActivity(), null, 0);
        initMovieListView(rootView);

        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY))
        {
            itemSelectedPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        return rootView;
    }

    private void initMovieListView(View rootView)
    {
        movieListView = (ListView) rootView.findViewById(R.id.moviesListView);
        movieListView.setAdapter(movieAdapter);
        movieListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                MovieListFragment.this.itemSelectedPosition = position;

                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                if (cursor != null)
                {
                    ((Callback) getActivity())
                            .onItemSelected(MovieContract.MovieEntry.buildMovieUri(cursor.getLong(MovieAdapter.MOVIE_ID_COLUMN_INDEX)));
                }
            }
        });
    }


    public void selectFirstItem()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        {
            movieListView.getChildAt(0).setActivated(true);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle)
    {
        switch (i)
        {
            case MOVIE_LOADER:
                return new CursorLoader(getActivity(),
                                        MovieContract.MovieEntry.CONTENT_URI,
                                        MovieAdapter.projection,
                                        null,
                                        null,
                                        null);
            default:
                return null;
        }
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data)
    {
        switch (loader.getId())
        {
            case MOVIE_LOADER:
                movieAdapter.swapCursor(data);
                if (itemSelectedPosition != ListView.INVALID_POSITION)
                {
                    movieListView.smoothScrollToPosition(itemSelectedPosition);
                }
                else
                {
                    (new NotifyDataUpdated(this)).sendEmptyMessage(HANDLER_WHAT);
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {
        switch (loader.getId())
        {
            case MOVIE_LOADER:
                movieAdapter.swapCursor(null);
                break;
        }
    }

    private static class NotifyDataUpdated extends Handler
    {
        private MovieListFragment movieListFragment;

        private NotifyDataUpdated(MovieListFragment movieListFragment)
        {
            this.movieListFragment = movieListFragment;
        }

        @Override
        public void handleMessage(Message msg)
        {
            if (movieListFragment.movieListView.getAdapter() != null && movieListFragment.movieListView.getAdapter()
                    .getCount() != 0)
            {
                if (msg.what == HANDLER_WHAT)
                {
                    Cursor cursor = (Cursor) movieListFragment.movieListView.getItemAtPosition(0);
                    if (cursor != null)
                    {
                        ((MovieListFragment.Callback) movieListFragment.getActivity()).onDataUpdate(MovieContract.MovieEntry
                                                                                                            .buildMovieWithNameAndYearUri(
                                                                                                                    cursor.getString(
                                                                                                                            MovieAdapter.MOVIE_NAME_COLUMN_INDEX),
                                                                                                                    cursor.getInt(
                                                                                                                            MovieAdapter.MOVIE_PUBLISHED_YEAR_COLUMN_INDEX)));
                    }
                }
            }
        }
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback
    {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri movieUri);

        public void onDataUpdate(Uri firstItemUri);
    }
}
