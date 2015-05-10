package com.qmovie.qmovie.data.data;

import android.content.UriMatcher;
import android.net.Uri;
import android.test.AndroidTestCase;

import com.qmovie.qmovie.data.MovieContract;
import com.qmovie.qmovie.data.MovieProvider;

public class TestUriMatcher extends AndroidTestCase
{
    private static final String TEST_MOVIE_NAME = "Avengers";
    private static final int    TEST_YEAR       = 2014;

    private static final Uri TEST_MOVIE_DIR                = MovieContract.MovieEntry.CONTENT_URI;
    private static final Uri TEST_MOVIE_WITH_NAME_AND_YEAR = MovieContract.MovieEntry
            .buildMovieWithNameAndYearUri(TEST_MOVIE_NAME, TEST_YEAR);
    private static final Uri TEST_SHOW_DIR                 = MovieContract.ShowEntry.CONTENT_URI;
    private static final Uri TEST_THEATER_DIR              = MovieContract.TheaterEntry.CONTENT_URI;

    public void testUriMatcher()
    {
        UriMatcher testMatcher = MovieProvider.buildUriMatcher();
        String errorFormat = "Error: The {0} was matched incorrectly.";

        assertEquals(String.format(errorFormat, "MOVIE URI"), testMatcher.match(TEST_MOVIE_DIR), MovieProvider.MOVIE);
        assertEquals(String.format(errorFormat, "MOVIE WITH NAME AND YEAR URI"),
                     testMatcher.match(TEST_MOVIE_WITH_NAME_AND_YEAR),
                     MovieProvider.MOVIE_WITH_NAME_AND_YEAR);
        assertEquals(String.format(errorFormat, "SHOW URI"), testMatcher.match(TEST_SHOW_DIR), MovieProvider.SHOW);
        assertEquals(String.format(errorFormat, "THEATER URI"), testMatcher.match(TEST_THEATER_DIR), MovieProvider.THEATER);
    }
}
