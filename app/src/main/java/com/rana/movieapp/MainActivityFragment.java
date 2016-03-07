package com.rana.movieapp;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

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

public class MainActivityFragment extends Fragment {

    private ArrayList<MovieItem> items;
    private GridView mGridView;
    private MovieGridViewAdapter mGridAdapter;
    SharedPreferences sharedpreferences;
    private int mPosition = GridView.INVALID_POSITION;
    private static final String SELECTED_KEY = "selected_position";

    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(MovieItem item);
    }

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    private void updateMovie() {
        sharedpreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        new FetchMoviesTask().execute(sharedpreferences.getString(getString(R.string.pref_key), getString(R.string.pref_default_value)));
    }

    void onSortChanged( ) {
        updateMovie();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        items = new ArrayList<>();
        mGridView = (GridView) rootView.findViewById(R.id.gridView);
        sharedpreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        if(sharedpreferences.getString(getString(R.string.pref_key), getString(R.string.pref_default_value)).trim().equals("favourites"))
            mGridAdapter = new MovieGridViewAdapter(getActivity().getApplicationContext(), R.layout.grid_item, items, "true");
        else
            mGridAdapter = new MovieGridViewAdapter(getActivity().getApplicationContext(), R.layout.grid_item, items, "false");

        mGridView.setAdapter(mGridAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MovieItem item = (MovieItem) parent.getItemAtPosition(position);
                ((Callback) getActivity()).onItemSelected(item);
                mPosition = mGridView.getFirstVisiblePosition();
            }
        });
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mPosition != GridView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovie();
    }

    public class FetchMoviesTask extends AsyncTask<String, Void, ArrayList<MovieItem>> {
        @Override
        protected void onPostExecute(ArrayList<MovieItem> movieData) {
            super.onPostExecute(movieData);

            if(sharedpreferences.getString(getString(R.string.pref_key), getString(R.string.pref_default_value)).trim().equals("favourites"))
                mGridAdapter.setGridData(movieData, "true");
            else
                mGridAdapter.setGridData(movieData, "false");

            if (mPosition != GridView.INVALID_POSITION) {
                    mGridView.smoothScrollToPosition(mPosition);
                }
                mGridAdapter.notifyDataSetChanged();
        }

        @Override
        protected ArrayList<MovieItem> doInBackground(String... params) {

            //check if you are showing favourite movies or not
            //if favourite, query database not using internet
            //if not, then connect to the api
            if(!params[0].trim().equals("favourites")) {
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;

                //will contain movie json data
                String movieJsonStr = null;

                try {
                    final String FORECAST_BASE_URL =
                            "http://api.themoviedb.org/3/discover/movie?";
                    final String SORT_PARAM = "sort_by";
                    final String API_KEY_PARAM = "api_key";

                    Uri.Builder builder = Uri.parse(FORECAST_BASE_URL)
                            .buildUpon()
                            .appendQueryParameter(SORT_PARAM, params[0])
                            .appendQueryParameter(API_KEY_PARAM, "505cf850fa9209395657e835f67f2aa0");

                    Log.i("url", builder.build().toString());
                    URL url = new URL(builder.build().toString());

                    // Create the request to MoviesAPI, and open the connection
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    // Read the input stream into a String
                    InputStream inputStream = urlConnection.getInputStream();
                    StringBuffer buffer = new StringBuffer();
                    if (inputStream == null) {
                        // Nothing to do.
                        movieJsonStr = null;
                    }
                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = reader.readLine()) != null) {
                       //append lines
                        buffer.append(line + "\n");
                    }

                    if (buffer.length() == 0) {
                        // Stream was empty.  No point in parsing.
                        movieJsonStr = null;
                    }
                    movieJsonStr = buffer.toString();

                } catch (IOException e) {
                    Log.e("PlaceholderFragment", "Error ", e);
                    // If the code didn't successfully get the movie data, there's no point in attempting
                    // to parse it.
                    movieJsonStr = null;
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
                    //if no data fetched then return empty array
                    if(movieJsonStr == null) {
                        items.clear();
                        return items;
                    }
                    else
                        //parse movie json data
                        return getDataFromJson(movieJsonStr);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return null;
            } else {
                //if you are in favourite sorting then query database
                return getFavMovies();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.movie_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateMovie();
        }

        return super.onOptionsItemSelected(item);
    }

    // parse movie json data
    private ArrayList<MovieItem> getDataFromJson(String movieData) throws JSONException {
        items.clear();
        String RESULTS = "results";
        JSONObject movieJson = new JSONObject(movieData);
        JSONArray movieArray = movieJson.getJSONArray(RESULTS);

        for(int i = 0;i < movieArray.length(); i++) {
            JSONObject movie = movieArray.getJSONObject(i);

            String title = movie.getString("original_title");
            String overview = movie.getString("overview");
            String release_date = movie.getString("release_date");
            String poster_path = movie.getString("poster_path");
            String vote_average = movie.getString("vote_average");
            String id = movie.getString("id");

            MovieItem item = new MovieItem(title, poster_path, overview, vote_average, release_date, id);
            items.add(item);
        }

        return items;
    }

    // get favourite movies data from the database
    private ArrayList<MovieItem> getFavMovies() {
        items.clear();
        //call function to get all fav movies using content provider
        ArrayList<MovieItem> i = Utility.getAllMovies(getActivity());
        for(int k = 0; k < i.size(); k++) {
            MovieItem m = new MovieItem(i.get(k).getOriginalTitle(), i.get(k).getImageUrl(),
                    i.get(k).getOverView(), i.get(k).getVoteAverage(), i.get(k).getReleaseDate(), i.get(k).getId());
            items.add(m);
        }
        return items;
    }
}
