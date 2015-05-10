package com.qmovie.qmovie.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;

public class MovieProvider extends ContentProvider
{
    private static final UriMatcher uriMatcher = buildUriMatcher();
    private static final SQLiteQueryBuilder showsJoinedTable;

    static
    {
        showsJoinedTable = new SQLiteQueryBuilder();
        showsJoinedTable
                .setTables("(" + MovieContract.MovieEntry.TABLE_NAME + " INNER JOIN " + MovieContract.ShowEntry.TABLE_NAME + " ON " +
                                   MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID + " = " +
                                   MovieContract.ShowEntry.TABLE_NAME + "." + MovieContract.ShowEntry.COLUMN_MOVIE_KEY + ") INNER JOIN " + MovieContract.TheaterEntry.TABLE_NAME +
                                   " ON " + MovieContract.ShowEntry.TABLE_NAME + "." + MovieContract.ShowEntry.COLUMN_THEATER_KEY +
                                   " = " + MovieContract.TheaterEntry.TABLE_NAME + "." + MovieContract.TheaterEntry._ID);
    }

    private static final SQLiteQueryBuilder theaterShowJoin;

    static
    {
        theaterShowJoin = new SQLiteQueryBuilder();
        theaterShowJoin
                .setTables(MovieContract.TheaterEntry.TABLE_NAME + " INNER JOIN " + MovieContract.ShowEntry.TABLE_NAME + " ON " +
                                   MovieContract.ShowEntry.TABLE_NAME + "." + MovieContract.ShowEntry.COLUMN_THEATER_KEY + " = " +
                                   MovieContract.TheaterEntry.TABLE_NAME + "." + MovieContract.TheaterEntry._ID);
        theaterShowJoin.setDistinct(true);
    }

    public static final  int SHOW                     = 100;
    private static final int SHOWS_OF_MOVIE_ID        = 101;
    public static final  int MOVIE                    = 200;
    public static final  int MOVIE_WITH_NAME_AND_YEAR = 201;
    public static final  int MOVIE_WITH_SHOWS         = 202;
    private static final int MOVIE_WITH_ID            = 203;
    public static final  int THEATER                  = 300;
    private static final int THEATER_OF_MOVIE_ID      = 301;

    private MovieDbHelper movieDBHelper;

