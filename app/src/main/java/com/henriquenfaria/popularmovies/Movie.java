package com.henriquenfaria.popularmovies;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

// Movie class that is used to store Movie data
public class Movie implements Parcelable {

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };
    private String mId;
    private String mTitle;
    private String mReleaseDate;
    private String mVoteAverage;
    private String mOverview;
    private Uri mPosterUri;

    public Movie(String id, String title, String releaseDate, String voteAverage, String
            overview, Uri posterUri) {
        mId = id;
        mTitle = title;
        mReleaseDate = releaseDate;
        mVoteAverage = voteAverage;
        mOverview = overview;
        mPosterUri = posterUri;
    }

    public Movie(Parcel in) {
        mId = in.readString();
        mTitle = in.readString();
        mReleaseDate = in.readString();
        mVoteAverage = in.readString();
        mOverview = in.readString();
        mPosterUri = (Uri) in.readValue(Movie.class.getClassLoader());
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getReleaseDate() {
        return mReleaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        mReleaseDate = releaseDate;
    }

    public String getVoteAverage() {
        return mVoteAverage;
    }

    public void setVoteAverage(String voteAverage) {
        mVoteAverage = voteAverage;
    }

    public String getOverview() {
        return mOverview;
    }

    public void setOverview(String overview) {
        mOverview = overview;
    }

    public Uri getPosterUri() {
        return mPosterUri;
    }

    public void setPosterUri(Uri posterUri) {
        mPosterUri = posterUri;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int i) {
        dest.writeString(mId);
        dest.writeString(mTitle);
        dest.writeString(mReleaseDate);
        dest.writeString(mVoteAverage);
        dest.writeString(mOverview);
        dest.writeValue(mPosterUri);
    }
}
