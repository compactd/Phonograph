package com.kabouzeid.gramophone.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.couchbase.lite.Manager;

import io.compactd.compactd.CompactdManager;
import io.compactd.compactd.models.CompactdAlbum;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class Song {
    public static final Song EMPTY_SONG = new Song(-1, "", -1, -1, -1, "", -1, -1, "", -1, "");

    public int id;
    public String title;
    public int trackNumber;
    public int year;
    public long duration;
    public String data;
    public long dateModified;
    public int albumId;
    public String albumName;
    public int artistId;
    public String artistName;

    public Song(int id) {
        this.id = id;

    }

    public Song(int id, String title, int trackNumber, int year, long duration, String data, long dateModified, int albumId, String albumName, int artistId, String artistName) {
        this.id = id;
        this.title = title;
        this.trackNumber = trackNumber;
        this.year = year;
        this.duration = duration;
        this.data = data;
        this.dateModified = dateModified;
        this.albumId = albumId;
        this.albumName = albumName;
        this.artistId = artistId;
        this.artistName = artistName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Song song = (Song) o;

        if (id != song.id) return false;
        if (trackNumber != song.trackNumber) return false;
        if (year != song.year) return false;
        if (duration != song.duration) return false;
        if (dateModified != song.dateModified) return false;
        if (albumId != song.albumId) return false;
        if (artistId != song.artistId) return false;
        if (title != null ? !title.equals(song.title) : song.title != null) return false;
        if (data != null ? !data.equals(song.data) : song.data != null) return false;
        if (albumName != null ? !albumName.equals(song.albumName) : song.albumName != null)
            return false;
        return artistName != null ? artistName.equals(song.artistName) : song.artistName == null;

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + trackNumber;
        result = 31 * result + year;
        result = 31 * result + (int) (duration ^ (duration >>> 32));
        result = 31 * result + (data != null ? data.hashCode() : 0);
        result = 31 * result + (int) (dateModified ^ (dateModified >>> 32));
        result = 31 * result + albumId;
        result = 31 * result + (albumName != null ? albumName.hashCode() : 0);
        result = 31 * result + artistId;
        result = 31 * result + (artistName != null ? artistName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Song{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", trackNumber=" + trackNumber +
                ", year=" + year +
                ", duration=" + duration +
                ", data='" + data + '\'' +
                ", dateModified=" + dateModified +
                ", albumId=" + albumId +
                ", albumName='" + albumName + '\'' +
                ", artistId=" + artistId +
                ", artistName='" + artistName + '\'' +
                '}';
    }

    public CompactdAlbum getAlbum (Context context, boolean fetch) {

        Manager manager = CompactdManager.getInstance(context);
        return CompactdAlbum.findById(manager, albumId, fetch);
    }

}
