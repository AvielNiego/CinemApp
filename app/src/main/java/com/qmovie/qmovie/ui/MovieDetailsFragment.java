package com.qmovie.qmovie.ui;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.qmovie.qmovie.R;
import com.qmovie.qmovie.data.MovieContract;
import com.squareup.picasso.Picasso;

public class MovieDetailsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>
{
    private static final int    DETAIL_LOADER  = 0;
    private static final int    THEATER_LOADER = 1;
    public static final  String LOG_TAG        = MovieDetailsFragment.class.getSimpleName();

    private TextView  movieNameTextView;
    private TextView  movieGenreTextView;
    private TextView  movieLimitAgeTextView;
    private TextView  summaryTextView;
    private ImageView moviePosterImageView;
    private Uri       movieUri;

    private String              shareString;
    private ShareActionProvider shareActionProvider;


    public MovieDetailsFragment()
    {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_detail_fragment, menu);
        MenuItem menuItem = menu.findItem(R.id.menu_item_share);

        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        if (shareActionProvider != null && shareString != null)
        {
            shareActionProvider.setShareIntent(createShareIntent());
        }
        else
        {
            Log.d(LOG_TAG, "Share action provider is null");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Bundle args = getArguments();
        if (args != null)
        {
            movieUri = args.getParcelable(MovieDetailActivity.DETAIL_URI);
        }

        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);
        initDataMembers(rootView);
        return rootView;
    }

    private void initDataMembers(View rootView)
    {
        moviePosterImageView = (ImageView) rootView.findViewById(R.id.moviePosterImageViewDetail);
        movieNameTextView = (TextView) rootView.findViewById(R.id.movieNameTextViewDetail);
        movieGenreTextView = (TextView) rootView.findViewById(R.id.movieGenreTextViewDetail);
        movieLimitAgeTextView = (TextView) rootView.findViewById(R.id.movieLimitAgeTextViewDetail);
        summaryTextView = (TextView) rootView.findViewById(R.id.movieSummaryTextViewDetail);
        summaryTextView.setMovementMethod(new ScrollingMovementMethod());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        getLoaderManager().initLoader(THEATER_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        switch (id)
        {
            case DETAIL_LOADER:
                if (movieUri == null)
                {
                    return null;
                }
                return new CursorLoader(getActivity(), movieUri, null, null, null, null);
            case THEATER_LOADER:
                if (movieUri == null)
                {
                    return null;
                }
                return new CursorLoader(getActivity(),
                                        MovieContract.TheaterEntry.buildTheaterWithMovieID(ContentUris.parseId(movieUri)),
                                        null,
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
        if (data == null || !data.moveToFirst())
        {
            return;
        }

        switch (loader.getId())
        {
            case DETAIL_LOADER:
                loadMovieDetailCursor(data);
                break;
            case THEATER_LOADER:
                Log.v(LOG_TAG, data.getCount() + " theaters loaded");
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {
        Log.v(LOG_TAG, String.valueOf(loader.getId()));
    }

    public void loadMovieDetailCursor(Cursor data)
    {
        String movieName = data.getString(data.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_NAME));
        movieNameTextView.setText(movieName);
        getActivity().setTitle(movieName);
        movieGenreTextView.setText(data.getString(data.getColumnIndex(MovieContract.MovieEntry.COLUMN_GENRE)));
        movieLimitAgeTextView.setText(data.getString(data.getColumnIndex(MovieContract.MovieEntry.COLUMN_LIMIT_AGE)));
        summaryTextView.setText(data.getString(data.getColumnIndex(MovieContract.MovieEntry.COLUMN_SUMMARY)));

        Picasso.with(getActivity()).load(data.getString(data.getColumnIndex(MovieContract.MovieEntry.COLUMN_PICTURE)))
                .into(moviePosterImageView);

        shareString = String.format(getString(R.string.share_action_string), movieName);
        if (shareActionProvider != null)
        {
            shareActionProvider.setShareIntent(createShareIntent());
        }
    }

    private Intent createShareIntent()
    {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareString);
        return sharingIntent;
    }
}