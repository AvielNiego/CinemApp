package com.qmovie.qmovie.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.qmovie.qmovie.data.MovieContract.*;


/**
 * Manages a local database for movie data.
 */
public class MovieDbHelper extends SQLiteOpenHelper
{
    private static final int    DATABASE_VERSION = 4;
    public static final  String DATABASE_NAME    = "movie.db";
    public static final String SHOWS_DATE_INDEX = "shows_date_index";

    public MovieDbHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " + MovieEntry.TABLE_NAME + " (" +
                MovieEntry._ID + " INTEGER  PRIMARY KEY AUTOINCREMENT, " +
                MovieEntry.COLUMN_MOVIE_NAME + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_PUBLISHED_YEAR + " INTEGER NOT NULL, " +
                MovieEntry.COLUMN_GENRE + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_SUMMARY + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_PICTURE + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_LIMIT_AGE + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_TRAILER + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_MOVIE_LENGTH + " INTEGER NOT NULL, " +
                "UNIQUE (" + MovieEntry.COLUMN_MOVIE_NAME + ", " + MovieEntry.COLUMN_PUBLISHED_YEAR + ") ON CONFLICT REPLACE);";

        db.execSQL(SQL_CREATE_MOVIE_TABLE);

        final String SQL_CREATE_THEATERS_TABLE = "CREATE TABLE " + TheaterEntry.TABLE_NAME + " (" +
                TheaterEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TheaterEntry.COLUMN_THEATER_NAME + " TEXT NOT NULL, " +
                TheaterEntry.COLUMN_COORD_LAT + " REAL, " +
                TheaterEntry.COLUMN_COORD_LONG + " REAL, " +
                "UNIQUE (" + TheaterEntry.COLUMN_THEATER_NAME + ") ON CONFLICT IGNORE);";

        db.execSQL(SQL_CREATE_THEATERS_TABLE);

        final String SQL_CREATE_SHOW_TABLE = "CREATE TABLE " + ShowEntry.TABLE_NAME + " (" +
                ShowEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ShowEntry.COLUMN_MOVIE_KEY + " INTEGER NOT NULL, " +
                ShowEntry.COLUMN_THEATER_KEY + " INTEGER NOT NULL, " +
                ShowEntry.COLUMN_SHOW_DATE + " REAL NOT NULL, " +
                "FOREIGN KEY (" + ShowEntry.COLUMN_MOVIE_KEY + ") REFERENCES " + MovieEntry.TABLE_NAME + " (" +
                MovieEntry._ID + "), " +
                "FOREIGN KEY (" + ShowEntry.COLUMN_THEATER_KEY + ") REFERENCES " + TheaterEntry.TABLE_NAME + " (" +
                TheaterEntry._ID + "), " +
                "UNIQUE (" + ShowEntry.COLUMN_MOVIE_KEY + ", " + ShowEntry.COLUMN_THEATER_KEY + ", " + ShowEntry.COLUMN_SHOW_DATE + ") ON CONFLICT IGNORE);";

        db.execSQL(SQL_CREATE_SHOW_TABLE);

        final String SQL_CREATE_SHOWS_DATE_INDEX = "CREATE INDEX " + SHOWS_DATE_INDEX + " ON " + ShowEntry.TABLE_NAME + " (" + ShowEntry.COLUMN_SHOW_DATE + ");";

        db.execSQL(SQL_CREATE_SHOWS_DATE_INDEX);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TheaterEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ShowEntry.TABLE_NAME);
        db.execSQL("DROP INDEX IF EXISTS " + SHOWS_DATE_INDEX);
        onCreate(db);
    }
}
