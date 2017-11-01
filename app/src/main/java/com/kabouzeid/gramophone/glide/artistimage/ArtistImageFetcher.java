package com.kabouzeid.gramophone.glide.artistimage;

import android.content.Context;
import android.util.Log;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.Headers;
import com.bumptech.glide.load.model.ModelLoader;
import com.kabouzeid.gramophone.lastfm.rest.LastFMRestClient;
import com.kabouzeid.gramophone.lastfm.rest.model.LastFmArtist;
import com.kabouzeid.gramophone.util.LastFMUtil;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.kabouzeid.gramophone.util.PreferenceUtil;
import com.kabouzeid.gramophone.util.Util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Response;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class ArtistImageFetcher implements DataFetcher<InputStream> {
    public static final String TAG = ArtistImageFetcher.class.getSimpleName();
    private Context context;
    private final LastFMRestClient lastFMRestClient;
    private final ArtistImage model;
    private ModelLoader<GlideUrl, InputStream> urlLoader;
    private final int width;
    private final int height;
    private volatile boolean isCancelled;
    private DataFetcher<InputStream> urlFetcher;

    public ArtistImageFetcher(Context context, LastFMRestClient lastFMRestClient, ArtistImage model, ModelLoader<GlideUrl, InputStream> urlLoader, int width, int height) {
        this.context = context;
        this.lastFMRestClient = lastFMRestClient;
        this.model = model;
        this.urlLoader = urlLoader;
        this.width = width;
        this.height = height;
    }

    @Override
    public String getId() {
        // makes sure we never ever return null here
        return model.artist.getId();
    }

    @Override
    public InputStream loadData(Priority priority) throws Exception {
        if (model.artist != null) {
            String host = PreferenceUtil.getInstance(this.context).lastServerURL();
            String path = model.artist.getImagePath(width);

            Headers headers = new Headers() {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.putAll(Headers.DEFAULT.getHeaders());
                    headers.put("Authorization", "Bearer " +
                            PreferenceUtil.getInstance(ArtistImageFetcher.this.context)
                                    .sessionToken());
                    return headers;
                }
            };

            GlideUrl url = new GlideUrl(host + path, headers);

            Log.d(TAG, "loadData: " + url.toStringUrl());

            urlFetcher = urlLoader.getResourceFetcher(url, width, height);

            return urlFetcher.loadData(priority);

        }
        if (!MusicUtil.isArtistNameUnknown(model.artist.getName()) && Util.isAllowedToDownloadMetadata(context)) {
            Response<LastFmArtist> response = lastFMRestClient.getApiService().getArtistInfo(model.artistName, null, model.skipOkHttpCache ? "no-cache" : null).execute();

            if (!response.isSuccessful()) {
                throw new IOException("Request failed with code: " + response.code());
            }

            LastFmArtist lastFmArtist = response.body();

            if (isCancelled) return null;

            GlideUrl url = new GlideUrl(LastFMUtil.getLargestArtistImageUrl(lastFmArtist.getArtist().getImage()));
            urlFetcher = urlLoader.getResourceFetcher(url, width, height);

            return urlFetcher.loadData(priority);
        }
        return null;
    }

    @Override
    public void cleanup() {
        if (urlFetcher != null) {
            urlFetcher.cleanup();
        }
    }

    @Override
    public void cancel() {
        isCancelled = true;
        if (urlFetcher != null) {
            urlFetcher.cancel();
        }
    }
}
