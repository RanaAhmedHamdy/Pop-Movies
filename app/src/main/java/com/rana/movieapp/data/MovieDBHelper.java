package com.rana.movieapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Rana on 12/27/2015.
 */
public class MovieDBHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    public static final String DATABASE_NAME = "movies.db";

    public MovieDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        //create favorite movie details table
        final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " + MovieContract.MovieEntry.TABLE_NAME + " (" +
                MovieContract.MovieEntry.MOVIE_TITLE + " TEXT UNIQUE NOT NULL, " +
                MovieContract.MovieEntry.MOVIE_IMAGE_URL + " TEXT NOT NULL, " +
                MovieContract.MovieEntry.MOVIE_OVERVIEW + " TEXT NOT NULL, " +
                MovieContract.MovieEntry.MOVIE_VOTE_AVERAGE + " TEXT NOT NULL, " +
                MovieContract.MovieEntry.MOVIE_RELEASE_DATE + " TEXT NOT NULL, " +
                MovieContract.MovieEntry._ID + " TEXT PRIMARY KEY " +
                " );";

        //create table for saving videos of favorite movies
        final String SQL_CREATE_VIDEO_TABLE = "CREATE TABLE " + MovieContract.VideoEntry.TABLE_NAME + " (" +
                MovieContract.VideoEntry._ID + " TEXT PRIMARY KEY, " +
                MovieContract.VideoEntry.VIDEO_KEY + " TEXT NOT NULL, " +
                MovieContract.VideoEntry.VIDEO_NAME + " TEXT NOT NULL, " +
                MovieContract.VideoEntry.VIDEO_TYPE + " TEXT NOT NULL, " +
                MovieContract.VideoEntry.MOVIE_ID + " TEXT NOT NULL, " +
                " FOREIGN KEY (" + MovieContract.VideoEntry.MOVIE_ID + ") REFERENCES " +
                MovieContract.MovieEntry.TABLE_NAME + " (" + MovieContract.MovieEntry._ID + "));";

        //create table for saving reviews of favorite movies
        final String SQL_CREATE_REVIEW_TABLE = "CREATE TABLE " + MovieContract.ReviewEntry.TABLE_NAME + " (" +
                MovieContract.ReviewEntry._ID + " TEXT PRIMARY KEY, " +
                MovieContract.ReviewEntry.REVIEW_AUTHOR + " TEXT NOT NULL, " +
                MovieContract.ReviewEntry.REVIEW_CONTENT + " TEXT NOT NULL, " +
                MovieContract.ReviewEntry.MOVIE_ID + " TEXT NOT NULL, " +
                " FOREIGN KEY (" + MovieContract.ReviewEntry.MOVIE_ID + ") REFERENCES " +
                MovieContract.MovieEntry.TABLE_NAME + " (" + MovieContract.MovieEntry._ID + "));";

        sqLiteDatabase.execSQL(SQL_CREATE_MOVIE_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_VIDEO_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_REVIEW_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        //drop tables of new version of database detected
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieContract.ReviewEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieContract.VideoEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieContract.MovieEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
