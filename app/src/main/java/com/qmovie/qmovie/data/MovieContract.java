package com.qmovie.qmovie.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.format.Time;

/**
 * Defines table and column names for the movie database.
 */
public class MovieContract
{
    // Make sure CONTENT_AUTHORITY is equals to
    public static final String CONTENT_AUTHORITY = "com.cinemapp";
    public static final Uri    BASE_CONTENT_URI  = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_MOVIE   = "movie";
    public static final String PATH_THEATER = "theater";
    public static final String PATH_SHOW    = "show";

    // To make it easy to query for the exact date, we normalize all dates that go into
    // the database to the start of the the Julian day at UTC.
    public static long normalizeDate(long startDate)
    {
        // normalize the start date to the beginning of the (UTC) day
        Time time = new Time();
        time.setToNow();
        int julianDay = Time.getJulianDay(startDate, time.gmtoff);
        return time.setJulianDay(julianDay);
    }

    public static final class MovieEntry implements BaseColumns
    {
        public static final String TABLE_NAME = "movies";

        public static final String COLUMN_MOVIE_NAME     = "name";
        public static final String COLUMN_PUBLISHED_YEAR = "year";
        public static final String COLUMN_GENRE          = "genre";
        public static final String COLUMN_SUMMARY        = "summary";
        public static final String COLUMN_PICTURE        = "pic";
        public static final String COLUMN_LIMIT_AGE      = "age";
        public static final String COLUMN_MOVIE_LENGTH   = "length";
        public static final String COLUMN_TRAILER        = "trailer";

        public static final Uri CONTENT_URI         = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).build();
        private static final Uri MOVIE_WITH_SHOW_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE)
                .appendPath(PATH_SHOW).build();

        public static final String CONTENT_TYPE      = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;

        public static Uri buildMovieUri(long id)
        {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildMovieWithNameAndYearUri(String movieName, int moviePublishedYear)
        {
            return CONTENT_URI.buildUpon().appendPath(movieName).appendPath(Integer.toString(moviePublishedYear)).build();
        }

        public static Uri buildMovieWithShowsUri(long movieId)
        {
            return MOVIE_WITH_SHOW_URI.buildUpon().appendPath(String.valueOf(movieId)).build();
        }

        public static int getShowsStartDateFromUri(Uri uri)
        {
            return Integer.parseInt(uri.getPathSegments().get(2));
        }

        public static String getMovieNameFromUri(Uri uri)
        {
            return uri.getPathSegments().get(1);
        }

        public static int getMoviePublishedYearFromUri(Uri uri)
        {
            return Integer.parseInt(uri.getPathSegments().get(2));
        }

        public static ContentValues createMovieContentValues(long id,String movieName, int publishedYear, String genre,
                                                             String summary, String pictureUrl, String limitAge,
                                                             int movieLength, String trailer)
        {
            ContentValues movieContentValues = new ContentValues();

            movieContentValues.put(_ID, id);
            movieContentValues.put(COLUMN_MOVIE_NAME, movieName);
            movieContentValues.put(COLUMN_PUBLISHED_YEAR, publishedYear);
            movieContentValues.put(COLUMN_GENRE, genre);
            movieContentValues.put(COLUMN_SUMMARY, summary);
            movieContentValues.put(COLUMN_PICTURE, pictureUrl);
            movieContentValues.put(COLUMN_LIMIT_AGE, limitAge);
            movieContentValues.put(COLUMN_MOVIE_LENGTH, movieLength);
            movieContentValues.put(COLUMN_TRAILER, trailer);

            return movieContentValues;
        }
    }

    public static final class TheaterEntry implements BaseColumns
    {
        public static final String TABLE_NAME = "theaters";

        public static final String COLUMN_THEATER_NAME = "name";
        public static final String COLUMN_COORD_LAT    = "coord_lat";
        public static final String COLUMN_COORD_LONG   = "coord_long";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_THEATER).build();

        public static final String CONTENT_TYPE      = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_THEATER;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_THEATER;

        public static Uri buildTheaterUri(long id)
        {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildTheaterWithMovieID(long movieID)
        {
            return CONTENT_URI.buildUpon().appendPath(String.valueOf(movieID)).build();
        }
    }

    public static final class ShowEntry implements BaseColumns
    {
        public static final String TABLE_NAME = "shows";

        public static final String COLUMN_MOVIE_KEY   = "movie_id";
        public static final String COLUMN_THEATER_KEY = "theater_id";
        public static final String COLUMN_SHOW_DATE   = "show_date";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_SHOW).build();

        public static final String CONTENT_TYPE      = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SHOW;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SHOW;

        public static Uri buildShowUri(long id)
        {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildShowWithStartDateUri(long startDate)
        {
            return CONTENT_URI.buildUpon().appendQueryParameter(COLUMN_SHOW_DATE, Long.toString(startDate)).build();
        }

        public static Uri buildShowWithMovieID(long movieID)
        {
            return CONTENT_URI.buildUpon().appendPath(String.valueOf(movieID)).build();
        }

        public static long getStartDateFromUri(Uri uri)
        {
            String dateString = uri.getQueryParameter(COLUMN_SHOW_DATE);
            if (null != dateString && dateString.length() > 0)
            {
                return Long.parseLong(dateString);
            }
            else
            {
                return 0;
            }
        }
    }
}
