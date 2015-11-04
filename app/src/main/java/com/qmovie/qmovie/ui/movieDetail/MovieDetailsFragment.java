package com.qmovie.qmovie.ui.movieDetail;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.qmovie.qmovie.R;
import com.qmovie.qmovie.Utilities;
import com.qmovie.qmovie.data.MovieContract;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class MovieDetailsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>
{
    private static final int    DETAIL_LOADER = 0;
    private static final int    SHOWS_LOADER  = 1;
    public static final  String LOG_TAG       = MovieDetailsFragment.class.getSimpleName();

    private Uri movieUri;

    private ShareActionProvider shareActionProvider;
    private String              shareString;

    private RecyclerView      showsRecyclerView;
    private MovieShowsAdapter movieShowAdapter;


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


        movieShowAdapter = new MovieShowsAdapter(getActivity(), null, null);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());

        showsRecyclerView = (RecyclerView) rootView.findViewById(R.id.showsRecycleView);
        showsRecyclerView.setLayoutManager(layoutManager);
        showsRecyclerView.setAdapter(movieShowAdapter);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        getLoaderManager().initLoader(SHOWS_LOADER, null, this);
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
            case SHOWS_LOADER:
                if (movieUri == null)
                {
                    return null;
                }
                return new CursorLoader(getActivity(),
                                        MovieContract.MovieEntry.buildMovieWithShowsUri(ContentUris.parseId(movieUri)),
                                        MovieShowsAdapter.PROJECTION,
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
                movieShowAdapter = new MovieShowsAdapter(getActivity(), movieShowAdapter.getShowsCursor(), data);
                showsRecyclerView.swapAdapter(movieShowAdapter, true);
                break;
            case SHOWS_LOADER:
                movieShowAdapter = new MovieShowsAdapter(getActivity(), data, movieShowAdapter.getDetailCursor());
                showsRecyclerView.swapAdapter(movieShowAdapter, true);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {
        switch (loader.getId())
        {
            case DETAIL_LOADER:
                movieShowAdapter = new MovieShowsAdapter(getActivity(), movieShowAdapter.getShowsCursor(), null);
                showsRecyclerView.swapAdapter(movieShowAdapter, true);
                break;
            case SHOWS_LOADER:
                movieShowAdapter = new MovieShowsAdapter(getActivity(), null, movieShowAdapter.getDetailCursor());
                showsRecyclerView.swapAdapter(movieShowAdapter, true);
                break;
        }
    }

    public void loadMovieDetailCursor(Cursor data)
    {
        String movieName = data.getString(data.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_NAME));

        final CollapsingToolbarLayout movieCollapsingToolbarLayout = (CollapsingToolbarLayout) getActivity()
                .findViewById(R.id.movieDetailsCollapsingToolbarLayout);
        if (movieCollapsingToolbarLayout != null)
        {
            movieCollapsingToolbarLayout.setTitle(movieName);
            movieCollapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.BackgroundedText);
            final ImageView toolbarMoviePoster = ((ImageView) movieCollapsingToolbarLayout.findViewById(R.id.toolbarMoviePoster));
            if (toolbarMoviePoster != null)
            {
                int widthPixels = getActivity().getResources().getDisplayMetrics().widthPixels;
                int heightPixels = (int) getResources().getDimension(R.dimen.toolbar_height);

                Picasso.with(getActivity())
                        .load(data.getString(data.getColumnIndex(MovieContract.MovieEntry.COLUMN_PICTURE)))
                        .resize(widthPixels, heightPixels).centerCrop().into(toolbarMoviePoster, new Callback()
                {
                    @Override
                    public void onSuccess()
                    {
                        Drawable drawable = toolbarMoviePoster.getDrawable();
                        if (drawable != null)
                        {
                            Palette.generateAsync(Utilities.drawableToBitmap(drawable), new Palette.PaletteAsyncListener()
                            {
                                @Override
                                public void onGenerated(Palette palette)
                                {
                                    if (palette == null)
                                    {
                                        return;
                                    }

                                    int rgb;
                                    if (palette.getVibrantSwatch() != null)
                                    {
                                        rgb = palette.getVibrantSwatch().getRgb();
                                    }
                                    else if(palette.getMutedSwatch() != null)
                                    {
                                        rgb = palette.getMutedSwatch().getRgb();
                                    }
                                    else
                                    {
                                        return;
                                    }

                                    movieCollapsingToolbarLayout.setContentScrim(new ColorDrawable(rgb));

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                                    {
                                        Window window = getActivity().getWindow();
                                        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

                                        float[] hsv = new float[3];
                                        Color.colorToHSV(rgb, hsv);
                                        hsv[2] *= 0.7f; // value component

                                        window.setStatusBarColor(Color.HSVToColor(hsv));
                                    }

                                    Utilities.setEdgeGlowColor(showsRecyclerView, rgb);
                                }
                            });
                        }
                    }

                    @Override
                    public void onError()
                    {

                    }
                });
            }
        }

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