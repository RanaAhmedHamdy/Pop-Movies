package com.rana.movieapp;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.rana.movieapp.data.MovieContract;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Rana on 1/5/2016.
 */
public class Utility {

    //load image bitmap for favourite movie from the internet to be used when offline
    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    //save favourite movie poster to internal storage
    public static void saveFile(Context context, Bitmap b, String picName){
        FileOutputStream fos;
        try {
            fos = context.openFileOutput(picName, Context.MODE_PRIVATE);
            b.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

    //load favorite movie poster from internal local storage
    public static Bitmap loadBitmap(Context context, String picName){
        Bitmap b = null;
        FileInputStream fis;
        try {
            fis = context.openFileInput(picName);
            b = BitmapFactory.decodeStream(fis);
            fis.close();

        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return b;
    }

    public static ArrayList<MovieItem> getAllMovies(Context context) {
        ArrayList<MovieItem> movieList = new ArrayList<>();

        //get all favourite movies using content provider
        Cursor cursor = context.getContentResolver().query(MovieContract.MovieEntry.CONTENT_URI ,null, null, null, null);

        if (cursor.moveToFirst()) {
            do {

                //get all favourite movies
                MovieItem movie = new MovieItem(cursor.getString(0), cursor.getString(1), cursor.getString(2),
                        cursor.getString(3), cursor.getString(4), cursor.getString(5));

                // add movie to arraylist
                movieList.add(movie);
            } while (cursor.moveToNext());
        }
        return movieList;
    }

    public static MovieItem getById(String id, Context context) {
        //get details of selected favourite movie using content provider
        Cursor cursor = context.getContentResolver().query(MovieContract.MovieEntry.CONTENT_URI ,null, MovieContract.MovieEntry._ID + "=?", new String[]{id}, null);
        MovieItem obj = null;
        try {
            if (cursor.moveToFirst()) {
                obj = new MovieItem(cursor.getString(0), cursor.getString(1), cursor.getString(2),
                        cursor.getString(3), cursor.getString(4), cursor.getString(5));
            }
        } finally {
            cursor.close();
        }
        return obj;
    }

    public static ArrayList<VideoItem> getVideoById(String id, Context context) {
        //get all saved videos for selected favourite movie
        Cursor cursor = context.getContentResolver().query(MovieContract.VideoEntry.CONTENT_URI ,null, MovieContract.VideoEntry.MOVIE_ID + "=?", new String[]{id}, null);

        ArrayList<VideoItem> items = new ArrayList<>();
        try {
            if (cursor.moveToFirst()) {
                do {
                    items.add(new VideoItem(cursor.getString(0), cursor.getString(1), cursor.getString(2),
                            cursor.getString(3)));
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }
        return items;
    }

    //get all reviews for selected favourite movie
    public static ArrayList<ReviewItem> getReviewById(String id, Context context) {
        Cursor cursor = context.getContentResolver().query(MovieContract.ReviewEntry.CONTENT_URI ,null, MovieContract.ReviewEntry.MOVIE_ID + "=?", new String[]{id}, null);
        ArrayList<ReviewItem> items = new ArrayList<>();
        try {
            if (cursor.moveToFirst()) {
                do {
                    items.add(new ReviewItem(cursor.getString(0), cursor.getString(1), cursor.getString(2)));
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }
        return items;
    }
}
