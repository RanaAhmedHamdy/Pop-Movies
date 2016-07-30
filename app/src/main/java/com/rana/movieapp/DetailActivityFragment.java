package com.rana.movieapp;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.rana.movieapp.data.MovieContract;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {

    private TextView overview;
    private TextView title;
    private TextView voteAverage;
    private TextView date;
    private ImageView poster;
    private ArrayList<VideoItem> video;
    private ArrayList<ReviewItem> reviews;
    private ListView videoList;
    private LinearLayout reviewList;
    private SimpleAdapter adapter;
    private ArrayAdapter<ReviewItem> ad;
    private ShareActionProvider mShareActionProvider;
    private boolean favorite;
    private Button favoriteBtn;
    private MovieItem currentMovie;
    private ProgressDialog p;

    public DetailActivityFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        favorite = false;
        overview = (TextView) rootView.findViewById(R.id.overview);
        title = (TextView) rootView.findViewById(R.id.title);
        voteAverage = (TextView) rootView.findViewById(R.id.vote_average);
        date = (TextView) rootView.findViewById(R.id.date);
        poster = (ImageView) rootView.findViewById(R.id.poster);
        videoList = (ListView) rootView.findViewById(R.id.video_list);
        reviewList = (LinearLayout) rootView.findViewById(R.id.review_list);
        favoriteBtn = (Button) rootView.findViewById(R.id.mark_as_favourite);
        video = new ArrayList<>();
        reviews = new ArrayList<>();

        //get movie details saved in parcelable
        Bundle args = getArguments();
        if(args != null) {
            currentMovie = args.getParcelable(getString(R.string.detail_movie));
        } else {
            return null;
        }

        favoriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if you add movie as favourite
                //save all data in the database
                new downloadImage().execute();

                favoriteBtn.setEnabled(false);
                favoriteBtn.setBackgroundColor(Color.GRAY);
            }
        });

        videoList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getActivity(),
                        video.get(position).getName(), Toast.LENGTH_LONG)
                        .show();

                //open video in available applications
                String ID = video.get(position).getKey();
                String videoUrl = getString(R.string.youtube_url) + ID;
                startActivity(new Intent(Intent.ACTION_VIEW, Uri
                        .parse(videoUrl)));
            }
        });

        new FetchMoviesTask().execute();
        return rootView;
    }

    //fetch reviews and videos
    public class FetchMoviesTask extends AsyncTask<Void, Void, ArrayList<VideoItem>> {

        public FetchMoviesTask() {
            p = new ProgressDialog(getActivity());
        }

        @Override
        protected void onPostExecute(ArrayList<VideoItem> videoData) {
            super.onPostExecute(videoData);

            //if not favorite movie load poster from the internet
            if(favorite == false) {
                Picasso.with(getActivity().getApplicationContext()).load(getString(R.string.image_url) + currentMovie.getImageUrl()).into(poster);
            } else {
                //if movie is favourite then load poster save in internal storage
                poster.setImageBitmap(Utility.loadBitmap(getActivity(), currentMovie.getImageUrl().substring(1)));
            }
            overview.setText(currentMovie.getOverView());
            title.setText(currentMovie.getOriginalTitle());
            voteAverage.setText(currentMovie.getVoteAverage() + "/10");
            date.setText(currentMovie.getReleaseDate());


            //map name of video to available textview in video row
            List<HashMap<String, String>> aList = new ArrayList<HashMap<String, String>>();
            for (int i = 0; i < video.size(); i++) {
                HashMap<String, String> hm = new HashMap<String, String>();
                hm.put("txt", video.get(i).getName());
                aList.add(hm);
            }
            String[] from = {"txt"};
            int[] to = {R.id.video_title};
            adapter = new SimpleAdapter(getActivity(), aList,
                    R.layout.video_item, from, to);
            videoList.setAdapter(adapter);

            //used to set height for listview
            //because of problem of listview in scrollview
            setListViewHeightBasedOnChildren(videoList);


            //set review to reviews linear layout
            ad = new ArrayAdapter<ReviewItem>(getActivity(), android.R.layout.simple_list_item_1, reviews);
            for (int i = 0; i < ad.getCount(); i++) {
                View item = ad.getView(i, null, null);
                item.setPadding(10, 10, 10, 10);
                reviewList.addView(item);
            }

            //if movie is marked as favorite then disable favorite button
            if (favorite == true) {
                favoriteBtn.setEnabled(false);
                favoriteBtn.setBackgroundColor(Color.GRAY);
            }

            //expose first video trailer to be shared
            if(!video.isEmpty() && mShareActionProvider != null)
                mShareActionProvider.setShareIntent(createShareForecastIntent());

            //dismiss progress dialog after finishing
            if(p.isShowing())
                p.dismiss();
        }

        @Override
        protected ArrayList<VideoItem> doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String videoJsonStr = null;

            /*check if this movie is marked as favorite*/
            if(Utility.getById(currentMovie.getId(), getActivity()) != null) {
                favorite = true;
                Log.i("f", "in");
            }

            //if it is not favorite then fetch reviews and video from the internet
            //else get them from the database
            if(favorite == false) {
                try {
                    //get available video trailers for this movie
                    final String FORECAST_BASE_URL =
                            getString(R.string.api_url) + currentMovie.getId() + "/videos";
                    final String API_KEY_PARAM = "api_key";

                    Uri.Builder builder = Uri.parse(FORECAST_BASE_URL)
                            .buildUpon()
                            .appendQueryParameter(API_KEY_PARAM, getString(R.string.api_key));

                    Log.i("url", builder.build().toString());
                    URL url = new URL(builder.build().toString());

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    InputStream inputStream = urlConnection.getInputStream();
                    StringBuffer buffer = new StringBuffer();
                    if (inputStream == null) {
                        videoJsonStr = null;
                    }
                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line + "\n");
                    }

                    if (buffer.length() == 0) {
                        videoJsonStr = null;
                    }
                    videoJsonStr = buffer.toString();
                } catch (IOException e) {
                    Log.e("PlaceholderFragment", "Error ", e);
                    videoJsonStr = null;
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (final IOException e) {
                            Log.e("PlaceholderFragment", "Error closing stream", e);
                        }
                    }
                }

            //get available reviews for the currently selected movie
            String reviewJsonData = null;
            try {
                final String FORECAST_BASE_URL =
                        "http://api.themoviedb.org/3/movie/" + currentMovie.getId() + "/reviews";
                final String API_KEY_PARAM = "api_key";

                Uri.Builder builder = Uri.parse(FORECAST_BASE_URL)
                        .buildUpon()
                        .appendQueryParameter(API_KEY_PARAM, "505cf850fa9209395657e835f67f2aa0");
                Log.i("url", builder.build().toString());
                URL url = new URL(builder.build().toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    reviewJsonData = null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }
                if (buffer.length() == 0) {
                    reviewJsonData = null;
                }
                reviewJsonData = buffer.toString();
            } catch (IOException e) {
                reviewJsonData = null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }
            }
            try {
                if(reviewJsonData != null)
                    getReviewDataFromJson(reviewJsonData);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            //////////////////////////////////////////////////////////////////////////////
            try {
                if(videoJsonStr != null)
                    return getDataFromJson(videoJsonStr);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            } else {
                //if movie is marked as favorite get video and reviews from database
                //no internet connection needed
                reviews = Utility.getReviewById(currentMovie.getId(), getActivity());
                return video = Utility.getVideoById(currentMovie.getId(), getActivity());
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //set progress dialog until data is being fetched
            p.setTitle("Loading");
            p.setMessage("please wait");
            p.setCancelable(false);
            p.show();
        }
    }

    //get videos from json string
    private ArrayList<VideoItem> getDataFromJson(String movieData) throws JSONException {
        String RESULTS = "results";
        JSONObject movieJson = new JSONObject(movieData);
        JSONArray movieArray = movieJson.getJSONArray(RESULTS);

        for (int i = 0; i < movieArray.length(); i++) {
            JSONObject movie = movieArray.getJSONObject(i);

            String id = movie.getString(getString(R.string.id));
            String type = movie.getString("type");
            String name = movie.getString("name");
            String key = movie.getString("key");

            VideoItem item = new VideoItem(id, key, name, type);
            video.add(item);
        }
        return video;
    }

    //get review data from json
    private void getReviewDataFromJson(String movieData) throws JSONException {
        String RESULTS = "results";
        JSONObject movieJson = new JSONObject(movieData);
        JSONArray movieArray = movieJson.getJSONArray(RESULTS);

        for (int i = 0; i < movieArray.length(); i++) {
            JSONObject movie = movieArray.getJSONObject(i);

            String id = movie.getString(getString(R.string.id));
            String author = movie.getString(getString(R.string.author));
            String content = movie.getString(getString(R.string.content));

            ReviewItem item = new ReviewItem(id, author, content);
            reviews.add(item);
        }
    }

    //save details, videos, reviews and poster for movies marked as favorite
    public class downloadImage extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            //save movie details in database
            ContentValues values = new ContentValues();
            values.put(MovieContract.MovieEntry._ID, currentMovie.getId());
            values.put(MovieContract.MovieEntry.MOVIE_TITLE, currentMovie.getOriginalTitle());
            values.put(MovieContract.MovieEntry.MOVIE_IMAGE_URL, currentMovie.getImageUrl());
            values.put(MovieContract.MovieEntry.MOVIE_OVERVIEW, currentMovie.getOverView());
            values.put(MovieContract.MovieEntry.MOVIE_VOTE_AVERAGE, currentMovie.getVoteAverage());
            values.put(MovieContract.MovieEntry.MOVIE_RELEASE_DATE, currentMovie.getReleaseDate());
            getActivity().getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI, values);

            //save all available videos in database
            ContentValues[] cvArray = new ContentValues[video.size()];
            for(int i = 0; i < video.size(); i++) {
                ContentValues cv = new ContentValues();
                cv.put(MovieContract.VideoEntry._ID, video.get(i).getId());
                cv.put(MovieContract.VideoEntry.MOVIE_ID, currentMovie.getId());
                cv.put(MovieContract.VideoEntry.VIDEO_KEY, video.get(i).getKey());
                cv.put(MovieContract.VideoEntry.VIDEO_NAME, video.get(i).getName());
                cv.put(MovieContract.VideoEntry.VIDEO_TYPE, video.get(i).getType());
                cvArray[i] = cv;
            }

            //bulk insert videos in database
            if ( cvArray.length > 0 ) {
                getActivity().getContentResolver().bulkInsert(MovieContract.VideoEntry.CONTENT_URI, cvArray);
            }

            //save all available reviews in database
            ContentValues[] cvReviewArray = new ContentValues[reviews.size()];
            for(int i = 0; i < reviews.size(); i++) {
                ContentValues cv = new ContentValues();
                cv.put(MovieContract.ReviewEntry._ID, reviews.get(i).getId());
                cv.put(MovieContract.ReviewEntry.MOVIE_ID, currentMovie.getId());
                cv.put(MovieContract.ReviewEntry.REVIEW_AUTHOR, reviews.get(i).getAuthor());
                cv.put(MovieContract.ReviewEntry.REVIEW_CONTENT, reviews.get(i).getContent());
                cvReviewArray[i] = cv;
            }

            //bulk insert reviews in database
            if ( cvReviewArray.length > 0 ) {
                getActivity().getContentResolver().bulkInsert(MovieContract.ReviewEntry.CONTENT_URI, cvReviewArray);
            }

            //download image
            Bitmap image = Utility.getBitmapFromURL(getString(R.string.image_url) + currentMovie.getImageUrl());

            //save image to internal storage
            Utility.saveFile(getActivity(), image, currentMovie.getImageUrl().substring(1));
            return null;
        }
    }

    //set height for videos list according to number of videos
    public void setListViewHeightBasedOnChildren(ListView listView) {
        if (adapter == null) {
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < adapter.getCount(); i++) {
            View listItem = adapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (adapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_detail_fragment, menu);
        MenuItem menuItem = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        mShareActionProvider.setShareIntent(createShareForecastIntent());
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");

        //share first movie trailer
        if(!video.isEmpty())
            shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.youtube_url) + video.get(0).getKey());
        return shareIntent;
    }

     public void onDestroy() {
         //dismiss progress dialog when destroying
        if (p != null && p.isShowing()) {
            p.dismiss();
            p = null;
        }
        super.onDestroy();
    }

    String onSortingChanged( String newLocation ) {
        return null;
    }
}