    @Override
    public boolean onCreate()
    {
        movieDBHelper = new MovieDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
    {
        Cursor queryResult;

        switch (uriMatcher.match(uri))
        {
            case SHOW:
                queryResult = getShowCursor(uri, projection, sortOrder);
                break;
            case MOVIE:
                queryResult = movieDBHelper.getReadableDatabase().query(MovieContract.MovieEntry.TABLE_NAME,
                                                                        projection,
                                                                        selection,
                                                                        selectionArgs,
                                                                        null,
                                                                        null,
                                                                        sortOrder);
                break;
            case MOVIE_WITH_ID:
                queryResult = movieDBHelper.getReadableDatabase().query(MovieContract.MovieEntry.TABLE_NAME,
                                                                        projection,
                                                                        MovieContract.MovieEntry._ID + " = ?",
                                                                        new String[]{
                                                                                String.valueOf(ContentUris.parseId(uri))},
                                                                        null,
                                                                        null,
                                                                        sortOrder);
                break;
            case MOVIE_WITH_NAME_AND_YEAR:
                String name = MovieContract.MovieEntry.getMovieNameFromUri(uri);
                int year = MovieContract.MovieEntry.getMoviePublishedYearFromUri(uri);
                queryResult = movieDBHelper.getReadableDatabase().query(MovieContract.MovieEntry.TABLE_NAME,
                                                                        projection,
                                                                        MovieContract.MovieEntry.COLUMN_MOVIE_NAME + " = ? AND " +
                                                                                MovieContract.MovieEntry.COLUMN_PUBLISHED_YEAR + " = ? ",
                                                                        new String[]{name, String.valueOf(year)},
                                                                        null,
                                                                        null,
                                                                        sortOrder);
                break;
            case MOVIE_WITH_SHOWS:
                queryResult = getMovieWithShowsCursor(uri);
                break;
            case THEATER:
                queryResult = movieDBHelper.getReadableDatabase().query(MovieContract.TheaterEntry.TABLE_NAME,
                                                                        projection,
                                                                        selection,
                                                                        selectionArgs,
                                                                        null,
                                                                        null,
                                                                        sortOrder);
                break;
            case THEATER_OF_MOVIE_ID:
                queryResult = theaterShowJoin.query(movieDBHelper.getReadableDatabase(),
                                                    new String[]{MovieContract.TheaterEntry.COLUMN_THEATER_NAME},
                                                    MovieContract.ShowEntry.TABLE_NAME + "." + MovieContract.ShowEntry.COLUMN_MOVIE_KEY + " = ?",
                                                    new String[]{String.valueOf(ContentUris.parseId(uri))},
                                                    null,
                                                    null,
                                                    null);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        queryResult.setNotificationUri(getContext().getContentResolver(), uri);
        return queryResult;
    }

    private Cursor getMovieWithShowsCursor(Uri uri)
    {
        int showsStartDate = MovieContract.MovieEntry.getShowsStartDateFromUri(uri);

        String innerSql = "SELECT " + MovieContract.ShowEntry.COLUMN_MOVIE_KEY + ", group_concat(strftime('%H:%M'," +
                MovieContract.ShowEntry.COLUMN_SHOW_DATE + "),',') " + MovieContract.ShowEntry.COLUMN_SHOW_DATE + " FROM " + MovieContract.ShowEntry.TABLE_NAME + " s WHERE s." +
                MovieContract.ShowEntry._ID + " IN (SELECT " + MovieContract.ShowEntry._ID + " FROM " +
                MovieContract.ShowEntry.TABLE_NAME + " WHERE " + MovieContract.ShowEntry.COLUMN_MOVIE_KEY + " = s." +
                MovieContract.ShowEntry.COLUMN_MOVIE_KEY + " ORDER BY " + MovieContract.ShowEntry.COLUMN_SHOW_DATE + " LIMIT " + String
                .valueOf(showsStartDate) + ") GROUP BY s." + MovieContract.ShowEntry.COLUMN_MOVIE_KEY;

        String sql = "SELECT m.*, dates." + MovieContract.ShowEntry.COLUMN_SHOW_DATE + " FROM(" + innerSql + ") dates, " + MovieContract.MovieEntry.TABLE_NAME +
                " m WHERE dates." + MovieContract.ShowEntry.COLUMN_MOVIE_KEY + " = m." + MovieContract.MovieEntry._ID;

        return movieDBHelper.getReadableDatabase().rawQuery(sql, null);
    }

    private Cursor getShowCursor(Uri uri, String[] projection, String sortOrder)
    {
        Cursor queryResult;
        long startDate = MovieContract.ShowEntry.getStartDateFromUri(uri);

        String selection = null;
        String[] selectionArgs = null;
        if (startDate != 0)
        {
            selection = MovieContract.ShowEntry.COLUMN_SHOW_DATE + " > ?";
            selectionArgs = new String[]{Long.toString(startDate)};
        }

        queryResult = showsJoinedTable
                .query(movieDBHelper.getReadableDatabase(), projection, selection, selectionArgs, null, null, sortOrder);
        return queryResult;
    }

    @Override
    public String getType(Uri uri)
    {
        switch (uriMatcher.match(uri))
        {
            case SHOW:
                return MovieContract.ShowEntry.CONTENT_TYPE;
            case MOVIE:
                return MovieContract.MovieEntry.CONTENT_TYPE;
            case MOVIE_WITH_NAME_AND_YEAR:
                return MovieContract.MovieEntry.CONTENT_ITEM_TYPE;
            case MOVIE_WITH_SHOWS:
                return MovieContract.MovieEntry.CONTENT_TYPE;
            case THEATER:
                return MovieContract.TheaterEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values)
    {
        final SQLiteDatabase movieWritableDB = movieDBHelper.getWritableDatabase();
        Uri insertedUri;

        String tableName;
        int match = uriMatcher.match(uri);
        long _id;

        switch (match)
        {
            case SHOW:
                tableName = MovieContract.ShowEntry.TABLE_NAME;
                _id = movieWritableDB.insert(tableName, null, values);
                insertedUri = MovieContract.ShowEntry.buildShowUri(_id);
                break;
            case MOVIE:
                tableName = MovieContract.MovieEntry.TABLE_NAME;
                _id = movieWritableDB.insert(tableName, null, values);
                insertedUri = MovieContract.MovieEntry.buildMovieUri(_id);
                break;
            case THEATER:
                tableName = MovieContract.TheaterEntry.TABLE_NAME;
                _id = movieWritableDB.insert(tableName, null, values);
                insertedUri = MovieContract.TheaterEntry.buildTheaterUri(_id);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return insertedUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs)
    {
        final SQLiteDatabase movieWritableDatabase = movieDBHelper.getWritableDatabase();

        // For getting deleted rows count
        selection = selection == null ? "1" : selection;
        int rowsDeleted;
        switch (uriMatcher.match(uri))
        {
            case SHOW:
                rowsDeleted = movieWritableDatabase.delete(MovieContract.ShowEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case MOVIE:
                rowsDeleted = movieWritableDatabase.delete(MovieContract.MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case THEATER:
                rowsDeleted = movieWritableDatabase.delete(MovieContract.TheaterEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsDeleted != 0)
        {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
    {
        final SQLiteDatabase movieWritableDatabase = movieDBHelper.getWritableDatabase();

        // For getting updated rows count
        selection = selection == null ? "1" : selection;
        int rowsUpdated;
        String tableName;
        switch (uriMatcher.match(uri))
        {
            case SHOW:
                tableName = MovieContract.ShowEntry.TABLE_NAME;
                break;
            case MOVIE:
                tableName = MovieContract.MovieEntry.TABLE_NAME;
                break;
            case THEATER:
                tableName = MovieContract.TheaterEntry.TABLE_NAME;
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        rowsUpdated = movieWritableDatabase.update(tableName, values, selection, selectionArgs);

        if (rowsUpdated != 0)
        {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    public static UriMatcher buildUriMatcher()
    {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        String contentAuthority = MovieContract.CONTENT_AUTHORITY;
        uriMatcher.addURI(contentAuthority, MovieContract.PATH_SHOW, SHOW);
        uriMatcher.addURI(contentAuthority, MovieContract.PATH_SHOW + "/#", SHOWS_OF_MOVIE_ID);

        uriMatcher.addURI(contentAuthority, MovieContract.PATH_MOVIE, MOVIE);
        uriMatcher
                .addURI(contentAuthority, MovieContract.PATH_MOVIE + "/" + MovieContract.PATH_SHOW + "/#", MOVIE_WITH_SHOWS);
        uriMatcher.addURI(contentAuthority, MovieContract.PATH_MOVIE + "/#", MOVIE_WITH_ID);
        uriMatcher.addURI(contentAuthority, MovieContract.PATH_MOVIE + "/*/#", MOVIE_WITH_NAME_AND_YEAR);

        uriMatcher.addURI(contentAuthority, MovieContract.PATH_THEATER, THEATER);
        uriMatcher.addURI(contentAuthority, MovieContract.PATH_THEATER + "/#", THEATER_OF_MOVIE_ID);

        return uriMatcher;
    }

    @Override
    public int bulkInsert(Uri uri, @NonNull ContentValues[] values)
    {
        final SQLiteDatabase movieWritableDatabase = movieDBHelper.getWritableDatabase();

        String tableName;
        int match = uriMatcher.match(uri);
        switch (match)
        {
            case SHOW:
                tableName = MovieContract.ShowEntry.TABLE_NAME;
                break;
            case MOVIE:
                tableName = MovieContract.MovieEntry.TABLE_NAME;
                break;
            case THEATER:
                tableName = MovieContract.TheaterEntry.TABLE_NAME;
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        movieWritableDatabase.beginTransaction();
        int returnCount = 0;

        try
        {
            for (ContentValues value : values)
            {
                long _id = movieWritableDatabase.insert(tableName, null, value);
                if (_id != -1)
                {
                    returnCount++;
                }
            }
            movieWritableDatabase.setTransactionSuccessful();
        } finally
        {
            movieWritableDatabase.endTransaction();
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return returnCount;
    }
}
