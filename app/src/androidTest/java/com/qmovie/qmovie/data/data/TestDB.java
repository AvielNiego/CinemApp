package com.qmovie.qmovie.data.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import com.qmovie.qmovie.data.MovieContract.*;
import com.qmovie.qmovie.data.MovieDbHelper;

import java.util.HashSet;

public class TestDB extends AndroidTestCase
{
    public static final String LOG_TAG = TestDB.class.getSimpleName();

    @Override
    protected void setUp() throws Exception
    {
        getContext().deleteDatabase(MovieDbHelper.DATABASE_NAME);
    }

    public void testCreateDB()
    {
        SQLiteDatabase db = new MovieDbHelper(getContext()).getWritableDatabase();
        assertTrue(db.isOpen());

        // Get all tables exists  DB
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        assertTrue("Error: This means that the database has not been created correctly", cursor.moveToFirst());

        // HashSet of all the tables expected to be in the DB
        final HashSet<String> tableNameHashSet = new HashSet<>();
        tableNameHashSet.add(MovieEntry.TABLE_NAME);
        tableNameHashSet.add(ShowEntry.TABLE_NAME);
        tableNameHashSet.add(TheaterEntry.TABLE_NAME);

        // Check if all the tables had been created
        do
        {
            tableNameHashSet.remove(cursor.getString(0));
        }
        while (cursor.moveToNext());

        assertTrue("Error: Not all tables had been created", tableNameHashSet.isEmpty());

        cursor.close();
        db.close();
    }



    public void testInsertAndQueryTables()
    {
        SQLiteDatabase db = new MovieDbHelper(getContext()).getWritableDatabase();
        assertTrue("Failed to open DB", db.isOpen());

        ContentValues movieValues = TestUtils.createTestMovieValues();
        long insertedMovieID = db.insert(MovieEntry.TABLE_NAME, null, movieValues);
        assertFalse("Failed to add row " + movieValues.toString(), insertedMovieID == -1);

        Cursor movieCursor = db.query(MovieEntry.TABLE_NAME,
                                      null,
                                      MovieEntry._ID + " = ?",
                                      new String[]{Long.toString(insertedMovieID)},
                                      null,
                                      null,
                                      null);
        assertTrue("Failed moving the cursor to first, no rows?", movieCursor.moveToFirst());
        TestUtils.validateCurrentRecord("Row returned from DB is not valid", movieCursor, movieValues);
        movieCursor.close();


        ContentValues theaterValues = TestUtils.createTestTheaterValues();
        long insertTheaterID = db.insert(TheaterEntry.TABLE_NAME, null, theaterValues);
        assertFalse("Failed to add row " + theaterValues.toString(), insertTheaterID == -1);

        Cursor theaterCursor = db.query(TheaterEntry.TABLE_NAME,
                                      null,
                                      TheaterEntry._ID + " = ?",
                                      new String[]{Long.toString(insertTheaterID)},
                                      null,
                                      null,
                                      null);
        assertTrue("Failed moving the cursor to first, no rows?", theaterCursor.moveToFirst());
        theaterCursor.close();


        ContentValues showValues = TestUtils.createTestShowValues(insertedMovieID, insertTheaterID);
        long insertShowID = db.insert(ShowEntry.TABLE_NAME, null, showValues);
        assertFalse("Failed to add row " + showValues.toString(), insertShowID == -1);

        Cursor showCursor = db.query(ShowEntry.TABLE_NAME,
                                        null,
                                        ShowEntry._ID + " = ?",
                                        new String[]{Long.toString(insertShowID)},
                                        null,
                                        null,
                                        null);
        assertTrue("Failed moving the cursor to first, no rows?", showCursor.moveToFirst());
        TestUtils.validateCurrentRecord("Row returned from DB is not valid", showCursor, showValues);
        showCursor.close();

        db.close();
    }
}
