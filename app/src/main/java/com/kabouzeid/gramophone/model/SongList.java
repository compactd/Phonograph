package com.kabouzeid.gramophone.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.couchbase.lite.Manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.compactd.compactd.CompactdManager;
import io.compactd.compactd.models.CompactdTrack;

/**
 * Created by vinz243 on 07/11/2017.
 */

public class SongList implements Parcelable {
    private ArrayList<Integer> songs = new ArrayList<>();

    public static SongList fromSongList (List<Song> songs) {
        SongList list = new SongList();
        for (Song song : songs) {
            list.add(song.id);
        }
        return list;
    }

    public SongList(Integer... id) {
        this(Arrays.asList(id));
    }

    public SongList() {
    }

    public void add (int id) {
        songs.add(id);
    }
    public SongList(List<Integer> ids) {
        songs.addAll(ids);
    }
    protected SongList(Parcel in) {
        in.readList(songs, null);
    }

    public static final Creator<SongList> CREATOR = new Creator<SongList>() {
        @Override
        public SongList createFromParcel(Parcel in) {
            return new SongList(in);
        }

        @Override
        public SongList[] newArray(int size) {
            return new SongList[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeList(songs);
    }

    public ArrayList<Song> fetchSongs (Context context) {
        ArrayList<Song> out = new ArrayList<>();
        Manager manager = CompactdManager.getInstance(context);
        for (Integer song : songs) {
            out.add(CompactdTrack.findById(manager, song, true).toSong());
        }
        return out;

    }
}
