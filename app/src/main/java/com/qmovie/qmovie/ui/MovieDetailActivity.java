package com.qmovie.qmovie.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.qmovie.qmovie.R;

public class MovieDetailActivity extends AppCompatActivity
{
    public static final String DETAIL_URI = "DETAIL_URI";
    public static final int DEFAULT_ELEVATION = 16;

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        if (savedInstanceState == null)
        {
            Bundle args = new Bundle();
            args.putParcelable(MovieDetailActivity.DETAIL_URI, getIntent().getData());
            MovieDetailsFragment fragment = new MovieDetailsFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction().add(R.id.container, fragment).commit();
        }

        setToolbar();
    }

    private void setToolbar()
    {
        toolbar = (Toolbar) findViewById(R.id.movieDetailToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setElevation(DEFAULT_ELEVATION);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_movie_detail, menu);
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
}
