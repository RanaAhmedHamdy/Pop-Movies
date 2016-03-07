package com.rana.movieapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Rana on 11/27/2015.
 */
public class MovieGridViewAdapter extends ArrayAdapter<MovieItem>{

    private Context mContext;
    private int layoutResourceId;
    private ArrayList<MovieItem> mGridData = new ArrayList<>();
    private String favourite;

    public MovieGridViewAdapter(Context mContext, int layoutResourceId, ArrayList<MovieItem> mGridData, String favourite) {
        super(mContext, layoutResourceId, mGridData);
        this.layoutResourceId = layoutResourceId;
        this.mContext = mContext;
        this.mGridData = mGridData;
        this.favourite = favourite;
    }


    /**
     * Updates grid data and refresh grid items.
     * @param mGridData
     */
    public void setGridData(ArrayList<MovieItem> mGridData, String favourite) {
        this.favourite = favourite;
        this.mGridData = mGridData;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder;

        if (row == null) {
            row = LayoutInflater.from(getContext()).inflate(layoutResourceId, parent, false);
            holder = new ViewHolder();
            holder.imageView = (ImageView) row.findViewById(R.id.grid_item_image);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

            MovieItem item = mGridData.get(position);

            //if it is not a favourite movie load poster from the internet
            if(favourite.equals("false"))
                Picasso.with(mContext).load("http://image.tmdb.org/t/p/w342" + item.getImageUrl()).into(holder.imageView);
            //if it is a favourite movie load posters from internal local storage
            else
                holder.imageView.setImageBitmap(Utility.loadBitmap(mContext, item.getImageUrl().substring(1)));
        return row;
       }

    static class ViewHolder {
        ImageView imageView;
    }
}