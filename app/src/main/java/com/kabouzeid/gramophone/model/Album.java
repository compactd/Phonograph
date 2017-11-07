package com.kabouzeid.gramophone.model;

import android.content.Context;

import com.couchbase.lite.CouchbaseLiteException;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import io.compactd.compactd.models.CompactdAlbum;
import io.compactd.compactd.models.CompactdArtist;
import io.compactd.compactd.models.CompactdModel;
import io.compactd.compactd.models.CompactdTrack;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class Album {
    public CompactdAlbum album;

    public Album(CompactdAlbum album) {
        this.album = album;
    }

    public Album() {
    }

    public int getId() {
        return album.getId().hashCode();
    }

    public String getTitle() {
        return album.getName();
    }

    public int getArtistId() {
        return album.getArtist().getId().hashCode();
    }

    public String getArtistName() {
        CompactdArtist artist = album.getArtist();
        try {
            artist.fetch();
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
        return artist.getName();
    }

    public CompactdArtist getArtist () {
        return album.getArtist();
    }

    public int getYear() {
        return -1;
    }

    public int getSongCount() {
        return album.getTrackCount();
    }

    public ArrayList<Song> getSongs() {
        ArrayList<Song> songs = new ArrayList<>();

        try {
            List<CompactdTrack> tracks = album.getTracks(CompactdModel.FindMode.Prefetch);
            for (CompactdTrack track : tracks) {
                songs.add(track.toSong());
            }
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
        return songs;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Album that = (Album) o;

        return that.getId() == this.getId();

    }

    @Override
    public int hashCode() {
        return 12 * album.hashCode() + 420;
    }

    @Override
    public String toString() {
        return "Album{" +
                "album=" + album.toString() +
                '}';
    }

    public long getDateModified() {
        return -1;
    }
}
