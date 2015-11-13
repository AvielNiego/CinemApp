package com.qmovie.qmovie.ui.movieList;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.qmovie.qmovie.R;
import com.qmovie.qmovie.Utilities;
import com.qmovie.qmovie.data.MovieContract;
import com.qmovie.qmovie.data.UpdateDataTask;

public class MovieListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, SharedPreferences.OnSharedPreferenceChangeListener
{
    private static final int    HANDLER_WHAT    = 1;
    private static final int    MOVIE_LOADER    = 10;
    private static final String SELECTED_KEY    = "ITEM_SELECTED_POSITION";
    private static final String NEW_SEARCH_TEXT = "search_text";

    private int itemSelectedPosition = ListView.INVALID_POSITION;
    private MovieAdapter movieAdapter;
    private ListView     movieListView;
    private TextView     emptyListTextView;

    private String searchText;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(MOVIE_LOADER, null, this);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        movieAdapter = new MovieAdapter(getActivity(), null, 0);
        movieAdapter.setSearchString(searchText);

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

        emptyListTextView = (TextView) rootView.findViewById(R.id.empty_list_view);
        movieListView.setEmptyView(emptyListTextView);
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
                    ((Callback) getActivity()).onItemSelected(MovieContract.MovieEntry
                                                                      .buildMovieUri(cursor.getLong(MovieAdapter.MOVIE_ID_COLUMN_INDEX)));
                }
            }
        });
    }


    // For big screen - master detail view
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
                String selection = null;
                String[] selectionArgs = null;
                if (bundle != null)
                {
                    selection = MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_MOVIE_NAME + " LIKE ?";
                    selectionArgs = new String[]{"%" + bundle.getString(NEW_SEARCH_TEXT) + "%"};
                }
                return new CursorLoader(getActivity(),
                                        MovieContract.MovieEntry.CONTENT_URI,
                                        MovieAdapter.PROJECTION,
                                        selection,
                                        selectionArgs,
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
                movieAdapter.setSearchString(searchText);
                movieAdapter.swapCursor(data);

                if (itemSelectedPosition != ListView.INVALID_POSITION)
                {
                    movieListView.smoothScrollToPosition(itemSelectedPosition);
                }
                else
                {
                    (new NotifyDataUpdated(this)).sendEmptyMessage(HANDLER_WHAT);
                }

                updateEmptyView();
                break;
        }
    }

    public void updateEmptyView()
    {
        if (movieAdapter.getCount() == 0)
        {
            if (searchText != null && !searchText.isEmpty())
            {
                emptyListTextView
                        .setText(getActivity().getString(R.string.empty_movie_list_no_search_re, searchText));
                return;
            }

            @UpdateDataTask.ServerStatus int serverStatus = Utilities.getServerStatus(getActivity());

            switch (serverStatus)
            {
                case UpdateDataTask.SERVER_STATUS_SERVER_DOWN:
                    emptyListTextView.setText(getActivity().getString(R.string.empty_movie_list_server_down));
                    break;
                case UpdateDataTask.SERVER_STATUS_SERVER_INVALID:
                    emptyListTextView.setText(getActivity().getString(R.string.empty_movie_list_server_invalid));
                    break;
                default:
                    if (!Utilities.isNetworkAvailable(getActivity()))
                    {
                        emptyListTextView.setText(getActivity().getString(R.string.empty_movie_list_no_internet));
                    }
            }
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        initSearchView(menu);
    }

    public void initSearchView(Menu menu)
    {
        final MenuItem searchItem = menu.findItem(R.id.action_search);

        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
        {
            @Override
            public boolean onQueryTextSubmit(String query)
            {
                return false;
            }

            @Override
            public boolean onQueryTextChange(final String newText)
            {
                searchText = newText;
                Bundle args = null;
                if (!searchText.isEmpty())
                {
                    args = new Bundle();
                    args.putString(NEW_SEARCH_TEXT, searchText);
                }
                getLoaderManager().restartLoader(MOVIE_LOADER, args, MovieListFragment.this);
                return false;
            }
        });
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        if (key.equals(R.string.pref_location_status_key))
        {
            updateEmptyView();
        }
    }

    @Override
    public void onResume()
    {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.registerOnSharedPreferenceChangeListener(this);
        super.onResume();
    }

    @Override
    public void onPause()
    {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
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
        void onItemSelected(Uri movieUri);

        void onDataUpdate(Uri firstItemUri);
    }
}
