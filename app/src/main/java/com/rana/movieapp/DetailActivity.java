package com.rana.movieapp;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;


public class DetailActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            Bundle arguments = new Bundle();
            MovieItem currentMovie = new MovieItem(getIntent().getExtras().getString(getString(R.string.title_extras)), getIntent().getExtras().getString(getString(R.string.image)), getIntent().getExtras().getString(getString(R.string.overview)), getIntent().getExtras().getString(getString(R.string.vote)),
                    getIntent().getExtras().getString(getString(R.string.date)), getIntent().getExtras().getString(getString(R.string.id)));

            arguments.putParcelable(getString(R.string.detail_movie), currentMovie);

            DetailActivityFragment fragment = new DetailActivityFragment();
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.movies_detail_container, fragment)
                    .commit();
        }
    }


   /* @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/
}
