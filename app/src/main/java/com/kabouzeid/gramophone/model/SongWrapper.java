package com.kabouzeid.gramophone.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.couchbase.lite.Manager;

import io.compactd.compactd.CompactdManager;
import io.compactd.compactd.models.CompactdTrack;

/**
 * Created by vinz243 on 07/11/2017.
 */

public class SongWrapper implements Parcelable {
    private final int id;

    public SongWrapper (Song song) {
        this(song.id);
    }
    protected SongWrapper(Parcel in) {
        this(in.readInt());
    }

    public static final Creator<SongWrapper> CREATOR = new Creator<SongWrapper>() {
        @Override
        public SongWrapper createFromParcel(Parcel in) {
            return new SongWrapper(in);
        }

        @Override
        public SongWrapper[] newArray(int size) {
            return new SongWrapper[size];
        }
    };

    public SongWrapper(int id) {
        this.id = id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.id);
    }

    public Song get (Context context) {
        Manager manager = CompactdManager.getInstance(context);
        return CompactdTrack.findById(manager, id, true).toSong();
    }
}
