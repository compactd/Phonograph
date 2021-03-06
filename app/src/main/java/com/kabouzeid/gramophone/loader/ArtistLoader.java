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

import io.compactd.compactd.CompactdManager;
import io.compactd.compactd.models.CompactdArtist;
import io.compactd.compactd.models.CompactdModel;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class ArtistLoader {
    private static final String TAG = "ArtistLoader";

    @NonNull
    public static ArrayList<Artist> getAllArtists(@NonNull final Context context) {
        long ms = System.currentTimeMillis();

        ArrayList<Artist> artists = new ArrayList<>();
        try {
            List<CompactdArtist> compactdArtists = CompactdArtist.findAll(CompactdManager.getInstance(context), CompactdModel.FindMode.Prefetch);
            artists.addAll(Collections2.transform(compactdArtists, new Function<CompactdArtist, Artist>() {

                @javax.annotation.Nullable
                @Override
                public Artist apply(@javax.annotation.Nullable CompactdArtist input) {
                    assert input != null;
                    return input.toArtist();
                }
            }));
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "getAllArtists: took " + (System.currentTimeMillis() - ms) + "ms");
        return artists;
    }

    @NonNull
    public static ArrayList<Artist> getArtists(@NonNull final Context context, final String query) {
        ArrayList<Artist> artists = new ArrayList<>();
        try {
            List<CompactdArtist> compactdArtists = CompactdArtist.findAll(CompactdManager.getInstance(context), CompactdModel.FindMode.Prefetch);
            List<CompactdArtist> filteredArtists = new ArrayList<>(Collections2.filter(compactdArtists, new Predicate<CompactdArtist>() {
                @Override
                public boolean apply(@javax.annotation.Nullable CompactdArtist input) {
                    return input != null && input.getName().contains(query);
                }
            }));

            artists.addAll(Collections2.transform(filteredArtists, new Function<CompactdArtist, Artist>() {

                @javax.annotation.Nullable
                @Override
                public Artist apply(@javax.annotation.Nullable CompactdArtist input) {
                    assert input != null;
                    return input.toArtist();
                }
            }));
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
        return artists;
    }

    @NonNull
    public static Artist getArtist(@NonNull final Context context, int artistId) {
        CompactdArtist artist =
                CompactdArtist.findById(
                        CompactdManager.getInstance(context),
                        artistId, true);

        if (artist != null) {

            return artist.toArtist();

        }
        throw new Error("not found");
    }
}
