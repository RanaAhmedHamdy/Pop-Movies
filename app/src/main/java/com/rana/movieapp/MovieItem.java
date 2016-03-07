package com.rana.movieapp;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Rana on 11/27/2015.
 */
public class MovieItem implements Parcelable {
    private String originalTitle;
    private String imageUrl;
    private String overView;
    private String voteAverage;
    private String releaseDate;
    private String id;

    public MovieItem(String originalTitle, String imageUrl, String overView, String voteAverage, String releaseDate, String id) {
        this.originalTitle = originalTitle;
        this.imageUrl = imageUrl;
        this.overView = overView;
        this.voteAverage = voteAverage;
        this.releaseDate = releaseDate;
        this.id = id;
    }

    private MovieItem(Parcel in){
        originalTitle = in.readString();
        imageUrl = in.readString();
        overView = in.readString();
        voteAverage = in.readString();
        releaseDate = in.readString();
        id = in.readString();
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public String getOverView() {
        return overView;
    }

    public String getVoteAverage() {
        return voteAverage;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public String getId() { return id; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(originalTitle);
        parcel.writeString(imageUrl);
        parcel.writeString(overView);
        parcel.writeString(voteAverage);
        parcel.writeString(releaseDate);
        parcel.writeString(id);
    }

    public static final Parcelable.Creator<MovieItem> CREATOR = new Parcelable.Creator<MovieItem>() {
        @Override
        public MovieItem createFromParcel(Parcel parcel) {
            return new MovieItem(parcel);
        }

        @Override
        public MovieItem[] newArray(int i) {
            return new MovieItem[i];
        }

    };
}
