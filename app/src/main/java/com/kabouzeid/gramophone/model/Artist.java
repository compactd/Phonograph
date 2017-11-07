package com.kabouzeid.gramophone.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.couchbase.lite.CouchbaseLiteException;

import java.util.ArrayList;
import java.util.List;

import io.compactd.compactd.models.CompactdArtist;
import io.compactd.compactd.models.CompactdModel;
import io.compactd.compactd.models.CompactdTrack;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class Artist {
    public ArrayList<Album> albums;
    public CompactdArtist artist;

    public Artist(CompactdArtist artist) {
        this.artist = artist;
    }

    public Artist () {

    }

    public int getId() {
        return artist.getId().hashCode();
    }

    public String getName() {
        return artist.getName();
    }

    public int getSongCount() {
        return artist.getTrackCount();
    }

    public int getAlbumCount() {
        return artist.getAlbumCount();
    }

    public ArrayList<Song> getSongs() {
        ArrayList<Song> songs = new ArrayList<>();
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

        Artist artist = (Artist) o;

        return albums != null ? albums.equals(artist.albums) : artist.albums == null;

    }

    @Override
    public int hashCode() {
        return 13 * artist.getId().hashCode() + 420;
    }

    @Override
    public String toString() {
        return "Artist{" +
                "name=" + artist.getName() +
                '}';
    }

}
