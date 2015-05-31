package com.qmovie.qmovie.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.qmovie.qmovie.R;
import com.qmovie.qmovie.sync.CinemappSyncAdapter;
import com.qmovie.qmovie.ui.movieList.MovieListFragment;


public class MainActivity extends ActionBarActivity implements MovieListFragment.Callback
{
    private static final String DETAIL_FRAGMENT_TAG = "DETAIL_FRAGMENT_TAG";
    private boolean twoPane;
    private boolean hasSavedInstanceState;
    private MovieListFragment movieListFragment;

    public MovieListFragment getMovieListFragment()
    {
        if (movieListFragment == null)
        {
            movieListFragment = ((MovieListFragment) getSupportFragmentManager().findFragmentById(R.id.movieListFragment));
        }

        return movieListFragment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // The detail container view will be present only in the large-screen layouts
        // (res/layout-sw600dp). If this view is present, then the activity should be
        // in two-pane mode.
        twoPane = (findViewById(R.id.movieDetailContainer) != null);
        hasSavedInstanceState = (savedInstanceState != null);

        CinemappSyncAdapter.initializeSyncAdapter(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings)
//        {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onItemSelected(Uri movieUri)
    {
        if (twoPane)
        {
            replaceMovieFragment(movieUri);
        } else {
            Intent intent = new Intent(this, MovieDetailActivity.class).setData(movieUri);
            startActivity(intent);
        }
    }

    @Override
    public void onDataUpdate(Uri firstItemUri)
    {
        if (twoPane && !hasSavedInstanceState)
        {
            replaceMovieFragment(firstItemUri);
            getMovieListFragment().selectFirstItem();
        }
    }

    public void replaceMovieFragment(Uri movieUri)
    {
        Bundle args = new Bundle();
        args.putParcelable(MovieDetailActivity.DETAIL_URI, movieUri);

        MovieDetailsFragment fragment = new MovieDetailsFragment();
        fragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.movieDetailContainer, fragment, DETAIL_FRAGMENT_TAG).commit();
    }
}
