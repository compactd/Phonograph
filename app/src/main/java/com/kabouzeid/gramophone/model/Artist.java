package com.kabouzeid.gramophone.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;

import com.couchbase.lite.CouchbaseLiteException;

import java.util.ArrayList;
import java.util.List;

import io.compactd.compactd.models.CompactdAlbum;
import io.compactd.compactd.models.CompactdArtist;
import io.compactd.compactd.models.CompactdModel;
import io.compactd.compactd.models.CompactdTrack;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class Artist {
    public CompactdArtist artist;

    public Artist(CompactdArtist artist) {
        this.artist = artist;
    }

    public Artist () {

    }

    public int getId() {
        if (artist == null) {
            return 0;
        }
        return artist.getId().hashCode();
    }

    public String getName() {
        if (artist == null) {
            return "Undefined";
        }
        return artist.getName();
    }

    public int getSongCount() {
        if (artist == null) {
            return 0;
        }
        return artist.getTrackCount();
    }

    public int getAlbumCount() {
        if (artist == null) {
            return 0;
        }

        return artist.getAlbumCount();
    }

    public ArrayList<Song> getSongs() {
        ArrayList<Song> songs = new ArrayList<>();
        if (artist == null) {
            return songs;
        }
        try {
            List<CompactdTrack> tracks = artist.getTracks(CompactdModel.FindMode.Prefetch);
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

        Artist that = (Artist) o;

        return that.artist != null && artist.equals(that.artist);

    }

    @Override
    public int hashCode() {
        if (artist == null) {
            return 0;
        }
        return 13 * artist.getId().hashCode() + 420;
    }

    @Override
    public String toString() {
        return "Artist{" +
                "artist=" + artist +
                '}';
    }

    public ArrayList<Album> getAlbums() {
        ArrayList<Album> albums = new ArrayList<>();
        if (artist == null) return albums;
        try {
            for (CompactdAlbum album :
                 artist.getAlbums(CompactdModel.FindMode.Prefetch)) {

            }
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
        return albums;
    }
}
