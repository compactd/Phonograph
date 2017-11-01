package com.kabouzeid.gramophone.glide.audiocover;

import android.content.Context;
import android.util.Log;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.Headers;
import com.bumptech.glide.load.model.ModelLoader;
import com.kabouzeid.gramophone.util.PreferenceUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class AudioFileCoverFetcher implements DataFetcher<InputStream> {
    private static final String TAG = AudioFileCoverFetcher.class.getSimpleName();
    private final AudioFileCover model;
    private FileInputStream stream;
    private final Context context;
    private ModelLoader<GlideUrl, InputStream> urlLoader;
    private int width, height;

    public AudioFileCoverFetcher(AudioFileCover model, Context context, ModelLoader<GlideUrl, InputStream> urlLoader, int width, int height) {
        this.model = model;
        this.context = context;
        this.urlLoader = urlLoader;
        this.width = width;
        this.height = height;
    }

    @Override
    public String getId() {
        if (model.album == null) {
            return null;
        }
        // makes sure we never ever return null here
        return String.valueOf(model.album.getId());
    }

    @Override
    public InputStream loadData(Priority priority) throws Exception {
        if (model.album != null) {
            String host = PreferenceUtil.getInstance(this.context).lastServerURL();
            String path = model.album.getImagePath(300);

            Headers headers = new Headers() {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.putAll(Headers.DEFAULT.getHeaders());
                    headers.put("Authorization", "Bearer " +
                            PreferenceUtil.getInstance(AudioFileCoverFetcher.this.context)
                                    .sessionToken());
                    return headers;
                }
            };

            GlideUrl url = new GlideUrl(host + path, headers);

            Log.d(TAG, "loadData: " + url.toStringUrl());

            DataFetcher<InputStream> urlFetcher = urlLoader.getResourceFetcher(url, 300, 300);

            return urlFetcher.loadData(priority);

        }
        return null;
    }

    private static final String[] FALLBACKS = {"cover.jpg", "album.jpg", "folder.jpg"};

    private InputStream fallback(String path) throws FileNotFoundException {
        File parent = new File(path).getParentFile();
        for (String fallback : FALLBACKS) {
            File cover = new File(parent, fallback);
            if (cover.exists()) {
                return stream = new FileInputStream(cover);
            }
        }
        return null;
    }

    @Override
    public void cleanup() {
        // already cleaned up in loadData and ByteArrayInputStream will be GC'd
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException ignore) {
                // can't do much about it
            }
        }
    }

    @Override
    public void cancel() {
        // cannot cancel
    }
}
