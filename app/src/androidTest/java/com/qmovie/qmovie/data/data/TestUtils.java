package com.qmovie.qmovie.data.data;

import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;

import com.qmovie.qmovie.data.MovieContract.MovieEntry;
import com.qmovie.qmovie.data.MovieContract.ShowEntry;
import com.qmovie.qmovie.data.MovieContract.TheaterEntry;
import com.qmovie.qmovie.data.utils.PollingCheck;

import java.util.Map;
import java.util.Set;

public class TestUtils extends AndroidTestCase
{
    private static int YEAR_COUNTER = 0;

    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }

    public static ContentValues createTestMovieValues()
    {
        YEAR_COUNTER++;

        ContentValues testValues = new ContentValues();
        testValues.put(MovieEntry.COLUMN_MOVIE_NAME, "Shir");
        testValues.put(MovieEntry.COLUMN_GENRE, "דרמה");
        testValues.put(MovieEntry.COLUMN_SUMMARY, "שיר נולדה בשנת 1992 למשפחת קונפורטי והיא לומדת פסיכולוגיה");
        testValues.put(MovieEntry.COLUMN_PICTURE, "https://scontent-lhr.xx.fbcdn.net/hphotos-xfp1/v/t1.0-9/71422_10202906986947657_2881714353852244906_n.jpg?oh=caa8fcc856a24b79f4952cc98d1ac201&oe=55AF59D9");
        testValues.put(MovieEntry.COLUMN_MOVIE_LENGTH, 2900);
        testValues.put(MovieEntry.COLUMN_PUBLISHED_YEAR, YEAR_COUNTER);
        testValues.put(MovieEntry.COLUMN_LIMIT_AGE, 18);
        testValues.put(MovieEntry.COLUMN_TRAILER, "http://www.google.com/");

        return testValues;
    }

    public static ContentValues createTestTheaterValues()
    {
        ContentValues testValues = new ContentValues();
        testValues.put(TheaterEntry.COLUMN_THEATER_NAME, "הבית של שיר");
        testValues.put(TheaterEntry.COLUMN_COORD_LAT, 32.0133765);
        testValues.put(TheaterEntry.COLUMN_COORD_LONG, 34.7812093);

        return testValues;
    }

    public static ContentValues createTestShowValues(long movieID, long theaterID)
    {
        ContentValues testValues = new ContentValues();
        testValues.put(ShowEntry.COLUMN_MOVIE_KEY, movieID);
        testValues.put(ShowEntry.COLUMN_THEATER_KEY, theaterID);
        testValues.put(ShowEntry.COLUMN_SHOW_DATE, System.currentTimeMillis() + 10000);

        return testValues;
    }

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                                 "' did not match the expected value '" +
                                 expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }

    static class TestContentObserver extends ContentObserver
    {
        final HandlerThread mHT;
        boolean mContentChanged;

        static TestContentObserver getTestContentObserver()
        {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        private TestContentObserver(HandlerThread ht)
        {
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }

        // On earlier versions of Android, this onChange method is called
        @Override
        public void onChange(boolean selfChange)
        {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri)
        {
            mContentChanged = true;
        }

        public void waitForNotificationOrFail()
        {
            // Note: The PollingCheck class is taken from the Android CTS (Compatibility Test Suite).
            // It's useful to look at the Android CTS source for ideas on how to test your Android
            // applications.  The reason that PollingCheck works is that, by default, the JUnit
            // testing framework is not running on the main Android application thread.
            new PollingCheck(5000)
            {
                @Override
                protected boolean check()
                {
                    return mContentChanged;
                }
            }.run();
            mHT.quit();
        }
    }
}
