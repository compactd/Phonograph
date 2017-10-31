package com.kabouzeid.gramophone.loader;

import android.content.Context;
import android.provider.MediaStore.Audio.AudioColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Manager;
import com.couchbase.lite.android.AndroidContext;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.kabouzeid.gramophone.model.Album;
import com.kabouzeid.gramophone.model.Artist;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.util.PreferenceUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.compactd.compactd.models.CompactdAlbum;
import io.compactd.compactd.models.CompactdArtist;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class AlbumLoader {

    private static final String TAG = "AlbumLoader";

    public static String getSongLoaderSortOrder(Context context) {
        return PreferenceUtil.getInstance(context).getAlbumSortOrder() + ", " + PreferenceUtil.getInstance(context).getAlbumSongSortOrder();
    }

    @NonNull
    public static ArrayList<Album> getAllAlbums(@NonNull final Context context) {

        ArrayList<Album> albums = new ArrayList<>();
        try {
            List<CompactdAlbum> compactdAlbums = CompactdAlbum.findAll(new Manager(new AndroidContext(context), Manager.DEFAULT_OPTIONS));
            albums.addAll(Collections2.transform(compactdAlbums, new Function<CompactdAlbum, Album>() {

                @javax.annotation.Nullable
                @Override
                public Album apply(@javax.annotation.Nullable CompactdAlbum input) {
                    assert input != null;
                    return input.toAlbum();
                }
            }));
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return albums;
    }

    @NonNull
    public static ArrayList<Album> getAlbums(@NonNull final Context context, final String query) {
        Log.d(TAG, "getAlbums: " + query);
        ArrayList<Album> albums = new ArrayList<>();
        try {
            List<CompactdAlbum> compactdAlbums = CompactdAlbum.findAll(new Manager(new AndroidContext(context), Manager.DEFAULT_OPTIONS));
            List<CompactdAlbum> filteredAlbums = new ArrayList<>(Collections2.filter(compactdAlbums, new Predicate<CompactdAlbum>() {
                @Override
                public boolean apply(@javax.annotation.Nullable CompactdAlbum input) {
                    return input != null && input.getName().contains(query);
                }
            }));

            albums.addAll(Collections2.transform(filteredAlbums, new Function<CompactdAlbum, Album>() {

                @javax.annotation.Nullable
                @Override
                public Album apply (@javax.annotation.Nullable CompactdAlbum input) {
                    assert input != null;
                    return input.toAlbum();
                }
            }));
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return albums;
    }

    @NonNull
    public static Album getAlbum(@NonNull final Context context, int albumId) {
        try {
            CompactdAlbum album =
                    CompactdAlbum.findById(
                            new Manager(new AndroidContext(context), Manager.DEFAULT_OPTIONS),
                            albumId);

            if (album != null) return album.toAlbum();
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new Error("not found");
    }

    private static Album getOrCreateAlbum(ArrayList<Album> albums, int albumId) {
        for (Album album : albums) {
            if (!album.songs.isEmpty() && album.songs.get(0).albumId == albumId) {
                return album;
            }
        }
        Album album = new Album();
        albums.add(album);
        return album;
    }
}
