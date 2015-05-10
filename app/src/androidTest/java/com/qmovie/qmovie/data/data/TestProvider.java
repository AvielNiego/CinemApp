package com.qmovie.qmovie.data.data;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.AndroidTestCase;

import com.qmovie.qmovie.data.MovieContract;
import com.qmovie.qmovie.data.MovieDbHelper;
import com.qmovie.qmovie.data.MovieProvider;

public class TestProvider extends AndroidTestCase
{


    private static final String LOG_TAG = TestProvider.class.getSimpleName();

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        //deleteAllRecords();
    }

    private void deleteAllRecords()
    {
        deleteAllRecordsFromDB();
    }

    public void deleteAllRecordsFromDB()
    {
        MovieDbHelper dbHelper = new MovieDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.delete(MovieContract.ShowEntry.TABLE_NAME, null, null);
        db.delete(MovieContract.TheaterEntry.TABLE_NAME, null, null);
        db.delete(MovieContract.MovieEntry.TABLE_NAME, null, null);
        db.close();
    }

    public void deleteAllRecordsFromProvider()
    {
        Cursor cursor;
        mContext.getContentResolver().delete(MovieContract.ShowEntry.CONTENT_URI, null, null);
        cursor = mContext.getContentResolver().query(MovieContract.ShowEntry.CONTENT_URI, null, null, null, null);
        assertEquals("Error: Records not deleted from Show table during delete", 0, cursor.getCount());
        cursor.close();

        mContext.getContentResolver().delete(MovieContract.TheaterEntry.CONTENT_URI, null, null);
        cursor = mContext.getContentResolver().query(MovieContract.TheaterEntry.CONTENT_URI, null, null, null, null);
        assertEquals("Error: Records not deleted from Show table during delete", 0, cursor.getCount());
        cursor.close();

        mContext.getContentResolver().delete(MovieContract.MovieEntry.CONTENT_URI, null, null);
        cursor = mContext.getContentResolver().query(MovieContract.MovieEntry.CONTENT_URI, null, null, null, null);
        assertEquals("Error: Records not deleted from Show table during delete", 0, cursor.getCount());
        cursor.close();
    }

    public void testShowsQuery()
    {
        Cursor cursor = getContext().getContentResolver()
                .query(MovieContract.ShowEntry.CONTENT_URI,null,null,null,null);

        System.out.println(cursor.getCount());

        cursor.close();
    }

    public void testProviderRegistry()
    {
        PackageManager pm = mContext.getPackageManager();

        // We define the component name based on the package name from the context and the
        // MovieProvider class.
        ComponentName componentName = new ComponentName(mContext.getPackageName(), MovieProvider.class.getName());
        try
        {
            // Fetch the provider info using the component name from the PackageManager
            // This throws an exception if the provider isn't registered.
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            // Make sure that the registered authority matches the authority from the Contract.
            assertEquals("Error: MovieProvider registered with authority: " + providerInfo.authority +
                                 " instead of authority: " + MovieContract.CONTENT_AUTHORITY,
                         providerInfo.authority,
                         MovieContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e)
        {
            // I guess the provider isn't registered correctly.
            assertTrue("Error: MovieProvider not registered at " + mContext.getPackageName(), false);
        }
    }

    public void testInsertProvider()
    {
        Uri MovieUri = testInsertProvider(MovieContract.MovieEntry.CONTENT_URI, TestUtils.createTestMovieValues());
        Uri theaterUri = getContext().getContentResolver()
                .insert(MovieContract.TheaterEntry.CONTENT_URI, TestUtils.createTestTheaterValues());

        testInsertProvider(MovieContract.ShowEntry.CONTENT_URI,
                           TestUtils.createTestShowValues(ContentUris.parseId(MovieUri), ContentUris.parseId(theaterUri)));
    }

    public Uri testInsertProvider(Uri uri, ContentValues values)
    {
        TestUtils.TestContentObserver tco = TestUtils.TestContentObserver.getTestContentObserver();
        getContext().getContentResolver().registerContentObserver(uri, true, tco);
        Uri insertedUri = getContext().getContentResolver().insert(uri, values);
        tco.waitForNotificationOrFail();
        getContext().getContentResolver().unregisterContentObserver(tco);

        long insertedID = ContentUris.parseId(insertedUri);
        assertTrue(insertedID != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        Cursor cursor = getContext().getContentResolver()
                .query(uri, null, // leaving "columns" null just returns all the columns.
                       null, // cols for "where" clause
                       null, // values for "where" clause
                       null  // sort order
                );

        TestUtils.validateCursor("Error validating MovieEntry.", cursor, values);

        return insertedUri;
    }

    public void testQueryMovieWithNameAndYearProvider()
    {
        ContentValues testMovieValues = TestUtils.createTestMovieValues();
        testInsertProvider(MovieContract.MovieEntry.CONTENT_URI, testMovieValues);

        String movieName = testMovieValues.getAsString(MovieContract.MovieEntry.COLUMN_MOVIE_NAME);
        int moviePublishedYear = testMovieValues.getAsInteger(MovieContract.MovieEntry.COLUMN_PUBLISHED_YEAR);
        Cursor queryResult = getContext().getContentResolver()
                .query(MovieContract.MovieEntry.buildMovieWithNameAndYearUri(movieName, moviePublishedYear), null, null, null, null);

        TestUtils.validateCursor("Error validating MovieEntry.", queryResult, testMovieValues);
    }

    public void testDeleteRecords()
    {
        testInsertProvider();

        TestUtils.TestContentObserver movieObserver = TestUtils.TestContentObserver.getTestContentObserver();
        getContext().getContentResolver().registerContentObserver(MovieContract.MovieEntry.CONTENT_URI, true, movieObserver);

        TestUtils.TestContentObserver theaterObserver = TestUtils.TestContentObserver.getTestContentObserver();
        getContext().getContentResolver()
                .registerContentObserver(MovieContract.TheaterEntry.CONTENT_URI, true, theaterObserver);

        TestUtils.TestContentObserver showObserver = TestUtils.TestContentObserver.getTestContentObserver();
        getContext().getContentResolver().registerContentObserver(MovieContract.ShowEntry.CONTENT_URI, true, showObserver);

        deleteAllRecordsFromProvider();

        movieObserver.waitForNotificationOrFail();
        theaterObserver.waitForNotificationOrFail();
        showObserver.waitForNotificationOrFail();

        getContext().getContentResolver().unregisterContentObserver(movieObserver);
        getContext().getContentResolver().unregisterContentObserver(theaterObserver);
        getContext().getContentResolver().unregisterContentObserver(showObserver);
    }

    public void testUpdateMovie()
    {
        ContentValues testMovieValues = TestUtils.createTestMovieValues();

        Uri insert = getContext().getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI, testMovieValues);
        long movieID = ContentUris.parseId(insert);
        assertTrue(movieID != -1);

        ContentValues updatedValues = new ContentValues(testMovieValues);
        updatedValues.put(MovieContract.MovieEntry._ID, movieID);
        updatedValues.put(MovieContract.MovieEntry.COLUMN_GENRE, "Comedy");

        Cursor movieCursor = getContext().getContentResolver()
                .query(MovieContract.MovieEntry.CONTENT_URI, null, null, null, null);

        TestUtils.TestContentObserver tco = TestUtils.TestContentObserver.getTestContentObserver();
        movieCursor.registerContentObserver(tco);

        int count = getContext().getContentResolver().update(MovieContract.MovieEntry.CONTENT_URI,
                                                             updatedValues,
                                                             MovieContract.MovieEntry._ID + " =?",
                                                             new String[]{String.valueOf(movieID)});
        assertEquals(count, 1);
        tco.waitForNotificationOrFail();
        movieCursor.unregisterContentObserver(tco);
        movieCursor.close();

        Cursor updatedMovieCursor = getContext().getContentResolver()
                .query(MovieContract.MovieEntry.CONTENT_URI, null, MovieContract.MovieEntry._ID + "=" + movieID, null, null);
        TestUtils.validateCursor("Error validating movie entry update.", updatedMovieCursor, updatedValues);
        updatedMovieCursor.close();
    }

    private static final int BULK_INSERT_RECORDS_TO_INSERT = 10;

    public void testBulkInsert()
    {
        ContentValues[] bulkInsertContentValues = new ContentValues[BULK_INSERT_RECORDS_TO_INSERT];
        for (int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++)
        {
            bulkInsertContentValues[i] = TestUtils.createTestMovieValues();
        }

        // Register a content observer for our bulk insert.
        TestUtils.TestContentObserver movieObserver = TestUtils.TestContentObserver.getTestContentObserver();
        getContext().getContentResolver().registerContentObserver(MovieContract.MovieEntry.CONTENT_URI, true, movieObserver);

        int insertCount = getContext().getContentResolver()
                .bulkInsert(MovieContract.MovieEntry.CONTENT_URI, bulkInsertContentValues);

        movieObserver.waitForNotificationOrFail();
        getContext().getContentResolver().unregisterContentObserver(movieObserver);

        assertEquals(insertCount, BULK_INSERT_RECORDS_TO_INSERT);

        // A cursor is your primary interface to the query results.
        Cursor cursor = getContext().getContentResolver()
                .query(MovieContract.MovieEntry.CONTENT_URI, null, // leaving "columns" null just returns all the columns.
                       null, // cols for "where" clause
                       null, // values for "where" clause
                       MovieContract.MovieEntry.COLUMN_MOVIE_NAME + " ASC"  // sort order == by DATE ASCENDING
                );

        // we should have as many records in the database as we've inserted
        assertEquals(BULK_INSERT_RECORDS_TO_INSERT, cursor.getCount());

        // and let's make sure they match the ones we created
        cursor.moveToFirst();
        for (int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++, cursor.moveToNext())
        {
            TestUtils.validateCurrentRecord("testBulkInsert.  Error validating MovieEntry " + i,
                                            cursor,
                                            bulkInsertContentValues[i]);
        }
        cursor.close();
    }
}
