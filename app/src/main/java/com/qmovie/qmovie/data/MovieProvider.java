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

import org.jsoup.helper.StringUtil;

import java.util.Arrays;

public class MovieProvider extends ContentProvider
{
    private static final UriMatcher uriMatcher = buildUriMatcher();
    private static final SQLiteQueryBuilder showsMovieTheaterJoinedTable;

    static
    {
        showsMovieTheaterJoinedTable = new SQLiteQueryBuilder();
        showsMovieTheaterJoinedTable
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

    private static final SQLiteQueryBuilder movieShowJoinedTable;
    static
    {
        movieShowJoinedTable = new SQLiteQueryBuilder();
        movieShowJoinedTable.setTables(MovieContract.MovieEntry.TABLE_NAME + " INNER JOIN " + MovieContract.ShowEntry.TABLE_NAME + " ON " +
                                               MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID + " = " +
                                               MovieContract.ShowEntry.TABLE_NAME + "." + MovieContract.ShowEntry.COLUMN_MOVIE_KEY);
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
            case MOVIE:
                String[] countProjection = new String[projection.length + 1];
                System.arraycopy(projection, 0, countProjection, 0, projection.length);
                countProjection[projection.length] = "COUNT(*)";

                // Make sure the shows are relevant
                String dateSelection = "show_date > ?";
                selection = selection==null? dateSelection : selection + " and " + dateSelection;
                if (selectionArgs == null)
                {
                    selectionArgs = new String[]{String.valueOf(System.currentTimeMillis())};
                }
                else
                {
                    // Add new parameter to the selectionArgs
                    String[] selectionArgsWithDateSelection = new String[selectionArgs.length + 1];
                    System.arraycopy(selectionArgs, 0, selectionArgsWithDateSelection, 0, selectionArgs.length);
                    selectionArgsWithDateSelection[selectionArgsWithDateSelection.length - 1] = String
                            .valueOf(System.currentTimeMillis());
                    selectionArgs = selectionArgsWithDateSelection;
                }

                queryResult = movieShowJoinedTable.query(movieDBHelper.getReadableDatabase(),
                                                         countProjection,
                                                         selection,
                                                         selectionArgs,
                                                         StringUtil.join(Arrays.asList(projection), ","),
                                                         "COUNT(*) > 1",sortOrder);
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
                queryResult = getMovieWithShowsCursor(uri, projection, sortOrder);
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

    private Cursor getMovieWithShowsCursor(Uri uri, String[] projection, String sortOrder)
    {
        long movieId = ContentUris.parseId(uri);

        return showsMovieTheaterJoinedTable.query(movieDBHelper.getReadableDatabase(),
                                      projection,
                                      MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID + "= ? AND " + MovieContract.ShowEntry.COLUMN_SHOW_DATE + " > ?",
                                      new String[]{
                                              String.valueOf(movieId), String.valueOf(System.currentTimeMillis())},
                                      null,
                                      null, sortOrder);
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
