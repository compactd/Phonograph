package com.kabouzeid.gramophone.glide.artistimage;

import javax.annotation.Nullable;

import io.compactd.compactd.models.CompactdArtist;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class ArtistImage {
    public CompactdArtist artist;
    public final String artistName;
    public final boolean skipOkHttpCache;

    public ArtistImage(CompactdArtist artist, boolean skipOkHttpCache) {
        this.artist = artist;
        this.artistName = artist.getName();
        this.skipOkHttpCache = skipOkHttpCache;
    }
}
